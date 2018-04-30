package com.iReadingGroup.iReading.AsyncTask;

/**
 * Created by taota on 2018/3/29.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


/**
 * Created by taota on 2018/3/22.
 */
public class FetchingArticleAsyncTask extends BaseAsyncTask {
    /**
     * The Delegate.
     */
    public AsyncResponse delegate = null;

    /**
     * Instantiates a new Fetch news async task.
     *
     * @param asyncResponse the async response
     */
    public FetchingArticleAsyncTask(AsyncResponse asyncResponse) {
        delegate = asyncResponse;//Assigning call back interfacethrough constructor
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null) delegate.processFinish(null);
        else if (result.equals("Timeout")) delegate.processFinish("Timeout");
        else {
            try {   //parse word from json
                //sample link.:http://dict-co.iciba.com/api/dictionary.php?w=go&key=341DEFE6E5CA504E62A567082590D0BD&type=json
                JSONObject reader = new JSONObject(result);
                Iterator<String> keys = reader.keys();
                String key = keys.next(); // First key in your json object
                JSONObject detail = reader.getJSONObject(key);
                JSONObject info = detail.getJSONObject("info");
                String body = info.getString("body");
                String imageUrl = info.getString("image");
                JSONArray categories = info.getJSONArray("categories");
                String a = categories.getString(0);
                String cataString = "";
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject each = categories.getJSONObject(i);
                    String c = each.getString("label");
                    String[] buff = c.split("/");
                    String final_c = buff[buff.length - 1];
                    cataString += final_c + "; ";

                }
                delegate.processFinish(imageUrl + "\r\n\r\n" + cataString.substring(0, cataString.length() - 2) + "\r\n\r\n\n" + body);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
