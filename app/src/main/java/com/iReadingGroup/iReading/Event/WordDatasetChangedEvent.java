package com.iReadingGroup.iReading.Event;

/**
 * Created by taota on 2018/4/6.
 */
public class WordDatasetChangedEvent {
    /**
     * The Count.
     */
    public final String word;
    /**
     * The Meaning.
     */
    public final String meaning;
    /**
     * The Operation.
     */
    public final String operation;

    /**
     * Instantiates a new Collect word event.
     *
     * @param word      the word
     * @param meaning   the meaning
     * @param operation the operation
     */
    public WordDatasetChangedEvent(String word, String meaning, String operation) {
        this.word = word;
        this.meaning = meaning;
        this.operation = operation;
    }
}