package com.iReadingGroup.iReading.Adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.Event.ArticleCollectionStatusChangedEvent;
import com.iReadingGroup.iReading.Bean.ArticleEntity;
import com.iReadingGroup.iReading.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * ArticleInfoAdapter
 * This Adapter is a bridge between actual ArrayList and RecycleView(ListView)
 * Set Text and Image to Class:ArticleInfo
 */
public class ArticleInfoAdapter extends BaseQuickAdapter<ArticleEntity, BaseViewHolder> {
    public ArticleInfoAdapter(Context context, int layoutResId, List<ArticleEntity> data) {
        super(layoutResId, data);
        mContext=context;
    }


    @Override
    protected void convert(BaseViewHolder helper, final ArticleEntity item) {
        final String uri=item.getUri();
        boolean collectionStatus=item.getCollectStatus();
        helper.setText(R.id.txt_title, item.getName());
        helper.setText(R.id.txt_source,item.getSource());
        helper.setText(R.id.txt_time,item.getTime());
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.mipmap.icon)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.HIGH);
        Glide.with(((MainActivity)mContext).getApplicationContext()).load(item.getImageUrl()).apply(options).into((ImageView) helper.getView(R.id.img));
        Button a=helper.getView(R.id.swipe_collection);

        if(collectionStatus) a.setText("取消收藏");
        else a.setText("收藏");
        a.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                EventBus.getDefault().post(new ArticleCollectionStatusChangedEvent(uri));
                if(((Button)v).getText()=="收藏") ((Button)v).setText("取消收藏");
                else ((Button)v).setText("收藏");

            }
        });
    }

}