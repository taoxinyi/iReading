package com.iReadingGroup.iReading.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.ArticleInfo;
import com.iReadingGroup.iReading.R;

import java.util.List;

/**
 * ArticleInfoAdapter
 * This Adapter is a bridge between actual ArrayList and RecycleView(ListView)
 * Set Text and Image to Class:ArticleInfo
 */
public class ArticleInfoAdapter extends BaseQuickAdapter<ArticleInfo, BaseViewHolder> {
    public ArticleInfoAdapter(Context context, int layoutResId, List<ArticleInfo> data) {
        super(layoutResId, data);
        mContext=context;
    }


    @Override
    protected void convert(BaseViewHolder helper, ArticleInfo item) {
        helper.setText(R.id.txt_title, item.getName());
        helper.setText(R.id.txt_source,item.getSource());
        helper.setText(R.id.txt_time,item.getTime());
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.mipmap.icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(((MainActivity)mContext).getApplicationContext()).load(item.getImageUrl()).apply(options).into((ImageView) helper.getView(R.id.img));

    }

}