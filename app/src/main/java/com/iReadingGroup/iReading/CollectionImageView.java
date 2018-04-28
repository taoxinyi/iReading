package com.iReadingGroup.iReading;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by xytao on 2018/4/28.
 */
public class CollectionImageView extends AppCompatImageView{
    private int idCurrentStatus;
    private int idStatus_1=R.drawable.collect_false;
    private int idStatus_2=R.drawable.collect_true;

    /**
     * Instantiates a new Collection button.
     *
     * @param context the context
     */
    public CollectionImageView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Collection button.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CollectionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Instantiates a new Collection button.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public CollectionImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Initialize.
     *
     * @param idCurrentStatus the id current status
     */

    public void initialize(int idCurrentStatus)
    {
        this.idCurrentStatus=idCurrentStatus;
        this.setImageResource(this.idCurrentStatus);

    }

    /**
     * Toggle image.
     */
    public void toggleImage() {
        idCurrentStatus=(idCurrentStatus==idStatus_1)?idStatus_2:idStatus_1;
        this.setImageResource(this.idCurrentStatus);
    }
}
