package com.iReadingGroup.iReading.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.Activity.WordDetailActivity;
import com.iReadingGroup.iReading.Adapter.WordInfoAdapter;
import com.iReadingGroup.iReading.Bean.OfflineDictBean;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.Event.ChangeWordCollectionDBEvent;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;
import com.iReadingGroup.iReading.Function;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;


/**
 * The type Word search fragment.
 */
public class WordSearchFragment extends Fragment {
    /**
     * The constant BUNDLE_TITLE.
     */
    private View v;
    private WordInfoAdapter wordInfoAdapter;//Custom adapter for article info
    private ArrayList<WordInfo> alWordInfo = new ArrayList<>();//ArrayList linked to adapter for listview
    private WordCollectionBeanDao daoCollection;
    private OfflineDictBeanDao daoDictionary;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != v) {
            ViewGroup parent = (ViewGroup) v.getParent();
            if (null != parent) {
                parent.removeView(v);
            }
        } else {//start initializing

            v = inflater.inflate(R.layout.fragment_word_search, container, false);
            final SearchView sv = v.findViewById(R.id.search_word);
            sv.setIconifiedByDefault(true);
            sv.setIconified(false);

            SearchView.SearchAutoComplete theTextArea = sv.findViewById(R.id.search_src_text);
            theTextArea.setTextColor(Color.GRAY);
            theTextArea.setHintTextColor(Color.GRAY);
            sv.setQueryHint("输入需要查找的单词");

            //database load

            daoDictionary = ((MainActivity) getActivity()).getDaoDictionary();
            daoCollection = ((MainActivity) getActivity()).getDaoCollection();
            RecyclerView infoListView = v.findViewById(R.id.list_word_search);//
            List<OfflineDictBean> l = daoDictionary.queryBuilder().limit(20).list();
            //sample of add initial articles' info.
            for (OfflineDictBean word : l) {
                WordInfo a = new WordInfo(word.getWord(), word.getMeaning(), getCollectionIcon(word.getWord()),
                        true, Function.getWordCollectionStatus(daoCollection, word.getWord()));
                alWordInfo.add(a);
            }


            wordInfoAdapter = new WordInfoAdapter(getActivity(),
                    R.layout.listitem_word_info, alWordInfo);//link the arrayList to adapter,using custom layout for each item
            infoListView.setAdapter(wordInfoAdapter);//link the adapter to ListView
            LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            infoListView.setLayoutManager(llm);
            infoListView.addOnItemTouchListener(new OnItemClickListener() {
                @Override
                public void onSimpleItemClick(BaseQuickAdapter parent, View view, int position) {
                    sv.clearFocus();
                    WordInfo h = alWordInfo.get(position);
                    String current_word = h.getWord();
                    goToWordDetail(current_word);
                }
            });
            infoListView.addOnItemTouchListener(new OnItemChildClickListener() {
                @Override
                public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    WordInfo item = alWordInfo.get(position);
                    if (item.getCollectStatus()) {
                        EventBus.getDefault().post(new ChangeWordCollectionDBEvent(item.getWord(), item.getMeaning(), "remove"));
                    } else {
                        EventBus.getDefault().post(new ChangeWordCollectionDBEvent(item.getWord(), item.getMeaning(), "add"));
                    }
                }
            });
            //searchview's listener
            sv.setOnQueryTextListener
                    (new SearchView.OnQueryTextListener() {
                         @Override
                         public boolean onQueryTextSubmit(String query) {
                             goToWordDetail(query);
                             sv.clearFocus();
                             return true;
                         }

                         @Override
                         public boolean onQueryTextChange(String query) {
                             search(query);
                             return true;
                         }

                         public void search(String query) {
                             if (query.length() == 0)//if search blank
                             {
                                 alWordInfo.clear();
                                 List<OfflineDictBean> l = daoDictionary.queryBuilder().limit(20).list();
                                 //sample of add initial articles' info.
                                 for (OfflineDictBean word : l) {
                                     WordInfo a = new WordInfo(word.getWord(), word.getMeaning(),
                                             getCollectionIcon(word.getWord()), true,
                                             Function.getWordCollectionStatus(daoCollection, word.getWord()));
                                     alWordInfo.add(a);
                                 }
                                 wordInfoAdapter.notifyDataSetChanged();
                             } else {//if search not blank
                                 List<OfflineDictBean> l = daoDictionary.queryBuilder()
                                         .where(OfflineDictBeanDao.Properties.Word.like(query + "%"))
                                         .list();
                                 if (l.size() == 0) {
                                     alWordInfo.clear();
                                     wordInfoAdapter.notifyDataSetChanged();
                                 } else {
                                     alWordInfo.clear();
                                     for (int i = 0; i < min(l.size(), 20); i++) {
                                         WordInfo lin = new WordInfo(l.get(i).getWord(), l.get(i).getMeaning(),
                                                 getCollectionIcon(l.get(i).getWord()), true,
                                                 Function.getWordCollectionStatus(daoCollection, l.get(i).getWord()));
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

    private int getCollectionIcon(String word) {
        if (!Function.getWordCollectionStatus(daoCollection, word))
            return R.drawable.collect_false;
        else return R.drawable.collect_true;
    }


    private void goToWordDetail(String word) {

        Intent intent = new Intent(getActivity(), WordDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("word", word);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    /**
     * Word dataset changed event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void WordDatasetChangedEvent(WordDatasetChangedEvent event) {
        String word = event.word;
        String meaning = event.meaning;
        String operation = event.operation;
        for (int i = 0; i < alWordInfo.size(); i++) {
            if (alWordInfo.get(i).getWord().equals(word)) {
                if (operation.equals("remove")) {
                    alWordInfo.get(i).setCollectStatus(false);
                    alWordInfo.get(i).setImageId(R.drawable.collect_false);
                } else {
                    alWordInfo.get(i).setCollectStatus(true);
                    alWordInfo.get(i).setImageId(R.drawable.collect_true);
                }
                wordInfoAdapter.notifyItemChanged(i, "partial");
                break;
            }
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }


    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
