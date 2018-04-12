package com.iReadingGroup.iReading;

/**
 * Created by taota on 2018/4/9.
 */

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by taota on 2018/3/22.
 */
public class FetchingWordDetailAsyncTask extends AsyncTask<String, String, String> {
    /**
     * The Delegate.
     */
    public AsyncResponse delegate = null;

    /**
     * Instantiates a new Fetching brief meaning async task.
     *
     * @param asyncResponse the async response
     */
    public FetchingWordDetailAsyncTask(AsyncResponse asyncResponse) {
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
        WordDetail wordDetail=new WordDetail();
        String ps="";
        String pos="";
        String orig="";
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(result));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("key".equals(parser.getName())){
                            wordDetail.setWord(parser.nextText().replaceAll("\n",""));
                        }
                        else if ("ps".equals(parser.getName())){
                           ps=parser.nextText().replaceAll("\n","");
                        }
                        else if ("pron".equals(parser.getName())){
                            wordDetail.addPron(ps,parser.nextText().replaceAll("\n",""));
                        }
                        else if ("pos".equals(parser.getName())){
                            pos=parser.nextText().replaceAll("\n","");
                        }
                        else if ("acceptation".equals(parser.getName())){
                            wordDetail.addMeaning(pos,parser.nextText().replaceAll("\n",""));
                        }
                        else if ("orig".equals(parser.getName())) {
                            orig=parser.nextText().replaceAll("\n","");
                        }
                        else if ("trans".equals(parser.getName())) {
                            wordDetail.addSent(orig,parser.nextText().replaceAll("\n",""));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                event = parser.next();

            }
            delegate.processFinish(wordDetail);


        }catch (XmlPullParserException a)
        {

        }catch (IOException a){

        }

    }
}