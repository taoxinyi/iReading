package com.iReadingGroup.iReading;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by taota on 2018/4/19.
 */
public class DoubleClickBackToContentTopListener implements View.OnClickListener {

    private final long delayTime = 300;
    private final IBackToContentTopView backToContentTopView;
    private long lastClickTime = 0;

    /**
     * Instantiates a new Double click back to content top listener.
     *
     * @param backToContentTopView the back to content top view
     */
    public DoubleClickBackToContentTopListener(@NonNull IBackToContentTopView backToContentTopView) {
        this.backToContentTopView = backToContentTopView;
    }

    @Override
    public final void onClick(View v) {
        long nowClickTime = System.currentTimeMillis();
        if (nowClickTime - lastClickTime > delayTime) {
            lastClickTime = nowClickTime;
        } else {
            onDoubleClick(v);
        }
    }

    /**
     * On double click.
     *
     * @param v the v
     */
    public void onDoubleClick(View v) {
        backToContentTopView.backToContentTop();
    }

    /**
     * The interface Back to content top view.
     */
    public interface IBackToContentTopView {
        /**
         * Back to content top.
         */
        void backToContentTop();
    }

}