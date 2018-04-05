package com.iReadingGroup.iReading.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



import java.util.List;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordInfo;


/**
 * WordInfoAdapter
 * This Adapter is a bridge between actual ArrayList and RecycleView(ListView)
 * Set Text and Image to Class:WordInfo
 */
public class WordInfoAdapter extends ArrayAdapter<WordInfo> {

    private int resourceId;

    /**
     * Instantiates a new Word info adapter.
     *
     * @param context  the context
     * @param resource the resource
     * @param objects  the objects
     */
    public WordInfoAdapter (Context context, int resource, List<WordInfo> objects) {
        super(context, resource, objects);
        resourceId=resource;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        WordInfo wordInfoItem=getItem(position); //获得当前项的wordInfoItem数据
        View view;
        ViewHolder viewHolder; //使用ViewHolder优化 ListView
        if (convertView==null){ //使用convertView重复使用查找加载好的布局
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);//使用布局填充器为子项加载我们传入的子布局「wordInfoItem_item」
            viewHolder=new ViewHolder();
            viewHolder.wordInfoItemImage= (ImageView) view.findViewById(R.id.img_word_info);//查找
            viewHolder.wordInfoItemWord= (TextView) view.findViewById(R.id.word_word_info);
            viewHolder.wordInfoItemMeaning= (TextView) view.findViewById(R.id.meaning_word_info);
            view.setTag(viewHolder);//把ViewHolder储存在View里面

        }else {
            view=convertView;
            viewHolder= (ViewHolder) view.getTag();
        }
        viewHolder.wordInfoItemImage.setImageResource(wordInfoItem.getImageId()); //设置数据
        viewHolder.wordInfoItemWord.setText(wordInfoItem.getWord());
        viewHolder.wordInfoItemMeaning.setText(wordInfoItem.getMeaning());

        return view;
    }

    /**
     * The type View holder.
     */
    class ViewHolder{
        /**
         * The Word info item image.
         */
        ImageView wordInfoItemImage;
        /**
         * The Word info item word.
         */
        TextView wordInfoItemWord;
        /**
         * The Word info item meaning.
         */
        TextView wordInfoItemMeaning;
    }
}