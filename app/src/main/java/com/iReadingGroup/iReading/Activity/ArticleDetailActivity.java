package com.iReadingGroup.iReading.Activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ganxin.library.LoadDataLayout;
import com.github.chrisbanes.photoview.PhotoView;
import com.iReadingGroup.iReading.Event.ArticleCollectionStatusChangedEvent;
import com.iReadingGroup.iReading.AsyncResponse;
import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Bean.DaoMaster;
import com.iReadingGroup.iReading.Bean.DaoSession;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBean;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.Event.CollectWordEvent;
import com.iReadingGroup.iReading.FetchArticleAsyncTask;
import com.iReadingGroup.iReading.FetchingBriefMeaningAsyncTask;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.database.Database;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * ArticleDetailActivity
 * Created once an article is clicked
 *
 * Get the parameters in Bundle from ArticleListFragment or ArticleCollectionNestFragment
 * These parameters includes article uri,title,source,time
 * First, using AsyncTask to fetch article's plain text and image using uri from API
 * Then, make Text clickable along with other ui settings
 * If a word is clicked, a popup window will show
 * It contains word brief meaning either from offline dictionary or Internet(using AsyncTask)
 *
 * The collection of this article and word(in popup window) is handled here
 * This should query in the database and then make change,add or delete
 */
public class ArticleDetailActivity extends AppCompatActivity {
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
    private TextView articleTextView;
    private PopupWindow popupWindow;
    private View ppwContentView;
    private OfflineDictBeanDao daoDictionary;//database instance
    private WordCollectionBeanDao daoCollection;//database instance
    private ArticleEntityDao daoArticle;//database instance
    private ArrayList<String> list_selected_words = new ArrayList<String>();
    private LoadDataLayout loadDataLayout;
    private ArticleEntity articleEntity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.iReadingGroup.iReading.R.layout.activity_article_detail);
        mContext = this;

        //get arguments from intent to bundle
        articleTitleFromBundle = getArticleTitleFromBundle();
        uri = getUriFromBundle();
        source=getSourceFromBundle();
        time=getTimeFromBundle();

        //get the whole article plain text from uri
        article = getArticle(uri);
        //initialize database
        initializeDatabase();
        initializeLoadingLayout();

    }
    /**
     * initialize UI
     * This function is processed once after the FetchingArticleAsyncTask is finished
     */
    private void initUI() {
        loadDataLayout.setStatus(LoadDataLayout.SUCCESS);//load succeed

        Slidr.attach(this);//Silde Back function

        initializeToolBar();
        initializeStatusBar();
        initializeTextView();
        initializeImageView();

    }

    private void initializeToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(com.iReadingGroup.iReading.R.id.toolbar);
        toolBar.setTitle(articleTitleFromBundle);//set corresponding title in toolbar
        setSupportActionBar(toolBar);
        ImageButton button=findViewById(R.id.collect_article_button);
        articleEntity=daoArticle.queryBuilder().where(ArticleEntityDao.Properties.Uri.eq(uri)).list().get(0);
        if (articleEntity.getCollectStatus())  button.setImageDrawable(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
        else  button.setImageDrawable(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));

        button.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                ImageButton button = (ImageButton) v;
                if (articleEntity.getCollectStatus()) {
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
                    articleEntity.setCollectStatus(false);
                }

                else {
                    //add collection
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
                    articleEntity.setCollectStatus(true);
                    Date currentTime = Calendar.getInstance().getTime();

                }
                EventBus.getDefault().postSticky(new ArticleCollectionStatusChangedEvent(uri));
            }

        });
    }

    private void initializeStatusBar() {
        //set StatusBar Color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void initializeDatabase() {

        MyApplication app = (MyApplication) getApplicationContext();//initialize UI
        daoDictionary=app.getDaoDicitionary();
        daoCollection=app.getDaoCollection();
        daoArticle= app.getDaoArticle();

    }

    private void initializeLoadingLayout() {
        //set Custom layout when fetching data
        loadDataLayout = (LoadDataLayout) findViewById(R.id.loadDataLayout);
        loadDataLayout.setOnReloadListener(new LoadDataLayout.OnReloadListener() {
            @Override
            public void onReload(View v, int status) {
            }
        });
        loadDataLayout.setStatus(LoadDataLayout.LOADING);

    }

    /**
     * initialize TextViews
     * Including the title, category and body
     * Set every textview clickable
     */
    private void initializeTextView() {
        articleTextView = (TextView) findViewById(com.iReadingGroup.iReading.R.id.text);
        makeTextViewSpannable(articleTextView, article);//make every word clickable
        TextView titleTextView = (TextView) findViewById(R.id.title);
        makeTextViewSpannable(titleTextView, articleTitleFromBundle);

        TextView categoryTextView = findViewById(R.id.category);
        makeTextViewSpannable(categoryTextView, "Category : " + category+"\nSource: "+source+"\nTime: "+time);

    }

    private void initializeImageView() {
        PhotoView imageView = findViewById(R.id.img_article);
        Glide.with(this).load(imageUrl).into(imageView);
    }

    private void initializePopupWindow() {
        ppwContentView = LayoutInflater.from(mContext).inflate(
                com.iReadingGroup.iReading.R.layout.ppw_tap_search, null);

        //initialize  popupwindow
        popupWindow = new PopupWindow(ppwContentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
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
    }

    private void initializeWordTextView() {
        //initial text in popupwindow
        ((TextView) popupWindow.getContentView().findViewById(com.iReadingGroup.iReading.R.id.meaning)).setText(meaning_result);
    }

    private void initializeCollectButton() {
        ImageButton button = (ImageButton) ppwContentView.findViewById(com.iReadingGroup.iReading.R.id.collect);
        final boolean flag_collected = getWordCollectedStatus(current_word);
        if (flag_collected) {   //already collected
            button.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
        } else {   //not collected yet
            button.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
        }
        button.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                ImageButton button = (ImageButton) v;
                if (getWordCollectedStatus(current_word)) {
                    removeWordFromCollection(current_word);
                    removeWordFromCurrentSelectedList(current_word);
                    //already in the db, the user means to removed this word from collection
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
                } else {
                    //not in the db, the user means to add this word to collection
                    addWordIntoCollection(current_word);
                    addWordIntoCurrentSelectedList(current_word);
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));

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
                        current_meaning=meaning_result.split("\n")[1];
                        //show popup window
                        showPopupWindow(widget);
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
                    current_meaning=joes.get(0).getMeaning();
                    meaning_result = mWord + "[离线]\n" + current_meaning;
                    //show popup window
                    showPopupWindow(widget);

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

    private void showPopupWindow(View view) {
        //loading layOut file
        initializePopupWindow();
        initializeWordTextView();
        initializeCollectButton();
        //show the popup window at the bottom
        popupWindow.showAtLocation(view, Gravity.BOTTOM,
                0, 0);


    }

    /**
     * Before Stop
     * Save to the database and post the event when new word(s) collected.
     */
    @Override
    public void onStop() {

        if (list_selected_words.size() > 0) {
            EventBus.getDefault().postSticky(new CollectWordEvent(0));

        }
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

    private void addWordIntoCollection(String word) {
        WordCollectionBean newWord = new WordCollectionBean();
        newWord.setWord(word);
        newWord.setMeaning(current_meaning);
        daoCollection.insertOrReplace(newWord);
    }

    private void removeWordFromCollection(String word) {
        List<WordCollectionBean> l = daoCollection.queryBuilder()
                .where(WordCollectionBeanDao.Properties.Word.eq(word))
                .list();
        for (WordCollectionBean existedWord : l) {
            daoCollection.delete(existedWord);
        }
    }

    private void removeWordFromCurrentSelectedList(String word) {
        if (list_selected_words.contains(word)) {
            list_selected_words.remove(word);
        }
    }

    private void addWordIntoCurrentSelectedList(String word) {
        list_selected_words.add(word);
    }
}
