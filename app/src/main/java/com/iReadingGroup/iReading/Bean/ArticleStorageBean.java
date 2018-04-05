package com.iReadingGroup.iReading.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

/**
 * ArticleStorageBean
 *
 * Entity Class of article's basic info
 * item instance of database
 * including:uri,name,time,source,imageUrl,collectStatus and collectTime
 */
@Entity
public class ArticleStorageBean {
    @Property(nameInDb = "uri")
    @Id
    private String uri;
    @Property(nameInDb = "name")
    private String name;
    @Property(nameInDb = "time")
    private String time;
    @Property(nameInDb = "source")
    private String source;
    @Property(nameInDb = "imageUrl")
    private String imageUrl;
    @Property(nameInDb = "collectStatus")
    private boolean collectStatus;
    @Property(nameInDb = "collectTime")
    private Date collectTime;
    @Generated(hash = 2133979663)
    public ArticleStorageBean(String uri, String name, String time, String source,
            String imageUrl, boolean collectStatus, Date collectTime) {
        this.uri = uri;
        this.name = name;
        this.time = time;
        this.source = source;
        this.imageUrl = imageUrl;
        this.collectStatus = collectStatus;
        this.collectTime = collectTime;
    }
    @Generated(hash = 1467132437)
    public ArticleStorageBean() {
    }
    public String getUri() {
        return this.uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getSource() {
        return this.source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getImageUrl() {
        return this.imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public boolean getCollectStatus() {
        return this.collectStatus;
    }
    public void setCollectStatus(boolean collectStatus) {
        this.collectStatus = collectStatus;
    }
    public Date getCollectTime() {
        return this.collectTime;
    }
    public void setCollectTime(Date collectTime) {
        this.collectTime = collectTime;
    }
    
}