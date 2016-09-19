package com.pplive.testppysdk;

import android.app.Application;
import android.pplive.media.MeetSDK;

import com.pplive.ppysdk.PPYStream;

import java.io.File;

/**
 * Created by ballackguan on 2016/8/23.
 */
public class TestApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPYStream.getInstance().init(this);

        String path = getCacheDir().getAbsolutePath() + "/log";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        MeetSDK.setLogPath(path + "/meetplayer.log", path + "/");
        MeetSDK.initSDK(this);
    }
}
