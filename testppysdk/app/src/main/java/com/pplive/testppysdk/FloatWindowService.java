package com.pplive.testppysdk;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by zimo on 15/12/15.
 */
public class FloatWindowService extends Service {

    public static final String ACTION_PLAY = "com.pplive.testppysdk.FloatWindowService.ACTION_PLAY";
    public static final String ACTION_EXIT = "com.pplive.testppysdk.FloatWindowService.ACTION_EXIT";
    public static final String PLAY_TYPE = "com.pplive.testppysdk.FloatWindowService.PLAY_TYPE";

    public static boolean mIsFloatWindowShown = false;
    private FloatWindow mFloatWindow;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ConstInfo.TAG, "FloatWindowService onCreate");
        mFloatWindow = new FloatWindow(this);
        mFloatWindow.createFloatView();
        mIsFloatWindowShown = true;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        Log.d(ConstInfo.TAG, "FloatWindowService onStart");
        if (intent.hasExtra(ACTION_EXIT))
        {
            stopSelf();
            return;
        }
        else
        {
            Bundle bundle = intent.getBundleExtra(ACTION_PLAY);
            if (bundle != null && mFloatWindow != null)
            {
                Log.d(ConstInfo.TAG, "FloatWindowService onStart play bundle");
                mFloatWindow.play(bundle);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(ConstInfo.TAG, "FloatWindowService onDestroy");
        if (mFloatWindow != null) {
            mFloatWindow.destroy();
        }
        mIsFloatWindowShown = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
