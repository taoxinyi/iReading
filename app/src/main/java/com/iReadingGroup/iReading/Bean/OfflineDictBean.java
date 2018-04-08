package com.iReadingGroup.iReading.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;

/**
 * OfflineDictBean
 *
 * Entity Class of offline dictionary
 * item instance of database
 * including:id,word,meaning,sentence
 */
@Entity(nameInDb = "WordInfo",createInDb = false)
public class OfflineDictBean {
    //Loading offline dictionary and Keep instance into this class

    @Property(nameInDb = "id")
    @Id(autoincrement = true)//key = id
    private long id;
    @Property(nameInDb = "word")
    private String word;
    @Property(nameInDb = "meaning")
    private String meaning;
    @Property (nameInDb = "sentence")
    private String sentence;
    
    @Generated(hash = 1101762644)
    public OfflineDictBean(long id, String word, String meaning, String sentence) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.sentence = sentence;
    }


    @Generated(hash = 224238106)
    public OfflineDictBean() {
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(long id) {
        this.id = id;
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

    /**
     * Gets meaning.
     *
     * @return the meaning
     */
    public String getMeaning() {
        return this.meaning;
    }

    /**
     * Sets meaning.
     *
     * @param meaning the meaning
     */
    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    /**
     * Gets sentence.
     *
     * @return the sentence
     */
    public String getSentence() {
        return this.sentence;
    }

    /**
     * Sets sentence.
     *
     * @param sentence the sentence
     */
    public void setSentence(String sentence) {
        this.sentence = sentence;
    }


}