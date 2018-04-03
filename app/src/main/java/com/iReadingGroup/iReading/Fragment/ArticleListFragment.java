package com.iReadingGroup.iReading.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.iReadingGroup.iReading.Activity.ArticleDetailActivity;
import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.Adapter.ArticleInfoAdapter;
import com.iReadingGroup.iReading.ArticleInfo;
import com.iReadingGroup.iReading.Bean.ArticleStorageBean;
import com.iReadingGroup.iReading.Bean.ArticleStorageBeanDao;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.SourceSelectEvent;
import com.wyt.searchbox.SearchFragment;
import com.wyt.searchbox.custom.IOnSearchClickListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

import static java.lang.Math.max;


/**
 * The type Article list fragment.
 */
public class ArticleListFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

    private BGARefreshLayout mRefreshLayout; //Layout for refreshing and loading
    //private ListView infoListView;////infoListView for list of brief info of each article
    private RecyclerView infoListView;
    private ArticleInfoAdapter articleInfoAdapter;//Custom adapter for article info
    private ArrayList<ArticleInfo> alArticleInfo = new ArrayList<>();//ArrayList linked to adapter for listview
    private ArrayList<String> current_uri_list = new ArrayList<>();
    private View view;
    private SearchFragment searchFragment;
    private OfflineDictBeanDao daoDictionary;
    private ArticleStorageBeanDao daoArticle;
    private String requestUrl = "http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22lang%22%3A%22eng%22%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=20&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
    ;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Must add in every fragments' onCreateView to avoid duplicate creating.
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent) {
                parent.removeView(view);
            }
        } else {
            //start initializing
            view = inflater.inflate(com.iReadingGroup.iReading.R.layout.fragment_article_info, container, false);//set layout
            daoDictionary = ((MainActivity) getActivity()).getDaoDictionary();//get database
            daoArticle = ((MainActivity) getActivity()).getDaoArticle();
            initializeUI();

        }
        return view;
    }

    /**
     * Initialize ui.
     */
    public void initializeUI() {
        initializeRefreshingLayout();//Initialize refreshing and loading layout
        initializeSearchView();
        initializeListView();
    }

    /**
     * Initialize refreshing layout.
     */
    public void initializeRefreshingLayout() {
        mRefreshLayout = (BGARefreshLayout) view.findViewById(com.iReadingGroup.iReading.R.id.rl_modulename_refresh);
        mRefreshLayout.setDelegate(this);
        BGAMoocStyleRefreshViewHolder refreshViewHolder = new BGAMoocStyleRefreshViewHolder(getContext(), true);
        refreshViewHolder.setOriginalImage(R.mipmap.icon1);
        refreshViewHolder.setUltimateColor(com.iReadingGroup.iReading.R.color.custom_imoocstyle);
        mRefreshLayout.setIsShowLoadingMoreView(true);
        refreshViewHolder.setLoadingMoreText("加载历史文章……");
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);
    }

    /**
     * Initialize search view.
     */
    public void initializeSearchView() {            //search view
        searchFragment = SearchFragment.newInstance();
        searchFragment.setOnSearchClickListener(new IOnSearchClickListener() {
            @Override
            public void OnSearchClick(String keyword) {
                //这里处理逻辑
                List<OfflineDictBean> joes = daoDictionary.queryBuilder()
                        .where(OfflineDictBeanDao.Properties.Word.eq(keyword))
                        .list();
                if (joes.size() == 0) {
                    Toast.makeText(getContext(), "离线词库中无此词，请联网查询", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getContext(), joes.get(0).getMeaning(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    /**
     * Initialize list view.
     */
    public void initializeListView() {//Establish the connection among listView,adapter and arrayList.

        infoListView = (RecyclerView) view.findViewById(com.iReadingGroup.iReading.R.id.list);//

        List<ArticleStorageBean> cache = daoArticle.loadAll();
        int size = cache.size();
        for (int i = size - 1; i > max(i - 21, -1); i--) {
            ArticleStorageBean item = cache.get(i);
            ArticleInfo lin = new ArticleInfo(item.getName(), item.getUri(), item.getImageUrl(), getDate(item.getTime()), item.getSource(), com.iReadingGroup.iReading.R.drawable.collect_false);
            alArticleInfo.add(lin);
        }


        //articleInfoAdapter = new ArticleInfoAdapter(getActivity(),
        //com.iReadingGroup.iReading.R.layout.listitem_article_info,alArticleInfo);//link the arrayList to adapter,using custom layout for each item
        articleInfoAdapter = new ArticleInfoAdapter(getActivity(),
                com.iReadingGroup.iReading.R.layout.listitem_article_info, alArticleInfo);
        infoListView.setAdapter(articleInfoAdapter);//link the adapter to ListView
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        infoListView.setLayoutManager(llm);
        //Set click event for listView and pass the arguments through Bundle to the following activity.

        articleInfoAdapter.openLoadAnimation(0x00000001);
        articleInfoAdapter.isFirstOnly(false);
        infoListView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter parent, View view, int position) {
                ArticleInfo h = (ArticleInfo) alArticleInfo.get(position);
                String number = h.getName();
                String uri = h.getUri();
                Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", number);
                bundle.putString("uri", uri);
                bundle.putString("time", h.getTime());
                bundle.putString("source", h.getSource());
                intent.putExtras(bundle);
                startActivity(intent);
                //FruitList.this.finish();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        // Refreshing the latest data from server.

        if (isNetworkAvailable()) {
            // if the network is good, continue.
            new RefreshingTask().execute(requestUrl);
        } else {
            // network is not connected,end
            mRefreshLayout.endRefreshing();
        }
    }

    class RefreshingTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            mRefreshLayout.endRefreshing();// finish fetching from sever
            if (result != null) {
                try {   //parse word from json
                    //sample link.:http://dict-co.iciba.com/api/dictionary.php?w=go&key=341DEFE6E5CA504E62A567082590D0BD&type=json
                    String uri, title, source_title, time;
                    JSONObject reader = new JSONObject(result);
                    JSONObject articles = reader.getJSONObject("articles");
                    JSONArray results = articles.getJSONArray("results");
                    for (int i = results.length() - 1; i > -1; i--) {
                        JSONObject article = results.getJSONObject(i);
                        uri = article.getString("uri");
                        if (getArticleCachedStatus(uri)) break;
                        if (article.getBoolean("isDuplicate")) continue;
                        title = article.getString("title");
                        time = article.getString("dateTime");
                        JSONObject source = article.getJSONObject("source");
                        source_title = source.getString("title");
                        String imageurl = article.getString("image");
                        ArticleInfo lin = new ArticleInfo(title, uri, imageurl, getDate(time), source_title, com.iReadingGroup.iReading.R.drawable.collect_false);
                        alArticleInfo.add(0, lin);
                        daoArticle.insert(new ArticleStorageBean(uri, title, time, source_title, imageurl, false, null));
                    }

                } catch (JSONException e) {

                }

                //sync to the listView
                articleInfoAdapter.notifyDataSetChanged();
            }
        }
    }

    private String getDate(String OurDate) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(OurDate);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd HH:mm"); //this format changeable
            dateFormatter.setTimeZone(TimeZone.getDefault());
            OurDate = dateFormatter.format(value);

        } catch (Exception e) {
            OurDate = "00-00-0000 00:00";
        }
        return OurDate;
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        //  Loading more (history) data from server or cache, Return false to disable the refreshing action.

        if (true) {
            // if the network is good, continue and return true.
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    mRefreshLayout.endLoadingMore();//finish loading
                    //start add items to arrayList
                    //ArticleInfo lin=new ArticleInfo("aa","a","old article", com.iReadingGroup.iReading.R.drawable.collect_false);
                    //alArticleInfo.add(lin);
                    //sync to the listView
                    //articleInfoAdapter.notifyDataSetChanged();
                    //infoListView.setSelection(n-1);
                }
            }.execute();

            return true;
        } else {
            // The network is not connected
            Toast.makeText(getContext(), "网络不可用", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                searchFragment.show(getActivity().getSupportFragmentManager(), SearchFragment.TAG);
                break;
        }
        return true;

    }

    /**
     * On message event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSourceSelectEvent(SourceSelectEvent event) {
        int size;
        List<ArticleStorageBean> cache;
        switch (event.index) {
            case 0: {//all
                requestUrl = "http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22lang%22%3A%22eng%22%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=20&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
                alArticleInfo.clear();
                cache = daoArticle.loadAll();
                size = cache.size();
                for (int i = size - 1; i > max(i - 21, -1); i--) {
                    ArticleStorageBean item = cache.get(i);
                    ArticleInfo lin = new ArticleInfo(item.getName(), item.getUri(), item.getImageUrl(), getDate(item.getTime()), item.getSource(), com.iReadingGroup.iReading.R.drawable.collect_false);
                    alArticleInfo.add(lin);
                }
                articleInfoAdapter.notifyDataSetChanged();
                break;
            }
            case 1:
            {   //national geographic
                requestUrl = "http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22%24and%22%3A%5B%7B%22%24or%22%3A%5B%7B%22sourceUri%22%3A%22news.nationalgeographic.com%22%7D%5D%7D%2C%7B%22lang%22%3A%22eng%22%7D%5D%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=20&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
                setSourceForView("National Geographic");
                break;
            }
            case 2:
            {   //nature
                requestUrl="http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22%24and%22%3A%5B%7B%22%24or%22%3A%5B%7B%22sourceUri%22%3A%22nature.com%22%7D%5D%7D%2C%7B%22lang%22%3A%22eng%22%7D%5D%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=20&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
                setSourceForView("Nature News");
                break;
            }
            case 3:
            {   //the economist
                requestUrl="http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22%24and%22%3A%5B%7B%22%24or%22%3A%5B%7B%22sourceUri%22%3A%22economist.com%22%7D%5D%7D%2C%7B%22lang%22%3A%22eng%22%7D%5D%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=20&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
                setSourceForView("The Economist");
                break;
            }
            case 4:
            {   //TIME
                requestUrl="http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22%24and%22%3A%5B%7B%22%24or%22%3A%5B%7B%22sourceUri%22%3A%22time.com%22%7D%5D%7D%2C%7B%22lang%22%3A%22eng%22%7D%5D%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=20&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
                setSourceForView("TIME");
                break;
            }
            case 5:
            {   //The New York Times
                requestUrl="http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22%24and%22%3A%5B%7B%22%24or%22%3A%5B%7B%22sourceUri%22%3A%22nytimes.com%22%7D%5D%7D%2C%7B%22lang%22%3A%22eng%22%7D%5D%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=20&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
                setSourceForView("The New York Times");
                break;
            }
            case 6:
            {   //The Wall Street Journal
                requestUrl="http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22%24and%22%3A%5B%7B%22%24or%22%3A%5B%7B%22sourceUri%22%3A%22wsj.com%22%7D%5D%7D%2C%7B%22lang%22%3A%22eng%22%7D%5D%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=20&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
                setSourceForView("The Wall Street Journal");
                break;
            }
        }
    }
    private void setSourceForView(String source)
    {   int size;
        List<ArticleStorageBean> cache;
        alArticleInfo.clear();
        cache=daoArticle.queryBuilder().where(ArticleStorageBeanDao.Properties.Source.like(source+"%")).list();
        size = cache.size();
        for (int i = size - 1; i > max(i - 21, -1); i--) {
            ArticleStorageBean item = cache.get(i);
            ArticleInfo lin = new ArticleInfo(item.getName(), item.getUri(), item.getImageUrl(), getDate(item.getTime()), item.getSource(), com.iReadingGroup.iReading.R.drawable.collect_false);
            alArticleInfo.add(lin);
        }
        articleInfoAdapter.notifyDataSetChanged();

    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {


        EventBus.getDefault().unregister(this);
        super.onStop();

    }

    /**
     * Begin refreshing.
     */
// 通过代码方式控制进入正在刷新状态。应用场景：某些应用在 activity 的 onStart 方法中调用，自动进入正在刷新状态获取最新数据
    public void beginRefreshing() {
        mRefreshLayout.beginRefreshing();
    }

    /**
     * Begin loading more.
     */
// 通过代码方式控制进入加载更多状态
    public void beginLoadingMore() {
        mRefreshLayout.beginLoadingMore();
    }

    private boolean getArticleCachedStatus(String uri) {
        current_uri_list = new ArrayList<>();
        for (ArticleStorageBean exist : daoArticle.loadAll()) {
            current_uri_list.add(exist.getUri());
        }
        if (current_uri_list.contains(uri)) return true;
        else return false;
    }

}



