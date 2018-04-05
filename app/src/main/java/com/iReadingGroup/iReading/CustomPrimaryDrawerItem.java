package com.iReadingGroup.iReading;

import android.support.annotation.LayoutRes;

import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.model.AbstractBadgeableDrawerItem;

import java.util.List;

/**
 * Created by taota on 2018/4/4.
 */

public class CustomPrimaryDrawerItem extends AbstractBadgeableDrawerItem<CustomPrimaryDrawerItem> {

    private ColorHolder background;

    public CustomPrimaryDrawerItem withBackgroundColor(int backgroundColor) {
        this.background = ColorHolder.fromColor(backgroundColor);
        return this;
    }

    public CustomPrimaryDrawerItem withBackgroundRes(int backgroundRes) {
        this.background = ColorHolder.fromColorRes(backgroundRes);
        return this;
    }

    @Override
    public void bindView(ViewHolder holder, List payloads) {
        super.bindView(holder, payloads);

        if (background != null) {
            background.applyToBackground(holder.itemView);
        }
    }
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.header;
    }
}
