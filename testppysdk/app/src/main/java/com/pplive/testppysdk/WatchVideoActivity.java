package com.pplive.testppysdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pplive.ppysdk.PPYVideoView;
import com.pplive.ppysdk.PPYVideoViewListener;

import java.util.ArrayList;

public class WatchVideoActivity extends BaseActivity{
    PPYVideoView mVideoView;

    Handler mHandler = new Handler();

    String mM3u8Url; // m3u8

    long mReconnectTimeout = 0;
    static final long RECONNECT_TIMEOUT = 30*1000;
    boolean mIsPlay = true;

    private TextView textview_video_duration;
    private TextView mMsgTextview;
    private boolean mIsExiting = false;
    Handler mHandle = new Handler();
    Runnable mHideMsgRunable = new Runnable() {
        @Override
        public void run() {
            Log.d(ConstInfo.TAG, "excute mHideMsgRunable");
            mMsgTextview.setVisibility(View.GONE);
        }
    };
    private int mVideoWidth, mVideoHeight;
//    private int mVideoBitrate, mVideoFPS, mVideoDelay;


    int mVideoDuration, mVideoCurrentPosition;
    private boolean mIsAlreadyPlay = false;
    private boolean mIsPlayStart = false;
    Runnable mUpdateDataTipRunable = new Runnable() {
        @Override
        public void run() {
            if (mIsPlayStart)
            {
                if (mVideoView != null)
                    mVideoCurrentPosition = mVideoView.getCurrentPosition();
                String str = String.format(getString(R.string.video_progress), ConstInfo.progress2TimeString(mVideoCurrentPosition/1000), ConstInfo.progress2TimeString(mVideoDuration/1000));
                textview_video_duration.setText(str);
                mVideoSeekbar.setProgress(mVideoCurrentPosition);
            }
            mHandle.postDelayed(mUpdateDataTipRunable, 1000);
        }
    };
    Runnable mBufferStartRunable = new Runnable() {
        @Override
        public void run() {
            checkStreamStatus(true);
        }
    };
    ImageButton lsq_closeButton;

    private boolean mIsShowReconnect = true;
    private long mLastBufferTime = 0;
    private static final long MAX_BUFFER_TIME = 10*1000;
    private SeekBar mVideoSeekbar;
    private Button button_play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.watch_video_activity);

        mM3u8Url = getIntent().getStringExtra("m3u8Url");

        textview_video_duration = (TextView)findViewById(R.id.textview_video_duration);
        mMsgTextview = (TextView)findViewById(R.id.msg_live);

        final Button button_litter_player = (Button) findViewById(R.id.button_litter_player);
        button_litter_player.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean check = ConstInfo.hasPermissionFloatWin(getApplicationContext());
                if (!check)
                {
                    Toast.makeText(getApplication(), "悬浮窗权限未打开，请去打开应用悬浮窗权限", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(WatchVideoActivity.this, FloatWindowService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("m3u8Url", getCurrentUrl());
                    bundle.putInt(FloatWindowService.PLAY_TYPE, 0); // 1: live, 0: vod
                    intent.putExtra(FloatWindowService.ACTION_PLAY, bundle);
                    startService(intent);

                    finish();
                }

            }
        });

        mVideoSeekbar = (SeekBar)findViewById(R.id.seekbar_video_progress);

        mVideoSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(ConstInfo.TAG, "onStopTrackingTouch progress="+seekBar.getProgress());

                        if (mIsAlreadyPlay)
                            mVideoView.seekTo(seekBar.getProgress());
                    }
                }).start();
            }
        });

        button_play = (Button) findViewById(R.id.button_play);
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsAlreadyPlay)
                {
                    stop_play();
                    start_play();
                    return;
                }

                mIsPlay = !mIsPlay;
                button_play.setBackgroundResource(mIsPlay?R.drawable.pause:R.drawable.play);
                if (mIsPlay)
                    mVideoView.start();
                else
                    mVideoView.pause();
            }
        });

        lsq_closeButton = (ImageButton)findViewById(R.id.lsq_closeButton);
        lsq_closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsExiting = true;

                finish();
            }
        });

        mVideoView = (PPYVideoView)findViewById(R.id.live_player_videoview);
        mVideoView.initialize();

        mVideoView.setListener(new PPYVideoViewListener() {
            @Override
            public void onPrepared() {
                Log.d(ConstInfo.TAG, "play onPrepared");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
//                Toast.makeText(getApplication(), "拉流成功", Toast.LENGTH_SHORT).show();
                        show_toast(getString(R.string.get_stream_ok), true);
                        mIsShowReconnect = true;
                        mReconnectTimeout = 0;
                        mVideoView.start();
                        mIsAlreadyPlay = true;
                        mIsShowLoading = true;
                        mIsPlay = true;
                        button_play.setEnabled(true);
                        button_play.setBackgroundResource(mIsPlay?R.drawable.pause:R.drawable.play);
                        mVideoDuration = mVideoView.getDuration();
                        Log.d(ConstInfo.TAG, "play onPrepared mVideoDuration="+mVideoDuration);
                        mVideoSeekbar.setMax(mVideoDuration);
                        mVideoSeekbar.setProgress(0);
                    }
                });
            }

            @Override
            public void onError(int i, int i1) {
                hideLoading();
                if(i == PPYVideoView.ERROR_DEMUXER_READ_FAIL)
                {
                    Log.d(ConstInfo.TAG, "fail to read data from network");
                }else if(i == PPYVideoView.ERROR_DEMUXER_PREPARE_FAIL)
                {
                    Log.d(ConstInfo.TAG, "fail to connect to media server");
                }else{
                    Log.d(ConstInfo.TAG, "onError : "+String.valueOf(i));
                }
                Log.d(ConstInfo.TAG, "onError i="+i+" i1="+i1);

                checkStreamStatus(true);
            }

            @Override
            public void onInfo(int what, int extra) {
                //Log.d(ConstInfo.TAG, "setOnInfoListener: what="+what+" extra="+extra);
                if(what == PPYVideoView.INFO_BUFFERING_START)
                {
                    Log.d(ConstInfo.TAG, "onInfo buffering start");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHandle.postDelayed(mBufferStartRunable, MAX_BUFFER_TIME);
                            show_toast(getString(R.string.seek_buffer_start), false);
                        }
                    });
                }

                if(what == PPYVideoView.INFO_BUFFERING_END)
                {
                    Log.d(ConstInfo.TAG, "onInfo buffering end");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHandle.removeCallbacks(mBufferStartRunable);
                            mMsgTextview.setVisibility(View.GONE);
                        }
                    });
                }

                if(what == PPYVideoView.INFO_VIDEO_RENDERING_START)
                {
                    Log.d(ConstInfo.TAG, "onInfo video rendering start");
                }
//
//                if(what == VideoView.INFO_REAL_BITRATE)
//                {
//                    //Log.d(ConstInfo.TAG, "onInfo real bitrate : "+String.valueOf(extra));
//                    mVideoBitrate = extra;
//                }
//
//                if(what == VideoView.INFO_REAL_FPS)
//                {
//                    //Log.d(ConstInfo.TAG, "onInfo real fps : "+String.valueOf(extra));
//                    mVideoFPS = extra;
//                }
//
//                if(what == VideoView.INFO_REAL_BUFFER_DURATION)
//                {
//                    // Log.d(ConstInfo.TAG, "onInfo real buffer duration : "+String.valueOf(extra));
//                    mVideoDelay = extra;
//                }

                if(what == PPYVideoView.INFO_CONNECTED_SERVER)
                {
                    Log.d(ConstInfo.TAG, "connected to media server");
                }

                if(what == PPYVideoView.INFO_DOWNLOAD_STARTED)
                {
                    Log.d(ConstInfo.TAG, "start download media data");
                }

                if(what == PPYVideoView.INFO_GOT_FIRST_KEY_FRAME)
                {
                    Log.d(ConstInfo.TAG, "got first key frame");
                }
            }

            @Override
            public void onCompletion() {
                Log.d(ConstInfo.TAG, "onCompletion");
                hideLoading();

                mIsAlreadyPlay = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIsPlay = false;
//                        button_play.setEnabled(false);
                        button_play.setBackgroundResource(mIsPlay?R.drawable.pause:R.drawable.play);
                    }
                });
                //checkStreamStatus(true);
            }

            @Override
            public void onVideoSizeChanged(int i, int i1) {
                Log.d(ConstInfo.TAG, "play setOnVideoSizeChanged w="+i+" h="+i1);
                mVideoWidth = i;
                mVideoHeight = i1;
            }

            @Override
            public void onBufferingUpdate(int i) {
                Log.d(ConstInfo.TAG, "play onBufferingUpdate i="+i);
            }

            @Override
            public void OnSeekComplete() {
                Log.d(ConstInfo.TAG, "play OnSeekComplete");
            }
        });

        mHandle.postDelayed(mUpdateDataTipRunable, 1000);

        registerBaseBoradcastReceiver(true);
    }

    private void checkStreamStatus(final boolean need_reconnect)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mIsExiting)
                    return;

                if (mReconnectTimeout == 0)
                    mReconnectTimeout = System.currentTimeMillis();

                if (System.currentTimeMillis() - mReconnectTimeout > RECONNECT_TIMEOUT)
                {
                    mIsShowLoading = false;
                    show_toast(getString(R.string.no_network), false);
                    mIsShowReconnect = false;
                    reconnect();
                }
                else
                {
                    if (mIsShowReconnect)
                        show_toast(getString(R.string.network_reconnect), true);
                    reconnect();
                }
            }
        });

    }
    boolean mIsStartTipNetwork = false;
    boolean mIsStartCheckStatus = false;
    private void show_toast(String msg, boolean isAutoDisplay)
    {
        mMsgTextview.setText(msg);
        mMsgTextview.setVisibility(View.VISIBLE);
        if (isAutoDisplay)
            mHandle.postDelayed(mHideMsgRunable, 3000);
        else
            mHandle.removeCallbacks(mHideMsgRunable);
    }
    void checkNetwork()
    {
        if (NetworkUtils.isNetworkAvailable(getApplicationContext()))
        {
            if (NetworkUtils.isMobileNetwork(getApplicationContext()))
            {
                if (mIsStartTipNetwork)
                {
                    Log.d(ConstInfo.TAG, "connect change mobile network avilable tip alert is start, so exit this time");
                    return;
                }
                mIsStartTipNetwork = true;
                Log.d(ConstInfo.TAG, "connect change mobile network avilable, show tip alert");
                ConstInfo.showDialog(WatchVideoActivity.this, getString(R.string.mobile_network_play_tip), "", getString(R.string.cannel), getString(R.string.ok), new AlertDialogResultCallack() {
                    @Override
                    public void cannel() {
                        mIsExiting = true;
                        finish();
                    }

                    @Override
                    public void ok() {
                        Log.d(ConstInfo.TAG, "connect change mobile network avilable, use mobile ok");
                        start_play();
                        mIsStartTipNetwork = false;
                    }
                });
            }
            else
            {
                Log.d(ConstInfo.TAG, "connect change wiff network avilable");
                start_play();
            }
        }
        else
        {
            Log.d(ConstInfo.TAG, "connect change network unavilable");
            stop_play();
            show_toast(getString(R.string.no_network), false);
        }
    }
    public void registerBaseBoradcastReceiver(boolean isregister) {
        if (isregister) {
            IntentFilter myIntentFilter = new IntentFilter();
            myIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mBaseBroadcastReceiver, myIntentFilter);
        } else {
            unregisterReceiver(mBaseBroadcastReceiver);
        }
    }

    private boolean mIsInBackground = false;
    private BroadcastReceiver mBaseBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (mIsInBackground)
                    return;
                Log.d(ConstInfo.TAG, "connect change");
                checkNetwork();
            }
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(ConstInfo.TAG, "onResume");
        mIsInBackground = false;
        Log.d(ConstInfo.TAG, "onPause mIsAlreadyPlay="+mIsAlreadyPlay);
        if (mIsAlreadyPlay)
            start_play();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(ConstInfo.TAG, "onPause mIsPlayStart="+mIsPlayStart+" mIsAlreadyPlay="+mIsAlreadyPlay);
        //mLastStopTime = System.currentTimeMillis();
        mIsInBackground = true;
        if (mIsAlreadyPlay)
            stop_play();
    }

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
    private String getCurrentUrl()
    {
        return mM3u8Url;
    }

    boolean mIsShowLoading = true;
    private void start_play()
    {
        if (mIsPlayStart)
            return;
        mIsPlayStart = true;
        Log.d(ConstInfo.TAG, "start_play");

        button_play.setEnabled(false);
        if (mIsShowLoading)
            showLoading(getString(R.string.loading_tip));

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(ConstInfo.TAG, "play url: "+ getCurrentUrl());
                mVideoView.setDataSource(getCurrentUrl(), PPYVideoView.VOD_HIGH_CACHE);
                mVideoView.prepareAsync();
            }
        }).start();
    }

    private void stop_play()
    {
        if (!mIsPlayStart)
            return;
        mIsPlayStart = false;
        Log.d(ConstInfo.TAG, "stop_play");
        mVideoView.stop(false);
        button_play.setEnabled(false);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mIsExiting = true;
        stop_play();
        mVideoView.release();
        registerBaseBoradcastReceiver(false);
    }
//
    @Override
    public void onBackPressed()
    {
        Log.d(ConstInfo.TAG, "onBackPressed");

        super.onBackPressed();
    }

}
