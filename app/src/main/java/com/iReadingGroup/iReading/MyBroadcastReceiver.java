package com.iReadingGroup.iReading;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iReadingGroup.iReading.Event.ArticleDatabaseChangedEvent;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;

import org.greenrobot.eventbus.EventBus;

import static com.chad.library.adapter.base.listener.SimpleClickListener.TAG;

/**
 * Created by taota on 2018/4/26.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        else if (intent.getAction().equals("com.iReadingGroup.iReading.WORD_DB_CHANGE")) {
            String word = intent.getStringExtra("word");
            String meaning = intent.getStringExtra("meaning");
            String operation = intent.getStringExtra("operation");
            EventBus.getDefault().post(new WordDatasetChangedEvent(word, meaning, operation));
        }
        else if(intent.getAction().equals("com.iReadingGroup.iReading.ARTICLE_DB_CHANGE"))
        {   String uri = intent.getStringExtra("uri");
            String operation = intent.getStringExtra("operation");
            EventBus.getDefault().post(new ArticleDatabaseChangedEvent(uri,operation));
        }
    }
}