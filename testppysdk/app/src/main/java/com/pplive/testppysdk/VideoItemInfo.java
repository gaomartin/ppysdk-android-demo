package com.pplive.testppysdk;

/**
 * Created by ballackguan on 2016/10/11.
 */
public class VideoItemInfo {
    private String playurl;
    private String imageurl;
    private String liveid;
    private int type;
    public VideoItemInfo(){}
    public VideoItemInfo(String _playurl, String _imageurl, String _liveid, int _type)
    {
        playurl = _playurl;
        imageurl = _imageurl;
        liveid = _liveid;
        type = _type;
    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLiveid() {
        return liveid;
    }

    public void setLiveid(String liveid) {
        this.liveid = liveid;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getPlayurl() {
        return playurl;
    }

    public void setPlayurl(String playurl) {
        this.playurl = playurl;
    }
}
