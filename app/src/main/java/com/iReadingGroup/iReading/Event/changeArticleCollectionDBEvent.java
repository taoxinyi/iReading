package com.iReadingGroup.iReading.Event;

/**
 * Created by taota on 2018/4/5.
 */

public class changeArticleCollectionDBEvent {
    /**
     * The Count.
     */
    public final String uri;
    public final String operation;

    /**
     * Instantiates a new Collect word event.
     *
     * @param uri the count
     */
    public changeArticleCollectionDBEvent(String uri,String operation) {
        this.uri = uri;
        this.operation=operation;
    }
}
