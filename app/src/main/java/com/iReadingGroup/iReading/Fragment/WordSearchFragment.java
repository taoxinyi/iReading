package com.iReadingGroup.iReading.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.Adapter.WordInfoAdapter;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordInfo;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;


public class WordSearchFragment extends Fragment {
    public static final String BUNDLE_TITLE = "title";
    private String mTitle = "DefaultValue";
    private View v;
    private ListView infoListView;////infoListView for list of brief info of each article
    private WordInfoAdapter wordInfoAdapter;//Custom adapter for article info
    private ArrayList<WordInfo> alWordInfo = new ArrayList<>();//ArrayList linked to adapter for listview

    public static WordSearchFragment newInstance(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TITLE, title);
        WordSearchFragment fragment = new WordSearchFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != v) {
            ViewGroup parent = (ViewGroup) v.getParent();
            if (null != parent) {
                parent.removeView(v);
            }
        } else {//start initializing
            //Bundle arguments = getArguments();
            //if (arguments != null) {
            //mTitle = arguments.getString(BUNDLE_TITLE);
            // }

            v = inflater.inflate(R.layout.fragment_word_search, container, false);
            SearchView sv = v.findViewById(R.id.search_word);
            sv.setIconifiedByDefault(true);
            sv.setFocusable(true);
            sv.setIconified(false);
            sv.clearFocus();
            sv.requestFocusFromTouch();
            SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) sv.findViewById(R.id.search_src_text);
            theTextArea.setTextColor(Color.GRAY);
            theTextArea.setHintTextColor(Color.GRAY);
            sv.setQueryHint("输入需要查找的单词");

            //database load

            final  OfflineDictBeanDao daoDictionary=((MainActivity)getActivity()).getDaoDictionary();

            infoListView = (ListView) v.findViewById(R.id.list_word_search);//

            //sample of add initial articles' info.
            for (int i = 0; i < 12; i++) {
                WordInfo a_1 = new WordInfo("What a good day Today","emmmm",com.iReadingGroup.iReading.R.drawable.collect_false);
                alWordInfo.add(a_1);
                WordInfo a_2 = new WordInfo("Test1","aaaaa",com.iReadingGroup.iReading.R.drawable.collect_false);
                alWordInfo.add(a_2);
            }


            wordInfoAdapter = new WordInfoAdapter(getActivity(),
                    R.layout.listitem_word_info, alWordInfo);//link the arrayList to adapter,using custom layout for each item
            infoListView.setAdapter(wordInfoAdapter);//link the adapter to ListView
            //searchview's listener
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {search(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    search(query);
                    return true;
                }

                public void search(String query) {
                    if (query.length()==0)//if search blank
                    {
                        alWordInfo.clear();
                        wordInfoAdapter.notifyDataSetChanged();
                    }
                    else{//if search not blank
                        List<OfflineDictBean> joes = daoDictionary.queryBuilder()
                                .where(OfflineDictBeanDao.Properties.Word.like(query+"%"))
                                .list();
                        if (joes.size()==0)
                        {
                            alWordInfo.clear();
                            wordInfoAdapter.notifyDataSetChanged();
                        }
                        else{
                            alWordInfo.clear();
                            Log.i("textchange", query);
                            for (int i=0;i<min(joes.size(),20);i++)
                            {
                                WordInfo lin=new WordInfo(joes.get(i).getWord(),joes.get(i).getMeaning(), com.iReadingGroup.iReading.R.drawable.collect_false);
                                alWordInfo.add(lin);
                            }

                            //sync to the listView
                            wordInfoAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            );

        }

        return v;
    }

}
