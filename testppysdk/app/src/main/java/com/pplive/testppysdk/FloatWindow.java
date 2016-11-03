package com.pplive.testppysdk;

import android.app.AppOpsManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.pplive.ppysdk.PPYVideoView;
import com.pplive.ppysdk.PPYVideoViewListener;

import java.lang.reflect.Method;

/**
 * Created by zimo on 15/12/15.
 */
public class FloatWindow {

    private RelativeLayout mFloatLayout;
    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private PPYVideoView mVideoView;
    private Service mHostService;
    private Context mAppContext;
    private Bundle mBundleParam;
    private ProgressBar progressbar_loading;
    public FloatWindow(Service hostService)
    {
        mHostService = hostService;
        mAppContext = mHostService.getApplication();
    }


    public void createFloatView() {

        boolean check = ConstInfo.hasPermissionFloatWin(mAppContext);
        Log.d(ConstInfo.TAG, "hasAuthorFloatWin check="+check);
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) mAppContext.getSystemService(mAppContext.WINDOW_SERVICE);
        wmParams.type = check?WindowManager.LayoutParams.TYPE_PHONE:WindowManager.LayoutParams.TYPE_TOAST;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

        wmParams.width = TestApplication.SCREEN_WIDTH/3;
        wmParams.height = (wmParams.width/3)*4;    // 4:3 高宽比

        LayoutInflater inflater = LayoutInflater.from(mAppContext);
        mFloatLayout = (RelativeLayout) inflater.inflate(R.layout.top_window_player, null);
        mWindowManager.addView(mFloatLayout, wmParams);
        progressbar_loading = (ProgressBar)mFloatLayout.findViewById(R.id.progressbar_loading);

        ImageButton closebutton = (ImageButton)mFloatLayout.findViewById(R.id.lsq_closeButton);
        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHostService.stopSelf();
            }
        });


        // 设置悬浮窗的Touch监听
        mFloatLayout.setOnTouchListener(new View.OnTouchListener()
        {
            int lastX, lastY;
            int paramX, paramY;
//            int dx,dy;

            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = wmParams.x;
                        paramY = wmParams.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        wmParams.x = paramX + dx;
                        wmParams.y = paramY + dy;
                        // 更新悬浮窗位置
                        mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                        break;
                    case MotionEvent.ACTION_UP:
//                        lastX = (int) event.getRawX();
//                        lastY = (int) event.getRawY();
                        if (Math.abs(event.getRawX()-lastX) < 5 && Math.abs(event.getRawY()-lastY) < 5)
                            mFloatLayout.callOnClick();
                        break;
                }
                return true;
            }
        });

        mVideoView = (PPYVideoView)mFloatLayout.findViewById(R.id.live_player_videoview);
        mVideoView.initialize();
        mVideoView.setListener(mVideoListener);

        mFloatLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(ConstInfo.TAG, "mFloatLayout setOnClickListener mType="+mType);

                if (mType == 1)
                {
                    Intent intent = new Intent(mHostService.getApplicationContext(), WatchStreamingActivity.class);
                    intent.putExtra("liveurl", mBundleParam.getBundle("liveurl"));
                    intent.putExtra("liveid", mBundleParam.getString("liveid"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.putExtra("type", 1); // rtmp
                    mHostService.startActivity(intent);
                    mHostService.stopSelf();
                }
                else
                {
                    Intent intent = new Intent(mHostService.getApplicationContext(), WatchVideoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("m3u8Url", mUrl);
                    mHostService.startActivity(intent);
                    mHostService.stopSelf();
                }
            }
        });
        registerBaseBoradcastReceiver(true);
    }

    public void registerBaseBoradcastReceiver(boolean isregister) {
        if (isregister) {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mHostService.registerReceiver(mBaseBroadcastReceiver, myIntentFilter);
        } else {
            mHostService.unregisterReceiver(mBaseBroadcastReceiver);
        }
    }

    void checkNetwork()
    {
        if (NetworkUtils.isNetworkAvailable(mAppContext))
        {
            Log.d(ConstInfo.TAG, "connect change network avilable");
            start_play();
        }
        else
        {
            Log.d(ConstInfo.TAG, "connect change network unavilable");
            stop_play();
        }
    }

    private BroadcastReceiver mBaseBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {

                Log.d(ConstInfo.TAG, "connect change");
                checkNetwork();
            }
        }
    };

    private PPYVideoViewListener mVideoListener = new PPYVideoViewListener() {
        @Override
        public void onPrepared() {
            Log.d(ConstInfo.TAG, "onPrepared");
            mVideoView.start();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressbar_loading.setVisibility(View.GONE);
                    mVideoView.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onError(int i, int i1) {
            reconnect();
        }

        @Override
        public void onInfo(int i, int i1) {

        }

        @Override
        public void onCompletion() {
            Log.d(ConstInfo.TAG, "onCompletion");
        }

        @Override
        public void onVideoSizeChanged(int i, int i1) {

        }

        @Override
        public void onBufferingUpdate(int i) {

        }

        @Override
        public void OnSeekComplete() {

        }
    };
    private Handler mHandler = new Handler();
    private void reconnect()
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(ConstInfo.TAG, "reconnect play");
                stop_play();
                start_play();
            }
        }, 3000);
    }
    public void play(Bundle param)
    {
        mBundleParam = param;
        if (mBundleParam == null)
            return;

        mType = mBundleParam.getInt(FloatWindowService.PLAY_TYPE);
        if (mType == 1)
        {
            // live
            Bundle liveBundle = mBundleParam.getBundle("liveurl");
            if (liveBundle != null)
                mUrl = liveBundle.getString("rtmpUrl");
        }
        else
        {
            // vod
            mUrl = mBundleParam.getString("m3u8Url");
        }

        Log.d(ConstInfo.TAG, "play url="+mUrl+" type="+mType);
        stop_play();


        start_play();
    }
    private String mUrl; // m3u8
    private int mType; // 1: live, 0: vod
    private String getCurrentUrl()
    {
        return mUrl;
    }
    private boolean mIsPlayStart = false;
    private boolean mIsShowLoading = true;
    private void start_play()
    {
        if (mIsPlayStart)
            return;
        mIsPlayStart = true;
        Log.d(ConstInfo.TAG, "start_play");

//        if (mIsShowLoading)
//            showLoading(getString(R.string.loading_tip));

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                progressbar_loading.setVisibility(View.VISIBLE);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(ConstInfo.TAG, "play url: "+ getCurrentUrl());
                mVideoView.setDataSource(getCurrentUrl(), (mType==1)?PPYVideoView.LIVE_LOW_DELAY:PPYVideoView.VOD_HIGH_CACHE);
                mVideoView.prepareAsync();
            }
        }).start();
    }

    private void stop_play()
    {
        if (!mIsPlayStart)
            return;
        mIsPlayStart = false;
        mVideoView.setVisibility(View.GONE);
        Log.d(ConstInfo.TAG, "stop_play");
        mVideoView.stop(false);
    }


    public void destroy() {
        registerBaseBoradcastReceiver(false);
        stop_play();
        mVideoView.release();

        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
    }

}
