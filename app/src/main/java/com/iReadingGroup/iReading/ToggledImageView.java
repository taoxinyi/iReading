package com.iReadingGroup.iReading;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by xytao on 2018/4/28.
 */
public class ToggledImageView extends AppCompatImageView{
    private int idCurrentStatus;
    private int idStatus_1=R.drawable.collect_false;
    private int idStatus_2=R.drawable.collect_true;

    /**
     * Instantiates a new Collection button.
     *
     * @param context the context
     */
    public ToggledImageView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Collection button.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public ToggledImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Instantiates a new Collection button.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public ToggledImageView(Context context, AttributeSet attrs, int defStyle) {
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
    public void initialize(int idCurrentStatus,int idStatus_1,int idStatus_2)
    {
        this.idCurrentStatus=idCurrentStatus;
        this.idStatus_1=idStatus_1;
        this.idStatus_2=idStatus_2;
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
