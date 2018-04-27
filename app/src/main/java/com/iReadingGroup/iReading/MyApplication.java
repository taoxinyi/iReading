package com.iReadingGroup.iReading;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Bean.DaoMaster;
import com.iReadingGroup.iReading.Bean.DaoSession;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBean;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.Event.ChangeWordCollectionDBEvent;
import com.iReadingGroup.iReading.Event.ArticleDatabaseChangedEvent;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;
import com.iReadingGroup.iReading.Event.changeArticleCollectionDBEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import static com.chad.library.adapter.base.listener.SimpleClickListener.TAG;

public class MyApplication extends Application {
    private ArticleEntityDao daoArticle;
    private OfflineDictBeanDao daoDictionary;
    private WordCollectionBeanDao daoCollection;
    public int countActivity=0;

    public ArticleEntityDao getDaoArticle() {return daoArticle;}
    public OfflineDictBeanDao getDaoDictionary() {return daoDictionary;}
    public WordCollectionBeanDao getDaoCollection() {return daoCollection;}
    public void setDaoArticle(ArticleEntityDao daoArticle) {this.daoArticle=daoArticle;}
    public void setDaoDictionary(OfflineDictBeanDao daoDictionary) {this.daoDictionary = daoDictionary;}
    public void setDaoCollection(WordCollectionBeanDao daoCollection) {this.daoCollection=daoCollection;}
    @SuppressLint("SdCardPath")
    private static final String DB_PATH = "/data/data/com.iReadingGroup.iReading/databases/";//database external path
    private static final String DB_NAME = "wordDetail.db";//database name

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        copyDBToDatabases();
        initializeDatabase();
        BroadcastReceiver br = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.iReadingGroup.iReading.WORD_DB_CHANGE");
        filter.addAction("com.iReadingGroup.iReading.ARTICLE_DB_CHANGE");
        this.registerReceiver(br, filter);

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
                return;
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
            e.printStackTrace();
        }

    }

    /**
     * Initialize the database into instance
     */
    private void initializeDatabase() {

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DB_NAME);
        helper.setWriteAheadLoggingEnabled(true);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        daoDictionary = daoSession.getOfflineDictBeanDao();//this is the offline dictionary database

        DaoMaster.DevOpenHelper helper_collection = new DaoMaster.DevOpenHelper(this, "userCollection.db");
        helper_collection.setWriteAheadLoggingEnabled(true);
        Database db_collection = helper_collection.getWritableDb();
        DaoSession daoSession_collection = new DaoMaster(db_collection).newSession();
        daoCollection = daoSession_collection.getWordCollectionBeanDao();// this is the database recording user's word collection

        DaoMaster.DevOpenHelper helper_article = new DaoMaster.DevOpenHelper(this, "userArticle.db");
        helper_article.setWriteAheadLoggingEnabled(true);
        Database db_article = helper_article.getWritableDb();
        DaoSession daoSession_article = new DaoMaster(db_article).newSession();
        daoArticle = daoSession_article.getArticleEntityDao();// this is the database(cache) recording user's articles

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangeWordCollectionDBEvent(ChangeWordCollectionDBEvent event) {
        String word = event.word;
        String meaning = event.meaning;
        String operation = event.operation;
        if (operation.equals("add")) {   //add into database
            daoCollection.insert(new WordCollectionBean(word, meaning));
            Toast.makeText(getApplicationContext(),"已收藏单词: "+word,Toast.LENGTH_SHORT).show();
        } else {    //delete from database
            daoCollection.delete(new WordCollectionBean(word, meaning));
            Toast.makeText(getApplicationContext(),"已取消收藏单词: "+word,Toast.LENGTH_SHORT).show();

        }
        Intent intent = new Intent("com.iReadingGroup.iReading.WORD_DB_CHANGE");
        intent.putExtra("word",word);
        intent.putExtra("meaning",meaning);
        intent.putExtra("operation",operation);
        sendBroadcast(intent);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangeArticleCollectionDBEvent (changeArticleCollectionDBEvent event) {
        String uri = event.uri;
        String operation=event.operation;
        final ArticleEntity article = daoArticle.queryBuilder().where(ArticleEntityDao.Properties.Uri.eq(uri)).list().get(0);
        if (operation.equals("remove")) {
            article.setCollectStatus(false);
            daoArticle.update(article);
            Toast.makeText(getApplicationContext(),"已取消收藏该文章",Toast.LENGTH_SHORT).show();

        } else {
            //add collection
            article.setCollectStatus(true);
            Date currentTime = Calendar.getInstance().getTime();
            article.setCollectTime(currentTime);
            daoArticle.update(article);
            Toast.makeText(getApplicationContext(),"已收藏该文章",Toast.LENGTH_SHORT).show();


        }
        Intent intent = new Intent("com.iReadingGroup.iReading.ARTICLE_DB_CHANGE");
        intent.putExtra("uri",uri);
        intent.putExtra("operation",operation);
        sendBroadcast(intent);
    }
    public void saveSetting(String name, String value) {
        SharedPreferences settings = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.apply();
    }

    public String getSetting(String name) {
        SharedPreferences settings = getSharedPreferences("setting", 0);
        if (name.equals("number"))
            return settings.getString(name, "10");
        else return "";
    }

}