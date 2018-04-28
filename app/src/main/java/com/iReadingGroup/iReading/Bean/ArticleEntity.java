package com.iReadingGroup.iReading.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

@Entity
public class ArticleEntity {
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


    @Generated(hash = 2131867082)
    public ArticleEntity(String uri, String name, String time, String source,
            String imageUrl, boolean collectStatus, Date collectTime) {
        this.uri = uri;
        this.name = name;
        this.time = time;
        this.source = source;
        this.imageUrl = imageUrl;
        this.collectStatus = collectStatus;
        this.collectTime = collectTime;
    }


    @Generated(hash = 1301498493)
    public ArticleEntity() {
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