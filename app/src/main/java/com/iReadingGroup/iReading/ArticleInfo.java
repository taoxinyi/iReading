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

    public ArticleInfo(String name,String uri ,String imageUrl,String time,String source, int...params) {
        this.name = name;//article name
        this.uri =uri;
        this.imageId = params[0];//article id
        this.imageUrl=imageUrl;
        this.time=time;
        this.source=source;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setUri(String uri) {this.uri=uri;}
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public void setImageUrl(String imageUrl){this.imageUrl=imageUrl;}
    public String getName() {
        return name;
    }
    public String getUri() {return uri;}
    public int getImageId() {
        return imageId;
    }
    public String getImageUrl(){return  imageUrl;}
    public String getSource(){return  source;}
    public String getTime(){return time;}

}