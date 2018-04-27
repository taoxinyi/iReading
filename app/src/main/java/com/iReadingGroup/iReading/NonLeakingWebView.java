package com.iReadingGroup.iReading;

/**
 * Created by taota on 2018/4/25.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

public class NonLeakingWebView extends WebView {
    private static Field sConfigCallback;

    static {
        try {
            sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
            sConfigCallback.setAccessible(true);
        } catch (Exception e) {
            // ignored
        }

    }


    public NonLeakingWebView(Context context) {
        super(context.getApplicationContext());
        setWebViewClient( new MyWebViewClient((Activity)context) );
    }

    public NonLeakingWebView(Context context, AttributeSet attrs) {
        super(context.getApplicationContext(), attrs);
        setWebViewClient(new MyWebViewClient((Activity)context));
    }

    public NonLeakingWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context.getApplicationContext(), attrs, defStyle);
        setWebViewClient(new MyWebViewClient((Activity)context));
    }

    @Override
    public void destroy() {
        super.destroy();

        try {
            if( sConfigCallback!=null )
                sConfigCallback.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected static class MyWebViewClient extends WebViewClient {
        protected WeakReference<Activity> activityRef;

        public MyWebViewClient( Activity activity ) {
            this.activityRef = new WeakReference<Activity>(activity);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                final Activity activity = activityRef.get();
                if( activity!=null )
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }catch( RuntimeException ignored ) {
                // ignore any url parsing exceptions
            }
            return true;
        }
    }
}