package com.iReadingGroup.iReading;

/**
 * Created by taota on 2018/3/29.
 */

import android.os.AsyncTask;
import android.util.Log;

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
import java.util.Iterator;


/**
 * Created by taota on 2018/3/22.
 */

public class FetchNewsAsyncTask extends AsyncTask<String, String, String> {
    public AsyncResponse delegate = null;
    public FetchNewsAsyncTask (AsyncResponse asyncResponse) {
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
        {   //parse word from json
            //sample link.:http://dict-co.iciba.com/api/dictionary.php?w=go&key=341DEFE6E5CA504E62A567082590D0BD&type=json
            JSONObject reader = new JSONObject(result);
            Iterator<String> keys = reader.keys();
            String key = (String)keys.next(); // First key in your json object
            JSONObject detail=reader.getJSONObject(key);
            JSONObject info=detail.getJSONObject("info");
            String body=info.getString("body");
            String imageUrl=info.getString("image");
            JSONArray categories=info.getJSONArray("categories");
            String a=categories.getString(0);
            String cataString="";
            for (int i=0;i<categories.length();i++)
            {
                JSONObject each=categories.getJSONObject(i);
                String c=each.getString("label");
                String[] buff = c.split("/");
                String final_c=buff[buff.length-1];
                cataString+=final_c+"; ";

            }
            delegate.processFinish(imageUrl+"\r\n\r\n"+cataString.substring(0,cataString.length()-2)+"\r\n\r\n\n"+body);
        }catch(JSONException e)
        {

        }


    }
}
