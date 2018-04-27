package com.iReadingGroup.iReading.Activity;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.iReadingGroup.iReading.R;


/**
 * This activity is the base activity fo ArticleDetailActivity and WordDetailActivity
 * A lof of function is almost identical like webView,toolbar,popupWindow,etc...
 */

public abstract class DetailBaseActivity extends AppCompatActivity {

    /**
     * initialize status bar
     * 初始化状态栏
     */
    public void initializeStatusBar(int color)
    {
        getWindow().setStatusBarColor(ContextCompat.getColor(this, color));
    }
    public void initializeToolBar(int idToolbar,int idBackLayout,int idTitle,String title)
    {
        Toolbar toolBar = (Toolbar) findViewById(idToolbar);
        toolBar.setTitle("");//set corresponding title in toolbar
        setSupportActionBar(toolBar);
        findViewById(idBackLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView)findViewById(idTitle)).setText(title);
    }


}
