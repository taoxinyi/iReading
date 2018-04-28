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

    /**
     * Gets word.
     *
     * @return the word
     */
    public String getWord() {
        return word;
    }

    /**
     * Sets word.
     *
     * @param word the word
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Gets pron.
     *
     * @return the pron
     */
    public List<List<String>> getPron() {
        return pron;
    }

    /**
     * Add pron.
     *
     * @param pron     the pron
     * @param voiceUrl the voice url
     */
    public void addPron(String pron,String voiceUrl) {
        ArrayList<String> a=new ArrayList<String>();
        a.add(pron);
        a.add(voiceUrl);
        this.pron.add(a);
    }

    /**
     * Gets meaning.
     *
     * @return the meaning
     */
    public List<List<String>> getMeaning() {
        return meaning;
    }

    /**
     * Add meaning.
     *
     * @param pos     the pos
     * @param meaning the meaning
     */
    public void addMeaning(String pos,String meaning) {
        ArrayList<String> a=new ArrayList<String>();
        a.add(pos);
        a.add(meaning);
        this.meaning.add(a);
    }

    /**
     * Gets sent.
     *
     * @return the sent
     */
    public List<List<String>> getSent() {
        return sent;
    }

    /**
     * Add sent.
     *
     * @param eng the eng
     * @param chn the chn
     */
    public void addSent(String eng,String chn) {
        ArrayList<String> a=new ArrayList<String>();
        a.add(eng);
        a.add(chn);
        this.sent.add(a);
    }





}
