package com.lzy.alphaindicatorview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by taota on 2018/3/23.
 */

public class MyListFragment extends Fragment {

    private ListView listView;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view= inflater.inflate(R.layout.list_fragment, container, false);
        listView = (ListView)view.findViewById(R.id.list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,getData());
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String number = (String) listView.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(),DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", number);
                Log.d("finish", number);
                intent.putExtras(bundle);
                startActivity(intent);
                //FruitList.this.finish();
            }
        });

        return view;
    }

    private List<String> getData(){
        List<String> data = new ArrayList<String>();
        for(int i = 0;i <20;i++) {
            data.add(i+"");
        }
        return data;
    }
}