package com.iReadingGroup.iReading;

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

/**
 * Created by taota on 2018/3/22.
 */
public class FetchingBriefMeaningAsyncTask extends AsyncTask<String, String, String> {
    /**
     * The Delegate.
     */
    public AsyncResponse delegate = null;

    /**
     * Instantiates a new Fetching brief meaning async task.
     *
     * @param asyncResponse the async response
     */
    public FetchingBriefMeaningAsyncTask(AsyncResponse asyncResponse) {
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
                buffer.append(line + "\n");

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
        try {   //parse word from json
            //sample link.:http://dict-co.iciba.com/api/dictionary.php?w=go&key=341DEFE6E5CA504E62A567082590D0BD&type=json
            if (result == null) delegate.processFinish("无网络\n请先联网");
            else {
                WordDetail word=new WordDetail();
                Log.d("fetch", "onPostExecute: ");

                JSONObject reader = new JSONObject(result);
                JSONArray symbols = reader.getJSONArray("symbols");
                JSONObject symbols_0 = symbols.getJSONObject(0);
                //add pron
                String ph_am=symbols_0.getString("ph_am");
                String ph_en_mp3=symbols_0.getString("ph_am_mp3");
                word.addPron(ph_am,ph_en_mp3);

                JSONArray parts = symbols_0.getJSONArray("parts");
                String meaning ;
                for (int i = 0; i < parts.length(); i++) {
                    //for each parts
                    meaning = "";
                    JSONObject parts_0 = parts.getJSONObject(i);
                    String part = parts_0.getString("part");
                    JSONArray means = parts_0.getJSONArray("means");

                    for (int j = 0; j < means.length(); j++) {
                        meaning += means.getString(j) + " ; ";
                    }
                    meaning = meaning.substring(0, meaning.length() - 2);
                    //add meaning
                    word.addMeaning(part,meaning);

                }
                //set word
                String word_name = reader.getString("word_name");
                word.setWord(word_name);

                //return word_name+part+meaning
                Log.d("fetch", "onPostExecute: "+word.getMeaning().toString());
                delegate.processFinish(word);
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }


    }
}