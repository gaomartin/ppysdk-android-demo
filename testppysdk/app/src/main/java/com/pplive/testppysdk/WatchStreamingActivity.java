package com.pplive.testppysdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.pplive.media.player.MediaInfo;
import android.pplive.media.player.MediaPlayer;
import android.pplive.media.player.MeetVideoView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pplive.ppysdk.PPYStream;

import java.util.HashMap;

public class WatchStreamingActivity extends BaseActivity{
    MeetVideoView mMeetVideoView;
    Handler mHandler = new Handler();
    String mRtmpUrl;
    String mLiveId;
    long mReconnectTimeout = 0;
    static final long RECONNECT_TIMEOUT = 30*1000;
    boolean mIsDataTipOpen = true;
    TextView liveid_tip;
    TextView msg_data_tip;
    private TextView mMsgTextview;
    Handler mHandle = new Handler();
    Runnable mHideMsgRunable = new Runnable() {
        @Override
        public void run() {
            Log.d(ConstInfo.TAG, "excute mHideMsgRunable");
            mMsgTextview.setVisibility(View.GONE);
        }
    };
    Runnable mUpdateDataTipRunable = new Runnable() {
        @Override
        public void run() {
            if (mMeetVideoView != null && mMeetVideoView.getMediaInfo() != null)
            {
                MediaInfo mediaInfo = mMeetVideoView.getMediaInfo();
                int videobitrate = mediaInfo.getBitrate();
                double fps = mediaInfo.getFrameRate();
                int vdeio_w = mediaInfo.getWidth();
                int video_h = mediaInfo.getHeight();
                String str = String.format(getString(R.string.data_tip), videobitrate, (int)fps, vdeio_w, video_h);
                msg_data_tip.setText(str);
            }
            mHandle.postDelayed(mUpdateDataTipRunable, 1000);
        }
    };
    ImageButton lsq_closeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_streaming_activity);

        mRtmpUrl = getIntent().getStringExtra("liveurl");
        mLiveId = getIntent().getStringExtra("liveid");

        liveid_tip = (TextView)findViewById(R.id.liveid);
        liveid_tip.setText(getString(R.string.liveid_tip, mLiveId));

        msg_data_tip = (TextView)findViewById(R.id.msg_tip);
        mMsgTextview = (TextView)findViewById(R.id.msg_live);

        final Button button_data_tip = (Button) findViewById(R.id.button_data_tip);
        button_data_tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsDataTipOpen = !mIsDataTipOpen;
                button_data_tip.setBackgroundResource(mIsDataTipOpen?R.drawable.data_tip_open:R.drawable.data_tip_close);
                msg_data_tip.setVisibility(mIsDataTipOpen?View.VISIBLE:View.GONE);
            }
        });

        Log.d(ConstInfo.TAG, "play rtmpurl: "+mRtmpUrl);
        lsq_closeButton = (ImageButton)findViewById(R.id.lsq_closeButton);
        lsq_closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
        mMeetVideoView = (MeetVideoView)findViewById(R.id.live_player_videoview);
        //mMeetVideoView.getMediaInfo().
        //mMeetVideoView.setDecodeMode(MediaPlayer.DecodeMode.HW_SYSTEM);
        mMeetVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                Log.d(ConstInfo.TAG, "onBufferingUpdate: "+i+"%");
            }
        });
        mMeetVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                Log.d(ConstInfo.TAG, "setOnInfoListener: i="+i+" i1="+i1);
                return false;
            }
        });
        mHandle.postDelayed(mUpdateDataTipRunable, 1000);

        mMeetVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int framework_err, int i1) {
                Log.d(ConstInfo.TAG, "play onError framework_err="+framework_err+" i1="+i1);


                PPYRestApi.stream_status(mLiveId, new PPYRestApi.StringResultCallack() {
                    @Override
                    public void result(int errcode, String data) {
                        Log.d(ConstInfo.TAG, "GET stream_status errcode="+errcode+" status="+data);
                        if (errcode == 0)
                        {
                            if (data.equals("stopped"))
                            {
                                show_play_end_popup();;
                            }
                            else
                            {
                                if (mReconnectTimeout == 0)
                                    mReconnectTimeout = System.currentTimeMillis();

                                if (System.currentTimeMillis() - mReconnectTimeout > RECONNECT_TIMEOUT)
                                {
                                    mMsgTextview.setText(getString(R.string.no_network));
                                    mMsgTextview.setVisibility(View.VISIBLE);
                                    mHandle.removeCallbacks(mHideMsgRunable);
                                }
                                else
                                {
                                    mMsgTextview.setText(getString(R.string.network_reconnect));
                                    mMsgTextview.setVisibility(View.VISIBLE);
                                    mHandle.postDelayed(mHideMsgRunable, 3000);
                                    reconnect();
                                }

                            }
                        }
                        else
                        {
                            show_play_end_popup();
                        }
                    }
                });

                return false;
            }
        });
        mMeetVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d(ConstInfo.TAG, "play complete");

                PPYRestApi.stream_status(mLiveId, new PPYRestApi.StringResultCallack() {
                    @Override
                    public void result(int errcode, String data) {
                        Log.d(ConstInfo.TAG, "GET stream_status errcode="+errcode+" status="+data);
                        if (errcode == 0)
                        {
                            if (data.equals("stopped"))
                            {
                                show_play_end_popup();
                            }
                            else
                            {
                                {
//                                    Toast.makeText(getApplication(), "网络异常，正在尝试重新连接", Toast.LENGTH_SHORT).show();
                                    mMsgTextview.setText(getString(R.string.network_reconnect));
                                    mMsgTextview.setVisibility(View.VISIBLE);
                                    mHandle.postDelayed(mHideMsgRunable, 3000);
                                    reconnect();
                                }

                            }
                        }
                        else
                            show_play_end_popup();
                    }
                });
            }
        });
        mMeetVideoView.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                Log.d(ConstInfo.TAG, "play setOnVideoSizeChanged w="+i+" h="+i1);

            }
        });

        // 默认是0
//        public static final int SCREEN_FIT = 0; // 自适应
//        public static final int SCREEN_STRETCH = 1; // 铺满屏幕
//        public static final int SCREEN_FILL = 2; // 放大裁切
//        public static final int SCREEN_CENTER = 3; // 原始大小

        mMeetVideoView.setDisplayMode(1);

//        if (NetworkUtils.isNetworkAvailable(getApplicationContext()))
//        {
//            if (NetworkUtils.isMobileNetwork(getApplicationContext()))
//            {
//                ConstInfo.showDialog(WatchStreamingActivity.this, "您当前使用的是移动数据，确定开播吗？", "", "取消", "确定", new AlertDialogResultCallack() {
//                    @Override
//                    public void cannel() {
//                        finish();
//                    }
//
//                    @Override
//                    public void ok() {
//                        StartStream();
//                        PPYStream.getInstance().OnResume();
//                    }
//                });
//            }
//            else
//                StartStream();
//        }
//        else
//        {
//            mMsgTextview.setText(getString(R.string.no_network));
//            mMsgTextview.setVisibility(View.VISIBLE);
//            mHandle.removeCallbacks(mHideMsgRunable);
//            StartStream();
//        }


        showLoading("");
        start_play();

        registerBaseBoradcastReceiver(true);
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

    private BroadcastReceiver mBaseBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                Log.d(ConstInfo.TAG, "connect change");
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    //StopStream();
                    mMsgTextview.setText(getString(R.string.no_network));
                    mMsgTextview.setVisibility(View.VISIBLE);
                    mHandle.postDelayed(mHideMsgRunable, 3000);
                }
//                else if (!mIsStreamingStart)
//                {
//                    if (NetworkUtils.isMobileNetwork(getApplicationContext()))
//                    {
//                        ConstInfo.showDialog(WatchStreamingActivity.this, "您当前使用的是移动数据，确定开播吗？", "", "取消", "确定", new AlertDialogResultCallack() {
//                            @Override
//                            public void cannel() {
//                                finish();
//                            }
//
//                            @Override
//                            public void ok() {
//                                StartStream();
//                                PPYStream.getInstance().OnResume();
//                            }
//                        });
//                    }
//                    else
//                        StartStream();
//                }

            }
        }
    };

    PopupWindow mPlayEndPopupWindow;
    public void show_play_end_popup()
    {
        if (mPlayEndPopupWindow == null)
            create_play_end_popup(null);
        if (mPlayEndPopupWindow != null && !mPlayEndPopupWindow.isShowing())
            mPlayEndPopupWindow.showAtLocation(lsq_closeButton, Gravity.CENTER, 0, 0);
        mMeetVideoView.stopPlayback();
    }
    public void hide_play_end_popup()
    {
        if (mPlayEndPopupWindow != null)
            mPlayEndPopupWindow.dismiss();
    }
    private void create_play_end_popup(final AlertDialogResult3Callack result2Callack)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout dialogView = (RelativeLayout)layoutInflater.inflate(R.layout.layout_play_end, null);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.popup_bg);
        ImageButton close = (ImageButton)dialogView.findViewById(R.id.close);
        ImageView bg = (ImageView)dialogView.findViewById(R.id.bg);
        Bitmap fastblurBitmap = ConstInfo.fastblur(bitmap, 20);
        bg.setImageBitmap(fastblurBitmap);

        mPlayEndPopupWindow = new PopupWindow(dialogView, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        //在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        mPlayEndPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPlayEndPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //点击空白处时，隐藏掉pop窗口
        mPlayEndPopupWindow.setFocusable(true);
        mPlayEndPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        mPlayEndPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayEndPopupWindow.dismiss();
            }
        });
        dialogView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch(keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            mPlayEndPopupWindow.dismiss();
                            return false;
                    }
                }
                return true;
            }
        });

    }

    private void reconnect()
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(ConstInfo.TAG, "reconnect play");

                mMeetVideoView.stopPlayback();
                start_play();
            }
        }, 3000);
    }
    private void start_play()
    {
        mMeetVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                hideLoading();

                Log.d(ConstInfo.TAG, "play onPrepared");
//                Toast.makeText(getApplication(), "拉流成功", Toast.LENGTH_SHORT).show();
                mMsgTextview.setText(getString(R.string.get_stream_ok));
                mMsgTextview.setVisibility(View.VISIBLE);
                mHandle.postDelayed(mHideMsgRunable, 3000);
                mMeetVideoView.start();
            }
        });
        mMeetVideoView.setVideoPath(mRtmpUrl);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mMeetVideoView.stopPlayback();
        registerBaseBoradcastReceiver(false);
    }
}
