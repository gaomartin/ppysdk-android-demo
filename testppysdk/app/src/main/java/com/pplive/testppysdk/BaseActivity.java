package com.pplive.testppysdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Dialog mPopupWindow = null;
    public void showLoading(String tip) {
        if (mPopupWindow == null)
            mPopupWindow = showLoadingDialog(this, tip);
        if(mPopupWindow != null)
            mPopupWindow.show();
    }
    public void hideLoading() {
        if (mPopupWindow != null && !isFinishing()) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }
    private Dialog showLoadingDialog(Context context, String tip) {
        if(context!=null && context instanceof Activity) {
            Activity activity = (Activity)context;
            if(activity.isFinishing()) {
                return null;
            }
        }
        Dialog mPopupWindow = new Dialog(context, R.style.dialogcustom);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.layout_loading_toast_small, null);
        if (!TextUtils.isEmpty(tip))
        {
            TextView title = (TextView) contentView.findViewById(R.id.title);
            title.setText(tip);
        }
        mPopupWindow.setContentView(contentView);

        WindowManager.LayoutParams lp = mPopupWindow.getWindow().getAttributes();
        lp.dimAmount = 0.5f;
        lp.gravity = Gravity.CENTER;
        mPopupWindow.getWindow().setAttributes(lp);
        mPopupWindow.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mPopupWindow.setContentView(contentView);
//        mPopupWindow.setCanceledOnTouchOutside(false);
        return mPopupWindow;
    }

}
