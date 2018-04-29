package com.iReadingGroup.iReading.Adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.iReadingGroup.iReading.Event.ChangeWordCollectionDBEvent;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * WordInfoAdapter
 * This Adapter is a bridge between actual ArrayList and RecycleView(ListView)
 * Set Text and Image to Class:WordInfo
 */

/**
 * ArticleInfoAdapter
 * This Adapter is a bridge between actual ArrayList and RecycleView(ListView)
 * Set Text and Image to Class:ArticleInfo
 */
public class WordInfoAdapter extends BaseQuickAdapter<WordInfo, BaseViewHolder> {
    /**
     * Instantiates a new Word info adapter.
     *
     * @param context     the context
     * @param layoutResId the layout res id
     * @param data        the data
     */
    public WordInfoAdapter(Context context, int layoutResId, List<WordInfo> data) {
        super(layoutResId, data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final WordInfo item, List<Object> payloads) {
        String command = payloads.get(0).toString();
        if (payloads.isEmpty()) {
            convert(helper, item);
        } else if (command.equals("show")) {
            helper.setText(R.id.meaning_word_info, item.getMeaning());

        } else if (command.equals("hide")) {
            helper.setText(R.id.meaning_word_info, "");

        } else {
            helper.setImageResource(R.id.img_word_info, item.getImageId());

        }
    }

    @Override
    protected void convert(final BaseViewHolder helper, final WordInfo item) {
        helper.setText(R.id.word_word_info, item.getWord());
        helper.setText(R.id.meaning_word_info, item.getMeaning());
        helper.setImageResource(R.id.img_word_info, item.getImageId());

        final Button a = helper.getView(R.id.swipe_collection);

        if (item.getCollectStatus())
            a.setText("取消收藏");
        else
            a.setText("收藏");
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getCollectStatus()) {
                    a.setText("收藏");
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(item.getWord(), item.getMeaning(), "remove"));
                } else {
                    a.setText("取消收藏");
                    EventBus.getDefault().post(new ChangeWordCollectionDBEvent(item.getWord(), item.getMeaning(), "add"));
                }
            }
        });

    }

}
