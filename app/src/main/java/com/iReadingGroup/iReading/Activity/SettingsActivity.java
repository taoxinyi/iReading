package com.iReadingGroup.iReading.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Constant;
import com.iReadingGroup.iReading.Function;
import com.iReadingGroup.iReading.MyApplication;
import com.iReadingGroup.iReading.R;
import com.leon.lib.settingview.LSettingItem;
import com.r0adkll.slidr.Slidr;

import java.util.Locale;

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
        initializeSetPage();
        initializeToolBar();
        initializeSetKey();
        initializeSetPolicy();
        initializeClearHistory();


    }

    private void initializeClearHistory() {
        final LSettingItem mSettingItemHistory = findViewById(R.id.item_clear_history);
        ArticleEntityDao daoArticle = ((MyApplication) getApplication()).getDaoArticle();
        final int num = Function.getCollectedArticlesList(daoArticle, false).size();

        mSettingItemHistory.setRightText("历史文章总数: " + num + "");
        mSettingItemHistory.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                new AlertView("是否确定清除历史文章", "注意：后将保留已收藏的文章\n",
                        "取消", new String[]{"确定"}, null, mContent,
                        AlertView.Style.Alert, new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != AlertView.CANCELPOSITION) {
                            ((MyApplication) getApplication()).saveSetting("history", false);
                            mSettingItemHistory.setRightText("历史文章总数: 0");
                            Toast.makeText(mContent, "清除历史文章：" + num, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();

            }
        });
    }

    private void initializeClearCache() {
        final LSettingItem mSettingItemOne = findViewById(R.id.item_one);
        final String size = Function.getTotalCacheSize(getApplicationContext());
        mSettingItemOne.setRightText("当前缓存大小：" + size);
        mSettingItemOne.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {

                Toast.makeText(getApplicationContext(), "清除缓存" + size, Toast.LENGTH_SHORT).show();
                Function.clearAllCache(getApplicationContext());
                mSettingItemOne.setRightText("当前缓存大小：" + Function.getTotalCacheSize(getApplicationContext()));

            }
        });
    }

    private void initializeSetNumber() {
        final LSettingItem NumberSettingItem = findViewById(R.id.item_number);
        NumberSettingItem.setRightText(((MyApplication) getApplication()).getNumberSetting());
        final String[] list = Constant.SETTINGS_REFRESHING_LIST;
        NumberSettingItem.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {

                new AlertView("修改每次刷新/加载的数量", null, "取消", null,
                        list,
                        mContent, AlertView.Style.ActionSheet, new OnItemClickListener() {
                    public void onItemClick(Object o, int position) {
                        if (position == -1) return;
                        ((MyApplication) getApplication()).saveSetting("number", list[position]);
                        Toast.makeText(mContent, String.format("修改每次刷新/加载的数量至%s条", list[position]), Toast.LENGTH_SHORT).show();
                        NumberSettingItem.setRightText(((MyApplication) getApplication()).getNumberSetting());

                    }
                }).show();

            }
        });
    }

    private void initializeSetPage() {
        final LSettingItem NumberSettingItem = findViewById(R.id.item_page);
        final String[] list = Constant.SETTINGS_LAUNCH_LIST;
        int index = ((MyApplication) getApplication()).getPageSetting();
        NumberSettingItem.setRightText(list[index]);
        NumberSettingItem.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {

                new AlertView("修改默认启动页", null, "取消", null,
                        list,
                        mContent, AlertView.Style.ActionSheet, new OnItemClickListener() {
                    public void onItemClick(Object o, int position) {
                        if (position == -1) return;
                        ((MyApplication) getApplication()).saveSetting("page", position);
                        Toast.makeText(mContent, String.format(Locale.getDefault(), "修改默认启动页至[%s]", list[position]), Toast.LENGTH_SHORT).show();
                        NumberSettingItem.setRightText(list[position]);

                    }
                }).show();

            }
        });
    }

    private void initializeSetPolicy() {
        final LSettingItem PolicySettingItem = findViewById(R.id.item_fetch);
        final String[] list = Constant.SETTINGS_POLICY_LIST;
        int index = ((MyApplication) getApplication()).getFetchingPolicy();
        PolicySettingItem.setRightText(list[index]);
        PolicySettingItem.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {

                new AlertView("修改查词策略", null, "取消", null,
                        list,
                        mContent, AlertView.Style.ActionSheet, new OnItemClickListener() {
                    public void onItemClick(Object o, int position) {
                        if (position == -1) return;
                        ((MyApplication) getApplication()).saveSetting("policy", position);
                        Toast.makeText(mContent, String.format(Locale.getDefault(), "修改查词策略至[%s]", list[position]), Toast.LENGTH_SHORT).show();
                        PolicySettingItem.setRightText(list[position]);

                    }
                }).show();

            }
        });

    }

    private void initializeSetKey() {
        final LSettingItem KeySettingItem = findViewById(R.id.item_apikey);
        KeySettingItem.setRightText(getProcessedApiKey());
        ViewGroup extView = (ViewGroup) LayoutInflater.from(mContent).inflate(R.layout.alertext_form, null);
        TextView registerUrl = extView.findViewById(R.id.registerUrl);
        registerUrl.setHighlightColor(Color.TRANSPARENT);
        Spannable span = Spannable.Factory.getInstance().newSpannable(Constant.SETTING_REGISTER_URL);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.SETTING_REGISTER_URL));
                startActivity(intent);
            }
        }, 0, Constant.SETTING_REGISTER_URL.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerUrl.setText(span);

        registerUrl.setMovementMethod(LinkMovementMethod.getInstance());

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final EditText editText = (EditText) extView.findViewById(R.id.edittext);
        final AlertView mAlertViewExt = new AlertView("修改你的apiKey",
                "当网络通畅时无法加载文章，可能是由于apiKey点数用完，请及时注册修改，点击如下连接后粘贴apiKey",
                "取消", new String[]{"确定"}, null, mContent, AlertView.Style.Alert,
                new OnItemClickListener() {
                    public void onItemClick(Object o, int position) {
                        //hide keyboard
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        final String apikey;
                        if (position != AlertView.CANCELPOSITION) {
                            //hide keyboard
                            apikey = editText.getText().toString();
                            if (apikey.isEmpty()) {
                                Toast.makeText(mContent, "尚未填写apiKey", Toast.LENGTH_SHORT).show();
                            } else {
                                AlertView al = new AlertView("是否确定修改apiKey", "错误的修改将会导致无法正确加载文章\n" +
                                        "即将修改apiKey为" + apikey,
                                        "取消", new String[]{"确定"}, null, mContent,
                                        AlertView.Style.Alert, new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (position != AlertView.CANCELPOSITION) {
                                            ((MyApplication) getApplication()).saveSetting("key", apikey);
                                            KeySettingItem.setRightText(getProcessedApiKey());
                                            Toast.makeText(mContent, "修改apikey为" + apikey, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                al.setMarginBottom(350);
                                al.show();

                            }
                        }

                    }
                });
        mAlertViewExt.setMarginBottom(350);
        mAlertViewExt.addExtView(extView);

        KeySettingItem.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                editText.setText(((MyApplication) getApplication()).getApiKeySetting());
                mAlertViewExt.show();
                //set focus
                editText.requestFocus();
                //show keyboard
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            }
        });


    }

    private void initializeToolBar() {
        Toolbar toolBar = findViewById(R.id.toolbar);
        toolBar.setTitle("");//set corresponding title in toolbar
        setSupportActionBar(toolBar);
        findViewById(R.id.backLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private String getProcessedApiKey() {   //make part of the key visible
        String apikey = ((MyApplication) getApplication()).getApiKeySetting();
        apikey = (apikey.length() > 13) ? apikey.substring(0, 13) + "..." : apikey;
        return apikey;
    }

}
