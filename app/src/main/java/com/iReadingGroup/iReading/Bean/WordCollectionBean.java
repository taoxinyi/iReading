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
    @Property(nameInDb = "meaning")
    private String meaning;

    @Generated(hash = 1032722198)
    public WordCollectionBean(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
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


    public String getMeaning() {
        return this.meaning;
    }


    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }



}
