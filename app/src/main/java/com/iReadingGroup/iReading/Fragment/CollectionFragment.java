package com.iReadingGroup.iReading.Fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.R;

import cn.bingoogolapple.badgeview.BGABadgeTextView;


/**
 * CollectionFragment
 * father fragment of Article/Word Collection Nested Fragment
 */
public class CollectionFragment extends Fragment {

    private View view;
    String[] mTitle = new String[20];
    String[] mData = new String[20];
    TabLayout mTabLayout;
    ViewPager mViewPager;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Must add in every fragments' onCreateView to avoid duplicate creating.
        if (null != view) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (null != parent) {
                parent.removeView(view);
            }
        } else {
            //start initializing
            view = inflater.inflate(R.layout.fragment_collection, container, false);//set layout
            initView();
            setHasOptionsMenu(true);
        }
        return view;
    }


    private void initView() {
        mTabLayout = (TabLayout) view.findViewById(R.id.tl_tab);
        mViewPager = (ViewPager) view.findViewById(R.id.vp_pager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            //此方法用来显示tab上的名字
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "单词";
                    case 1:
                        return "文章";
                    default:
                        return "单词";
                }
            }

            @Override
            public Fragment getItem(int position) {
                //创建Fragment并返回
                switch (position) {
                    case 0: {
                        WordCollectionNestedFragment wfragment = new WordCollectionNestedFragment();
                        return wfragment;
                    }
                    case 1: {
                        ArticleCollectionNestedFragment afragment = new ArticleCollectionNestedFragment();
                        return afragment;

                    }
                    default: {
                        WordCollectionNestedFragment wfragment = new WordCollectionNestedFragment();
                        return wfragment;
                    }
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                //if the page is selected change the title to corresponding category.
                switch (position) {
                    case 0:
                        mViewPager.setCurrentItem(0);
                        hideTabBadge(0);
                        ((MainActivity)getActivity()).button.setVisible(true);
                        ((MainActivity)getActivity()).last_nested_page=0;
                        break;
                    case 1:
                        mViewPager.setCurrentItem(1);
                        hideTabBadge(1);
                        ((MainActivity)getActivity()).button.setVisible(false);
                        ((MainActivity)getActivity()).last_nested_page=1;
                        break;

                }
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
        TabLayout.Tab tab = mTabLayout.getTabAt(0);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.badged_tab, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText("单词");
        tab.setCustomView(view);
        tab = mTabLayout.getTabAt(1);
        view = LayoutInflater.from(getContext()).inflate(R.layout.badged_tab, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText("阅读");
        tab.setCustomView(view);
    }



    private void hideTabBadge(int position) {
        TabLayout.Tab tab = mTabLayout.getTabAt(position);
        if (tab != null) {
            View view = tab.getCustomView();
            ((BGABadgeTextView) view.findViewById(R.id.tv_title)).hiddenBadge();

        }
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

    }





}



