package com.iReadingGroup.iReading.AsyncTask;

import com.iReadingGroup.iReading.WordDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by taota on 2018/3/22.
 */
public class FetchingBriefMeaningAsyncTask extends BaseAsyncTask {
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


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {   //parse word from json
            //sample link.:http://dict-co.iciba.com/api/dictionary.php?w=go&key=341DEFE6E5CA504E62A567082590D0BD&type=json
            if (result == null || result.equals("Timeout")) delegate.processFinish(null);
            else {
                WordDetail word = new WordDetail();
                JSONObject reader = new JSONObject(result);
                JSONArray symbols = reader.getJSONArray("symbols");
                JSONObject symbols_0 = symbols.getJSONObject(0);
                //add voice_url

                String ph_am = symbols_0.getString("ph_am");
                String ph_am_mp3 = symbols_0.getString("ph_am_mp3");
                if (!ph_am.equals("") && !ph_am.equals(""))//if voice not null
                    word.addPron(ph_am, ph_am_mp3);
                else {
                    String ph_en = symbols_0.getString("ph_en");
                    String ph_en_mp3 = symbols_0.getString("ph_en_mp3");
                    if (!ph_en.equals("") && !ph_en.equals(""))//if voice not null
                        word.addPron(ph_en, ph_en_mp3);
                }

                JSONArray parts = symbols_0.getJSONArray("parts");
                String meaning;
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
                    word.addMeaning(part, meaning);

                }
                //set word
                String word_name = reader.getString("word_name");
                word.setWord(word_name);

                //return word_name+part+meaning
                delegate.processFinish(word);
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }


    }
}