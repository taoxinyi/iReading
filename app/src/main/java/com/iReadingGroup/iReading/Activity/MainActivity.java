package com.iReadingGroup.iReading.Activity;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iReadingGroup.iReading.Adapter.MainActivityPagesAdapter;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Bean.DaoMaster;
import com.iReadingGroup.iReading.Bean.DaoSession;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.DoubleClickBackToContentTopListener;
import com.iReadingGroup.iReading.Event.ArticleSearchDoneEvent;
import com.iReadingGroup.iReading.Event.ArticleSearchEvent;
import com.iReadingGroup.iReading.Event.BackToTopEvent;
import com.iReadingGroup.iReading.Event.ButtonCheckEvent;
import com.iReadingGroup.iReading.Event.SourceSelectEvent;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.lzy.widget.AlphaIndicator;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.DimenHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.suke.widget.SwitchButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.database.Database;

import java.util.HashMap;

import cn.bingoogolapple.badgeview.BGABadgeAlphaView;


/**
 * MainActivity
 * Initialize UI and database when app launched
 * <p>
 * Keep in mind that every UI object in menu,toolbar,slide drawer is initialized here
 * The visibility of such object is controlled here
 * They are handled here and post Event to according fragment(or nested fragment if necessary)
 * <p>
 * Databases are loaded here in service of other fragment in this activity
 *
 * @author iReadingGroup
 * @version 1.1.0
 */
public class MainActivity extends AppCompatActivity implements
        DoubleClickBackToContentTopListener.IBackToContentTopView {
    public Toolbar toolBar;
    private ViewPager viewPager;
    private BGABadgeAlphaView collectionBadge;
    private WordCollectionBeanDao daoCollection;
    private OfflineDictBeanDao daoDictionary;
    private ArticleEntityDao daoArticle;
    public SwitchButton switchButton;
    public MenuItem button;
    public MenuItem searchItem;
    public boolean buttonStatus = false;//whether the button is clicked
    public int last_nested_page = 0;//which nested page is selected when the page changes
    private Drawer drawer;
    private String last_section = "所有";

    /**
     * Create the activity
     * Initialize UI
     * Copy database in assets folder to external storage and then initialize database
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.iReadingGroup.iReading.R.layout.activity_main);//set layout
        initializeUI();
        MyApplication app = (MyApplication) getApplicationContext();//initialize UI
        initializeDatabase();//initializeDatabase
        app.setDaoArticle(daoArticle);
        app.setDaoCollection(daoCollection);
        app.setDaoDictionary(daoDictionary);

    }

    @Override
    public void backToContentTop() {
        EventBus.getDefault().post(new BackToTopEvent());
    }

    /**
     * Initialize UI
     * Including toolbar,status bar, view pager, tab layout and badge
     */

    private void initializeUI() {
        //initializeUI in main activity
        initializeToolBar(); //initialize ToolBar
        initializeStatusBar();//set color for status bar for immersive looking
        initializeViewPager();//initialize ViewPage for each tab's fragment
        initializeTabLayout();//initialize weChat stle tabLayout and link it to viewpager
        initializeCollectionBadge();//initialize the Badge of Collection
        initializeDrawer();

    }

    /**
     * Copy the database in asset into external storage
     */

    private void initializeDatabase() {


        daoDictionary =  ((MyApplication)getApplication()).getDaoDictionary();//this is the offline dictionary database
        daoCollection =  ((MyApplication)getApplication()).getDaoCollection();// this is the database recording user's word collection
        daoArticle = ((MyApplication)getApplication()).getDaoArticle();

    }

    /**
     * Initialize the toolbar and title
     */

    private void initializeToolBar() {
        //initialize ToolBar
        toolBar = (Toolbar) findViewById(com.iReadingGroup.iReading.R.id.toolbar);
        ((TextView) findViewById(R.id.toolbar_title)).setText("所有");

        toolBar.setTitle("");
        toolBar.setOnClickListener(new DoubleClickBackToContentTopListener(this));

        setSupportActionBar(toolBar);
    }

    /**
     * Initialize the status bar with primary color
     */
    private void initializeStatusBar() {
        //set StatusBar Color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    /**
     * Initialize the viewpager
     * Create the connection among each page's fragment, the adapter and the viewpager
     */
    private void initializeViewPager() {
        //initialize ViewPage for each tab's fragment
        MainActivityPagesAdapter mainActivityPagesAdapter = new MainActivityPagesAdapter(getSupportFragmentManager());//set Fragment main adapter
        viewPager = (ViewPager) findViewById(com.iReadingGroup.iReading.R.id.viewPager);
        viewPager.setAdapter(mainActivityPagesAdapter);//set adapter,link to each fragment
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                //if the page is selected change the title to corresponding category.
                invalidateOptionsMenu();
                switch (position) {
                    case 0:
                        viewPager.setCurrentItem(0);
                        ((TextView) findViewById(R.id.toolbar_title)).setText(last_section);
                        break;
                    case 1:
                        viewPager.setCurrentItem(1);
                        ((TextView) findViewById(R.id.toolbar_title)).setText("查词");
                        break;
                    case 2:
                        viewPager.setCurrentItem(2);
                        ((TextView) findViewById(R.id.toolbar_title)).setText("收藏");
                        collectionBadge.hiddenBadge();
                        break;
                    case 3:
                        viewPager.setCurrentItem(3);
                        ((TextView) findViewById(R.id.toolbar_title)).setText("关于");
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

    private void initializeTabLayout() {
        AlphaIndicator alphaIndicator = (AlphaIndicator) findViewById(com.iReadingGroup.iReading.R.id.alphaIndicator);//
        alphaIndicator.setViewPager(viewPager);
    }

    private void initializeCollectionBadge() {
        collectionBadge = findViewById(R.id.collectionIcon);
    }

    /**
     * Initialize the side drawer with source and icon
     */
    private void initializeDrawer() {
        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item0 = new PrimaryDrawerItem().withName("选择你的源");
        PrimaryDrawerItem all = new PrimaryDrawerItem().withName("所有").withIcon(R.mipmap.icon);
        PrimaryDrawerItem nG = new PrimaryDrawerItem().withName("National Geographic").withIcon(R.mipmap.icon_ng);
        PrimaryDrawerItem nature = new PrimaryDrawerItem().withName("Nature").withIcon(R.mipmap.icon_nature);
        PrimaryDrawerItem tE = new PrimaryDrawerItem().withName("The Economist").withIcon(R.mipmap.icon_economist);
        PrimaryDrawerItem time = new PrimaryDrawerItem().withName("TIME").withIcon(R.mipmap.icon_time);
        PrimaryDrawerItem tNYT = new PrimaryDrawerItem().withName("The New York Times").withIcon(R.mipmap.icon_nytimes);
        PrimaryDrawerItem bB = new PrimaryDrawerItem().withName("Bloomberg Business").withIcon(R.mipmap.icon_bloomberg);
        PrimaryDrawerItem cnn = new PrimaryDrawerItem().withName("CNN").withIcon(R.mipmap.icon_cnn);
        PrimaryDrawerItem fN = new PrimaryDrawerItem().withName("Fox News").withIcon(R.mipmap.icon_fox);
        PrimaryDrawerItem forbes = new PrimaryDrawerItem().withName("Forbes").withIcon(R.mipmap.icon_forbes);
        PrimaryDrawerItem wP = new PrimaryDrawerItem().withName("Washington Post").withIcon(R.mipmap.icon_washingtonpost);
        PrimaryDrawerItem tG = new PrimaryDrawerItem().withName("The Guardian").withIcon(R.mipmap.icon_theguardian);
        PrimaryDrawerItem tT = new PrimaryDrawerItem().withName("The Times").withIcon(R.mipmap.icon_thetimes);
        PrimaryDrawerItem dm = new PrimaryDrawerItem().withName("Mail Online").withIcon(R.mipmap.icon_dailymail);
        PrimaryDrawerItem bbc = new PrimaryDrawerItem().withName("BBC").withIcon(R.mipmap.icon_bbc);
        PrimaryDrawerItem people = new PrimaryDrawerItem().withName("PEOPLE").withIcon(R.mipmap.icon_people);

        final HashMap<String, String> map = new HashMap<String, String>();//map for title:chinese name
        map.put("National Geographic", "国家地理");
        map.put("Nature", "自然");
        map.put("The Economist", "经济学人");
        map.put("TIME", "时代");
        map.put("The New York Times", "纽约时报");
        map.put("Bloomberg Business", "彭博商业");
        map.put("CNN", "有线电视新闻网");
        map.put("Fox News", "福克斯新闻");
        map.put("Forbes", "福布斯");
        map.put("Washington Post", "华盛顿邮报");
        map.put("The Guardian", "卫报");
        map.put("The Times", "泰晤士报");
        map.put("Mail Online", "每日邮报");
        map.put("BBC", "英国广播公司");
        map.put("PEOPLE", "人物");
        // Create the AccountHeader
        final ProfileDrawerItem item = new ProfileDrawerItem().withIdentifier(0).withName("iReading").withIcon(getResources().getDrawable(R.mipmap.icon));
        final AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(
                        item
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return true;
                    }
                })
                .build();

        //create the drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult, true)
                .withHeader(R.layout.header)
                .withHeaderHeight(DimenHolder.fromDp(30))
                .withStickyHeaderShadow(false)
                .addDrawerItems(
                        all,
                        bB,
                        bbc,
                        cnn,
                        forbes,
                        fN,
                        dm,
                        nG,
                        nature,
                        people,
                        tE,
                        tG,
                        time,
                        tNYT,
                        tT,
                        wP

                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //post the event to article list fragment about what section is selected
                        String title = ((PrimaryDrawerItem) drawerItem).getName().toString();
                        ((TextView) findViewById(R.id.toolbar_title)).setText(title);
                        last_section = title;
                        EventBus.getDefault().post(new SourceSelectEvent(title));
                        headerResult.updateProfile(new ProfileDrawerItem().withIdentifier(0).withEmail(title)
                                .withIcon(((PrimaryDrawerItem) drawerItem).getIcon().getIconRes()).withName(map.get(title)));
                        return true;
                    }
                })
                .build();

    }


    /**
     * Get the database instance of WordCollection
     * It can be used for other fragment.
     *
     * @return WordCollectionBeanDao the word collection instance
     */
    public WordCollectionBeanDao getDaoCollection() {
        return daoCollection;
    }

    /**
     * Get the database instance of OfflineDictionary
     * It can be used for other fragment.
     *
     * @return OfflineDictBeanDao the offline dictionary instance
     */
    public OfflineDictBeanDao getDaoDictionary() {
        return daoDictionary;
    }

    /**
     * Get the database instance of OfflineDictionary
     * It can be used for other fragment.
     *
     * @return ArticleEntityDao  the article cache instance
     */
    public ArticleEntityDao getDaoArticle() {
        return daoArticle;
    }

    /**
     * Create custom option menu
     *
     * @param menu
     * @return super of it's original function
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        button = menu.findItem(R.id.switch_button_layout);
        searchItem = menu.findItem(R.id.action_search);
        //set menu item's visibility when page changes
        DrawerLayout drawerLayout = drawer.getDrawerLayout();
        if (viewPager.getCurrentItem() == 0) {
            searchItem.setVisible(true);
            button.setVisible(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        } else if (viewPager.getCurrentItem() == 1) {
            searchItem.setVisible(false);
            button.setVisible(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else if (viewPager.getCurrentItem() == 2) {
            searchItem.setVisible(false);
            if (last_nested_page == 0) button.setVisible(true);
            else button.setVisible(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else if (viewPager.getCurrentItem() == 3) {
            searchItem.setVisible(false);
            button.setVisible(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        }
        LinearLayout view = (LinearLayout) menu.findItem(R.id.switch_button_layout).getActionView();
        switchButton = view.findViewById(R.id.switch_button);
        switchButton.setChecked(buttonStatus);
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                //post event to ArticleCollectionNestedFragment once button clicked
                EventBus.getDefault().post(new ButtonCheckEvent(isChecked));

            }
        });
        LinearLayout view2 = (LinearLayout) menu.findItem(R.id.action_search).getActionView();
        final SearchView sv = view2.findViewById(R.id.searchView);
        sv.setMaxWidth(Integer.MAX_VALUE);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                //post event to ArticleListFragment once search submitted
                EventBus.getDefault().post(new ArticleSearchEvent(query));
                sv.clearFocus();

                return true;
            }
        });
        sv.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //post event to ArticleListFragment once search closed
                EventBus.getDefault().post(new ArticleSearchDoneEvent());
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


}


