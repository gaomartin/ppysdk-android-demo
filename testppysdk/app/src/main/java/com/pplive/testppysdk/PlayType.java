package com.pplive.testppysdk;

/**
 * Created by ballackguan on 2016/10/8.
 */
public enum PlayType {

    UNKNOW_TYPE("UNKNOW_TYPE", 0), RTMP("RTMP", 1), FLV("FLV", 2), HLS("HLS", 3);

    private String name;
    private int index;

    private PlayType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String toString()
    {
        return this.name;
    }

    public static PlayType get(int index)
    {
        if (index == 1)
            return RTMP;
        else if (index == 2)
            return FLV;
        else if (index == 3)
            return HLS;
        return UNKNOW_TYPE;
    }
}
