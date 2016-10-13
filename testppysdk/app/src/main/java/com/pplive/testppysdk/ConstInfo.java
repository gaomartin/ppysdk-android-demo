package com.pplive.testppysdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.util.HashMap;

/**
 * Created by ballackguan on 2016/8/25.
 */
public class ConstInfo {
    public static String TAG = "---PPYSDK---";


    public static void showTipDialog(Context context, String title, String msg, String ok_title, final AlertDialogResultCallack callback) {
        if(context!=null && context instanceof Activity) {
            Activity activity = (Activity)context;
            if(activity.isFinishing()) {
                return;
            }
        }
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout dialogView = (LinearLayout)layoutInflater.inflate(R.layout.layout_tip_dialog, null);
        TextView _t = (TextView)dialogView.findViewById(R.id.title);
        _t.setText(title);
        TextView _msg = (TextView)dialogView.findViewById(R.id.msg);
        if (TextUtils.isEmpty(msg))
            _msg.setVisibility(View.GONE);
        else
            _msg.setText(msg);

        Button _ok = (Button)dialogView.findViewById(R.id.ok);
        if (!TextUtils.isEmpty(ok_title))
            _ok.setText(ok_title);

        final Dialog mPopupWindow = new Dialog(context, R.style.dialogcustom);
        WindowManager.LayoutParams lp = mPopupWindow.getWindow().getAttributes();
        lp.dimAmount = 0.5f;
        lp.gravity = Gravity.CENTER;
        mPopupWindow.getWindow().setAttributes(lp);
        mPopupWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mPopupWindow.setContentView(dialogView);
        mPopupWindow.setCanceledOnTouchOutside(false);

        _ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                if (callback != null)
                    callback.ok();
            }
        });
        mPopupWindow.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (callback != null)
                    callback.cannel();
            }
        });
        mPopupWindow.show();
    }
    private static boolean isFirst = true;
    public static void showDialog(Context context, String title, String msg, String cancel_title, String ok_title, final AlertDialogResultCallack callback) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout dialogView = (LinearLayout)layoutInflater.inflate(R.layout.layout_alert_dialog, null);
        TextView _t = (TextView)dialogView.findViewById(R.id.title);
        _t.setText(title);
        TextView _msg = (TextView)dialogView.findViewById(R.id.msg);
        if (TextUtils.isEmpty(msg))
            _msg.setVisibility(View.GONE);
        else
            _msg.setText(msg);

        final Button _cannel = (Button)dialogView.findViewById(R.id.cannel);
        if (!TextUtils.isEmpty(cancel_title))
            _cannel.setText(cancel_title);
       // _cannel.requestFocus();
        final Button _ok = (Button)dialogView.findViewById(R.id.ok);
        if (!TextUtils.isEmpty(ok_title))
            _ok.setText(ok_title);

        final Dialog mPopupWindow = new Dialog(context, R.style.dialogcustom);
        WindowManager.LayoutParams lp = mPopupWindow.getWindow().getAttributes();
        lp.dimAmount = 0.5f;
        lp.gravity = Gravity.CENTER;
        mPopupWindow.getWindow().setAttributes(lp);
        mPopupWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mPopupWindow.setContentView(dialogView);
        mPopupWindow.setCanceledOnTouchOutside(true);

        _cannel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    mPopupWindow.dismiss();
                    if (callback != null)
                        callback.cannel();
                }
                return false;
            }
        });

        _ok.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    mPopupWindow.dismiss();
                    if (callback != null)
                        callback.ok();
                }
                return false;
            }
        });
        mPopupWindow.show();
    }

    public static void showEditDialog(Context context, final AlertDialogResult2Callack callback) {
        if(context!=null && context instanceof Activity) {
            Activity activity = (Activity)context;
            if(activity.isFinishing()) {
                return;
            }
        }
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout dialogView = (LinearLayout)layoutInflater.inflate(R.layout.layout_dialog, null);

        final EditText _liveid = (EditText)dialogView.findViewById(R.id.liveid);

        Button _ok = (Button)dialogView.findViewById(R.id.ok);
        Button _cannel = (Button)dialogView.findViewById(R.id.cannel);
        final Dialog mPopupWindow = new Dialog(context, R.style.dialogcustom);
        WindowManager.LayoutParams lp = mPopupWindow.getWindow().getAttributes();
        lp.dimAmount = 0.5f;
        lp.gravity = Gravity.CENTER;
        mPopupWindow.getWindow().setAttributes(lp);
        mPopupWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mPopupWindow.setContentView(dialogView);
        mPopupWindow.setCanceledOnTouchOutside(false);

        _ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                if (callback != null)
                    callback.ok(_liveid.getText().toString());
            }
        });
        _cannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                if (callback != null)
                    callback.cannel();
            }
        });
        mPopupWindow.show();
    }

    public static void showDialogEx(Context context, View locationView, String title, String msg, String cancel_title, String ok_title, final AlertDialogResultCallack callback) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout dialogView = (LinearLayout)layoutInflater.inflate(R.layout.layout_alert_dialog, null);
        TextView _t = (TextView)dialogView.findViewById(R.id.title);
        _t.setText(title);
        TextView _msg = (TextView)dialogView.findViewById(R.id.msg);
        if (TextUtils.isEmpty(msg))
            _msg.setVisibility(View.GONE);
        else
            _msg.setText(msg);

        Button _cannel = (Button)dialogView.findViewById(R.id.cannel);
        if (!TextUtils.isEmpty(cancel_title))
            _cannel.setText(cancel_title);
        _cannel.requestFocus();
        Button _ok = (Button)dialogView.findViewById(R.id.ok);
        if (!TextUtils.isEmpty(ok_title))
            _ok.setText(ok_title);

        final PopupWindow mPopupWindow = new PopupWindow(dialogView, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //在PopupWindow里面就加上下面代码，让键盘弹出时，不会挡住pop窗口。
        mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //点击空白处时，隐藏掉pop窗口
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
//                if (result2Callack != null)
//                    result2Callack.cannel();
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
        mPopupWindow.showAtLocation(locationView, Gravity.CENTER, 0, 0);
    }

    /*
     * This method was copied from http://stackoverflow.com/a/10028267/694378.
     * The only modifications I've made are to remove a couple of Log
     * statements which could slow things down slightly.
     */
    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

//    private static int totalSeconds(String startTime, String endTime) {
//
//        String[] st = startTime.split(":");
//        String[] et = endTime.split(":");
//
//        int st_h = Integer.valueOf(st[0]);
//        int st_m = Integer.valueOf(st[1]);
//        int st_s = Integer.valueOf(st[2]);
//
//        int et_h = Integer.valueOf(et[0]);
//        int et_m = Integer.valueOf(et[1]);
//        int et_s = Integer.valueOf(et[2]);
//
//        int totalSeconds = (et_h - st_h) * 3600 + (et_m - st_m) * 60
//                + (et_s - st_s);
//
//        return totalSeconds;
//
//    }

    /**
     * 根据当前选择的秒数还原时间点
     *
     * @param args
     */

    public static String progress2TimeString(int progress) {
        String return_h = "", return_m = "", return_s = "";
        int h = progress / 3600;
        int m = (progress % 3600) / 60;
        int s = (progress % 3600) % 60;

        if (h < 10) {
            return_h = "0" + (h);
        } else {
            return_h = h + "";
        }
        if ((m) < 10) {
            return_m = "0" + (m);
        } else {
            return_m = m + "";
        }
        if ((s) < 10) {
            return_s = "0" + (s);
        } else {
            return_s = s + "";
        }
        return return_h + ":" + return_m + ":" + return_s;
    }

    private static void setImageView(String imageUri, Bitmap loadedImage, View parentView) {
        if (parentView != null) {
            ImageView image = (ImageView) parentView.findViewWithTag(imageUri);
            while (image != null)
            {
//				LogUtils.d("setImageView tag i="+i+" imageurl="+imageUri);
                image.setTag("");
                image.setImageBitmap(loadedImage);
                image = (ImageView) parentView.findViewWithTag(imageUri);
            }
        }
    }

    public static void loadImage(String imageUrl, int defaultResId, ImageSize imageSize, final ImageView imageView, final View parentView) {
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setTag("");
            imageView.setImageResource(defaultResId);
            return;
        }

        String memoryCacheKey = MemoryCacheUtils.generateKey(imageUrl, imageSize);
        Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(memoryCacheKey);
        if (bitmap != null && !bitmap.isRecycled())
        {
            imageView.setTag("");
            //LogUtils.d("use cache imageurl="+memoryCacheKey);
            imageView.setImageBitmap(bitmap);
        }
        else {
            //LogUtils.d("reload imageurl="+imageUrl);
            imageView.setTag(imageUrl);
            imageView.setImageResource(defaultResId);
            ImageLoader.getInstance().loadImage(imageUrl, imageSize, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    //LogUtils.d("reload onLoadingFailed imageurl="+imageUri);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //LogUtils.d("reload onLoadingComplete imageurl="+imageUri);
                    if (parentView != null) {
                        setImageView(imageUri, loadedImage, parentView);
                    }
                    else
                        imageView.setImageBitmap(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    //LogUtils.d("reload onLoadingCancelled imageurl="+imageUri);
                }
            });
        }
    }
}
