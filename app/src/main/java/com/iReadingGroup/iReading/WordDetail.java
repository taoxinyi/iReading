package com.iReadingGroup.iReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by taota on 2018/4/9.
 */

public class WordDetail {
    private String word;
    private List<List<String>> pron=new ArrayList<List<String>>();
    private List<List<String>>  meaning=new ArrayList<List<String>>();
    private List<List<String>>  sent=new ArrayList<List<String>>();

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<List<String>> getPron() {
        return pron;
    }

    public void addPron(String pron,String voiceUrl) {
        ArrayList<String> a=new ArrayList<String>();
        a.add(pron);
        a.add(voiceUrl);
        this.pron.add(a);
    }

    public List<List<String>> getMeaning() {
        return meaning;
    }

    public void addMeaning(String pos,String meaning) {
        ArrayList<String> a=new ArrayList<String>();
        a.add(pos);
        a.add(meaning);
        this.meaning.add(a);
    }

    public List<List<String>> getSent() {
        return sent;
    }

    public void addSent(String eng,String chn) {
        ArrayList<String> a=new ArrayList<String>();
        a.add(eng);
        a.add(chn);
        this.sent.add(a);
    }





}
