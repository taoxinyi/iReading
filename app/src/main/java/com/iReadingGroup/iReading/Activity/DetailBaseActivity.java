package com.iReadingGroup.iReading.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;
import com.daasuu.bl.BubblePopupHelper;
import com.iReadingGroup.iReading.AsyncTask.AsyncResponse;
import com.iReadingGroup.iReading.AsyncTask.FetchingBriefMeaningAsyncTask;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBean;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.CollectionImageView;
import com.iReadingGroup.iReading.Constant;
import com.iReadingGroup.iReading.Event.ChangeWordCollectionDBEvent;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordDetail;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

import me.codeboy.android.aligntextview.AlignTextView;


/**
 * This activity is the base activity fo ArticleDetailActivity and WordDetailActivity
 * A lof of function is almost identical like webView,toolbar,popupWindow,etc...
 */
public abstract class DetailBaseActivity extends AppCompatActivity {
    /**
     * The Dao dictionary.
     */
    protected OfflineDictBeanDao daoDictionary;
    /**
     * The Dao article.
     */
    protected ArticleEntityDao daoArticle;
    /**
     * The Dao collection.
     */
    protected WordCollectionBeanDao daoCollection;
    /**
     * The X.
     */
    protected int x, /**
     * The Y.
     */
    y;//record click position on webview
    /**
     * The Text webview.
     */
    protected WebView textWebview;
    /**
     * The Current word.
     */
    protected String current_word;
    /**
     * The Current meaning.
     */
    protected String current_meaning;
    /**
     * The Popup window.
     */
    protected PopupWindow popupWindow;
    /**
     * The Bubble layout.
     */
    protected BubbleLayout bubbleLayout;
    /**
     * The Ppw collection button.
     */
    protected CollectionImageView ppwCollectionImageView;


    /**
     * The Clicked text view.
     */
    protected boolean clickedTextView = false;

    /**
     * initialize status bar
     * 初始化状态栏
     *
     * @param color the color
     */
    public void initializeStatusBar(int color) {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, color));
    }

    /**
     * Initialize tool bar.
     *
     * @param idToolbar    the id toolbar
     * @param idBackLayout the id back layout
     * @param idTitle      the id title
     * @param title        the title
     */
    public void initializeToolBar(int idToolbar, int idBackLayout, int idTitle, String title) {
        Toolbar toolBar = findViewById(idToolbar);
        toolBar.setTitle("");//set corresponding title in toolbar
        setSupportActionBar(toolBar);
        findViewById(idBackLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(idTitle)).setText(title);
    }

    /**
     * Initialize database.
     */
    public void initializeDatabase() {

        daoDictionary = ((MyApplication) getApplication()).getDaoDictionary();//this is the offline dictionary database
        daoCollection = ((MyApplication) getApplication()).getDaoCollection();// this is the database recording user's word collection
        daoArticle = ((MyApplication) getApplication()).getDaoArticle();
    }

    /**
     * The type My java script interface.
     */
    protected class MyJavaScriptInterface {
        private Context ctx;

        /**
         * Instantiates a new My java script interface.
         *
         * @param ctx the ctx
         */
        public MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        /**
         * Show menu.
         *
         * @param x the x
         * @param y the y
         */
        @JavascriptInterface
        public void showMenu(int x, int y) {
            final int px = (int) (x * ctx.getResources().getDisplayMetrics().density);
            final int py = (int) (y * ctx.getResources().getDisplayMetrics().density);

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    managePopup(px, py);
                }
            };
            mainHandler.post(myRunnable);
        }
    }

    private void managePopup(int px, int py) {
        x = px;
        y = -textWebview.getHeight() + py;

    }

    /**
     * Initialize web view.
     *
     * @param idWebView the id web view
     * @param htmlBody  the html body
     */
    public void initializeWebView(int idWebView, String htmlBody) {
        textWebview = findViewById(idWebView);
        textWebview.requestFocus();
        textWebview.getSettings().setJavaScriptEnabled(true);
        textWebview.addJavascriptInterface(new MyJavaScriptInterface(this), "Android");
        textWebview.setBackgroundColor(0);
        // disable scroll on touch
        textWebview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        textWebview.loadDataWithBaseURL("file:///android_asset/", htmlBody, "text/html", "utf-8",
                null);

    }

    /**
     * The type My web view client.
     */
    public class MyWebViewClient extends WebViewClient {

        /**
         * Instantiates a new My web view client.
         */

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            clickedTextView = false;
            String mWord = url.substring(4).toLowerCase();
            //if clicked on a word
            //if the asyncTask finishes fetching word meaning from Internet
            current_word = mWord;
            FetchBriefMeaning();
            return true;
        }
    }

    protected void FetchBriefMeaning() {
        Log.d("", "FetchBriefMeaning: " + ((MyApplication) getApplication()).getFetchingPolicy());
        switch (((MyApplication) getApplication()).getFetchingPolicy()) {
            case Constant.POLICY_ONLINE_FIRST:
                fetchingFromOnlineFirst();
                break;
            case Constant.POLICY_OFFLINE_FIRST:
                if (!ShowFromOffline()) fetchingBriefMeaningOnline();
                break;
            case Constant.POLICY_MOBILE_OFFLINE_FIRST:
                if (isMobile()) {
                    Log.d("", "FetchBriefMeaning: mobile");
                    if (!ShowFromOffline()) fetchingBriefMeaningOnline();
                } else//online
                    fetchingBriefMeaningOnline();
                break;
            case Constant.POLICY_OFFLINE_ALWAYS:
                ShowFromOffline();
                break;
        }
    }

    private boolean isMobile() {
        ConnectivityManager cm;
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;


    }

    private void fetchingBriefMeaningOnline() {
        FetchingBriefMeaningAsyncTask asyncTask = new FetchingBriefMeaningAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                showFromOnline(output);
            }
        });
        //using iciba api to search the word, type json is small to transfer
        asyncTask.execute(Constant.URL_SEARCH_BRIEF_ICIBA + current_word);
    }

    private void fetchingFromOnlineFirst() {
        FetchingBriefMeaningAsyncTask asyncTask = new FetchingBriefMeaningAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                if (!showFromOnline(output)) ShowFromOffline();
            }
        });
        //using iciba api to search the word, type json is small to transfer
        asyncTask.execute(Constant.URL_SEARCH_BRIEF_ICIBA + current_word);
    }

    private boolean showFromOnline(Object output) {
        if (output != null) {
            WordDetail wordDetail = (WordDetail) output;
            if (wordDetail.getMeaning().isEmpty()) ShowFromOffline();
            else {
                current_meaning = "";
                for (List<String> meaningPair : wordDetail.getMeaning()) {
                    meaningPair.get(0);
                    current_meaning += meaningPair.get(0) + " " + meaningPair.get(1) + "\n";
                }
                current_meaning = current_meaning.substring(0, current_meaning.length() - 2);
                try {//determine voice
                    String pron = wordDetail.getPron().get(0).get(0);
                    String voice_url = wordDetail.getPron().get(0).get(1);
                    //show popup window
                    showPopupWindow(current_word, current_meaning, pron, voice_url, Constant.METHOD_FROM_ONLINE);
                } catch (IndexOutOfBoundsException e) {//no voice
                    showPopupWindow(current_word, current_meaning, null, null, Constant.METHOD_FROM_ONLINE);
                }
                return true;
            }

        }
        return false;
    }

    /**
     * if in offline show
     *
     * @return boolean : whether in offline
     */
    protected boolean ShowFromOffline() {
        //find meaning in offline dictionary
        List<OfflineDictBean> l = daoDictionary.queryBuilder()
                .where(OfflineDictBeanDao.Properties.Word.eq(current_word))
                .list();
        if (l.size() > 0) {
            current_meaning = l.get(0).getMeaning();
            showPopupWindow(current_word, current_meaning, null, null, Constant.METHOD_FROM_OFFLINE);
            return true;
        } else return false;
    }
    protected boolean getWordOfflineStatus(String word) {
        //find meaning in offline dictionary
        List<OfflineDictBean> l = daoDictionary.queryBuilder()
                .where(OfflineDictBeanDao.Properties.Word.eq(word))
                .list();
        return l.size()>0;
    }
    /**
     * Show popup window.
     */
    public void showPopupWindow(String word, String meaning, String pron, String voice_url, int method) {
        //loading layOut file


        initializePopupWindow();
        initializePopupTextView(word, meaning, pron, voice_url, method);
        initializePopupCollectButton();

        popupWindow.showAsDropDown(findViewById(R.id.content), x, y);

    }

    private void initializeVoiceButton(final String voice_url) {
        ImageButton ib = bubbleLayout.findViewById(R.id.voice);
        ib.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(voice_url);
                    MediaPlayer player = new MediaPlayer();
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource(getApplicationContext(), uri);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializePopupWindow() {
        bubbleLayout = (BubbleLayout) LayoutInflater.from(this).inflate(R.layout.ppw_tap_search, null);
        popupWindow = BubblePopupHelper.create(this, bubbleLayout);
        bubbleLayout.setVisibility(View.INVISIBLE);

        bubbleLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        bubbleLayout.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        int width = bubbleLayout.getWidth(); // 获取宽度
                        int height = bubbleLayout.getHeight(); // 获取高度
                        int totalWidth = textWebview.getWidth();
                        int arrowWidth = Math.round(bubbleLayout.getArrowWidth());
                        int final_pos_x;
                        int final_pos_y;
                        popupWindow.dismiss();
                        if (x + width / 2 > totalWidth) {//bottom-right
                            if (clickedTextView) bubbleLayout.setArrowDirection(ArrowDirection.TOP);
                            else bubbleLayout.setArrowDirection(ArrowDirection.BOTTOM);
                            bubbleLayout.setArrowPosition(width - 30 - arrowWidth);
                            final_pos_x = x + 30 - width + arrowWidth / 2;

                        } else if (x - width / 2 < 0) {//bottom-left
                            if (clickedTextView) bubbleLayout.setArrowDirection(ArrowDirection.TOP);
                            else bubbleLayout.setArrowDirection(ArrowDirection.BOTTOM);
                            bubbleLayout.setArrowPosition(30);
                            final_pos_x = x - 20 - arrowWidth / 2;
                        } else {//bottom-center
                            if (clickedTextView)
                                bubbleLayout.setArrowDirection(ArrowDirection.TOP_CENTER);
                            else bubbleLayout.setArrowDirection(ArrowDirection.BOTTOM_CENTER);
                            final_pos_x = x - width / 2;
                        }
                        final_pos_y = clickedTextView ? y : y - height - 85;
                        if (clickedTextView)
                            popupWindow.showAtLocation(findViewById(R.id.content), 0, final_pos_x, final_pos_y);
                        else popupWindow.showAsDropDown(textWebview, final_pos_x, final_pos_y);
                        bubbleLayout.setVisibility(View.VISIBLE);
                    }


                });
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        bubbleLayout.setOnClickListener(new View.OnClickListener() {   //once click, goto WordDetailActivity
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WordDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("word", current_word);
                bundle.putString("meaning", current_meaning);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void initializePopupTextView(String word, String meaning, String pron, String voice_url, int method) {
        //initial text in popupwindow
        if (method == Constant.METHOD_FROM_OFFLINE)
            ((TextView) bubbleLayout.findViewById(R.id.word)).setText(current_word + " [离线]");
        else ((TextView) bubbleLayout.findViewById(R.id.word)).setText(current_word);
        if (voice_url != null) {
            bubbleLayout.findViewById(R.id.pronLayout).setVisibility(View.VISIBLE);
            initializeVoiceButton(voice_url);
            ((TextView) bubbleLayout.findViewById(R.id.pron)).setText(String.format("/%s/", pron));
        }
        ((AlignTextView) bubbleLayout.findViewById(R.id.meaning)).setText(current_meaning);
    }

    private void initializePopupCollectButton() {
        ppwCollectionImageView = bubbleLayout.findViewById(R.id.collect);

        if (getWordCollectedStatus(current_word)) {   //already collected
            ppwCollectionImageView.initialize(R.drawable.collect_true);
        } else {   //not collected yet
            ppwCollectionImageView.initialize(R.drawable.collect_false);

        }
        ppwCollectionImageView.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                ((CollectionImageView) v).toggleImage();
                if (getWordCollectedStatus(current_word)) {
                    //already in the db, the user means to removed this word from collection
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(current_word, current_meaning, "remove"));
                } else {
                    //not in the db, the user means to add this word to collection
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(current_word, current_meaning, "add"));
                }
            }
        });
    }

    /**
     * Gets word collected status.
     *
     * @param word the word
     * @return the word collected status
     */
    protected boolean getWordCollectedStatus(String word) {
        List<WordCollectionBean> l = daoCollection.queryBuilder()
                .where(WordCollectionBeanDao.Properties.Word.eq(word))
                .list();
        return l.size() != 0;
    }

    protected boolean isAlpha(String name) {
        return name.matches("[a-zA-Z-]+");
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


    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

}
