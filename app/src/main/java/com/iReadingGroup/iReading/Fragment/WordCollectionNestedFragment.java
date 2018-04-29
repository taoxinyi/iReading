package com.iReadingGroup.iReading.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.iReadingGroup.iReading.Activity.MainActivity;
import com.iReadingGroup.iReading.Activity.WordDetailActivity;
import com.iReadingGroup.iReading.Adapter.WordInfoAdapter;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBean;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;
import com.iReadingGroup.iReading.Event.ButtonCheckEvent;
import com.iReadingGroup.iReading.Event.WordDatasetChangedEvent;
import com.iReadingGroup.iReading.R;
import com.iReadingGroup.iReading.WordInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by taota on 2018/4/2.
 */
public class WordCollectionNestedFragment extends Fragment {
    /**
     * The constant BUNDLE_TITLE.
     */
    public static final String BUNDLE_TITLE = "title";
    private String mTitle = "DefaultValue";
    private View v;
    private RecyclerView infoListView;////infoListView for list of brief info of each article
    private WordInfoAdapter wordInfoAdapter;//Custom adapter for article info
    private ArrayList<WordInfo> alWordInfo = new ArrayList<>();//ArrayList linked to adapter for listview
    private WordCollectionBeanDao daoCollection;
    private OfflineDictBeanDao daoDictionary;
    private LinearLayoutManager llm;

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

            v = inflater.inflate(R.layout.fragment_word_collection, container, false);


            //database load

            daoDictionary = ((MainActivity) getActivity()).getDaoDictionary();

            infoListView = v.findViewById(R.id.list_word_collected);//

            //sample of add initial articles' info.


            wordInfoAdapter = new WordInfoAdapter(getActivity(),
                    R.layout.listitem_word_info, alWordInfo);//link the arrayList to adapter,using custom layout for each item
            infoListView.setAdapter(wordInfoAdapter);//link the adapter to ListView

            llm = new LinearLayoutManager(getContext(), LinearLayout.VERTICAL,false){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            infoListView.setLayoutManager(llm);
            //llm.setOrientation(LinearLayoutManager.VERTICAL);
            //nfoListView.setLayoutManager(llm);

            daoCollection = ((MainActivity) getActivity()).getDaoCollection();
            List<WordCollectionBean> wordlist = daoCollection.loadAll();
            for (WordCollectionBean word : wordlist) {
                alWordInfo.add(0, new WordInfo(word.getWord(), word.getMeaning(), R.drawable.collect_true, false, true));
            }

            wordInfoAdapter.notifyDataSetChanged();
            infoListView.addOnItemTouchListener(new OnItemClickListener() {
                @Override
                public void onSimpleItemClick(BaseQuickAdapter parent, View view, int position) {
                    WordInfo h = alWordInfo.get(position);

                    String current_word = h.getWord();
                    String meaning = h.getRealMeaning();
                    Intent intent = new Intent(getActivity(), WordDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("word", current_word);
                    bundle.putString("meaning", meaning);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    //FruitList.this.finish();
                }
            });



        }

        return v;
    }


    /**
     * On button check event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonCheckEvent(ButtonCheckEvent event) {
        ((MainActivity) getActivity()).buttonStatus = event.message;
        for (WordInfo w : alWordInfo) {
            w.setShowingMeaning(event.message);
        }
        wordInfoAdapter.notifyDataSetChanged();


    }

    /**
     * On word dataset changed event.
     *
     * @param event the event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWordDatasetChangedEvent(WordDatasetChangedEvent event) {
        String word = event.word;
        String meaning = event.meaning;
        String operation = event.operation;
        int index = -1;
        for (int i = 0; i < alWordInfo.size(); i++) {
            if (alWordInfo.get(i).getWord().equals(word)) {
                index = i;
                break;
            }
        }
        if (operation.equals("remove") && index != -1) {//not in dataset but in collection list
            alWordInfo.remove(index);
            wordInfoAdapter.notifyItemRemoved(index);
        } else if (operation.equals("add") && index == -1) {//in the dataset but not in the list
            alWordInfo.add(0, new WordInfo(word, meaning, R.drawable.collect_true, ((MainActivity) getActivity()).buttonStatus, true));
            wordInfoAdapter.notifyItemInserted(0);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

}
