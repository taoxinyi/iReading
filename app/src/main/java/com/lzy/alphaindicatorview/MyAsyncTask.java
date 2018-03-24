package com.lzy.alphaindicatorview;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by taota on 2018/3/22.
 */

public class MyAsyncTask extends AsyncTask<String, String, String> {
    public AsyncResponse delegate = null;
    public MyAsyncTask(AsyncResponse asyncResponse) {
        delegate = asyncResponse;//Assigning call back interfacethrough constructor
    }
    protected void onPreExecute() {
        super.onPreExecute();
    }
    protected String doInBackground(String... params) {


        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try
        {
            JSONObject reader = new JSONObject(result);
            JSONArray symbols =reader.getJSONArray("symbols");
            JSONObject symbols_0=symbols.getJSONObject(0);
            JSONArray parts=symbols_0.getJSONArray("parts");
            JSONObject parts_0=parts.getJSONObject(0);
            String part=parts_0.getString("part");
            JSONArray means=parts_0.getJSONArray("means");
            String meaning="";
            for (int i=0;i<means.length();i++){
                meaning+=means.getString(i)+";";
            }
            meaning=meaning.substring(0,meaning.length()-1);


            String word_name = reader.getString("word_name");
            //TextView meaningView= (TextView) findViewById(R.id.meaning);
            //meaningView.setText(word_name+" "+part+" "+meaning);
            Log.d("Response: ", word_name+" "+part+" "+meaning);
            delegate.processFinish(word_name+"\n"+part+" "+meaning);
        }catch(JSONException e)
        {

        }


    }
}