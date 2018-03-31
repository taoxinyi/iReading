package com.iReadingGroup.iReading;

/**
 * Created by taota on 2018/3/29.
 */

public class WordInfo {
    private String word;
    private String meaning;
    private int imageId;

    public WordInfo(String word, String meaning, int imageId) {
        this.word = word;//word
        this.meaning=meaning;//meaning
        this.imageId = imageId;//image
    }

    public void setWord(String word) {
        this.word = word;
    }
    public void setMeaning(String meaning){this.meaning=meaning;}
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public String getWord() {
        return word;
    }
    public String getMeaning(){return  meaning;}
    public int getImageId() {
        return imageId;
    }
}