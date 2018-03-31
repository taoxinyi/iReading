package com.iReadingGroup.iReading.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

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

    public String getWord() {
        return this.word;
    }

    public void setWord(String word) {
        this.word = word;
    }



}
