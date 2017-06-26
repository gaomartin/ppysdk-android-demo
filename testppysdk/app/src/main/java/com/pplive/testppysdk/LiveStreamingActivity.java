package com.pplive.testppysdk;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pplive.ppysdk.PPYLiveView;
import com.pplive.ppysdk.PPYStatusListener;
import com.pplive.ppysdk.PPYStream;
import com.pplive.ppysdk.PPYStreamerConfig;
import com.pplive.ppysdk.PPYVideoView;
import com.pplive.ppysdk.VIDEO_RESOLUTION_TYPE;

import java.util.Timer;
import java.util.TimerTask;

public class LiveStreamingActivity extends BaseActivity {


    /**
     * 关闭按钮
     */
    private ImageButton mCloseButton;

    /**
     * 美颜控制按钮
     */
    private Button mBeautyButton;

    /**
     * 帧率、码流切换按钮
     */
    private Button mFPSButton;

    /**
     * 闪光灯按钮
     */
    private Button mFlashButton;

    /**
     * 音量按钮
     */
    private Button mMuteButton;

    /**
     * 切换摄像头
     */
    private Button mToggleButton;
    private Button mToggleMirrorButton;
    /**
     * 推流操作按钮
     */
    private Button mDataTipButton;

    TextView mDataTipTextview;

    //-----------------------------------------------------

    // 美颜状态
    private Boolean mBeautyEnabled = false;

    // 静音状态
    private Boolean mMuted = false;

    // 闪光灯状态
    private Boolean mFlashEnabled = false;

    private Boolean mShowDataTip = true;

    private Boolean mFrontCameraMirror = true;

    private TextView mMsgTextview;
    private boolean mIsPlayEnd = false;
    private String mChannelWebId;
    // 美颜处理

    //------------------------------------------------------
//
//    // 要支持多款滤镜，直接添加到数组即可
//    private String[] videoFilters = new String[]{"VideoFair", "VideoWarmSunshine"};
//
//    private int mVideoFilterIndex = 0;
//
//    private GestureDetector mGestureDetector;
//

    String mLiveId;
    String mRtmpUrl;
    PPYLiveView mCameraView;
    PPYStream mPPYStream = new PPYStream();
    boolean mIsStreamingStart = false;
    boolean mIsStartTipNetwork = false;
    boolean mIsInBackground = false;
    long mLastStopTime = 0;
    final long MAX_STOP_TIME = 3*60*1000; // 3分钟
    int mType = 1;
    boolean mIsLandscape = false;
    Handler mHandle = new Handler();

    Runnable mHideMsgRunable = new Runnable() {
        @Override
        public void run() {
            Log.d(ConstInfo.TAG, "excute mHideMsgRunable");
            mMsgTextview.setVisibility(View.GONE);
        }
    };

    Timer mStatusRunableTimer;
    Timer mStartRunableTimer;
    Timer mUpdateDataTipTimer;

    private final long MAX_CONNECT_TIMEOUT = 10*1000;
    private long mLastConnectTime = 0;
    void updateTip()
    {
        if (mPPYStream.IsStreaming())
        {
            int videobitrate = mPPYStream.getVideoBitrate();
            int fps = mPPYStream.getVideoFrameRate();
            int vdeio_w = mPPYStream.getVideoWdith();
            int video_h = mPPYStream.getVideoHeight();
            String str = String.format(getString(R.string.live_data_tip), videobitrate, (int)fps, vdeio_w, video_h);
            mDataTipTextview.setText(str);

            get_watch_url();
        }
    }
    private String mM3u8Url;
    private void get_watch_url()
    {
        if (mM3u8Url != null && !mM3u8Url.isEmpty())
            return;
        PPYRestApi.stream_watch(mLiveId, new PPYRestApi.StringResultMapCallack() {
            @Override
            public void result(int errcode, final Bundle data) {
                if (errcode==0 && data != null)
                {
                    String m3u8Url = data.getString("m3u8Url");
                    if (m3u8Url != null && !m3u8Url.isEmpty())
                    {
                        mM3u8Url = m3u8Url;
                        mChannelWebId = data.getString("channelWebId");
                    }
                }
            }
        });
    }
    ScreenWake mScreenWake = null;
    //AspectFrameLayout mAspectFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.live_streaming_activity);

        mScreenWake = new ScreenWake(getApplicationContext());
        initView();

        mLiveId = getIntent().getStringExtra("liveid");
        mRtmpUrl = getIntent().getStringExtra("rtmpurl");
        mType = getIntent().getIntExtra("type", 0);
        mIsLandscape = getIntent().getBooleanExtra("mode", false);

        mCameraView = (PPYLiveView)findViewById(R.id.lsq_cameraView);

        AppSettingMode.setSetting(this, "last_liveid", mLiveId);
        AppSettingMode.setSetting(this, "last_liveurl", mRtmpUrl);
        AppSettingMode.setIntSetting(this, "last_type", mType);
        AppSettingMode.setSetting(this, "last_mode", mIsLandscape);

        TextView textView = (TextView)findViewById(R.id.liveid);
        textView.setText(getString(R.string.liveid_tip, mLiveId));

        mDataTipTextview = (TextView)findViewById(R.id.msg_tip);
        registerBaseBoradcastReceiver(true);
        InitStream();

    }

    boolean mIsStartCheckStatus = false;
    void checkNetwork()
    {
        if (NetworkUtils.isNetworkAvailable(getApplicationContext()))
        {
            if (mIsStartCheckStatus || mIsPlayEnd)
            {
                Log.d(ConstInfo.TAG, "connect change network avilable and check status is already start, so exit this time");
                return;
            }
            mIsStartCheckStatus = true;
            PPYRestApi.stream_status(mLiveId, new PPYRestApi.StringResultStatusCallack() {
                @Override
                public void result(int errcode, String livestatus, String streamstatus) {

                    mIsStartCheckStatus = false;
                    Log.d(ConstInfo.TAG, "connect change network avilable GET stream_status errcode="+errcode+" livestatus="+livestatus);
                    if (errcode == 0 && livestatus != null)
                    {
                        if (livestatus.equals("stopped"))
                        {
                            hideLoading();
                            show_play_end_popup();
                        }
                        else
                        {
                            hideLoading();
                            if (NetworkUtils.isMobileNetwork(getApplicationContext()))
                            {
                                if (mIsStartTipNetwork)
                                {
                                    Log.d(ConstInfo.TAG, "connect change mobile network avilable tip alert is start, so exit this time");
                                    return;
                                }
                                mIsStartTipNetwork = true;
                                Log.d(ConstInfo.TAG, "connect change mobile network avilable, show tip alert");
                                ConstInfo.showDialog(LiveStreamingActivity.this, getString(R.string.mobile_network_tip), "", getString(R.string.cannel), getString(R.string.ok), new AlertDialogResultCallack() {
                                    @Override
                                    public void cannel() {
                                        finish();
                                    }

                                    @Override
                                    public void ok() {
                                        Log.d(ConstInfo.TAG, "connect change mobile network avilable, use mobile ok");
                                        StartStream();
                                        mIsStartTipNetwork = false;
                                    }
                                });
                            }
                            else
                            {
                                Log.d(ConstInfo.TAG, "connect change wiff network avilable");
                                StartStream();
                            }

                        }
                    }
                    else if (errcode == 98 || errcode == 97)
                    {
                        hideLoading();
                        show_play_end_popup();
                    }
                }
            });
        }
        else
        {
            hideLoading();
            Log.d(ConstInfo.TAG, "connect change network unavilable");
            StopStream();
            mMsgTextview.setText(getString(R.string.no_network));
            mMsgTextview.setVisibility(View.VISIBLE);
            mHandle.removeCallbacks(mHideMsgRunable);
        }
    }


    void StartStream()
    {
        if (mIsStreamingStart || mIsPlayEnd)
            return;
        mIsStreamingStart = true;
        Log.d(ConstInfo.TAG, "StartStream");
        showLoading(getString(R.string.loading_tip));
        mPPYStream.setPublishUrl(mRtmpUrl);
        mPPYStream.StartStream();
        mPPYStream.EnableAudio(!mMuted);
        mStartRunableTimer = new Timer();
        mStartRunableTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(ConstInfo.TAG, "excute mStartRunable");
                PPYRestApi.stream_start(mLiveId, new PPYRestApi.StringResultCallack() {
                    @Override
                    public void result(int errorcode, String data) {
                        if (errorcode != 0)
                        {
                            Log.d(ConstInfo.TAG, "try stream_start agaim");
                        }
                        else
                        {
                            if (mStartRunableTimer != null)
                            {
                                mStartRunableTimer.cancel();
                                mStartRunableTimer.purge();
                                mStartRunableTimer = null;
                            }
                        }
                    }
                });
            }
        }, 1000, 1000);

        mUpdateDataTipTimer = new Timer();
        mUpdateDataTipTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(ConstInfo.TAG, "excute mUpdateDataTipRunable");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTip();
                    }
                });

            }
        }, 1000, 1000);
    }

    boolean mIsReconnectTime = false;
    long mReconnectLastTime = 0;
    final long MAX_RECONNECT_TIMEOUT = 30*1000;

    long mLastLiveStartTime = 0;
    final long MAX_LIVE_TIME = 10*1000;
    PPYStatusListener mPPStatusListener = new PPYStatusListener() {
        @Override
        public void onStateChanged(final int i, final Object o) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(ConstInfo.TAG, "onStateChanged i=" +i);
                    if (i == PPYStatusListener.PPY_SDK_INIT_SUCC)
                    {
                        Log.d(ConstInfo.TAG, "camera init success, start stream ");
                        long currentTime = System.currentTimeMillis();
                        Log.d(ConstInfo.TAG, "camera init success, start stream mLastStopTime="+mLastStopTime+" currentTime="+currentTime);
                        if (mLastStopTime != 0 && (currentTime - mLastStopTime > MAX_STOP_TIME))
                        {
                            hideLoading();
                            stream_stop();
                            show_play_end_popup();
                        }
                        else
                        {
                            mLastStopTime = 0;
                            checkNetwork();
                        }
                    }
                    else if (i == PPY_STREAM_STOP_EXPECTION)
                    {
                        Log.d(ConstInfo.TAG, "onStateChanged PPY_STREAM_STOP_EXPECTION");
                        if (mReconnectLastTime == 0)
                            mReconnectLastTime = System.currentTimeMillis();
                        if (System.currentTimeMillis() - mReconnectLastTime > MAX_RECONNECT_TIMEOUT)
                        {
                            mIsReconnectTime = true;
                            mMsgTextview.setText(getString(R.string.no_network));
                            mMsgTextview.setVisibility(View.VISIBLE);
                            mHandle.removeCallbacks(mHideMsgRunable);
                        }
                        else
                        {
                            mMsgTextview.setText(getString(R.string.network_reconnect));
                            mMsgTextview.setVisibility(View.VISIBLE);
                            mHandle.postDelayed(mHideMsgRunable, 3000);
                        }
                    }
                    else if (i == PPY_STREAM_CONNECTED)
                    {
                        hideLoading();
                        Log.d(ConstInfo.TAG, "onStateChanged PPY_STREAM_CONNECTED");

                        if (!mIsReconnectTime)
                        {
                            mMsgTextview.setText(getString(R.string.push_stream_running));
                            mMsgTextview.setVisibility(View.VISIBLE);
                            mHandle.removeCallbacks(mHideMsgRunable);
                        }

                        //mLastConnectTime = System.currentTimeMillis();

                        if (mStatusRunableTimer != null) {
                            mStatusRunableTimer.cancel();
                            mStatusRunableTimer.purge();
                            mStatusRunableTimer = null;
                        }
                        mStatusRunableTimer = new Timer();
                        mStatusRunableTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.d(ConstInfo.TAG, "excute mStatusRunableTimer");
                                PPYRestApi.stream_status(mLiveId, new PPYRestApi.StringResultStatusCallack() {
                                    @Override
                                    public void result(int errcode, String livestatus, String streamstatus) {
                                        Log.d(ConstInfo.TAG, "mStatusRunableTimer GET stream_status errcode="+errcode+" livestatus="+livestatus+" streamstatus="+streamstatus);
                                        if (errcode == 0 && (livestatus != null && livestatus.equals("living")) && (streamstatus != null && streamstatus.equals("ok"))) {
                                            mIsReconnectTime = false;
                                            mReconnectLastTime = 0;
                                            if (mLastLiveStartTime == 0)
                                                mLastLiveStartTime = System.currentTimeMillis();
                                            mMsgTextview.setText(getString(R.string.push_stream_ok));
                                            mMsgTextview.setVisibility(View.VISIBLE);
                                            mHandle.postDelayed(mHideMsgRunable, 3000);
                                            if (mStatusRunableTimer != null) {
                                                mStatusRunableTimer.cancel();
                                                mStatusRunableTimer.purge();
                                                mStatusRunableTimer = null;
                                            }
                                        }
                                    }
                                });
                            }
                        }, 1000, 1000);
                    }
                    else if (i == PPY_STREAM_DOWN_BITRATE)
                    {
                        if (!mIsReconnectTime)
                        {
                            mMsgTextview.setText(getString(R.string.stream_bitrate_down));
                            mMsgTextview.setVisibility(View.VISIBLE);
                            mHandle.postDelayed(mHideMsgRunable, 3000);
                        }

                    }
                    else if (i == PPY_STREAM_UP_BITRATE)
                    {
                        if (!mIsReconnectTime)
                        {
                            mMsgTextview.setText(getString(R.string.stream_bitrate_up));
                            mMsgTextview.setVisibility(View.VISIBLE);
                            mHandle.postDelayed(mHideMsgRunable, 3000);
                        }
                    }
                    else if (i == PPY_STREAM_RECONNECT_TIME)
                    {
                        Log.d(ConstInfo.TAG, "onStateChanged PPY_STREAM_RECONNECT_TIME");
                        mIsReconnectTime = true;
                        mMsgTextview.setText(getString(R.string.no_network));
                        mMsgTextview.setVisibility(View.VISIBLE);
                        mHandle.removeCallbacks(mHideMsgRunable);
                    }
                }
            });

        }
    };

    private void StopStream()
    {
        if (!mIsStreamingStart)
            return;
        mIsStreamingStart = false;
        Log.d(ConstInfo.TAG, "StopStream");
        mPPYStream.StopStream();

        if (mStatusRunableTimer != null) {
            mStatusRunableTimer.cancel();
            mStatusRunableTimer.purge();
            mStatusRunableTimer = null;
        }

        if (mStartRunableTimer != null)
        {
            mStartRunableTimer.cancel();
            mStartRunableTimer.purge();
            mStartRunableTimer = null;
        }
        if (mUpdateDataTipTimer != null)
        {
            mUpdateDataTipTimer.cancel();
            mUpdateDataTipTimer.purge();
            mUpdateDataTipTimer = null;
        }
    }

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public void InitStream()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission_group.CAMERA, Manifest.permission_group.MICROPHONE, Manifest.permission_group.STORAGE},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else
        {
            InitStreamImpl();
        }
    }

    public void InitStreamImpl()
    {
        Log.d(ConstInfo.TAG, "InitStreamImpl init stream");
        PPYStreamerConfig config = new PPYStreamerConfig();
        config.setDefaultLandscape(mIsLandscape);
        if (mIsLandscape)
        {
            // 横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else {
            // 竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (mType == 0)
        {
            config.setVideoResolution(VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_480P);
            config.setVideoBitrate(800);
        }
        else if (mType == 1)
        {
            config.setVideoResolution(VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_540P);
            config.setVideoBitrate(1000);
        }
        else if (mType == 2)
        {
            config.setVideoResolution(VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_720P);
            config.setVideoBitrate(1400);
        }
        else
        {
            config.setVideoResolution(VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_480P);
            config.setVideoBitrate(800);
        }
        config.setFrameRate(24);
        mPPYStream.CreateStream(getApplicationContext(), config, mCameraView);
        mPPYStream.setPPYStatusListener(mPPStatusListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                InitStreamImpl();
            } else
            {
                // Permission Denied
                mMsgTextview.setText(getString(R.string.camera_premission_fail));
                mMsgTextview.setVisibility(View.VISIBLE);
                mHandle.removeCallbacks(mHideMsgRunable);
                //Toast.makeText(LiveStreamingActivity.this, "相机权限不够", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                if (mIsInBackground || mIsPlayEnd)
                    return;
                Log.d(ConstInfo.TAG, "connect change");
                checkNetwork();
            }
        }
    };

    @Override
    protected  void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mScreenWake.disable();

        Log.d(ConstInfo.TAG, "onResume");
        mPPYStream.OnResume();
        mIsInBackground = false;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mScreenWake.enable();
        mLastStopTime = System.currentTimeMillis();
        Log.d(ConstInfo.TAG, "onPause mLastStopTime="+mLastStopTime);
        mPPYStream.OnPause();
        StopStream();

        mIsInBackground = true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(ConstInfo.TAG, "onDestroy");

        stream_stop();

        AppSettingMode.setSetting(LiveStreamingActivity.this, "last_liveid", "");
        AppSettingMode.setSetting(LiveStreamingActivity.this, "last_liveurl", "");
        AppSettingMode.setIntSetting(LiveStreamingActivity.this, "last_type", 0);
        AppSettingMode.setSetting(this, "last_mode", mIsLandscape);

        registerBaseBoradcastReceiver(false);

        mPPYStream.setPPYStatusListener(null);
        mPPYStream.OnDestroy();
    }

    //--------------------------------------------------  界面处理 ------------------------------------------------

    boolean mIsShowControlPanel = false;
    LinearLayout beauty_control_container;
    LinearLayout up_control_container;
    float mBeautyWhite = 0.5f;
    float mBeautyBright = 0.5f;
    float mBeautyTone = 0.5f;


    private void initView()
    {
        mMsgTextview = (TextView)findViewById(R.id.msg_live);
        mMsgTextview.setVisibility(View.GONE);

        mCloseButton = (ImageButton)findViewById(R.id.lsq_closeButton);
        mCloseButton.setOnClickListener(mButtonClickListener);

        mBeautyButton = (Button)findViewById(R.id.lsq_beautyButton);
        mBeautyButton.setOnClickListener(mButtonClickListener);

        mFlashButton = (Button)findViewById(R.id.lsq_flashhightButton);
        mFlashButton.setOnClickListener(mButtonClickListener);

        mMuteButton = (Button)findViewById(R.id.lsq_muteButton);
        mMuteButton.setOnClickListener(mButtonClickListener);

        mToggleButton = (Button)findViewById(R.id.lsq_cameraroationButton);
        mToggleButton.setOnClickListener(mButtonClickListener);

        mDataTipButton = (Button)findViewById(R.id.button_data_tip);
        mDataTipButton.setOnClickListener(mButtonClickListener);

        mToggleMirrorButton = (Button)findViewById(R.id.button_mirror);
        mToggleMirrorButton.setOnClickListener(mButtonClickListener);


        CheckBox checkbox_beauty = (CheckBox)findViewById(R.id.checkbox_beauty);
        checkbox_beauty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                mPPYStream.EnableBeauty(b);
                if (b)
                    mPPYStream.SetBeautyParam(mBeautyWhite, mBeautyBright, mBeautyTone);
            }
        });
        beauty_control_container = (LinearLayout)findViewById(R.id.beauty_control_container);
        beauty_control_container.setVisibility(mBeautyEnabled?View.VISIBLE:View.GONE);

        up_control_container = (LinearLayout)findViewById(R.id.control_container);
        up_control_container.setVisibility(mIsShowControlPanel?View.VISIBLE:View.GONE);
        Button upButton = (Button)findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsShowControlPanel = !mIsShowControlPanel;
                up_control_container.setVisibility(mIsShowControlPanel?View.VISIBLE:View.GONE);
                mBeautyEnabled = false;
                beauty_control_container.setVisibility(View.GONE);
            }
        });


        final TextView textview_beauty_white = (TextView)findViewById(R.id.textview_beauty_white);
        SeekBar seekbar_beauty_white = (SeekBar)findViewById(R.id.seekbar_beauty_white);
        seekbar_beauty_white.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textview_beauty_white.setText(seekBar.getProgress()+"%");
                mBeautyWhite = seekBar.getProgress()/100.0f;
                mPPYStream.SetBeautyParam(mBeautyWhite, mBeautyBright, mBeautyTone);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        final TextView textview_beauty_bright = (TextView)findViewById(R.id.textview_beauty_bright);
        SeekBar seekbar_beauty_bright = (SeekBar)findViewById(R.id.seekbar_beauty_bright);
        seekbar_beauty_bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textview_beauty_bright.setText(seekBar.getProgress()+"%");
                mBeautyBright = seekBar.getProgress()/100.0f;
                mPPYStream.SetBeautyParam(mBeautyWhite, mBeautyBright, mBeautyTone);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        final TextView textview_beauty_tone = (TextView)findViewById(R.id.textview_beauty_tone);
        SeekBar seekbar_beauty_tone = (SeekBar)findViewById(R.id.seekbar_beauty_tone);
        seekbar_beauty_tone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textview_beauty_tone.setText(seekBar.getProgress()+"%");
                mBeautyTone = seekBar.getProgress()/100.0f;
                mPPYStream.SetBeautyParam(mBeautyWhite, mBeautyBright, mBeautyTone);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        updateMuteButtonStatus();

//        updateBeautyButtonStatus();

        updateFlashButtonStatus();
    }

    /** 按钮点击事件处理 */
    private View.OnClickListener mButtonClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if (v == mCloseButton)
            {
                ConstInfo.showDialog(LiveStreamingActivity.this, getString(R.string.close_tip), "", getString(R.string.cannel), getString(R.string.ok), new AlertDialogResultCallack() {
                    @Override
                    public void cannel() {
                    }

                    @Override
                    public void ok() {
                        mPPYStream.OnPause();
                        StopStream();
                        mIsInBackground = true;
                        stream_stop();

                        show_play_end_popup();
                    }
                });
            }
            else if (v == mBeautyButton)
            {
                mBeautyEnabled = !mBeautyEnabled;

                beauty_control_container.setVisibility(mBeautyEnabled?View.VISIBLE:View.GONE);
                mIsShowControlPanel = false;
                up_control_container.setVisibility(View.GONE);
                //updateBeautyButtonStatus();
            }
            else if (v == mFlashButton)
            {
                mFlashEnabled = !mFlashEnabled;

                mPPYStream.setFlashLightState(mFlashEnabled);
                // 闪光灯
                updateFlashButtonStatus();
            }
            else if (v == mMuteButton)
            {
                mMuted = !mMuted;
                mPPYStream.EnableAudio(!mMuted);
                updateMuteButtonStatus();
            }
            else if (v == mToggleButton)
            {
                mPPYStream.SwitchCamera();
            }
            else if (v == mDataTipButton)
            {
                mShowDataTip = !mShowDataTip;
                updateShowStatus(mShowDataTip);
            }
            else if (v == mToggleMirrorButton)
            {
                mFrontCameraMirror = !mFrontCameraMirror;
                mPPYStream.EnableFrontCameraMirror(mFrontCameraMirror);
            }
        }
    };

    @Override
    public void onBackPressed()
    {
        ConstInfo.showDialog(LiveStreamingActivity.this, getString(R.string.close_tip), "", getString(R.string.cannel), getString(R.string.ok), new AlertDialogResultCallack() {
            @Override
            public void cannel() {
            }

            @Override
            public void ok() {
                mPPYStream.OnPause();
                StopStream();
                mIsInBackground = true;


                show_play_end_popup();
            }
        });
    }

    private void stream_stop()
    {
        if (mLiveId.isEmpty())
        {
            Log.d(ConstInfo.TAG, "liveid is already stop!");
            return;
        }
        PPYRestApi.stream_stop(mLiveId, new PPYRestApi.StringResultCallack() {
            @Override
            public void result(int errcode, String data) {
                if (errcode == 0)
                    mLiveId = "";
            }
        });
    }
    private void updateMuteButtonStatus()
    {
        if (mMuteButton != null)
        {
            int imgID = mMuted ? R.drawable.audio_close : R.drawable.audio_open;

            mMuteButton.setBackgroundResource(imgID);
        }
    }

    private void updateFlashButtonStatus()
    {
        if (mFlashButton != null)
        {
            int imgID = mFlashEnabled ? R.drawable.flashlight_open : R.drawable.flashlight_close;

            mFlashButton.setBackgroundResource(imgID);
        }
    }

//    private void changeVideoFilterCode(String code)
//    {
//        mPPYStream.EnableBeauty(mBeautyEnabled);
//    }

//    private void updateBeautyButtonStatus()
//    {
//        int imgID = mBeautyEnabled ? R.drawable.beatuy_open : R.drawable.beatuy_close;
//
//        if (mBeautyButton != null)
//            mBeautyButton.setBackgroundResource(imgID);
//    }


    /**
     * 更新操作按钮
     *
     * @param isRunning 是否直播中
     */
    private void updateShowStatus(Boolean isRunning)
    {
        int imgID = isRunning ? R.drawable.data_tip_rtmp_open : R.drawable.data_tip_rtmp_close;

        mDataTipTextview.setVisibility(isRunning?View.VISIBLE:View.GONE);

        if (mDataTipButton != null)
            mDataTipButton.setBackgroundResource(imgID);
    }

    PopupWindow mPlayEndPopupWindow;
    public void show_play_end_popup()
    {
        if (mPlayEndPopupWindow == null)
            create_play_end_popup(null);
        if (mPlayEndPopupWindow != null && !mPlayEndPopupWindow.isShowing())
            mPlayEndPopupWindow.showAtLocation(mCloseButton, Gravity.CENTER, 0, 0);
        StopStream();
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
        //final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.popup_bg);
        ImageButton close = (ImageButton)dialogView.findViewById(R.id.close);
        Button button_watch_video = (Button)dialogView.findViewById(R.id.button_watch_video);
        button_watch_video.setOnTouchListener(new View.OnTouchListener() {
              @Override
              public boolean onTouch(View view, MotionEvent motionEvent) {
                  if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                      if (TextUtils.isEmpty(mM3u8Url))
                      {
                          // toast
                          Toast.makeText(getApplication(), "网络错误", Toast.LENGTH_SHORT).show();
                      }
                      else
                      {
                          PPYRestApi.stream_detail(mChannelWebId, new PPYRestApi.StringResultMapCallack() {
                              @Override
                              public void result(int errcode, Bundle result) {
                                  if (errcode == 0 && result != null)
                                  {
                                      int duration = result.getInt("duration");
                                      if (duration > 10)
                                      {
                                          Intent intent = new Intent(LiveStreamingActivity.this, WatchVideoActivity.class);
                                          intent.putExtra("m3u8Url", mM3u8Url);
                                          intent.putExtra("channelWebId", mChannelWebId);
                                          startActivity(intent);
                                          hide_play_end_popup();
                                      }
                                      else
                                      {
                                          Toast.makeText(getApplication(), "直播时长太短，不能观看", Toast.LENGTH_SHORT).show();
                                          hide_play_end_popup();
                                      }
                                  }
                              }
                          });

                      }
                  }
                  return false;
              }
        });
//        final ImageView bg = (ImageView)dialogView.findViewById(R.id.bg);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try{
//                    final Bitmap fastblurBitmap = ConstInfo.fastblur(bitmap, 40);
//                    mHandle.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            bg.setImageBitmap(fastblurBitmap);
//                        }
//                    });
//                }catch (Exception e) { e.printStackTrace();}
//            }
//        }).start();

        dialogView.setFocusable(true);
        dialogView.setFocusableInTouchMode(true);

        mPlayEndPopupWindow = new PopupWindow(dialogView, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        //在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        mPlayEndPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPlayEndPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //点击空白处时，隐藏掉pop窗口
        mPlayEndPopupWindow.setFocusable(true);
//        mPlayEndPopupWindow.setBackgroundDrawable(new BitmapDrawable());

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
                            Log.d(ConstInfo.TAG, "mPlayEndPopupWindow KEYCODE_BACK");
                            mPlayEndPopupWindow.dismiss();
                            return false;
                    }
                }
                return true;
            }
        });

    }

}
