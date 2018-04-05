package com.iReadingGroup.iReading;

/**
 * Created by taota on 2018/4/4.
 */

public class ArticleSearchEvent {

    /**
     * The Message.
     */
    public final String keyword;

    /**
     * Instantiates a new Message event.
     *
     * @param keyword the message
     */
    public ArticleSearchEvent(String keyword) {
        this.keyword = keyword;
    }
}
