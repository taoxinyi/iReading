package com.iReadingGroup.iReading.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.ArticleInfo;
import com.iReadingGroup.iReading.R;

import java.util.List;



public class ArticleInfoAdapter extends ArrayAdapter<ArticleInfo> {

    private int resourceId;
    private Context mContext;

    public ArticleInfoAdapter (Context context, int resource, List<ArticleInfo> objects) {
        super(context, resource, objects);
        resourceId=resource;
        mContext=context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ArticleInfo wordInfoItem=getItem(position); //获得当前项的wordInfoItem数据
        View view;
        ViewHolder viewHolder; //使用ViewHolder优化 ListView
        if (convertView==null){ //使用convertView重复使用查找加载好的布局
            view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);//使用布局填充器为子项加载我们传入的子布局「wordInfoItem_item」
            viewHolder=new ViewHolder();
            viewHolder.wordInfoItemImage= (ImageView) view.findViewById(com.iReadingGroup.iReading.R.id.img);//查找
            viewHolder.wordInfoItemText= (TextView) view.findViewById(R.id.txt_title);
            viewHolder.wordInfoItemSource=(TextView)view.findViewById(R.id.txt_source);
            viewHolder.WordInfoItemTime = (TextView)view.findViewById(R.id.txt_time);
            view.setTag(viewHolder);//把ViewHolder储存在View里面

        }else {
            view=convertView;
            viewHolder= (ViewHolder) view.getTag();
        }
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(R.drawable.collect_false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(((MainActivity)mContext).getApplicationContext()).load(wordInfoItem.getImageUrl()).apply(options).into(viewHolder.wordInfoItemImage);
        //iewHolder.wordInfoItemImage.setImageResource(wordInfoItem.getImageId()); //设置数据
        viewHolder.wordInfoItemText.setText(wordInfoItem.getName());
        viewHolder.wordInfoItemSource.setText(wordInfoItem.getSource());
        viewHolder.WordInfoItemTime.setText(wordInfoItem.getTime());
        return view;

    }
    class ViewHolder{
        ImageView wordInfoItemImage;
        TextView wordInfoItemText;
        TextView wordInfoItemSource;
        TextView WordInfoItemTime;
    }
}