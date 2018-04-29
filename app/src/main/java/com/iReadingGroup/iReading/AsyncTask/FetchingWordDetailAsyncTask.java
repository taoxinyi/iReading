package com.iReadingGroup.iReading.AsyncTask;

/**
 * Created by taota on 2018/4/9.
 */

import android.util.Xml;

import com.iReadingGroup.iReading.WordDetail;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by taota on 2018/3/22.
 */
public class FetchingWordDetailAsyncTask extends BaseAsyncTask {
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


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        WordDetail wordDetail = new WordDetail();
        if (result == null || result.equals("Timeout")) delegate.processFinish(wordDetail);
        else {
            String ps = "";
            String pos = "";
            String orig = "";
            XmlPullParser parser = Xml.newPullParser();
            try {
                parser.setInput(new StringReader(result));
                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    switch (event) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            if ("key".equals(parser.getName())) {
                                wordDetail.setWord(parser.nextText().replaceAll("\n", ""));
                            } else if ("ps".equals(parser.getName())) {
                                ps = parser.nextText().replaceAll("\n", "");
                            } else if ("pron".equals(parser.getName())) {
                                wordDetail.addPron(ps, parser.nextText().replaceAll("\n", ""));
                            } else if ("pos".equals(parser.getName())) {
                                pos = parser.nextText().replaceAll("\n", "");
                            } else if ("acceptation".equals(parser.getName())) {
                                //get rid of last ';'
                                String s = parser.nextText().replaceAll("\n", "");
                                s = s.substring(0, s.length() - 1);
                                wordDetail.addMeaning(pos, s);
                            } else if ("orig".equals(parser.getName())) {
                                orig = parser.nextText().replaceAll("\n", "");
                            } else if ("trans".equals(parser.getName())) {
                                wordDetail.addSent(orig, parser.nextText().replaceAll("\n", ""));
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            break;
                    }

                    event = parser.next();

                }
                delegate.processFinish(wordDetail);


            } catch (XmlPullParserException a) {

            } catch (IOException a) {

            }
        }
    }
}