package com.iReadingGroup.iReading.Activity;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iReadingGroup.iReading.AsyncResponse;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBean;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.Event.CollectWordEvent;
import com.iReadingGroup.iReading.Event.WordCollectionStatusChangedEvent;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;
import com.iReadingGroup.iReading.FetchingWordDetailAsyncTask;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordDetail;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

/**
 * Created by taota on 2018/4/9.
 */

public class WordDetailActivity extends AppCompatActivity {
    private String current_word, current_meaning,meaning, sentence;
    private OfflineDictBeanDao daoDictionary;
    private String online_meaning = "";
    private String online_sentence = "";
    private WordDetail wordDetail;
    private WordCollectionBeanDao daoCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        Slidr.attach(this);
        //get arguments from intent to bundle
        current_word = getWordFromBundle();
        current_meaning=getMeaningFromBundle();
        TextView word = findViewById(R.id.word_itself);
        word.setText(current_word);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        initializeDatabase();
        initializeToolBar();
        initializeImageButton();
        List<OfflineDictBean> l = daoDictionary.queryBuilder().where(OfflineDictBeanDao.Properties.Word.eq(current_word)).list();
        if (l.size() > 0) {
            OfflineDictBean w = l.get(0);
            meaning = w.getMeaning();
            sentence = w.getSentence();
            TextView m = findViewById(R.id.word_meaning);
            m.setText(meaning);
            if (!sentence.equals("")) {
                TextView s = findViewById(R.id.word_sentence);
                s.setText(sentence);
            }

        }
        FetchingWordDetailAsyncTask asyncTask = new FetchingWordDetailAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                wordDetail = (WordDetail) output;
                updateUI();
            }
        });
        asyncTask.execute("http://dict-co.iciba.com/api/dictionary.php?key=341DEFE6E5CA504E62A567082590D0BD&w=" + current_word.toLowerCase());


    }

    private void updateUI() {
        for (List<String> meaningPair : wordDetail.getMeaning()) {

            meaningPair.get(0);
            online_meaning += meaningPair.get(0) + " " + meaningPair.get(1) + "\n";

        }
        TextView a = findViewById(R.id.online_word_meaning);
        a.setText(online_meaning);
        a = findViewById(R.id.online_word_sentence);
        for (List<String> sentPair :wordDetail.getSent()) {

            Spannable word = new SpannableString(sentPair.get(0) + "\n");
            word.setSpan(new ForegroundColorSpan(Color.BLUE), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            a.append(word);
            Spannable wordTwo = new SpannableString(sentPair.get(1)  + "\n");
            wordTwo.setSpan(new ForegroundColorSpan(Color.BLACK), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            a.append(wordTwo);
        }
        int index = 0;
        ImageButton b=findViewById(R.id.voice_1);
        b.setVisibility(View.INVISIBLE);
        b=findViewById(R.id.voice_2);
        b.setVisibility(View.INVISIBLE);
        for (List<String> pronPair: wordDetail.getPron()) {

            final String value = pronPair.get(1);
            if (index == 0) {
                a = findViewById(R.id.pron_1);
                a.setText(String.format("/BrE %s/",pronPair.get(0)));;
                b = findViewById(R.id.voice_1);
                b.setVisibility(View.VISIBLE);
                b.setOnClickListener(new View.OnClickListener() {
                    //collect word function.
                    @Override
                    public void onClick(View v) {
                        try{
                            Toast.makeText(getApplicationContext(),"英音",Toast.LENGTH_SHORT).show();
                            Uri uri = Uri.parse(value);
                            MediaPlayer player = new MediaPlayer();
                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            player.setDataSource(getApplicationContext(), uri);
                            player.prepare();
                            player.start();
                        }catch( IOException e){
                        }
                    }
                });
            } else {
                a = findViewById(R.id.pron_2);
                a.setText(String.format("/AmE %s/",pronPair.get(0)));
                b = findViewById(R.id.voice_2);
                b.setVisibility(View.VISIBLE);
                b.setOnClickListener(new View.OnClickListener() {
                    //collect word function.
                    @Override
                    public void onClick(View v) {
                        try{
                            Toast.makeText(getApplicationContext(),"美音",Toast.LENGTH_SHORT).show();
                            Uri uri = Uri.parse(value);
                            MediaPlayer player = new MediaPlayer();
                            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            player.setDataSource(getApplicationContext(), uri);
                            player.prepare();
                            player.start();
                        }catch( IOException e){
                        }
                    }
                });
            }
            index++;
        }


    }

    private void initializeImageButton(){
        ImageButton b=findViewById(R.id.collect_word_button);
        if( getWordCollectionStatus(current_word))
            b.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
        else b.setImageDrawable(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
        b.setOnClickListener(new View.OnClickListener() {
            //collect word function.
            @Override
            public void onClick(View v) {
                ImageButton button = (ImageButton) v;
                if (getWordCollectionStatus(current_word)) {
                    removeWordFromCollection(current_word);
                    //already in the db, the user means to removed this word from collection
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
                } else {
                    //not in the db, the user means to add this word to collection
                    addWordIntoCollection(current_word);
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));
                }
                EventBus.getDefault().postSticky(new WordDatasetChangedEvent(current_word,current_meaning));

            }
        });
    }
    private String getWordFromBundle() {
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("word");
    }
    private String getMeaningFromBundle(){
        Bundle bundle = getIntent().getExtras();
        return bundle.getString("meaning");
    }
    private void initializeDatabase() {

        MyApplication app = (MyApplication) getApplicationContext();//initialize UI
        daoDictionary = app.getDaoDicitionary();
        daoCollection = app.getDaoCollection();

    }

    private void initializeToolBar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        toolBar.setTitle(current_word);//set corresponding title in toolbar
        setSupportActionBar(toolBar);
    }
    private boolean getWordCollectionStatus(String word) {
        if (daoCollection.queryBuilder().where(WordCollectionBeanDao.Properties.Word.eq(word)).list().size() == 0)
            return false;
        else
            return true;

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

}
