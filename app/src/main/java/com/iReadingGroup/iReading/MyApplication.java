package com.iReadingGroup.iReading;

import android.app.Application;

import com.iReadingGroup.iReading.Bean.ArticleEntityDao;
import com.iReadingGroup.iReading.Bean.OfflineDictBeanDao;
import com.iReadingGroup.iReading.Bean.WordCollectionBeanDao;

public class MyApplication extends Application {
    private ArticleEntityDao daoArticle;
    private OfflineDictBeanDao daoDicitionary;
    private WordCollectionBeanDao daoCollection;
    public ArticleEntityDao getDaoArticle() {return daoArticle;}
    public OfflineDictBeanDao getDaoDicitionary() {return daoDicitionary;}
    public WordCollectionBeanDao getDaoCollection() {return daoCollection;}
    public void setDaoArticle(ArticleEntityDao daoArticle) {this.daoArticle=daoArticle;}
    public void setDaoDicitionary(OfflineDictBeanDao daoDicitionary) {this.daoDicitionary=daoDicitionary;}
    public void setDaoCollection(WordCollectionBeanDao daoCollection) {this.daoCollection=daoCollection;}
}