package com.iReadingGroup.iReading.Adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;


import java.util.List;

import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordInfo;
import com.iReadingGroup.iReading.Event.WordCollectionStatusChangedEvent;

import org.greenrobot.eventbus.EventBus;


/**
 * WordInfoAdapter
 * This Adapter is a bridge between actual ArrayList and RecycleView(ListView)
 * Set Text and Image to Class:WordInfo
 */

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

/**
 * ArticleInfoAdapter
 * This Adapter is a bridge between actual ArrayList and RecycleView(ListView)
 * Set Text and Image to Class:ArticleInfo
 */
public class WordInfoAdapter extends BaseQuickAdapter<WordInfo, BaseViewHolder> {
    public WordInfoAdapter(Context context, int layoutResId, List<WordInfo> data) {
        super(layoutResId, data);
        mContext=context;
    }

    @Override
    protected void convert(final BaseViewHolder helper, final WordInfo item) {
        boolean collectionStatus=item.getCollectStatus();
        helper.setText(R.id.word_word_info, item.getWord());
        helper.setText(R.id.meaning_word_info,item.getMeaning());
        helper.setImageResource(R.id.img_word_info,item.getImageId());

        Button a=helper.getView(R.id.swipe_collection);

        if (item.getCollectStatus())
            a.setText("取消收藏");
        else
            a.setText("收藏");
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new WordCollectionStatusChangedEvent(item.getWord(),item.getMeaning()));
            }
        });

            }

}
