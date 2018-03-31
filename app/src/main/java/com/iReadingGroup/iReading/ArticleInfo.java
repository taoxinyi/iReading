package com.iReadingGroup.iReading;

/**
 * Created by taota on 2018/3/25.
 */
public class ArticleInfo {
    private String name;
    private String uri;
    private String time;
    private String source;
    private int imageId;
    private String imageUrl;

    /**
     * Instantiates a new Article info.
     *
     * @param name     the name
     * @param uri      the uri
     * @param imageUrl the image url
     * @param time     the time
     * @param source   the source
     * @param params   the params
     */
    public ArticleInfo(String name, String uri, String imageUrl, String time, String source, int...params) {
        this.name = name;//article name
        this.uri =uri;
        this.imageId = params[0];//article id
        this.imageUrl=imageUrl;
        this.time=time;
        this.source=source;
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
     * Sets uri.
     *
     * @param uri the uri
     */
    public void setUri(String uri) {this.uri=uri;}

    /**
     * Sets image id.
     *
     * @param imageId the image id
     */
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    /**
     * Set image url.
     *
     * @param imageUrl the image url
     */
    public void setImageUrl(String imageUrl){this.imageUrl=imageUrl;}

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets uri.
     *
     * @return the uri
     */
    public String getUri() {return uri;}

    /**
     * Gets image id.
     *
     * @return the image id
     */
    public int getImageId() {
        return imageId;
    }

    /**
     * Get image url string.
     *
     * @return the string
     */
    public String getImageUrl(){return  imageUrl;}

    /**
     * Get source string.
     *
     * @return the string
     */
    public String getSource(){return  source;}

    /**
     * Get time string.
     *
     * @return the string
     */
    public String getTime(){return time;}

}