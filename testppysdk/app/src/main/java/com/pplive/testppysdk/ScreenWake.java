package com.pplive.testppysdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;


public class ScreenWake
{
    private PowerManager.WakeLock wakeLock;
    private Context mContext;
    public ScreenWake(Context context)
    {
        mContext = context;
    }
    public void disable()
    {
        if (wakeLock != null)
            return;
        wakeLock = ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ON_AFTER_RELEASE, "---PPYSDK---");
        wakeLock.acquire();
    }
    public void enable()
    {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}