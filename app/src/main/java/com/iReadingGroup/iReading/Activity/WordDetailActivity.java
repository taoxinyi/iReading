package com.iReadingGroup.iReading.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;
import com.daasuu.bl.BubblePopupHelper;
import com.iReadingGroup.iReading.AsyncResponse;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBean;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.Event.ChangeWordCollectionDBEvent;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;
import com.iReadingGroup.iReading.FetchingBriefMeaningAsyncTask;
import com.iReadingGroup.iReading.FetchingWordDetailAsyncTask;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordDetail;
import com.r0adkll.slidr.Slidr;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.codeboy.android.aligntextview.AlignTextView;

/**
 * Created by taota on 2018/4/9.
 */

public class WordDetailActivity extends AppCompatActivity {
    private String detailed_word, detailed_meaning, meaning, sentence;
    private String online_meaning;
    private WordDetail wordDetail;
    private Context mContext;
    private String current_word;
    private String current_meaning;
    private String meaning_result;
    private PopupWindow popupWindow;
    private OfflineDictBeanDao daoDictionary;//database instance
    private WordCollectionBeanDao daoCollection;//database instance
    private ImageButton ppwCollectionButton;
    private String sumString = "";
    private ImageButton collectionButtonToolbar;
    private View pronView;
    private WebView textWebview;
    private BubbleLayout bubbleLayout;
    private String pron = "";
    private ScrollView scrollView;
    private boolean expandLast = false;
    private boolean current_expand=false;
    private int x, y;
    private boolean collectedInOffline = false;
    private ExpandableLayout expandableLayoutOnline, expandableLayoutOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        Slidr.attach(this);

        ((MyApplication) getApplication()).countActivity++;

        mContext = this;
        //get arguments from intent to bundle
        detailed_word = getWordFromBundle();
        detailed_meaning = getMeaningFromBundle();
        TextView word = findViewById(R.id.word_online);
        word.setText(detailed_word);
        word = findViewById(R.id.word_offline);
        word.setText(detailed_word);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        initializeDatabase();
        initializeToolBar();
        initializeImageButton();
        initializeOfflineCollectionUI();

        //make offline GONE at first
        pronView = findViewById(R.id.proView);
        scrollView = findViewById(R.id.scroll);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (expandLast &&current_expand)
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);

                        }
                    });

            }
        });
        //make unsure part gone

        //fetching word from internet
        FetchingWordDetailAsyncTask asyncTask = new FetchingWordDetailAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                wordDetail = (WordDetail) output;
                updateUI();//update UI after obtaining data
            }
        });
        asyncTask.execute("http://dict-co.iciba.com/api/dictionary.php?key=341DEFE6E5CA504E62A567082590D0BD&w=" + detailed_word.toLowerCase());

        expandableLayoutOnline = findViewById(R.id.expandable_iciba);
        TextView a = findViewById(R.id.expand_iciba);
        final ImageView b = findViewById(R.id.arrow_iciba);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_expand=false;
                if (expandableLayoutOnline.isExpanded()) {
                    expandableLayoutOnline.collapse();
                    b.setImageResource(R.drawable.arrow_down);
                } else {
                    expandableLayoutOnline.expand();
                    b.setImageResource(R.drawable.arrow_up);

                }
            }

        });
        expandableLayoutOffline = findViewById(R.id.expandable_offline);
        TextView a1 = findViewById(R.id.expand_offline);
        final ImageView b1 = findViewById(R.id.arrow_offline);
        a1.setStateListAnimator(null);
        a1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_expand=true;
                expandLast = false;
                if (expandableLayoutOffline.isExpanded()) {
                    expandableLayoutOffline.collapse();
                    b1.setImageResource(R.drawable.arrow_down);
                } else {
                    expandLast = true;
                    expandableLayoutOffline.expand();
                    b1.setImageResource(R.drawable.arrow_up);

                }

            }

        });

    }

    private void initializeOfflineCollectionUI() {
        List<OfflineDictBean> l = daoDictionary.queryBuilder().where(OfflineDictBeanDao.Properties.Word.eq(detailed_word)).list();

        if (l.size() > 0) {
            collectedInOffline = true;
            OfflineDictBean w = l.get(0);
            meaning = w.getMeaning();
            sentence = w.getSentence();
            TextView m = findViewById(R.id.word_meaning);
            m.setText(meaning);
            if (sentence != null && !sentence.equals("")) {
                TextView s = findViewById(R.id.word_sentence);
                s.setText(sentence);
            }

        }
    }

    private void updateOnlineMeaning() {
        online_meaning = "<p style=\"text-align: justify\">";
        for (List<String> meaningPair : wordDetail.getMeaning()) {
            meaningPair.get(0);
            online_meaning += meaningPair.get(0) + " " + meaningPair.get(1) + "<br>";
        }
        online_meaning += "</p>";
    }

    private void updateOnlineSentence() {
        for (List<String> sentPair : wordDetail.getSent()) {
            sumString += getClickedString(sentPair.get(0)) + "<span style=\"color: #0D47A1;\">" + sentPair.get(1) + "</span>" + "<br>";
        }
        sumString += "<p>";
        initializeWebView();
    }

    private void updateOnlinePronunciation() {

        int index = 0;
        TextView tv;
        ImageButton ib;
        for (List<String> pronPair : wordDetail.getPron()) {
            final String value = pronPair.get(1);
            if (index == 0) {
                tv = findViewById(R.id.pron_1);
                tv.setText(String.format("/BrE %s/", pronPair.get(0)));
                tv.setVisibility(View.VISIBLE);
                ib = findViewById(R.id.voice_1);
                ib.setVisibility(View.VISIBLE);
                ib.setOnClickListener(new View.OnClickListener() {
                    //collect word function.
                    @Override
                    public void onClick(View v) {
                        try {
                            Toast.makeText(mContext, "英音", Toast.LENGTH_SHORT).show();
                            Uri uri = Uri.parse(value);
                            MediaPlayer player = new MediaPlayer();
                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            player.setDataSource(mContext, uri);
                            player.prepare();
                            player.start();
                        } catch (IOException e) {
                        }
                    }
                });
            } else {
                tv = findViewById(R.id.pron_2);
                tv.setText(String.format("/AmE %s/", pronPair.get(0)));
                ib = findViewById(R.id.voice_2);
                tv.setVisibility(View.VISIBLE);
                ib.setVisibility(View.VISIBLE);
                ib.setOnClickListener(new View.OnClickListener() {
                    //collect word function.
                    @Override
                    public void onClick(View v) {
                        try {
                            Toast.makeText(mContext, "美音", Toast.LENGTH_SHORT).show();
                            Uri uri = Uri.parse(value);
                            MediaPlayer player = new MediaPlayer();
                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            player.setDataSource(mContext, uri);
                            player.prepare();
                            player.start();
                        } catch (IOException e) {
                        }
                    }
                });
            }
            index++;
        }

    }

    private void updateUI() {

        updateOnlineMeaning();
        updateOnlineSentence();
        updateOnlinePronunciation();

    }

    private void initializeImageButton() {
        collectionButtonToolbar = findViewById(R.id.collect_word_button);

        if (getWordCollectionStatus(detailed_word))
            collectionButtonToolbar.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_true));
        else collectionButtonToolbar.setImageDrawable(
                ContextCompat.getDrawable(mContext, R.drawable.collect_false));
        collectionButtonToolbar.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                String operation;
                ImageButton button = (ImageButton) v;
                if (getWordCollectionStatus(detailed_word)) {
                    operation = "remove";
                    //already in the db, the user means to removed this word from collection
                    button.setImageDrawable(
                            ContextCompat.getDrawable(mContext, R.drawable.collect_false));
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(detailed_word, detailed_meaning, "remove"));

                } else {
                    //not in the db, the user means to add this word to collection
                    operation = "add";
                    button.setImageDrawable(
                            ContextCompat.getDrawable(mContext, R.drawable.collect_true));
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(detailed_word, detailed_meaning, "add"));

                }

            }
        });
    }

    private String getWordFromBundle() {
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("word");
    }

    private String getMeaningFromBundle() {
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("meaning");
    }

    private void initializeDatabase() {


        daoDictionary = ((MyApplication) getApplication()).getDaoDictionary();//this is the offline dictionary database
        daoCollection = ((MyApplication) getApplication()).getDaoCollection();// this is the database recording user's word collection


    }

    private void initializeToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setTitle("");//set corresponding title in toolbar
        ((TextView) findViewById(R.id.title)).setText(detailed_word);
        setSupportActionBar(toolBar);
        findViewById(R.id.backLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean getWordCollectionStatus(String word) {
        if (daoCollection.queryBuilder().where(WordCollectionBeanDao.Properties.Word.eq(word)).list().size() == 0)
            return false;
        else
            return true;

    }

    private void managePopup(int px, int py) {
        x = px;
        y = -textWebview.getHeight() + py;

    }

    private class MyJavaScriptInterface {
        private Context ctx;

        public MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

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

    private void initializeWebView() {
        textWebview = findViewById(R.id.wv);
        textWebview.setWebViewClient(new MyWebViewClient(this));
        textWebview.requestFocus();
        textWebview.getSettings().setJavaScriptEnabled(true);
        textWebview.addJavascriptInterface(new MyJavaScriptInterface(this), "Android");
        textWebview.setBackgroundColor(0);
        textWebview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }


        });
        String htmlBody = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + online_meaning + sumString +
                "<script>\n" +
                "       var myHref = document.getElementsByTagName(\"a\");\n" +
                "        for (var i = 0,mylength = myHref.length; i<mylength; i++) {\n" +
                "    (function(i){  //这里的i跟外部的i实际不是一个i\n" +
                "        myHref[i].addEventListener(\"click\",function(e){\n" +
                "\t\t\tvar rect = myHref[i].getBoundingClientRect();\n" +
                "\t\t\tAndroid.showMenu((rect.left+rect.right)/2, rect.top + rect.height);\n" +
                "        },\"false\");\n" +
                "    })(i);\n" +
                "}\n" +
                "</script>";
        textWebview.loadDataWithBaseURL("file:///android_asset/", htmlBody, "text/html", "utf-8",
                null);
    }

    private String getClickedString(String originalString) {
        StringBuilder sum = new StringBuilder("<p style=\"text-align: justify\">");
        Pattern ptrn = Pattern.compile("((?:\\w+-)+\\w+)|[^\\s\\W]+|\\S+|(\\s)+");
        Matcher m = ptrn.matcher(originalString);
        List<String> list = new ArrayList<>();
        while (m.find()) {
            list.add(m.group(0));
        }
        for (String a : list) {
            a = a.replace("\n", "<br>");
            if (isAlpha(a)) {
                sum.append("<a href=\"a://" + a + "\">" + a + "</a>");
            } else sum.append(a);
        }
        sum.append("<br>");
        return sum.toString();
    }

    private static class MyWebViewClient extends WebViewClient {

        private final WeakReference<WordDetailActivity> mActivity;

        public MyWebViewClient(WordDetailActivity activity) {
            mActivity = new WeakReference<WordDetailActivity>(activity);
        }

        @Override
        public void onPageCommitVisible(WebView view,
                                        String url) {
            final WordDetailActivity activity = mActivity.get();
            if (activity.collectedInOffline)//if in offline
                activity.expandableLayoutOffline.setVisibility(View.VISIBLE);
            else { // if not in offline ,make them all gone
                for (int i = 0; i < activity.expandableLayoutOffline.getChildCount(); i++) {
                    View child = activity.expandableLayoutOffline.getChildAt(i);
                    child.setVisibility(View.GONE);
                }
            }
            //offline title visible
            activity.findViewById(R.id.title_offline).setVisibility(View.VISIBLE);
            //pro view visible
            activity.pronView.setVisibility(View.VISIBLE);
            //webview visible
            view.setVisibility(View.VISIBLE);
            if (!activity.wordDetail.getMeaning().isEmpty()) {//if online fetched something
                activity.expandableLayoutOnline.setVisibility(View.VISIBLE);
            } else { // if online fetched nothing,make them all gone
                for (int i = 0; i < activity.expandableLayoutOnline.getChildCount(); i++) {
                    View child = activity.expandableLayoutOnline.getChildAt(i);
                    child.setVisibility(View.GONE);
                }

            }


        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            String mWord = url.substring(4).toLowerCase();
            final WordDetailActivity activity = mActivity.get();
            //if clicked on a word
            //if the asyncTask finishes fetching word meaning from Internet
            activity.current_word = mWord;
            FetchingBriefMeaningAsyncTask asyncTask = new FetchingBriefMeaningAsyncTask(new AsyncResponse() {
                @Override
                public void processFinish(Object output) {
                    WordDetail wordDetail = (WordDetail) output;
                    activity.current_meaning = "";
                    for (List<String> meaningPair : wordDetail.getMeaning()) {
                        meaningPair.get(0);
                        activity.current_meaning += meaningPair.get(0) + " " + meaningPair.get(1) + "\n";
                    }
                    activity.current_meaning = activity.current_meaning.substring(0, activity.current_meaning.length() - 1);

                    activity.pron = wordDetail.getPron().get(0).get(0);

                    //show popup window
                    activity.showPopupWindow();
                }
            });
            //using iciba api to search the word, type json is small to transfer
            List<OfflineDictBean> joes = activity.daoDictionary.queryBuilder()
                    .where(OfflineDictBeanDao.Properties.Word.eq(mWord))
                    .list();
            if (joes.size() == 0) {   //find nothing in offline dictionary,using online query
                asyncTask.execute("https://dict-co.iciba.com/api/dictionary.php?key=341DEFE6E5CA504E62A567082590D0BD&type=json&w=" + mWord);

            } else {
                //find meaning in offline dictionary
                activity.current_meaning = joes.get(0).getMeaning();
                activity.meaning_result = mWord + "[离线]\n" + activity.current_meaning;
                //show popup window
                activity.showPopupWindow();

            }

            return true;
        }
    }

    private boolean isAlpha(String name) {
        return name.matches("[a-zA-Z-]+");
    }

    private void showPopupWindow() {
        //loading layOut file
        initializePopupWindow();
        initializeWordTextView();
        initializeCollectButton();
        //show the popup window at the bottom
        popupWindow.showAtLocation(findViewById(R.id.content), Gravity.BOTTOM,
                0, 0);


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
                        popupWindow.dismiss();
                        if (x + width / 2 > totalWidth) {//bottom-right
                            bubbleLayout.setArrowDirection(ArrowDirection.BOTTOM);
                            bubbleLayout.setArrowPosition(width - 30 - arrowWidth);
                            popupWindow.showAsDropDown(textWebview, x + 30 - width + arrowWidth / 2, y - height - 85);

                        } else if (x - width / 2 < 0) {
                            bubbleLayout.setArrowDirection(ArrowDirection.BOTTOM);
                            bubbleLayout.setArrowPosition(30);
                            popupWindow.showAsDropDown(textWebview, x - 20 - arrowWidth / 2, y - height - 85);


                        } else
                            popupWindow.showAsDropDown(textWebview, x - width / 2, y - height - 85);
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

    private void initializeWordTextView() {
        //initial text in popupwindow
        ((TextView) bubbleLayout.findViewById(R.id.word)).setText(current_word);
        if (!pron.equals("")) {
            ((TextView) bubbleLayout.findViewById(R.id.pron)).setText(String.format("/%s/", pron));
            bubbleLayout.findViewById(R.id.pronLayout).setVisibility(View.VISIBLE);
        }
        pron = "";
        ((AlignTextView) bubbleLayout.findViewById(R.id.meaning)).setText(current_meaning);

    }


    private void initializeCollectButton() {
        ppwCollectionButton = (ImageButton) bubbleLayout.findViewById(com.iReadingGroup.iReading.R.id.collect);
        final boolean flag_collected = getWordCollectedStatus(current_word);
        if (flag_collected) {   //already collected
            ppwCollectionButton.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
        } else {   //not collected yet
            ppwCollectionButton.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
        }
        ppwCollectionButton.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                ImageButton button = (ImageButton) v;
                if (getWordCollectedStatus(current_word)) {
                    //already in the db, the user means to removed this word from collection
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(current_word, current_meaning, "remove"));

                } else {
                    //not in the db, the user means to add this word to collection
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(current_word, current_meaning, "add"));


                }
            }
        });
    }

    private boolean getWordCollectedStatus(String word) {
        List<WordCollectionBean> l = daoCollection.queryBuilder()
                .where(WordCollectionBeanDao.Properties.Word.eq(word))
                .list();
        if (l.size() == 0) {
            return false;//not collected yet
        } else return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWordDatasetChangedEvent(WordDatasetChangedEvent event) {
        boolean flag_collected = getWordCollectedStatus(current_word);
        if (flag_collected) {   //already collected
            ppwCollectionButton.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_true));
        } else {   //not collected yet
            ppwCollectionButton.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_false));
        }
        flag_collected = getWordCollectedStatus(detailed_word);
        if (flag_collected) {   //already collected
            collectionButtonToolbar.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_true));
        } else {   //not collected yet
            collectionButtonToolbar.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_false));
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (((MyApplication) getApplication()).countActivity-- == 1)
            System.exit(0);
    }


}
