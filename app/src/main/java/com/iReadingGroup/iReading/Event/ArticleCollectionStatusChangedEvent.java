package com.iReadingGroup.iReading.Event;

/**
 * Created by taota on 2018/4/5.
 */

public class ArticleCollectionStatusChangedEvent {
    /**
     * The Count.
     */
    public final String uri;

    /**
     * Instantiates a new Collect word event.
     *
     * @param uri the count
     */
    public ArticleCollectionStatusChangedEvent(String uri) {
        this.uri = uri;
    }
}
