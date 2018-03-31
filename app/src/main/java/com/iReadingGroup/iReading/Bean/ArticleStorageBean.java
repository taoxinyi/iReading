package com.iReadingGroup.iReading.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * Created by taota on 2018/3/30.
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

    @Generated(hash = 1659294483)
    public ArticleStorageBean(String uri, String name, String time, String source,
                              String imageUrl) {
        this.uri = uri;
        this.name = name;
        this.time = time;
        this.source = source;
        this.imageUrl = imageUrl;
    }


    @Generated(hash = 1467132437)
    public ArticleStorageBean() {
    }

    /**
     * Gets uri.
     *
     * @return the uri
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Sets uri.
     *
     * @param uri the uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets time.
     *
     * @return the time
     */
    public String getTime() {
        return this.time;
    }

    /**
     * Sets time.
     *
     * @param time the time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Gets source.
     *
     * @return the source
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Sets source.
     *
     * @param source the source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets image url.
     *
     * @return the image url
     */
    public String getImageUrl() {
        return this.imageUrl;
    }

    /**
     * Sets image url.
     *
     * @param imageUrl the image url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}