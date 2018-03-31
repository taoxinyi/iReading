package com.iReadingGroup.iReading.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.iReadingGroup.iReading.Adapter.MainAdapter;
import com.iReadingGroup.iReading.Bean.ArticleStorageBeanDao;
import com.iReadingGroup.iReading.CollectWordEvent;
import com.iReadingGroup.iReading.Bean.DaoMaster;
import com.iReadingGroup.iReading.Bean.DaoSession;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.lzy.widget.AlphaIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.bingoogolapple.badgeview.BGABadgeAlphaView;


public class MainActivity extends AppCompatActivity{
    private Toolbar toolBar;
    private ViewPager viewPager;
    private MainAdapter mainAdapter;
    private BGABadgeAlphaView collectionBadge;
    private WordCollectionBeanDao daoCollection;
    private OfflineDictBeanDao daoDictionary;
    private ArticleStorageBeanDao daoArticle;
    @SuppressLint("SdCardPath")
    private static final String DB_PATH = "/data/data/com.iReadingGroup.iReading/databases/";//database external path
    private static final String DB_NAME = "wordDetail.db";//database name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.iReadingGroup.iReading.R.layout.activity_main);//set layout
        initializeUI();//initialize UI
        copyDBToDatabases();//copy offline database to external
        initializeDatabase();//initializeDatabase


    }
    private void initializeUI(){
        //initializeUI in main activity
        initializeToolBar(); //initialize ToolBar
        initializeStatusBar();//set color for status bar for immersive looking
        initializeViewPager();//initialize ViewPage for each tab's fragment
        initializeTabLayout();//initialize weChat stle tabLayout and link it to viewpager
        initializeCollectionBadge();//initialize the Badge of Collection


    }

    private void copyDBToDatabases() {
        //copy offline database to external.
        try {
            String outFileName = DB_PATH + DB_NAME;
            File file = new File(DB_PATH);
            if (!file.mkdirs()) {
                file.mkdirs();
            }
            File dataFile = new File(outFileName);
            if (dataFile.exists()) {
                dataFile.delete();
            }
            InputStream myInput;
            myInput = getApplicationContext().getAssets().open(DB_NAME);
            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (IOException e) {
            Log.i("dbdbdb", "error--->" + e.toString());
            e.printStackTrace();
        }

    }

    private void initializeDatabase(){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "wordDetail.db");
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        daoDictionary = daoSession.getOfflineDictBeanDao();

        DaoMaster.DevOpenHelper helper_collection = new DaoMaster.DevOpenHelper(this, "notes-db");
        Database db_collection = helper_collection.getWritableDb();
        DaoSession daoSession_collection = new DaoMaster(db_collection).newSession();
        daoCollection=daoSession_collection.getWordCollectionBeanDao();

        DaoMaster.DevOpenHelper helper_article = new DaoMaster.DevOpenHelper(this, "articledb");
        Database db_article = helper_article.getWritableDb();
        DaoSession daoSession_article = new DaoMaster(db_article).newSession();
        daoArticle=daoSession_article.getArticleStorageBeanDao();
    }

    private void initializeToolBar() {
        //initialize ToolBar
        toolBar=(Toolbar ) findViewById(com.iReadingGroup.iReading.R.id.toolbar);
        toolBar.setTitle("阅读");
        setSupportActionBar(toolBar);
    }

    private void initializeStatusBar(){
        //set StatusBar Color
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimary));
    }

    private void initializeViewPager(){
        //initialize ViewPage for each tab's fragment
        mainAdapter=new MainAdapter(getSupportFragmentManager());//set Fragment main adapter
        viewPager = (ViewPager) findViewById(com.iReadingGroup.iReading.R.id.viewPager);
        viewPager.setAdapter(mainAdapter);//set adapter,link to each fragment
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            // This method will be invoked when a new page becomes selected.

            @Override
            public void onPageSelected(int position) {
                //if the page is selected change the title to corresponding category.
                switch (position) {

                    case 0:
                        viewPager.setCurrentItem(0);
                        toolBar.setTitle("阅读");
                        break;

                    case 1:
                        viewPager.setCurrentItem(1);
                        toolBar.setTitle("查词");
                        break;

                    case 2:
                        viewPager.setCurrentItem(2);
                        toolBar.setTitle("收藏");
                        collectionBadge.hiddenBadge();
                        break;

                    case 3:
                        viewPager.setCurrentItem(3);
                        toolBar.setTitle("我");
                        break;

                }
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });

    }

    private void initializeTabLayout(){
        AlphaIndicator alphaIndicator = (AlphaIndicator) findViewById(com.iReadingGroup.iReading.R.id.alphaIndicator);//
        alphaIndicator.setViewPager(viewPager);
    }

    private void initializeCollectionBadge(){
        collectionBadge=findViewById(R.id.collectionIcon);
    }

    //Subscribe the event
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onCollectWordEvent(CollectWordEvent event) {
        //show the badge when the event is fired;
        collectionBadge.showCirclePointBadge();//show red when collected
    }

    //register for EventBus on CollectWordEvent
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    //unregister for EventBus on CollectWordEvent
    @Override
    public void onStop() {
        //get rid of the CollectWordEvent before it dies.
        CollectWordEvent event = EventBus.getDefault().getStickyEvent(CollectWordEvent.class);
        if (event != null) {
            //remove the received stickyEvent
            EventBus.getDefault().removeStickyEvent(event);
        }
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public WordCollectionBeanDao getdaoCollection(){
        return daoCollection;
    }
    public OfflineDictBeanDao getDaoDictionary(){
        return daoDictionary;
    }
    public ArticleStorageBeanDao getDaoArticle() {return daoArticle;}
}


