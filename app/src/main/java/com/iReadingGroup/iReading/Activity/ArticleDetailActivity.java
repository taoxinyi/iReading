package com.iReadingGroup.iReading.Activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ganxin.library.LoadDataLayout;
import com.github.chrisbanes.photoview.PhotoView;
import com.iReadingGroup.iReading.AsyncTask.AsyncResponse;
import com.iReadingGroup.iReading.AsyncTask.FetchArticleAsyncTask;
import com.iReadingGroup.iReading.AsyncTask.FetchingBriefMeaningAsyncTask;
import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.CollectionImageView;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;
import com.iReadingGroup.iReading.Event.changeArticleCollectionDBEvent;
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


/**
 * ArticleDetailActivity
 * Created once an article is clicked
 * <p>
 * Get the parameters in Bundle from ArticleListFragment or ArticleCollectionNestFragment
 * These parameters includes article uri,title,source,time
 * First, using AsyncTask to fetch article's plain text and image using uri from API
 * Then, make Text clickable using WebView along with other ui settings
 * If a word is clicked, a popup window will show
 * It contains word brief meaning either from offline dictionary or Internet(using AsyncTask)
 * <p>
 * The collection of this article and word(in popup window) is handled here
 */
public class ArticleDetailActivity extends DetailBaseActivity {

    /**
     * The Load data layout.
     */
    public LoadDataLayout loadDataLayout;
    private String articleTitleFromBundle;
    private String article;
    private String imageUrl;
    private String uri;
    private String source;
    private String time;
    private String category;
    private ArticleEntity articleEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.iReadingGroup.iReading.R.layout.activity_article_detail);
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

        initializeToolBar(R.id.toolbar, R.id.backLayout, R.id.toolbar_title, "正文");
        initializeCollectionButton();
        initializeStatusBar(R.color.colorPrimary);
        initializeTextView();
        initializeWebView(R.id.wv, getWebViewString());
        textWebview.setWebViewClient(new MyWebViewClient() {
            @Override
            public void onPageCommitVisible(WebView view,
                                            String url) {
                loadDataLayout.setStatus(LoadDataLayout.SUCCESS);
            }
        });
        initializeImageView();
        Slidr.attach(this);//Silde Back function


    }

    private void initializeCollectionButton() {
        CollectionImageView button = findViewById(R.id.collect_article_button);
        //get database status
        articleEntity = daoArticle.queryBuilder().where(ArticleEntityDao.Properties.Uri.eq(uri)).list().get(0);
        if (articleEntity.getCollectStatus())
            //already collected
            button.initialize(R.drawable.collect_true);
        else //not collected
            button.initialize(R.drawable.collect_false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first toggle image
                ((CollectionImageView) v).toggleImage();
                //then post to event to app in order to change db
                if (articleEntity.getCollectStatus())
                    EventBus.getDefault().post(new changeArticleCollectionDBEvent(uri, "remove"));
                else
                    EventBus.getDefault().post(new changeArticleCollectionDBEvent(uri, "add"));
            }
        });

    }


    private void initializeLoadingLayout() {
        //set Custom layout when fetching data
        loadDataLayout = findViewById(R.id.loadDataLayout);
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
     * Including the title, category and source and time
     * Set every title and category clickable
     */
    private void initializeTextView() {


        TextView titleTextView = findViewById(R.id.title);
        makeTextViewSpannable(titleTextView, articleTitleFromBundle);

        TextView categoryTextView = findViewById(R.id.category);
        makeTextViewSpannable(categoryTextView, category + " ");
        ((TextView) findViewById(R.id.source)).setText(source);
        ((TextView) findViewById(R.id.time)).setText(time);
    }

    /**
     * get the HTML String for webview
     * @return
     */
    private String getWebViewString() {
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
        //sum.insert(0,String.format("<image src=%s\"></image>",imageUrl));
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
        return htmlBody;
    }


    private void initializeImageView() {
        PhotoView imageView = findViewById(R.id.img_article);
        Glide.with(this).load(imageUrl).into(imageView);
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
        tv.setHighlightColor(Color.TRANSPARENT);//set the color of highlighting

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
                TextView parentTextView = (TextView) widget;
                Rect parentTextViewRect = new Rect();

                // Initialize values for the computing of clickedText position
                SpannableString completeText = (SpannableString) (parentTextView).getText();
                Layout textViewLayout = parentTextView.getLayout();

                double startOffsetOfClickedText = completeText.getSpanStart(this);
                double endOffsetOfClickedText = completeText.getSpanEnd(this);

                double startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int) startOffsetOfClickedText);
                double endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int) endOffsetOfClickedText);

                // Get the rectangle of the clicked text
                int currentLineStartOffset = textViewLayout.getLineForOffset((int) startOffsetOfClickedText);
                int currentLineEndOffset = textViewLayout.getLineForOffset((int) endOffsetOfClickedText);

                boolean keywordIsInMultiLine = currentLineStartOffset != currentLineEndOffset;
                textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);

                // Update the rectangle position to his real position on screen
                int[] parentTextViewLocation = {0, 0};
                parentTextView.getLocationOnScreen(parentTextViewLocation);

                double parentTextViewTopAndBottomOffset = (
                        parentTextViewLocation[1] -
                                parentTextView.getScrollY() +
                                parentTextView.getCompoundPaddingTop()
                );

                parentTextViewRect.top += parentTextViewTopAndBottomOffset;
                parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;

                parentTextViewRect.left += (
                        parentTextViewLocation[0] +
                                startXCoordinatesOfClickedText +
                                parentTextView.getCompoundPaddingLeft() -
                                parentTextView.getScrollX()
                );
                parentTextViewRect.right = (int) (
                        parentTextViewRect.left +
                                endXCoordinatesOfClickedText -
                                startXCoordinatesOfClickedText
                );

                x = (parentTextViewRect.left + parentTextViewRect.right) / 2;
                y = parentTextViewRect.bottom;
                if (keywordIsInMultiLine) {
                    x = parentTextViewRect.left;
                }

                clickedTextView = true;
                //if clicked on a word
                //if the asyncTask finishes fetching word meaning from Internet
                current_word = mWord;
                FetchingBriefMeaningAsyncTask asyncTask = new FetchingBriefMeaningAsyncTask(new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        WordDetail wordDetail = (WordDetail) output;
                        if (wordDetail.getMeaning().isEmpty()) searchOffline();
                        else {
                            current_meaning = "";
                            for (List<String> meaningPair : wordDetail.getMeaning()) {
                                meaningPair.get(0);
                                current_meaning += meaningPair.get(0) + " " + meaningPair.get(1) + "\n";

                            }
                            current_meaning = current_meaning.substring(0, current_meaning.length() - 1);
                            voice_url = wordDetail.getPron().get(0).get(0);
                            //show popup window
                            showPopupWindow();
                        }
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
                    //show popup window
                    showPopupWindow();

                }
                //set the selected word color transparent which means highlighted
                spans.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
                        start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


            }

            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);//no underline for word
            }
        };
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


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * On word dataset changed event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWordDatasetChangedEvent(WordDatasetChangedEvent event) {
        final boolean flag_collected = getWordCollectedStatus(current_word);
        if (flag_collected) {   //already collected
            ppwCollectionImageView.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
        } else {   //not collected yet
            ppwCollectionImageView.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass
        System.exit(0);
    }

}
