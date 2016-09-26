package com.pplive.testppysdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.pplive.ppysdk.PPYStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends BaseActivity{

    Button start_live_streaming;
    boolean isFirst = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getWindow().setSoftInputMode( WindowManager.LayoutParams.INPUT_METHOD_NOT_NEEDED);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        start_live_streaming = (Button)findViewById(R.id.start_live_streaming);
        start_live_streaming.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    start_live_streaming();
                }
                return false;
            }
        });

        final Button start_watch_streaming = (Button)findViewById(R.id.start_watch_streaming);
        start_watch_streaming.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    start_watch_streaming();
                }
                return false;
            }
        });

    }
//
//    public void backgroundAlpha(float bgAlpha)
//    {
//        WindowManager.LayoutParams lp = ((Activity) this).getWindow()
//        .getAttributes();
//        lp.alpha = bgAlpha;
//        ((Activity) this).getWindow().setAttributes(lp);
//    }
    public void popup_room_input_win(final boolean is_live_stream, final AlertDialogResult3Callack result2Callack)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final RelativeLayout dialogView = (RelativeLayout)layoutInflater.inflate(R.layout.layout_room_dialog, null);
                ImageButton close = (ImageButton)dialogView.findViewById(R.id.close);
                final Button button_room_ok = (Button)dialogView.findViewById(R.id.button_room_ok);
                button_room_ok.setEnabled(false);

                final RadioGroup radio_group_container = (RadioGroup)dialogView.findViewById(R.id.radio_group_container);
                radio_group_container.setVisibility(is_live_stream?View.VISIBLE:View.GONE);

                final EditText edit_text_room = (EditText)dialogView.findViewById(R.id.edit_text_room);
                edit_text_room.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String roomid = editable.toString();
                        roomid.trim();
                        button_room_ok.setEnabled(!roomid.isEmpty());
                    }
                });
                button_room_ok.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        {
                            if (result2Callack != null)
                            {
                                String liveid = edit_text_room.getText().toString();
                                liveid.trim();

                                HashMap<String, Object> result = new HashMap<String, Object>();
                                result.put("liveid", liveid);

                                if (is_live_stream)
                                {
                                    int type = 0;
                                    int id = radio_group_container.getCheckedRadioButtonId();
                                    if (id == R.id.RADIO_BUTTON_480P)
                                        type = 0;
                                    else  if (id == R.id.RADIO_BUTTON_540P)
                                        type = 1;
                                    else  if (id == R.id.RADIO_BUTTON_720P)
                                        type = 2;

                                    result.put("type", type);
                                }

                                result2Callack.ok(result);
                            }
                        }
                        return false;
                    }
                });

                final PopupWindow mPopupWindow = new PopupWindow(dialogView, RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                //在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
                mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                //点击空白处时，隐藏掉pop窗口
                mPopupWindow.setFocusable(true);
                mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

                mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        if (result2Callack != null)
                            result2Callack.cannel();
                    }
                });
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPopupWindow.dismiss();
                    }
                });
                dialogView.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            switch(keyCode) {
                                case KeyEvent.KEYCODE_BACK:
                                    mPopupWindow.dismiss();
                                    return false;
                            }
                        }
                        return true;
                    }
                });
                mPopupWindow.showAtLocation(start_live_streaming, Gravity.CENTER, 0, 0);
            }
        });
    }
//    boolean mIsLiveStreaming = false;
    public void start_live_streaming_impl()
    {

        popup_room_input_win(true, new AlertDialogResult3Callack() {
            @Override
            public void cannel() {

            }

            @Override
            public void ok(HashMap<String, Object> result) {
//                if (mIsLiveStreaming)
//                {
//                    Log.d(ConstInfo.TAG, "popup_room_input_win is running, so exit this time");
//                    return;
//                }
//                mIsLiveStreaming = true;
                final int type = (int)result.get("type");
                final String liveid = (String)result.get("liveid");
                showLoading("");
                PPYRestApi.stream_create(liveid, new PPYRestApi.StringResultCallack() {
                    @Override
                    public void result(final int errcode, final String url) {
                        hideLoading();
                        if (errcode==0 && url != null && !url.isEmpty())
                        {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(MainActivity.this, LiveStreamingActivity.class);
                                    intent.putExtra("rtmpurl", url);
                                    intent.putExtra("liveid", liveid);
                                    intent.putExtra("type", type);
                                    startActivity(intent);
                                }
                            });
                        }
                        else
                        {
                            if (errcode == 399995)
                                Toast.makeText(getApplication(), "创建直播失败: 房间号已存在", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getApplication(), "创建直播失败: 网络错误", Toast.LENGTH_SHORT).show();
                        }
//                        mIsLiveStreaming = false;
                    }
                });
            }
        });
    }
    public void start_live_streaming()
    {
        final String last_liveid = AppSettingMode.getSetting(this, "last_liveid", "");
        final String last_liveurl = AppSettingMode.getSetting(this, "last_liveurl", "");
        final int last_type = AppSettingMode.getIntSetting(this, "last_type", 0);
        if (last_liveid == null || last_liveid.isEmpty() || last_liveurl == null || last_liveurl.isEmpty())
        {
            start_live_streaming_impl();
        }
        else
        {
            showLoading("");
            PPYRestApi.stream_status(last_liveid, new PPYRestApi.StringResultStatusCallack() {
                @Override
                public void result(int errcode,String livestatus, String streamstatus) {
                    hideLoading();
                    Log.d(ConstInfo.TAG, "GET stream_status errcode="+errcode+" livestatus="+livestatus);
                    if (errcode == 0 && livestatus != null && streamstatus != null)
                    {
                        if (!livestatus.equals("stopped"))
                        {
                            Log.d(ConstInfo.TAG, "GET stream_status last liveid="+last_liveid+" is ok");
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(MainActivity.this, LiveStreamingActivity.class);
                                    intent.putExtra("rtmpurl", last_liveurl);
                                    intent.putExtra("liveid", last_liveid);
                                    intent.putExtra("type", last_type);
                                    startActivity(intent);
                                }
                            });
                            return;
                        }
                    }
                    start_live_streaming_impl();

                }
            });

        }
    }
    public void start_watch_streaming()
    {
        popup_room_input_win(false, new AlertDialogResult3Callack() {
            @Override
            public void cannel() {

            }

            @Override
            public void ok(HashMap<String, Object> result) {
                final String liveid = (String)result.get("liveid");
                showLoading("");
                PPYRestApi.stream_watch(liveid, new PPYRestApi.StringResultWatchCallack() {
                    @Override
                    public void result(int errcode, final String rtmpurl, final String live2url) {
                        hideLoading();
                        if (errcode==0)
                        {
                            if (rtmpurl != null && !rtmpurl.isEmpty())
                            {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, WatchStreamingActivity.class);
                                        intent.putExtra("liveurl", rtmpurl);
                                        intent.putExtra("liveid", liveid);
                                        startActivity(intent);
                                    }
                                });
                            }
                            else
                                Toast.makeText(getApplication(), "主播已断开", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            if (errcode == 1001)
                                Toast.makeText(getApplication(), "观看直播失败: 房间直播流状态无效", Toast.LENGTH_SHORT).show();
                            else if (errcode == 300005)
                                Toast.makeText(getApplication(), "观看直播失败: 房间号不存在", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getApplication(), "观看直播失败: 网络错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
   // @Override
    public void onClick(View view) {
        switch (view.getId())
        {
//            case R.id.start_live_streaming:
//            {
//                start_live_streaming();
//            }
//            break;
//            case R.id.start_watch_streaming:
//            {
//                start_watch_streaming();
//            }
//            break;
            case R.id.start_watch_live2_streaming:
            {
                ConstInfo.showEditDialog(MainActivity.this, new AlertDialogResult2Callack() {
                    @Override
                    public void cannel() {

                    }

                    @Override
                    public void ok(final String result) {
                        showLoading("");
                        PPYRestApi.stream_watch(result, new PPYRestApi.StringResultWatchCallack() {
                            @Override
                            public void result(int errcode, final String rtmpurl, final String m3u8Url) {
                                hideLoading();
                                if (errcode==0)
                                {
                                    if (rtmpurl != null && !rtmpurl.isEmpty())
                                    {
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(MainActivity.this, WatchStreamingActivity.class);
                                                intent.putExtra("liveurl", m3u8Url);
                                                intent.putExtra("liveid", result);
                                                startActivity(intent);
                                            }
                                        });
                                    }
                                    else
                                        Toast.makeText(getApplication(), "主播已断开", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    if (errcode == 1001)
                                        Toast.makeText(getApplication(), "观看直播失败: 房间直播流状态无效", Toast.LENGTH_SHORT).show();
                                    else if (errcode == 300005)
                                        Toast.makeText(getApplication(), "观看直播失败: 房间号不存在", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(getApplication(), "观看直播失败: 网络错误", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
            break;
        }
    }
}
