package com.iReadingGroup.iReading.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.iReadingGroup.iReading.Activity.ArticleDetailActivity;
import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.Adapter.ArticleInfoAdapter;
import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Event.ArticleDatabaseChangedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import static java.lang.Math.max;
import static java.util.Collections.max;


/**
 * ArticleCollectionNestedFragment
 * The nested fragment from CollectionFragment
 *
 * Load the articles from database and add them into ArrayList
 * Almost identical to ArticleListFragment, unless here we cannot refresh
 *

 *
 */
public  class ArticleCollectionNestedFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate{
    private BGARefreshLayout mRefreshLayout; //Layout for refreshing and loading
    private RecyclerView infoListView;
    private ArticleInfoAdapter articleInfoAdapter;//Custom adapter for article info
    private List<ArticleEntity> alArticleInfo=new ArrayList<>();//ArrayList linked to adapter for listview
    private View view;
    private ArticleEntityDao daoArticle;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Must add in every fragments' onCreateView to avoid duplicate creating.
        if (null != view){
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent) {
                parent.removeView(view);
            }
        }else {
            //start initializing
            view = inflater.inflate(com.iReadingGroup.iReading.R.layout.fragment_article_info, container, false);//set layout
            daoArticle = ((MainActivity)getActivity()).getDaoArticle();//get dataset
            initializeUI();

        }
        return view;
    }

    /**
     * Initialize ui.
     */
    public void initializeUI(){
        initializeRefreshingLayout();//Initialize refreshing and loading layout
        initializeRecycleView();
    }

    /**
     * Initialize refreshing layout.
     */
    public void initializeRefreshingLayout(){
        mRefreshLayout = (BGARefreshLayout) view.findViewById(com.iReadingGroup.iReading.R.id.rl_modulename_refresh);
        mRefreshLayout.setDelegate(this);
        mRefreshLayout.setIsShowLoadingMoreView(true);
        mRefreshLayout.setPullDownRefreshEnable(false);//disable refresh
    }


    /**
     * Initialize list view.
     */
    public void initializeRecycleView(){//Establish the connection among listView,adapter and arrayList.

        infoListView = (RecyclerView) view.findViewById(com.iReadingGroup.iReading.R.id.list);//

       alArticleInfo=daoArticle.queryBuilder().orderAsc(ArticleEntityDao.Properties.CollectTime)
                .where(ArticleEntityDao.Properties.CollectStatus.eq(true)).list();

        articleInfoAdapter = new ArticleInfoAdapter(getActivity(),
                com.iReadingGroup.iReading.R.layout.listitem_article_info,alArticleInfo);
        infoListView.setAdapter(articleInfoAdapter);//link the adapter to ListView
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        infoListView.setLayoutManager(llm);
        //Set click event for listView and pass the arguments through Bundle to the following activity.

        articleInfoAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        articleInfoAdapter.isFirstOnly(true);
        infoListView.addOnItemTouchListener(new  OnItemClickListener( ) {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter parent, View view, int position)
            {
                ArticleEntity h = (ArticleEntity) alArticleInfo.get(position);
                String number=h.getName();
                String uri=h.getUri();
                Intent intent = new Intent(getActivity(),ArticleDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", number);
                bundle.putString("uri",uri);
                bundle.putString("time",getDate(h.getTime()));
                bundle.putString("source",h.getSource());
                intent.putExtras(bundle);
                startActivity(intent);
                //FruitList.this.finish();
            }
        });
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        // Cannot refresh here
            mRefreshLayout.endRefreshing();
        }



    /**
     *
     * @param refreshLayout
     * @return
     */
    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return  false;
    }
    private String getDate(String OurDate)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date value = formatter.parse(OurDate);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd HH:mm"); //this format changeable
            dateFormatter.setTimeZone(TimeZone.getDefault());
            OurDate = dateFormatter.format(value);

        }
        catch (Exception e)
        {
            OurDate = "00-00-0000 00:00";
        }
        return OurDate;
    }
    /**
     * On ArticleDatabaseChangedEvent
     *
     * Receive the event when new article(s) collected
     * than read data from database again and update view
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onArticleDatabaseChangedEvent (ArticleDatabaseChangedEvent event) {
        String uri=event.uri;
        String operation=event.operation;
        ArticleEntity article = daoArticle.queryBuilder().where(ArticleEntityDao.Properties.Uri.eq(uri)).list().get(0);
        int index=-1;
        for (int i=0;i<alArticleInfo.size();i++) {
            if (alArticleInfo.get(i).getUri().equals(uri)) {
                index = i;
                break;
            }
        }
        if (operation.equals("remove") && index!=-1)
        {//not collected but in collection list
            alArticleInfo.remove(index);
            articleInfoAdapter.notifyItemRemoved(index);
        }
        else if (operation.equals("add")&& index==-1) {//collected but not in the list
            alArticleInfo.add(0, article);
            articleInfoAdapter.notifyItemInserted(0);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!EventBus.getDefault().isRegistered(this))
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



}



