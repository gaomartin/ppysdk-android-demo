package com.pplive.testppysdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;

import android.slkmedia.mediaplayer.VideoView;
import android.slkmedia.mediaplayer.VideoViewListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pplive.ppysdk.PPYStream;

import java.util.ArrayList;
import java.util.HashMap;

public class WatchStreamingActivity extends BaseActivity{
    VideoView mVideoView;

    Handler mHandler = new Handler();
    String mRtmpUrl; // rtmp
    String mHdlUrl; // http-flv
    String mM3u8Url; // m3u8
    ArrayList<String> mRtmpUrlList = new ArrayList<>();
    String mCurrentUrl;
    PlayType mUrlType;
    PlayMode mRtmpPlayMode = PlayMode.GAOQING;
    String mLiveId;
    long mReconnectTimeout = 0;
    static final long RECONNECT_TIMEOUT = 30*1000;
    boolean mIsDataTipOpen = true;
    boolean mIsRtmpUrl = true;
    TextView liveid_tip;
    TextView msg_data_tip;
    private TextView mMsgTextview;
    private boolean mIsPlayEnd = false;
    private boolean mIsExiting = false;
    Handler mHandle = new Handler();
    Runnable mHideMsgRunable = new Runnable() {
        @Override
        public void run() {
            Log.d(ConstInfo.TAG, "excute mHideMsgRunable");
            mMsgTextview.setVisibility(View.GONE);
        }
    };
    private int mVideoWidth, mVideoHeight, mVideoBitrate, mVideoFPS, mVideoDelay;

    private boolean mIsAlreadyPlay = false;
    private boolean mIsPlayStart = false;
    Runnable mUpdateDataTipRunable = new Runnable() {
        @Override
        public void run() {
            if (mIsPlayStart)
            {
                String str = String.format(getString(R.string.watch_data_tip), mVideoBitrate, mVideoFPS, mVideoWidth, mVideoHeight, mVideoDelay, mUrlType.toString());
                msg_data_tip.setText(str);
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.watch_streaming_activity);
        mLiveId = getIntent().getStringExtra("liveid");
        mUrlType = PlayType.get(getIntent().getIntExtra("type", 0));
        Bundle data = getIntent().getBundleExtra("liveurl");
        mRtmpUrlList = data.getStringArrayList("rtmpsUrl");
//        if (mRtmpUrlList != null && mRtmpUrlList.size() > 0)
//            mRtmpUrl = mRtmpUrlList.get(0);
//        else
        mRtmpUrl = data.getString("rtmpUrl");
        mHdlUrl = data.getString("hdlUrl");
        mM3u8Url = data.getString("m3u8Url");
        if (TextUtils.isEmpty(mRtmpUrl) || TextUtils.isEmpty(mHdlUrl))
            mUrlType = PlayType.M3U8;

        mRtmpPlayMode = PlayMode.GAOQING;

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
        final LinearLayout rtmp_play_mode_control_container = (LinearLayout)findViewById(R.id.rtmp_play_mode_control_container);
        final TextView textview_rtmp_play_mode = (TextView) findViewById(R.id.textview_rtmp_play_mode);
        final TextView textview_play_type = (TextView) findViewById(R.id.textview_play_type);
        if (TextUtils.isEmpty(mHdlUrl) || TextUtils.isEmpty(mRtmpUrl))
            textview_play_type.setVisibility(View.GONE);
        else
            textview_play_type.setVisibility(View.VISIBLE);

//        if (mUrlType == PlayType.RTMP && mRtmpUrlList != null && !mRtmpUrlList.isEmpty())
//        {
//            textview_rtmp_play_mode.setText("高清");
//            textview_rtmp_play_mode.setVisibility(View.VISIBLE);
//        }
//        else
//            textview_rtmp_play_mode.setVisibility(View.GONE);

        textview_play_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUrlType == PlayType.RTMP)
                    mUrlType = PlayType.FLV;
                else
                    mUrlType = PlayType.RTMP;

                textview_play_type.setText(mUrlType.toString());
//                button_url_type.setBackgroundResource((mUrlType == PlayType.RTMP)?R.drawable.rtmp:R.drawable.flv);

                if (mUrlType == PlayType.RTMP && mRtmpUrlList != null && !mRtmpUrlList.isEmpty())
                {
                    mRtmpPlayMode = PlayMode.GAOQING;
                    //textview_rtmp_play_mode.setText(mRtmpPlayMode.toString());
                    //textview_rtmp_play_mode.setVisibility(View.VISIBLE);
                }
                else
                {
                    textview_rtmp_play_mode.setVisibility(View.GONE);
                    //rtmp_play_mode_control_container.setVisibility(View.GONE);
                }

                if (mIsPlayEnd)
                    return;
                Log.d(ConstInfo.TAG, "reconnect play");
                stop_play();
                start_play();
            }
        });

        textview_rtmp_play_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rtmp_play_mode_control_container.setVisibility(rtmp_play_mode_control_container.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
            }
        });

        final RadioGroup radio_group_container = (RadioGroup)findViewById(R.id.radio_group_container);
        radio_group_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = radio_group_container.getCheckedRadioButtonId();
                if (id == R.id.RADIO_BUTTON_480P)
                {

                }

                rtmp_play_mode_control_container.setVisibility(rtmp_play_mode_control_container.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
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

        mVideoView = (VideoView)findViewById(R.id.live_player_videoview);
        mVideoView.initialize();
        mVideoView.setListener(new VideoViewListener() {
            @Override
            public void onPrepared() {
                Log.d(ConstInfo.TAG, "play onPrepared");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        hide_play_error_popup();
//                Toast.makeText(getApplication(), "拉流成功", Toast.LENGTH_SHORT).show();
                        show_toast(getString(R.string.get_stream_ok), true);
                        mIsShowReconnect = true;
                        mReconnectTimeout = 0;
                        mVideoView.start();
                        mIsAlreadyPlay = true;
                        mIsShowLoading = true;
                    }
                });
            }

            @Override
            public void onError(int i, int i1) {
                hideLoading();
                if(i == VideoView.ERROR_DEMUXER_READ_FAIL)
                {
                    Log.d(ConstInfo.TAG, "fail to read data from network");
                }else if(i == VideoView.ERROR_DEMUXER_PREPARE_FAIL)
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
                if(what == VideoView.INFO_BUFFERING_START)
                {
                    Log.d(ConstInfo.TAG, "onInfo buffering start");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHandle.postDelayed(mBufferStartRunable, MAX_BUFFER_TIME);
                            show_toast(getString(R.string.buffer_start), false);
                        }
                    });
                }

                if(what == VideoView.INFO_BUFFERING_END)
                {
                    Log.d(ConstInfo.TAG, "onInfo buffering end");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHandle.removeCallbacks(mBufferStartRunable);
                            hide_play_error_popup();
                            mMsgTextview.setVisibility(View.GONE);
                        }
                    });
                }

                if(what == VideoView.INFO_VIDEO_RENDERING_START)
                {
                    Log.d(ConstInfo.TAG, "onInfo video rendering start");
                }

                if(what == VideoView.INFO_REAL_BITRATE)
                {
                    //Log.d(ConstInfo.TAG, "onInfo real bitrate : "+String.valueOf(extra));
                    mVideoBitrate = extra;
                }

                if(what == VideoView.INFO_REAL_FPS)
                {
                    //Log.d(ConstInfo.TAG, "onInfo real fps : "+String.valueOf(extra));
                    mVideoFPS = extra;
                }

                if(what == VideoView.INFO_REAL_BUFFER_DURATION)
                {
                    // Log.d(ConstInfo.TAG, "onInfo real buffer duration : "+String.valueOf(extra));
                    mVideoDelay = extra;
                }

                if(what == VideoView.INFO_CONNECTED_SERVER)
                {
                    Log.d(ConstInfo.TAG, "connected to media server");
                }

                if(what == VideoView.INFO_DOWNLOAD_STARTED)
                {
                    Log.d(ConstInfo.TAG, "start download media data");
                }

                if(what == VideoView.INFO_GOT_FIRST_KEY_FRAME)
                {
                    Log.d(ConstInfo.TAG, "got first key frame");
                }
            }

            @Override
            public void onCompletion() {
                Log.d(ConstInfo.TAG, "onCompletion");
                hideLoading();
                checkStreamStatus(true);
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
        if (mIsExiting)
            return;
        PPYRestApi.stream_status(mLiveId, new PPYRestApi.StringResultStatusCallack() {
            @Override
            public void result(int errcode, String livestatus, String streamstatus) {
                if (mIsExiting)
                    return;
                Log.d(ConstInfo.TAG, "checkStreamStatus GET stream_status errcode="+errcode+" livestatus="+livestatus+" streamstatus="+streamstatus);
                if (errcode == 0 && livestatus != null && streamstatus != null)
                {
                    if (livestatus.equals("stopped"))
                    {
                        hide_play_error_popup();
                        show_play_end_popup();
                    }
                    else if ((livestatus.equals("living") || livestatus.equals("broken"))&& streamstatus.equals("error"))
                    {
                        show_play_error_popup();

                        reconnect();
                    }
                    else
                    {
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
                }
                else if (errcode == 98 || errcode == 97)
                {
                    hide_play_error_popup();
                    show_play_end_popup();
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
            if (mIsStartCheckStatus)
            {
                Log.d(ConstInfo.TAG, "connect change network avilable and check status is already start, so exit this time");
                return;
            }
            mIsStartCheckStatus = true;
            PPYRestApi.stream_status(mLiveId, new PPYRestApi.StringResultStatusCallack() {
                @Override
                public void result(int errcode, String livestatus, String streamstatus) {

                    mIsStartCheckStatus = false;
                    Log.d(ConstInfo.TAG, "connect change network avilable GET stream_status errcode="+errcode+" livestatus="+livestatus+" streamstatus="+streamstatus);
                    if (errcode == 0 && livestatus != null)
                    {
                        if (livestatus.equals("stopped"))
                        {
                            hide_play_error_popup();
                            show_play_end_popup();
                        }
                        else
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
                                ConstInfo.showDialog(WatchStreamingActivity.this, getString(R.string.mobile_network_play_tip), "", getString(R.string.cannel), getString(R.string.ok), new AlertDialogResultCallack() {
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
                    }
                    else if (errcode == 98 || errcode == 97)
                    {
                        hide_play_error_popup();
                        show_play_end_popup();
                    }
                }
            });
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

    PopupWindow mPlayErrorPopupWindow;
    public void show_play_error_popup()
    {
        if (mPlayErrorPopupWindow == null)
            create_play_error_popup(null);
        if (mPlayErrorPopupWindow != null && !mPlayErrorPopupWindow.isShowing())
            mPlayErrorPopupWindow.showAtLocation(lsq_closeButton, Gravity.CENTER, 0, 0);
        mIsShowLoading = false;
    }
    public void hide_play_error_popup()
    {
        mIsShowLoading = true;
        if (mPlayErrorPopupWindow != null)
            mPlayErrorPopupWindow.dismiss();
    }
    private void create_play_error_popup(final AlertDialogResult3Callack result2Callack)
    {
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final RelativeLayout dialogView = (RelativeLayout)layoutInflater.inflate(R.layout.layout_play_error, null);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.popup_bg);
        ImageButton close = (ImageButton)dialogView.findViewById(R.id.close);
        ImageView bg = (ImageView)dialogView.findViewById(R.id.bg);
        TextView textview_error_msg = (TextView)dialogView.findViewById(R.id.textview_error_msg);
        textview_error_msg.setTextSize(20.0f);
        textview_error_msg.setText(getString(R.string.stream_error));
        Bitmap fastblurBitmap = ConstInfo.fastblur(bitmap, 20);
        bg.setImageBitmap(fastblurBitmap);

//        dialogView.setFocusable(true);
//        dialogView.setFocusableInTouchMode(true);
//        dialogView.requestFocus();

        mPlayErrorPopupWindow = new PopupWindow(dialogView, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        //在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        mPlayErrorPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPlayErrorPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //点击空白处时，隐藏掉pop窗口
//        mPlayErrorPopupWindow.setFocusable(true);

//        mPlayErrorPopupWindow.setBackgroundDrawable(new BitmapDrawable());
//
//        mPlayErrorPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                finish();
//            }
//        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayErrorPopupWindow.dismiss();
                mIsExiting = true;
                finish();
            }
        });
//        dialogView.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    switch(keyCode) {
//                        case KeyEvent.KEYCODE_BACK:
//                            Log.d(ConstInfo.TAG, "mPlayErrorPopupWindow KEYCODE_BACK");
//                            mPlayErrorPopupWindow.dismiss();
//                            mIsExiting = true;
//                            finish();
//                            return false;
//                    }
//                }
//                return true;
//            }
//        });

    }

    PopupWindow mPlayEndPopupWindow;
    public void show_play_end_popup()
    {
        if (mPlayEndPopupWindow == null)
            create_play_end_popup(null);
        if (mPlayEndPopupWindow != null && !mPlayEndPopupWindow.isShowing())
            mPlayEndPopupWindow.showAtLocation(lsq_closeButton, Gravity.CENTER, 0, 0);
        stop_play();
        mIsPlayEnd = true;
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
        Button button_watch_video = (Button)dialogView.findViewById(R.id.button_watch_video);
        button_watch_video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (TextUtils.isEmpty(mM3u8Url))
                    {
                        // toast
                    }
                    else
                    {
                        Intent intent = new Intent(WatchStreamingActivity.this, WatchVideoActivity.class);
                        intent.putExtra("m3u8Url", mM3u8Url);
                        startActivity(intent);
                        WatchStreamingActivity.this.finish();
                    }
                }
                return false;
            }
        });
        ImageView bg = (ImageView)dialogView.findViewById(R.id.bg);
        Bitmap fastblurBitmap = ConstInfo.fastblur(bitmap, 18);
        bg.setImageBitmap(fastblurBitmap);
//        dialogView.setFocusable(true);
//        dialogView.setFocusableInTouchMode(true);

        mPlayEndPopupWindow = new PopupWindow(dialogView, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        //在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        mPlayEndPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPlayEndPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //点击空白处时，隐藏掉pop窗口
//        mPlayEndPopupWindow.setFocusable(true);
//        mPlayEndPopupWindow.setBackgroundDrawable(new BitmapDrawable());

//        mPlayEndPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                mIsExiting = true;
//                finish();
//            }
//        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayEndPopupWindow.dismiss();
                mIsExiting = true;
                finish();
            }
        });
//        dialogView.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    switch(keyCode) {
//                        case KeyEvent.KEYCODE_BACK:
//                        {
//                            Log.d(ConstInfo.TAG, "mPlayEndPopupWindow KEYCODE_BACK");
//                            mPlayEndPopupWindow.dismiss();
//
//                        }
//                        return false;
//                    }
//                }
//                return true;
//            }
//        });

    }

    private void reconnect()
    {
        if (mIsPlayEnd)
            return;
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
        if (mUrlType == PlayType.RTMP)
            mCurrentUrl = mRtmpUrl;
        else if (mUrlType == PlayType.FLV)
            mCurrentUrl = mHdlUrl;
        else if (mUrlType == PlayType.M3U8)
            mCurrentUrl = mM3u8Url;
        return mCurrentUrl;
    }

    boolean mIsShowLoading = true;
    private void start_play()
    {
        if (mIsPlayStart || mIsPlayEnd)
            return;
        mIsPlayStart = true;
        Log.d(ConstInfo.TAG, "start_play");

        if (mIsShowLoading)
            showLoading(getString(R.string.loading_tip));

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(ConstInfo.TAG, "play url: "+ getCurrentUrl());
                mVideoView.setDataSource(getCurrentUrl(), VideoView.LIVE_LOW_DELAY);
                mVideoView.prepareAsync();
            }
        }).start();
    }

    private void stop_play()
    {
        if (!mIsPlayStart || mIsPlayEnd)
            return;
        mIsPlayStart = false;
        Log.d(ConstInfo.TAG, "stop_play");
        mVideoView.stop(false);
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
        if (mPlayErrorPopupWindow != null && mPlayErrorPopupWindow.isShowing())
        {
            mPlayErrorPopupWindow.dismiss();
        }
        if (mPlayEndPopupWindow != null && mPlayEndPopupWindow.isShowing())
        {
            mPlayEndPopupWindow.dismiss();
        }

        super.onBackPressed();
    }

}
