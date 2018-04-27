package com.iReadingGroup.iReading.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.iReadingGroup.iReading.ClearCache;
import com.iReadingGroup.iReading.Event.RefreshingNumberChangedEvent;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.leon.lib.settingview.LSettingItem;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by taota on 2018/4/25.
 */


public class SettingsActivity extends AppCompatActivity {
    private Context mContent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Slidr.attach(this);
        setContentView(R.layout.activity_settings);
        mContent = this;
        initializeClearCache();
        initializeSetNumber();


//更改右侧文字

    }

    private void initializeClearCache() {
        final LSettingItem mSettingItemOne = (LSettingItem) findViewById(R.id.item_one);
        final String size = ClearCache.getTotalCacheSize(getApplicationContext());
        mSettingItemOne.setRightText("当前缓存大小：" + size);
        mSettingItemOne.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {

                Toast.makeText(getApplicationContext(), "清除缓存" + size, Toast.LENGTH_SHORT).show();
                ClearCache.clearAllCache(getApplicationContext());
                mSettingItemOne.setRightText("当前缓存大小：" + ClearCache.getTotalCacheSize(getApplicationContext()));

            }
        });
    }

    private void initializeSetNumber() {
        final LSettingItem NumberSettingItem = (LSettingItem) findViewById(R.id.item_number);
        NumberSettingItem.setRightText(((MyApplication)getApplication()).getSetting("number"));
        final String[] list = {"1", "5", "10", "15", "20"};
        NumberSettingItem.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {

                new AlertView("修改每次刷新/加载的数量", null, "取消", null,
                        list,
                        mContent, AlertView.Style.ActionSheet, new OnItemClickListener() {
                    public void onItemClick(Object o, int position) {
                        if (position==-1) return;
                        ((MyApplication)getApplication()).saveSetting("number",list[position]);
                        Toast.makeText(mContent, String.format("修改每次刷新/加载的数量至%s条", list[position]), Toast.LENGTH_SHORT).show();
                        NumberSettingItem.setRightText(((MyApplication)getApplication()).getSetting("number"));

                    }
                }).show();

            }
        });
    }



}
