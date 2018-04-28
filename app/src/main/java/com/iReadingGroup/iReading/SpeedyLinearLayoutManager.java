package com.iReadingGroup.iReading;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

/**
 * Created by taota on 2018/4/19.
 */
public class SpeedyLinearLayoutManager extends LinearLayoutManager {

    private static final float MILLISECONDS_PER_INCH = 5f; //default is 25f (bigger = slower)

    /**
     * Instantiates a new Speedy linear layout manager.
     *
     * @param context the context
     */
    public SpeedyLinearLayoutManager(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Speedy linear layout manager.
     *
     * @param context       the context
     * @param orientation   the orientation
     * @param reverseLayout the reverse layout
     */
    public SpeedyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    /**
     * Instantiates a new Speedy linear layout manager.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     * @param defStyleRes  the def style res
     */
    public SpeedyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return super.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}