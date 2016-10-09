package com.pplive.testppysdk;

/**
 * Created by ballackguan on 2016/10/8.
 */
public enum PlayMode {

    UNKNOW_MODE("UNKNOW_MODE", 0), GAOQING("高清", 1), BIAOQING("标清", 2), CHAOQING("超清", 3);

    private String name;
    private int index;

    private PlayMode(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String toString()
    {
        return this.name;
    }

    public static PlayMode get(int index)
    {
        if (index == 1)
            return GAOQING;
        else if (index == 2)
            return BIAOQING;
        else if (index == 3)
            return CHAOQING;
        return UNKNOW_MODE;
    }
}
