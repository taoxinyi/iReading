package com.iReadingGroup.iReading.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * The type Word collection bean.
 */
@Entity
public class WordCollectionBean {
    @Property(nameInDb = "word")
    @Id
    private String word;


    @Generated(hash = 509351561)
    public WordCollectionBean(String word) {
        this.word = word;
    }


    @Generated(hash = 972460215)
    public WordCollectionBean() {
    }

    /**
     * Gets word.
     *
     * @return the word
     */
    public String getWord() {
        return this.word;
    }

    /**
     * Sets word.
     *
     * @param word the word
     */
    public void setWord(String word) {
        this.word = word;
    }



}
