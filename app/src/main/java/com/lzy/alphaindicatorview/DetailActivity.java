package com.lzy.alphaindicatorview;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
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
import java.text.BreakIterator;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private Context mContext = null;
    private String meaning_result="Loading";
    private boolean flag_finished=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);
        mContext = this;



        init();
    }
    private void init() {
        String definition = "For much of the past week, Facebook has been embroiled in a controversy involving Cambridge Analytica, a political consulting firm with ties to Donald J. Trump’s 2016 presidential campaign, and how the firm improperly obtained and exploited personal data from 50 million Facebook users.\n" +
                "\n" +
                "On Wednesday, following widespread questions about his whereabouts, Mark Zuckerberg, the chief executive of Facebook, spoke with two New York Times reporters, Sheera Frenkel and Kevin Roose, about the controversy and the steps he was taking to make the social network less prone to abuse.\n" +
                "\n" +
                "Below is a transcript of the conversation, edited for length and clarity.\n" +
                "\n" +
                "Sheera Frenkel: Did it come as a surprise to you, the user response to the news that Cambridge Analytica had accessed this trove of data?\n" +
                "\n" +
                "Mark Zuckerberg: Privacy issues have always been incredibly important to people. One of our biggest responsibilities is to protect data. If you think about what our services are, at their most basic level, you put some content into a service, whether it’s a photo or a video or a text message — whether it’s Facebook or WhatsApp or Instagram — and you’re trusting that that content is going to be shared with the people you want to share it with. Whenever there’s an issue where someone’s data gets passed to someone who the rules of the system shouldn’t have allowed it to, that’s rightfully a big issue and deserves to be a big uproar.".trim();
        TextView definitionView = (TextView) findViewById(R.id.text);
        definitionView.setMovementMethod(LinkMovementMethod.getInstance());
        definitionView.setText(definition, TextView.BufferType.SPANNABLE);
        definitionView.setHighlightColor(Color.GRAY);
        //definitionView.setTextIsSelectable(true);
        final Spannable spans = (Spannable) definitionView.getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(definition);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = definition.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                ClickableSpan clickSpan = getClickableSpan(possibleWord,start,end,spans);
                spans.setSpan(clickSpan, start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        }

    }

    private ClickableSpan getClickableSpan(final String word,final int start,final int end,final Spannable spans) {
        return new ClickableSpan() {
            final String mWord;
            {
                mWord = word;
            }

            @Override
            public void onClick(final View widget) {

                MyAsyncTask asyncTask = new MyAsyncTask(new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        Log.d("finish", "finsh");

                        meaning_result = (String) output;
                        showPopupWindow(widget);
                    }
                });
                asyncTask.execute("https://dict-co.iciba.com/api/dictionary.php?key=341DEFE6E5CA504E62A567082590D0BD&type=json&w=" + word.toLowerCase());
                spans.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
                        start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                Log.d("tapped on:", mWord);
                //Toast.makeText(widget.getContext(), mWord, Toast.LENGTH_SHORT)
                //.show();

            }

            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
    }

    private void showPopupWindow(View view) {
// 一个自定义的布局，作为显示的内容

        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.popup_layout, null);
        // 设置按钮的点击事件


        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.i("mengdd", "onTouch : ");

                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        ((TextView)popupWindow.getContentView().findViewById(R.id.meaning)).setText(meaning_result);
        ImageButton button = (ImageButton) contentView.findViewById(R.id.collect);
        button.setOnClickListener(new View.OnClickListener() {
            private boolean flag=false;
            @Override
            public void onClick(View v) {
                ImageButton button = (ImageButton) v;
                if (flag) {
                    flag = false;
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_false));
                } else {
                    flag = true;
                    button.setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.collect_true));

                }
            }
        });

        //int location[] = new int[2];
        //view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.BOTTOM,
                0, 0);



    }

}
