package com.iReadingGroup.iReading.Event;

/**
 * Created by taota on 2018/4/6.
 */

public class WordDatasetChangedEvent  {
    /**
     * The Count.
     */
    public final String word;
    public final String meaning;

    /**
     * Instantiates a new Collect word event.
     *
     * @param word the word
     *
     *
     */
    public WordDatasetChangedEvent(String word,String meaning) {
        this.word = word;
        this.meaning=meaning;
    }
}