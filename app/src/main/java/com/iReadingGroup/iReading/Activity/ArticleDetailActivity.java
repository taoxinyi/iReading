package com.iReadingGroup.iReading.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.daasuu.bl.ArrowDirection;
import com.daasuu.bl.BubbleLayout;
import com.daasuu.bl.BubblePopupHelper;
import com.ganxin.library.LoadDataLayout;
import com.github.chrisbanes.photoview.PhotoView;
import com.iReadingGroup.iReading.AsyncResponse;
import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBean;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.Event.ChangeWordCollectionDBEvent;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;
import com.iReadingGroup.iReading.Event.changeArticleCollectionDBEvent;
import com.iReadingGroup.iReading.FetchArticleAsyncTask;
import com.iReadingGroup.iReading.FetchingBriefMeaningAsyncTask;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordDetail;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.codeboy.android.aligntextview.AlignTextView;


/**
 * ArticleDetailActivity
 * Created once an article is clicked
 * <p>
 * Get the parameters in Bundle from ArticleListFragment or ArticleCollectionNestFragment
 * These parameters includes article uri,title,source,time
 * First, using AsyncTask to fetch article's plain text and image using uri from API
 * Then, make Text clickable along with other ui settings
 * If a word is clicked, a popup window will show
 * It contains word brief meaning either from offline dictionary or Internet(using AsyncTask)
 * <p>
 * The collection of this article and word(in popup window) is handled here
 * This should query in the database and then make change,add or delete
 */
public class ArticleDetailActivity extends DetailBaseActivity {
    private Context mContext;
    private String articleTitleFromBundle;
    private String article;
    private String imageUrl;
    private String uri;
    private String source;
    private String time;
    private String category;
    private String current_word;
    private String current_meaning;
    private String meaning_result = "Loading";
    private PopupWindow popupWindow;
    private OfflineDictBeanDao daoDictionary;//database instance
    private WordCollectionBeanDao daoCollection;//database instance
    private ArticleEntityDao daoArticle;//database instance
    public LoadDataLayout loadDataLayout;
    private ArticleEntity articleEntity;
    private ImageButton ppwCollectionButton;
    private WebView textWebview;
    private BubbleLayout bubbleLayout;
    private int x, y;
    private String pron="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.iReadingGroup.iReading.R.layout.activity_article_detail);
        mContext = this;
        initializeLoadingLayout();

        if (isNetworkAvailable()) {
            //get arguments from intent to bundle
            articleTitleFromBundle = getArticleTitleFromBundle();
            uri = getUriFromBundle();
            source = getSourceFromBundle();
            time = getTimeFromBundle();

            //get the whole article plain text from uri
            article = getArticle(uri);
            //initialize database
            initializeDatabase();
        }


    }

    /**
     * initialize UI
     * This function is processed once after the FetchingArticleAsyncTask is finished
     */
    private void initUI() {

        initializeToolBar();
        initializeStatusBar(R.color.colorPrimary);
        initializeTextView();
        initializeImageView();
        Slidr.attach(this);//Silde Back function


    }

    private void initializeToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(com.iReadingGroup.iReading.R.id.toolbar);
        toolBar.setTitle("");//set corresponding title in toolbar
        setSupportActionBar(toolBar);
        findViewById(R.id.backLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ImageButton button = findViewById(R.id.collect_article_button);
        articleEntity = daoArticle.queryBuilder().where(ArticleEntityDao.Properties.Uri.eq(uri)).list().get(0);
        if (articleEntity.getCollectStatus()) button.setImageDrawable(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
        else button.setImageDrawable(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));

        button.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                ImageButton button = (ImageButton) v;
                if (articleEntity.getCollectStatus()) {
                    //already collected,need to remove collection
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
                    EventBus.getDefault().post(new changeArticleCollectionDBEvent(uri, "remove"));
                } else {
                    //add collection
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
                    EventBus.getDefault().post(new changeArticleCollectionDBEvent(uri, "add"));


                }

            }

        });
    }



    private void initializeDatabase() {


        daoDictionary = ((MyApplication) getApplication()).getDaoDictionary();//this is the offline dictionary database
        daoCollection = ((MyApplication) getApplication()).getDaoCollection();// this is the database recording user's word collection
        daoArticle = ((MyApplication) getApplication()).getDaoArticle();

    }

    private void initializeLoadingLayout() {
        //set Custom layout when fetching data
        loadDataLayout = (LoadDataLayout) findViewById(R.id.loadDataLayout);
        loadDataLayout.setOnReloadListener(new LoadDataLayout.OnReloadListener() {
            @Override
            public void onReload(View v, int status) {
                loadDataLayout.setStatus(LoadDataLayout.LOADING);
                if (isNetworkAvailable()) {
                    //get arguments from intent to bundle
                    articleTitleFromBundle = getArticleTitleFromBundle();
                    uri = getUriFromBundle();
                    source = getSourceFromBundle();
                    time = getTimeFromBundle();

                    //get the whole article plain text from uri
                    article = getArticle(uri);
                    //initialize database
                    initializeDatabase();
                } else loadDataLayout.setStatus(LoadDataLayout.NO_NETWORK);
            }
        });
        loadDataLayout.setStatus(LoadDataLayout.LOADING);
        if (!isNetworkAvailable()) loadDataLayout.setStatus(LoadDataLayout.NO_NETWORK);


    }

    /**
     * initialize TextViews
     * Including the title, category and body
     * Set every textview clickable
     */
    private void initializeTextView() {
        //articleTextView = (TextView) findViewById(com.iReadingGroup.iReading.R.id.text);
        //makeTextViewSpannable(articleTextView, article);//make every word clickable
        initializeWebView();
        TextView titleTextView = (TextView) findViewById(R.id.title);
        makeTextViewSpannable(titleTextView, articleTitleFromBundle);

        TextView categoryTextView = findViewById(R.id.category);
        makeTextViewSpannable(categoryTextView, "Category : " + category + "\nSource: " + source + "\nTime: " + time);

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
        textWebview.setWebViewClient(new MyWebViewClient());
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

        StringBuilder sum = new StringBuilder("<p style=\"text-align: justify;line-height: 130%\">");
        Pattern ptrn = Pattern.compile("((?:\\w+-)+\\w+)|[^\\s\\W]+|\\S+|(\\s)+");
        Matcher m = ptrn.matcher(article);
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
        sum.append("</p>");
        String htmlBody = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + sum + "<script>\n" +
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

    private void initializeImageView() {
        PhotoView imageView = findViewById(R.id.img_article);
        Glide.with(this).load(imageUrl).into(imageView);
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
        pron="";
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

    private String getArticleTitleFromBundle() {
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("name");
    }

    private String getUriFromBundle() {
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("uri");
    }

    private String getTimeFromBundle() {
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("time");
    }

    private String getSourceFromBundle() {
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("source");
    }

    /**
     * Get article from api and initialize UI immediately once finished
     * Using the FetchAsyncTask
     *
     * @param uri String the article unique uri
     * @return String  the article plain text
     */
    private String getArticle(String uri) {
        FetchArticleAsyncTask asyncTask = new FetchArticleAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                String b = (String) output;
                String[] buff = b.split("\r\n\r\n");
                imageUrl = buff[0];
                category = buff[1];
                article = buff[2];
                initUI();
            }
        });
        //using iciba api to search the word, type json is small to transfer
        asyncTask.execute("http://eventregistry.org/json/article?action=getArticle&resultType=info&infoIncludeArticleBasicInfo=false&infoIncludeArticleEventUri=false&infoIncludeArticleCategories=true&infoArticleBodyLen=-1&infoIncludeArticleImage=true&infoIncludeConceptLabel=false&apiKey=475f7fdb-7929-4222-800e-0151bdcd4af2&articleUri=" + uri);
        return article;
    }

    /**
     * Make the TextView clickable
     * Using BreakIterator
     *
     * @param tv   TextView
     * @param text String
     */
    private void makeTextViewSpannable(TextView tv, String text) {
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(text, TextView.BufferType.SPANNABLE);
        tv.setHighlightColor(Color.GRAY);//set the color of highlighting

        Spannable spans = (Spannable) tv.getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = text.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                ClickableSpan clickSpan = getClickableSpan(possibleWord, start, end, spans);
                spans.setSpan(clickSpan, start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private ClickableSpan getClickableSpan(final String word, final int start, final int end, final Spannable spans) {
        return new ClickableSpan() {
            final String mWord;

            {
                mWord = word.toLowerCase();
            }

            @Override
            public void onClick(final View widget) {
                //if clicked on a word
                //if the asyncTask finishes fetching word meaning from Internet
                current_word = mWord;
                FetchingBriefMeaningAsyncTask asyncTask = new FetchingBriefMeaningAsyncTask(new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        meaning_result = (String) output;
                        current_meaning = meaning_result.split("\n")[1];
                        //show popup window
                        showPopupWindow();
                    }
                });
                //using iciba api to search the word, type json is small to transfer
                List<OfflineDictBean> joes = daoDictionary.queryBuilder()
                        .where(OfflineDictBeanDao.Properties.Word.eq(mWord))
                        .list();
                if (joes.size() == 0) {   //find nothing in offline dictionary,using online query
                    asyncTask.execute("https://dict-co.iciba.com/api/dictionary.php?key=341DEFE6E5CA504E62A567082590D0BD&type=json&w=" + mWord);

                } else {
                    //find meaning in offline dictionary
                    current_meaning = joes.get(0).getMeaning();
                    meaning_result = mWord + "[离线]\n" + current_meaning;
                    //show popup window
                    showPopupWindow();

                }
                //set the selected word color transparent which means highlighted
                spans.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
                        start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                //Toast.makeText(widget.getContext(), mWord, Toast.LENGTH_SHORT)
                //.show();

            }

            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);//no underline for word
            }
        };
    }

    private void showPopupWindow() {
        //loading layOut file


        initializePopupWindow();
        initializeWordTextView();
        initializeCollectButton();
        popupWindow.showAsDropDown(textWebview, x, y);


    }

    private class MyWebViewClient extends WebViewClient {


        public MyWebViewClient() {
        }

        @Override
        public void onPageCommitVisible(WebView view,
                                        String url) {
            loadDataLayout.setStatus(LoadDataLayout.SUCCESS);


        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            String mWord = url.substring(4).toLowerCase();
            //if clicked on a word
            //if the asyncTask finishes fetching word meaning from Internet
            current_word = mWord;
            FetchingBriefMeaningAsyncTask asyncTask = new FetchingBriefMeaningAsyncTask(new AsyncResponse() {
                @Override
                public void processFinish(Object output) {
                    WordDetail wordDetail = (WordDetail) output;
                    current_meaning = "";
                    for (List<String> meaningPair : wordDetail.getMeaning()) {
                        meaningPair.get(0);
                        current_meaning += meaningPair.get(0) + " " + meaningPair.get(1) + "\n";

                    }
                    current_meaning = current_meaning.substring(0, current_meaning.length() - 1);
                    pron = wordDetail.getPron().get(0).get(0);
                    //show popup window
                    showPopupWindow();
                }
            });
            //using iciba api to search the word, type json is small to transfer
            List<OfflineDictBean> joes = daoDictionary.queryBuilder()
                    .where(OfflineDictBeanDao.Properties.Word.eq(mWord))
                    .list();
            if (true) {   //find nothing in offline dictionary,using online query
                asyncTask.execute("https://dict-co.iciba.com/api/dictionary.php?key=341DEFE6E5CA504E62A567082590D0BD&type=json&w=" + mWord);

            } else {
                //find meaning in offline dictionary
                current_meaning = joes.get(0).getMeaning();
                meaning_result = mWord + "[离线]\n" + current_meaning;
                //show popup window
                showPopupWindow();

            }

            return true;
        }
    }

    private boolean isAlpha(String name) {
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

    private boolean getWordCollectedStatus(String word) {
        List<WordCollectionBean> l = daoCollection.queryBuilder()
                .where(WordCollectionBeanDao.Properties.Word.eq(word))
                .list();
        if (l.size() == 0) {
            return false;//not collected yet
        } else return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWordDatasetChangedEvent(WordDatasetChangedEvent event) {
        final boolean flag_collected = getWordCollectedStatus(current_word);
        if (flag_collected) {   //already collected
            ppwCollectionButton.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
        } else {   //not collected yet
            ppwCollectionButton.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass
        System.exit(0);
    }

}
