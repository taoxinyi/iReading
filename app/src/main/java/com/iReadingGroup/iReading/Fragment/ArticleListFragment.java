package com.iReadingGroup.iReading.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Event.ArticleDatabaseChangedEvent;
import com.iReadingGroup.iReading.Event.ArticleSearchDoneEvent;
import com.iReadingGroup.iReading.Event.ArticleSearchEvent;
import com.iReadingGroup.iReading.Event.BackToTopEvent;
import com.iReadingGroup.iReading.Event.SourceSelectEvent;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.SpeedyLinearLayoutManager;

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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

import static android.content.ContentValues.TAG;


/**
 * ArticleListFragment
 * Load article when refresh, change source when source section changes
 * <p>
 * Load the articles from database and add them into ArrayList
 * Update through refreshing or loading using AsyncTask
 * If source changes, the view changes accordingly
 * search in corresponding source
 */
public class ArticleListFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate {
    private String numberPerLoading;
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
    private String requestUrl;
    private HashMap<String, Integer> pageMap = new HashMap<>();
    private String current_source = "所有";
    private String requestUrlCache;
    private String searchUrlPrefix;
    private Integer pageCache;
    private SpeedyLinearLayoutManager layoutManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        numberPerLoading = ((MyApplication) getActivity().getApplication()).getSetting("number");
        if (requestUrl == null)
            requestUrl = "http://eventregistry.org/json/article?" +
                    "lang=eng&action=getArticles&resultType=articles&articlesSortBy=date&" +
                    "articlesCount=" + numberPerLoading +
                    "&articlesIncludeArticleEventUri=false&" +
                    "articlesIncludeArticleImage=true&" +
                    "articlesArticleBodyLen=0&articlesIncludeConceptLabel=false&" +
                    "apiKey=475f7fdb-7929-4222-800e-0151bdcd4af2";
        else
            requestUrl=requestUrl.replaceAll("(articlesCount=)[^&]*(&)", String.format("$1%s$2",numberPerLoading));
        searchUrlPrefix = requestUrl + "&keywordLoc=title&keyword=";
    }

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

            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int first = layoutManager.findFirstVisibleItemPosition();
                    int last = layoutManager.findLastVisibleItemPosition();
                    articleInfoAdapter.notifyItemRangeChanged(first, last, "payload");
                    //要做的事情
                    handler.postDelayed(this, 60000);
                }
            };
            handler.postDelayed(runnable, 60000);//每60秒执行一次runnable.

        }
        return view;
    }

    /**
     * Initialize ui.
     */
    public void initializeUI() {
        initializeRefreshingLayout();//Initialize refreshing and loading layout
        initializeListView();
    }

    /**
     * Initialize refreshing layout.
     */
    public void initializeRefreshingLayout() {
        mRefreshLayout = view.findViewById(R.id.rl_modulename_refresh);
        mRefreshLayout.setDelegate(this);
        BGAMoocStyleRefreshViewHolder refreshViewHolder = new BGAMoocStyleRefreshViewHolder(getContext(), true);
        refreshViewHolder.setOriginalImage(R.mipmap.icon1);
        refreshViewHolder.setUltimateColor(com.iReadingGroup.iReading.R.color.custom_imoocstyle);
        mRefreshLayout.setIsShowLoadingMoreView(true);
        refreshViewHolder.setLoadingMoreText("加载历史文章……");
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);
    }


    /**
     * Initialize list view.
     */
    public void initializeListView() {//Establish the connection among listView,adapter and arrayList.

        infoListView = view.findViewById(R.id.list);//

        alArticleInfo = daoArticle.queryBuilder().orderDesc(ArticleEntityDao.Properties.Time).list();
        alArticleInfoCache.addAll(alArticleInfo);


        //articleInfoAdapter = new ArticleInfoAdapter(getActivity(),
        //com.iReadingGroup.iReading.R.layout.listitem_article_info,alArticleInfo);//link the arrayList to adapter,using custom layout for each item
        articleInfoAdapter = new ArticleInfoAdapter(getActivity(),
                com.iReadingGroup.iReading.R.layout.listitem_article_info, alArticleInfo);
        infoListView.setAdapter(articleInfoAdapter);//link the adapter to ListView
        layoutManager = new SpeedyLinearLayoutManager(getContext(), SpeedyLinearLayoutManager.VERTICAL, false);
        infoListView.setLayoutManager(layoutManager);
        //Set click event for listView and pass the arguments through Bundle to the following activity.

        articleInfoAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        articleInfoAdapter.isFirstOnly(true);

        infoListView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter parent, View view, int position) {
                ArticleEntity h = alArticleInfo.get(position);
                String number = h.getName();
                String uri = h.getUri();
                Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", number);
                bundle.putString("uri", uri);
                bundle.putString("time", getDate(h.getTime()));
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
    private void initializeMap() {
        pageMap.put("National Geographic", 0);
        pageMap.put("Nature", 0);
        pageMap.put("The Economist", 0);
        pageMap.put("TIME", 0);
        pageMap.put("The New York Times", 0);
        pageMap.put("Bloomberg Business", 0);
        pageMap.put("CNN", 0);
        pageMap.put("Fox News", 0);
        pageMap.put("Forbes", 0);
        pageMap.put("Washington Post", 0);
        pageMap.put("The Guardian", 0);
        pageMap.put("The Times", 0);
        pageMap.put("Mail Online", 0);
        pageMap.put("BBC", 0);
        pageMap.put("PEOPLE", 0);
        pageMap.put("所有", 0);

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
            new RefreshingTask(this).execute(requestUrl + "&articlesPage=1");
        } else {
            // network is not connected,end
            mRefreshLayout.endRefreshing();
        }
    }

    /**
     * The type Refreshing task.
     */
    static class RefreshingTask extends AsyncTask<String, String, String> {
        private WeakReference<ArticleListFragment> weakFragmentRef;

        /**
         * Instantiates a new Refreshing task.
         *
         * @param fragment the fragment
         */
        public RefreshingTask(ArticleListFragment fragment) {
            weakFragmentRef = new WeakReference<ArticleListFragment>(fragment);
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                Log.d("here",params[0]);
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
            ArticleListFragment fragment = weakFragmentRef.get();
            if (fragment == null) return;

            fragment.pageMap.put(fragment.current_source, 1);
            int count = 0;
            if (result != null) {
                try {   //parse word from json
                    //sample link.:http://dict-co.iciba.com/api/dictionary.php?w=go&key=341DEFE6E5CA504E62A567082590D0BD&type=json
                    Log.d(TAG, "here "+result);
                    String uri, title, source_title, time;
                    JSONObject reader = new JSONObject(result);
                    JSONObject articles = reader.getJSONObject("articles");
                    JSONArray results = articles.getJSONArray("results");
                    for (int i = results.length() - 1; i > -1; i--) {
                        JSONObject article = results.getJSONObject(i);
                        uri = article.getString("uri");
                        if (fragment.getArticleCachedStatus(uri) && (!fragment.flag_search))
                            continue;
                        //if (article.getBoolean("isDuplicate")) continue;
                        title = article.getString("title");
                        time = article.getString("dateTime");
                        JSONObject source = article.getJSONObject("source");
                        source_title = source.getString("title");
                        String imageurl = article.getString("image");
                        ArticleEntity lin = new ArticleEntity(uri, title, time, source_title, imageurl, false, null);
                        fragment.alArticleInfo.add(0, lin);
                        count++;
                        if (!fragment.getArticleCachedStatus(uri))
                            fragment.daoArticle.insert(lin);
                    }

                } catch (JSONException e) {

                }

                //sync to the listView
                fragment.articleInfoAdapter.notifyItemRangeInserted(0, count);
                fragment.infoListView.smoothScrollToPosition(0);
                fragment.mRefreshLayout.endRefreshing();// finish fetching from sever

            }
        }
    }

    /**
     * The type Loading task.
     */
    static class LoadingTask extends AsyncTask<String, String, String> {
        private WeakReference<ArticleListFragment> weakFragmentRef;

        /**
         * Instantiates a new Loading task.
         *
         * @param fragment the fragment
         */
        public LoadingTask(ArticleListFragment fragment) {
            weakFragmentRef = new WeakReference<ArticleListFragment>(fragment);
        }

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
            ArticleListFragment fragment = weakFragmentRef.get();
            if (fragment == null) return;

            int size = fragment.alArticleInfo.size();
            int count = 0;
            if (result != null) {
                try {
                    fragment.mRefreshLayout.endLoadingMore();
                    String uri, title, source_title, time;
                    JSONObject reader = new JSONObject(result);
                    JSONObject articles = reader.getJSONObject("articles");
                    JSONArray results = articles.getJSONArray("results");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject article = results.getJSONObject(i);
                        uri = article.getString("uri");
                        title = article.getString("title");
                        time = article.getString("dateTime");
                        JSONObject source = article.getJSONObject("source");
                        source_title = source.getString("title");
                        String imageurl = article.getString("image");
                        ArticleEntity lin = new ArticleEntity(uri, title, time, source_title, imageurl, false, null);

                        count++;
                        if (!fragment.getArticleCachedStatus(uri)) {
                            fragment.alArticleInfo.add(lin);
                            fragment.daoArticle.insert(lin);
                        }
                    }

                } catch (JSONException e) {

                }

                //sync to the listView
                fragment.articleInfoAdapter.notifyItemRangeInserted(size, count);
            }
        }
    }


    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        //  Loading more (history) data from server or cache, Return false to disable the refreshing action.

        if (isNetworkAvailable()) {
            pageMap.put(current_source, pageMap.get(current_source) + 1);
            new LoadingTask(this).execute(requestUrl + "&articlesPage=" + pageMap.get(current_source) + "");
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
        numberPerLoading = ((MyApplication) (getActivity().getApplication())).getSetting("number");
        int size;
        List<ArticleEntity> cache;
        current_source = event.title;
        switch (event.title) {
            case "所有": {//all
                requestUrl = "http://eventregistry.org/json/article?lang=eng&action=getArticles" +
                        "&resultType=articles&articlesSortBy=date&articlesCount=" + numberPerLoading +
                        "&articlesIncludeArticleEventUri=false&articlesIncludeArticleImage=true" +
                        "&articlesArticleBodyLen=0&articlesIncludeConceptLabel=false" +
                        "&apiKey=475f7fdb-7929-4222-800e-0151bdcd4af2";
                setSourceForView("所有");

                break;
            }
            case "National Geographic": {   //national geographic
                requestUrl = getRequestUrl("news.nationalgeographic.com");
                setSourceForView("National Geographic");
                break;
            }
            case "Nature": {   //nature
                requestUrl = getRequestUrl("nature.com");
                setSourceForView("Nature");
                break;
            }
            case "The Economist": {   //the economist
                requestUrl = getRequestUrl("economist.com");
                setSourceForView("The Economist");
                break;
            }
            case "TIME": {   //TIME
                requestUrl = getRequestUrl("time.com");
                setSourceForView("TIME");
                break;
            }
            case "The New York Times": {   //The New York Times
                requestUrl = getRequestUrl("nytimes.com");
                setSourceForView("The New York Times");
                break;
            }
            case "Bloomberg Business": {   //Bloomberg Business
                requestUrl = getRequestUrl("bloomberg.com");
                setSourceForView("Bloomberg Business");
                break;
            }
            case "CNN": {   //CNN
                requestUrl = getRequestUrl("edition.cnn.com");
                setSourceForView("CNN");
                break;
            }
            case "Fox News": {   //Fox
                requestUrl = getRequestUrl("foxnews.com");
                setSourceForView("Fox News");
                break;
            }
            case "Forbes": {   //Forbes
                requestUrl = getRequestUrl("forbes.com");
                setSourceForView("Forbes");
                break;
            }
            case "Washington Post": {   //Washington Post
                requestUrl = getRequestUrl("washingtonpost.com");
                setSourceForView("Washington Post");
                break;
            }
            case "The Guardian": {   //The Guardian
                requestUrl = getRequestUrl("theguardian.com");
                setSourceForView("The Guardian");
                break;
            }
            case "The Times": {   //The Times
                requestUrl = getRequestUrl("thetimes.co.uk");
                setSourceForView("The Times");
                break;
            }
            case "Mail Online": {   //Mail Online
                requestUrl = getRequestUrl("dailymail.co.uk");
                setSourceForView("Mail Online");
                break;
            }
            case "BBC": {   //BBC
                requestUrl = getRequestUrl("bbc.com");
                setSourceForView("BBC");
                break;
            }
            case "PEOPLE": {   //PEOPLE
                requestUrl = getRequestUrl("people.com");
                setSourceForView("PEOPLE");
                break;
            }
        }
        searchUrlPrefix = requestUrl + "&keywordLoc=title&keyword=";
        alArticleInfoCache.clear();
        alArticleInfoCache.addAll(alArticleInfo);

    }


    /**
     * On article search event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArticleSearchEvent(ArticleSearchEvent event) {
        if (!flag_search) {
            flag_search = true;
            requestUrlCache = requestUrl;

        }
        requestUrl = searchUrlPrefix + event.keyword;
        alArticleInfo.clear();
        articleInfoAdapter.notifyDataSetChanged();
        Log.d(TAG, "hereonArticleSearchEvent: "+requestUrl);
        mRefreshLayout.beginRefreshing();


    }


    /**
     * On article search done event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArticleSearchDoneEvent(ArticleSearchDoneEvent event) {
        flag_search = false;

        alArticleInfo.clear();
        alArticleInfo.addAll(alArticleInfoCache);
        articleInfoAdapter.notifyDataSetChanged();
        requestUrl = requestUrlCache;

    }

    private String getRequestUrl(String source_url) {
        numberPerLoading = ((MyApplication) (getActivity().getApplication())).getSetting("number");
        return "http://eventregistry.org/json/article?sourceUri=" +
                source_url +
                "&lang=eng" +
                "&action=getArticles&" +
                "resultType=articles&" +
                "articlesSortBy=date&" +
                "articlesCount=" + numberPerLoading +
                "&articlesIncludeArticleEventUri=false&" +
                "articlesIncludeArticleImage=true&" +
                "articlesArticleBodyLen=0&" +
                "articlesIncludeConceptLabel=false&" +
                "apiKey=475f7fdb-7929-4222-800e-0151bdcd4af2";

    }

    private void setSourceForView(String source) {
        int size;
        List<ArticleEntity> cache;
        alArticleInfo.clear();
        if (source != "所有") {
            alArticleInfo.addAll(daoArticle.queryBuilder().orderDesc(ArticleEntityDao.Properties.Time).where(ArticleEntityDao.Properties.Source.like(source + "%")).list());
        } else
            alArticleInfo.addAll(daoArticle.queryBuilder().orderDesc(ArticleEntityDao.Properties.Time).list());
        articleInfoAdapter.notifyDataSetChanged();
        numberPerLoading = ((MyApplication) (getActivity().getApplication())).getSetting("number");
        pageMap.put(source, pageMap.get(source) / Integer.parseInt(numberPerLoading));

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

    /**
     * On article database changed event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArticleDatabaseChangedEvent(ArticleDatabaseChangedEvent event) {
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        articleInfoAdapter.notifyItemRangeChanged(first, last, "ChangeSwipeButton");
    }

    /**
     * On back to top event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackToTopEvent(BackToTopEvent event) {
        infoListView.smoothScrollToPosition(0);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);

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
        return current_uri_list.contains(uri);
    }

}



