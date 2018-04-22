package com.iReadingGroup.iReading.Event;

/**
 * Created by taota on 2018/4/3.
 */

public class CollectArticleEvent {
    /**
     * The Count.
     */
    public final String uri;

    /**
     * Instantiates a new Collect word event.
     *
     * @param uri the count
     */
    public CollectArticleEvent(String uri) {
        this.uri = uri;
    }
}
