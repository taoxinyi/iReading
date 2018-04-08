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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.iReadingGroup.iReading.Activity.ArticleDetailActivity;
import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.Adapter.ArticleInfoAdapter;
import com.iReadingGroup.iReading.Event.ArticleSearchDoneEvent;
import com.iReadingGroup.iReading.Event.ArticleSearchEvent;
import com.iReadingGroup.iReading.Event.ArticleCollectionStatusChangedEvent;
import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Event.CollectArticleEvent;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.Event.SourceSelectEvent;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

import static java.lang.Math.max;


/**
 * ArticleListFragment
 * Load article when refresh, change source when source section changes
 *
 * Load the articles from database and add them into ArrayList
 * Update through refreshing or loading using AsyncTask
 * If source changes, the view changes accordingly
 * search in corresponding source
 *
 *
 */
public class ArticleListFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

    private BGARefreshLayout mRefreshLayout; //Layout for refreshing and loading
    //private ListView infoListView;////infoListView for list of brief info of each article
    private RecyclerView infoListView;
    private ArticleInfoAdapter articleInfoAdapter;//Custom adapter for article info
    private List<ArticleEntity> alArticleInfo = new ArrayList<>();//ArrayList linked to adapter for listview
    private ArrayList<ArticleEntity> alArticleInfoCache = new ArrayList<>();//cache of ArrayList linked to adapter for listview when searching
    private boolean flag_search = false;
    private ArrayList<String> current_uri_list = new ArrayList<>();
    private View view;
    private ArticleEntityDao daoArticle;
    private String requestUrl = "http://eventregistry.org/json/article?lang=eng&action=getArticles&resultType=articles&articlesSortBy=date&articlesCount=5&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
    private HashMap<String,Integer> pageMap=new HashMap<>();
    private String current_source="所有";
    private String requestUrlCache;
    private String searchUrlPrefix=requestUrl+"&keywordLoc=title&keyword=";
    private Integer pageCache;

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

    }

    /**
     * Initialize list view.
     */
    public void initializeListView() {//Establish the connection among listView,adapter and arrayList.

        infoListView = (RecyclerView) view.findViewById(com.iReadingGroup.iReading.R.id.list);//

        alArticleInfo = daoArticle.queryBuilder().orderDesc(ArticleEntityDao.Properties.Time).list();



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
                ArticleEntity h = (ArticleEntity) alArticleInfo.get(position);
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
        initializeMap();

    }

    /**
     * Map for source:current page
     */
    private void initializeMap()
    {
        pageMap.put("National Geographic",0);
        pageMap.put("Nature",0);
        pageMap.put("The Economist",0);
        pageMap.put("TIME",0);
        pageMap.put("The New York Times",0);
        pageMap.put("Bloomberg Business",0);
        pageMap.put("CNN",0);
        pageMap.put("Fox News",0);
        pageMap.put("Forbes",0);
        pageMap.put("Washington Post",0);
        pageMap.put("The Guardian",0);
        pageMap.put("The Times",0);
        pageMap.put("所有",0);

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
            pageMap.put(current_source,1);
            int count=0;
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
                        if (getArticleCachedStatus(uri)&&(!flag_search)) continue;
                        //if (article.getBoolean("isDuplicate")) continue;
                        title = article.getString("title");
                        time = article.getString("dateTime");
                        JSONObject source = article.getJSONObject("source");
                        source_title = source.getString("title");
                        String imageurl = article.getString("image");
                        ArticleEntity lin = new ArticleEntity( uri,title,  getDate(time), source_title,imageurl, false,null);
                        alArticleInfo.add(0, lin);
                        count++;
                        if (!getArticleCachedStatus(uri))
                        daoArticle.insert(lin);
                    }

                } catch (JSONException e) {

                }

                //sync to the listView
                articleInfoAdapter.notifyItemRangeInserted(0,count);
                infoListView.smoothScrollToPosition(0);
                mRefreshLayout.endRefreshing();// finish fetching from sever

            }
        }
    }
    class LoadingTask extends AsyncTask<String, String, String> {
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
            mRefreshLayout.endLoadingMore();// finish fetching from sever
            int size=alArticleInfo.size();
            int count=0;
            if (result != null) {
                try {   //parse word from json
                    //sample link.:http://dict-co.iciba.com/api/dictionary.php?w=go&key=341DEFE6E5CA504E62A567082590D0BD&type=json
                    String uri, title, source_title, time;
                    JSONObject reader = new JSONObject(result);
                    JSONObject articles = reader.getJSONObject("articles");
                    JSONArray results = articles.getJSONArray("results");
                    for (int i = 0; i <result.length(); i++) {
                        JSONObject article = results.getJSONObject(i);
                        uri = article.getString("uri");
                        title = article.getString("title");
                        time = article.getString("dateTime");
                        JSONObject source = article.getJSONObject("source");
                        source_title = source.getString("title");
                        String imageurl = article.getString("image");
                        ArticleEntity lin = new ArticleEntity(uri, title, getDate(time), source_title, imageurl, false, null);
                        alArticleInfo.add(lin);
                        count++;
                        if (!getArticleCachedStatus(uri))
                            daoArticle.insert(lin);
                    }

                } catch (JSONException e) {

                }

                //sync to the listView
                articleInfoAdapter.notifyItemRangeInserted(size,count);
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

        if (isNetworkAvailable()) {
            pageMap.put(current_source,pageMap.get(current_source)+1);
            new LoadingTask().execute(requestUrl+"&articlesPage="+(String)(pageMap.get(current_source)+""));

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


    /**
     * On message event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSourceSelectEvent(SourceSelectEvent event) {
        int size;
        List<ArticleEntity> cache;
        current_source=event.title;
        switch (event.title) {
            case "所有": {//all
                requestUrl = "http://eventregistry.org/json/article?query=%7B%22%24query%22%3A%7B%22lang%22%3A%22eng%22%7D%7D&action=getArticles&resultType=articles&articlesSortBy=rel&articlesCount=5&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&apiKey=19411967-5bfe-4f2a-804e-580654db39c9";
                alArticleInfo.clear();
                alArticleInfo .addAll(daoArticle.queryBuilder().orderDesc(ArticleEntityDao.Properties.Time).list());
                articleInfoAdapter.notifyDataSetChanged();
                break;
            }
            case "National Geographic": {   //national geographic
                requestUrl=getRequestUrl("news.nationalgeographic.com");
                setSourceForView("National Geographic");
                break;
            }
            case "Nature": {   //nature
                requestUrl=getRequestUrl("nature.com");
                setSourceForView("Nature");
                break;
            }
            case "The Economist": {   //the economist
                requestUrl=getRequestUrl("economist.com");
                setSourceForView("The Economist");
                break;
            }
            case "TIME": {   //TIME
                requestUrl=getRequestUrl("time.com");
                setSourceForView("TIME");
                break;
            }
            case "The New York Times": {   //The New York Times
                requestUrl=getRequestUrl("nytimes.com");
                setSourceForView("The New York Times");
                break;
            }
            case "Bloomberg Business": {   //Bloomberg Business
                requestUrl=getRequestUrl("bloomberg.com");
                setSourceForView("Bloomberg Business");
                break;
            }
            case "CNN": {   //CNN
                requestUrl=getRequestUrl("edition.cnn.com");
                setSourceForView("CNN");
                break;
            }
            case "Fox News": {   //Fox
                requestUrl=getRequestUrl("foxnews.com");
                setSourceForView("Fox News");
                break;
            }
            case "Forbes": {   //Forbes
                requestUrl=getRequestUrl("forbes.com");
                setSourceForView("Forbes");
                break;
            }
            case "Washington Post": {   //Washington Post
                requestUrl=getRequestUrl("washingtonpost.com");
                setSourceForView("Washington Post");
                break;
            }
            case "The Guardian": {   //The Guardian
                requestUrl=getRequestUrl("theguardian.com");
                setSourceForView("The Guardian");
                break;
            }
            case "The Times": {   //The Guardian
                requestUrl=getRequestUrl("thetimes.co.uk");
                setSourceForView("The Times");
                break;
            }
        }
        searchUrlPrefix=requestUrl+"&keywordLoc=title&keyword=";
        alArticleInfoCache.addAll(alArticleInfo);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArticleSearchEvent(ArticleSearchEvent event) {
        if (!flag_search) {
            flag_search=true;
            requestUrlCache=requestUrl;

        }
            requestUrl=searchUrlPrefix+event.keyword;
            alArticleInfo.clear();
            articleInfoAdapter.notifyDataSetChanged();
            mRefreshLayout.beginRefreshing();


    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArticleSearchDoneEvent(ArticleSearchDoneEvent event){
        flag_search=false;
        alArticleInfo.clear();
        alArticleInfo.addAll(alArticleInfoCache);
        alArticleInfoCache.clear();
        articleInfoAdapter.notifyDataSetChanged();
        requestUrl=requestUrlCache;
    }
    private String getRequestUrl(String source_url) {
        return "http://eventregistry.org/json/article?sourceUri=" +
                source_url +
                "&action=getArticles&" +
                "resultType=articles&" +
                "articlesSortBy=date&" +
                "articlesCount=5&" +
                "articlesIncludeArticleEventUri=false&" +
                "articlesIncludeArticleImage=true&" +
                "articlesArticleBodyLen=0&" +
                "articlesIncludeConceptLabel=false&" +
                "apiKey=19411967-5bfe-4f2a-804e-580654db39c9";

    }

    private void setSourceForView(String source) {
        int size;
        List<ArticleEntity> cache;
        alArticleInfo.clear();
        alArticleInfo.addAll( daoArticle.queryBuilder().orderDesc(ArticleEntityDao.Properties.Time).where(ArticleEntityDao.Properties.Source.like(source + "%")).list());

        pageMap.put(source,pageMap.get(source)/5);
        articleInfoAdapter.notifyDataSetChanged();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArticleSwipeCollectEvent(ArticleCollectionStatusChangedEvent event) {
        String uri = event.uri;
        final ArticleEntity article = daoArticle.queryBuilder().where(ArticleEntityDao.Properties.Uri.eq(uri)).list().get(0);
        if (article.getCollectStatus()) {
            article.setCollectStatus(false);
            daoArticle.update(article);
        } else {
            //add collection
            article.setCollectStatus(true);
            Date currentTime = Calendar.getInstance().getTime();
            article.setCollectTime(currentTime);
            daoArticle.update(article);


        }
        EventBus.getDefault().postSticky(new CollectArticleEvent(0));
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
        for (ArticleEntity exist : daoArticle.loadAll()) {
            current_uri_list.add(exist.getUri());
        }
        if (current_uri_list.contains(uri)) return true;
        else return false;
    }

}



