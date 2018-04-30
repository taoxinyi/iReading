package com.iReadingGroup.iReading.Event;

/**
 * Created by taota on 2018/4/3.
 */
public class ArticleDatabaseChangedEvent {
    /**
     * The Count.
     */
    public final String uri;
    /**
     * The Operation.
     */
    public final String operation;

    /**
     * Instantiates a new Collect word event.
     *
     * @param uri       the count
     * @param operation the operation
     */
    public ArticleDatabaseChangedEvent(String uri, String operation) {
        this.uri = uri;
        this.operation = operation;
    }
}
