package com.iReadingGroup.iReading.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iReadingGroup.iReading.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * The type Collection fragment.
 */
public class CollectionFragment extends Fragment {
    /**
     * The constant BUNDLE_TITLE.
     */
    public static final String BUNDLE_TITLE = "title";
    private String mTitle = "DefaultValue";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTitle = arguments.getString(BUNDLE_TITLE);
        }

        TextView tv = new TextView(getActivity());
        tv.setText(mTitle);
        tv.setTextSize(30);
        tv.setPadding(50, 50, 50, 50);
        tv.setGravity(Gravity.CENTER);


        return tv;
    }

    /**
     * On message event.
     *
     * @param event the event
     */
// This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEvent(MessageEvent event) {
        Log.d("eventbus", event.message);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("eventbusBBBBBBBBBB", "START");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        Log.d("eventbusBBBBBBBBBB", "STOP");
        super.onStop();
    }

    /**
     * New instance collection fragment.
     *
     * @param title the title
     * @return the collection fragment
     */
    public static CollectionFragment newInstance(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_TITLE, title);
        CollectionFragment fragment = new CollectionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

}
