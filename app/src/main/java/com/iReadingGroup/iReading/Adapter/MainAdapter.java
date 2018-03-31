package com.iReadingGroup.iReading.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.iReadingGroup.iReading.Fragment.SettingsFragment;
import com.iReadingGroup.iReading.Fragment.WordSearchFragment;
import com.iReadingGroup.iReading.Fragment.ArticleListFragment;
import com.iReadingGroup.iReading.Fragment.CollectionFragment;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends FragmentPagerAdapter {

    public List<Fragment> fragments = new ArrayList<>();
    private String[] titles = {//
            "第一页\n\n",//
            "第二页\n\n查询单词，类似于一般查词软件",//
            "第三页\n\n收藏，显示收藏的文章和单词用于学习", //
            "第四页\n\n用户信息和设置"};

    public MainAdapter(FragmentManager fm) {
        super(fm);
        fragments.add(new ArticleListFragment());
        fragments.add(new WordSearchFragment());
        fragments.add(CollectionFragment.newInstance(titles[2]));
        fragments.add(SettingsFragment.newInstance(titles[3]));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}