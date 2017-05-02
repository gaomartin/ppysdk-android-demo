package com.pplive.testppysdk;

import java.io.Serializable;

/**
 * Created by ballackguan on 2017/2/23.
 */
public class Ft implements Serializable{
    public int getFt() {
        return ft;
    }

    public void setFt(int ft) {
        this.ft = ft;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText()
    {
        switch (ft)
        {
            case 0:
                return "原画";
            case 1:
                return "流畅";
            case 2:
                return "标清";
            case 3:
                return "高清";
            case 4:
                return "超清";
            default:
                return "原画";
        }
    }
    int ft;
    String url;
    public Ft(int _ft, String _url)
    {
        ft = _ft;
        url = _url;
    }
}
