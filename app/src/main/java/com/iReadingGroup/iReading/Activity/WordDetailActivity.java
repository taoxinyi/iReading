package com.iReadingGroup.iReading.Activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.iReadingGroup.iReading.AsyncTask.AsyncResponse;
import com.iReadingGroup.iReading.AsyncTask.FetchingWordDetailAsyncTask;
import com.iReadingGroup.iReading.Constant;
import com.iReadingGroup.iReading.Event.ChangeWordCollectionDBEvent;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;
import com.iReadingGroup.iReading.Function;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.ToggledImageView;
import com.iReadingGroup.iReading.WordDetail;
import com.r0adkll.slidr.Slidr;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Word detail activity.
 */
public class WordDetailActivity extends DetailBaseActivity {
    private String detailed_word, detailed_meaning, meaning, sentence;
    private String online_meaning;
    private WordDetail wordDetail;
    private Context mContext;
    private String sumString = "";
    private ToggledImageView collectionImageViewToolbar;
    private View pronView;
    private ScrollView scrollView;
    private boolean isOfflineTitleExpanded = false;
    private boolean isOfflineTitleClicked = false;//indicating wht
    private boolean collectedInOffline = false;
    private ExpandableLayout expandableLayoutOnline, expandableLayoutOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        Slidr.attach(this);

        ((MyApplication) getApplication()).countActivity++;//record the Word activity number

        mContext = this;
        //get arguments from intent to bundle
        detailed_word = getWordFromBundle();
        detailed_meaning = getMeaningFromBundle();

        initializeWordTextView();
        initializeStatusBar(R.color.colorPrimary);
        initializeDatabase();
        initializeToolBar(R.id.toolbar, R.id.backLayout, R.id.title, detailed_word);
        initializeImageButton();
        initializeOfflineCollectionUI();
        initializeScrollView();//make scrollview handle size change automatically
        //make offline GONE at first
        pronView = findViewById(R.id.proView);
        initializeOnlineExpandableLayout();
        initializeOfflineExpandableLayout();
        fetchDetailedMeaning(((MyApplication) getApplication()).getFetchingPolicy());

    }

    /**
     * initialize the Textview indicating the  detailed word,both offline and online;
     */
    private void initializeWordTextView() {
        TextView word = findViewById(R.id.word_online);
        word.setText(detailed_word);
        word = findViewById(R.id.word_offline);
        word.setText(detailed_word);
    }

    /**
     * if offline is folded and need to expand, scroll to bottom after it expanded
     */
    private void initializeScrollView() {
        scrollView = findViewById(R.id.scroll);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isOfflineTitleExpanded && isOfflineTitleClicked)
                    scrollView.post(new Runnable() {
                        public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);

                        }
                    });
            }
        });
    }

    /**
     * Initialize Online ExpandableLayout
     * toggle arrow change when click
     */
    private void initializeOnlineExpandableLayout() {
        expandableLayoutOnline = findViewById(R.id.expandable_iciba);
        TextView a = findViewById(R.id.expand_iciba);
        final ImageView b = findViewById(R.id.arrow_iciba);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOfflineTitleClicked = false;
                if (expandableLayoutOnline.isExpanded()) {
                    expandableLayoutOnline.collapse();
                    b.setImageResource(R.drawable.arrow_down);
                } else {
                    expandableLayoutOnline.expand();
                    b.setImageResource(R.drawable.arrow_up);

                }
            }

        });
    }

    private void initializeOfflineExpandableLayout() {
        expandableLayoutOffline = findViewById(R.id.expandable_offline);
        TextView a1 = findViewById(R.id.expand_offline);
        final ImageView b1 = findViewById(R.id.arrow_offline);
        a1.setStateListAnimator(null);
        a1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOfflineTitleClicked = true;
                isOfflineTitleExpanded = false;
                if (expandableLayoutOffline.isExpanded()) {
                    expandableLayoutOffline.collapse();
                    b1.setImageResource(R.drawable.arrow_down);
                } else {
                    isOfflineTitleExpanded = true;
                    expandableLayoutOffline.expand();
                    b1.setImageResource(R.drawable.arrow_up);

                }

            }

        });
    }

    //fetching detailed meaning according to policy
    private void fetchDetailedMeaning(int policy) {
        if (policy != Constant.SETTING_POLICY_OFFLINE_ALWAYS && Function.isNetworkAvailable(this)) {
            //enable online policy
            FetchingWordDetailAsyncTask asyncTask = new FetchingWordDetailAsyncTask(new AsyncResponse() {
                @Override
                public void processFinish(Object output) {
                    wordDetail = (WordDetail) output;
                    updateUI();//update UI after obtaining data
                }
            });
            asyncTask.execute(Function.getWordDetailUrl(detailed_word.toLowerCase()));
        } else {//disable online searching
            makeLayoutGone(expandableLayoutOnline);
            findViewById(R.id.title_offline).setVisibility(View.VISIBLE);
            if (!Function.getWordOfflineStatus(daoDictionary, detailed_word)) {
                makeLayoutGone(expandableLayoutOffline);
                Toast.makeText(mContext, "采用仅离线模式,该词不在本地词库中", Toast.LENGTH_SHORT).show();
            } else
                expandableLayoutOffline.setVisibility(View.VISIBLE);

        }
    }

    private void makeLayoutGone(ViewGroup v) {
        v.setVisibility(View.GONE);
        for (int i = 0; i < v.getChildCount(); i++) {
            View child = v.getChildAt(i);
            child.setVisibility(View.GONE);
        }
    }

    private void initializeOfflineCollectionUI() {
        meaning = Function.getWordOfflineMeaning(daoDictionary, detailed_word);
        if (meaning != null) {
            collectedInOffline = true;
            sentence = Function.getWordOfflineSentence(daoDictionary, detailed_word);
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
        if (detailed_meaning == null)
            detailed_meaning = online_meaning.substring(31).replace("<br>", "\t");
        online_meaning += "</p>";
    }

    private void updateOnlineSentence() {
        for (List<String> sentPair : wordDetail.getSent()) {
            sumString += getClickedString(sentPair.get(0)) + "<span style=\"color: #0D47A1;\">" + sentPair.get(1) + "</span>" + "<br>";
        }
        sumString += "<p>";
        initializeWebView(R.id.wv, getWebViewString());
        textWebview.setWebViewClient(new MyWebViewClient() {
            @Override
            public void onPageCommitVisible(WebView view,
                                            String url) {
                if (collectedInOffline)//if in offline
                    expandableLayoutOffline.setVisibility(View.VISIBLE);
                else  // if not in offline ,make them all gone
                    makeLayoutGone(expandableLayoutOffline);
                //offline title visible
                findViewById(R.id.title_offline).setVisibility(View.VISIBLE);
                //pro view visible
                pronView.setVisibility(View.VISIBLE);
                //webview visible
                view.setVisibility(View.VISIBLE);
                if (!wordDetail.getMeaning().isEmpty()) {//if online fetched something
                    expandableLayoutOnline.setVisibility(View.VISIBLE);
                } else  // if online fetched nothing,make them all gone
                    makeLayoutGone(expandableLayoutOnline);
            }

        });
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
                            e.printStackTrace();
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
                            e.printStackTrace();
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
        collectionImageViewToolbar = findViewById(R.id.collect_word_button);
        if (getWordCollectedStatus(detailed_word))
            collectionImageViewToolbar.initialize(R.drawable.collect_true);
        else
            collectionImageViewToolbar.initialize(R.drawable.collect_false);
        collectionImageViewToolbar.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                ((ToggledImageView) v).toggleImage();
                if (getWordCollectedStatus(detailed_word)) {
                    //already in the db, the user means to removed this word from collection
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(detailed_word, detailed_meaning, "remove"));

                } else {
                    //not in the db, the user means to add this word to collection
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


    private String getWebViewString() {

        return Function.getHtml(online_meaning + sumString);

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


    /**
     * On word dataset changed event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWordDatasetChangedEvent(WordDatasetChangedEvent event) {
        boolean flag_collected = getWordCollectedStatus(current_word);
        if (flag_collected) {   //already collected
            ppwCollectionImageView.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_true));
        } else {   //not collected yet
            ppwCollectionImageView.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_false));
        }
        flag_collected = getWordCollectedStatus(detailed_word);
        if (flag_collected) {   //already collected
            collectionImageViewToolbar.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_true));
        } else {   //not collected yet
            collectionImageViewToolbar.setImageDrawable(
                    ContextCompat.getDrawable(mContext, R.drawable.collect_false));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (((MyApplication) getApplication()).countActivity-- == 1)
            System.exit(0);
    }


}
