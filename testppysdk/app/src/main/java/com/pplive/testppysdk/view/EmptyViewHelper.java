package com.pplive.testppysdk.view;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pplive.testppysdk.R;

public final class EmptyViewHelper {
    private Activity mParentActivity;
    private View mEmptyView;
    private TextView mTextView;
    private ImageView mImageView;

    public EmptyViewHelper(Activity activity) {
        mParentActivity = activity;
        init();
    }

    private void init() {
        mEmptyView = mParentActivity.getLayoutInflater().inflate(R.layout.layout_emptytip_item, null);
        mTextView = (TextView)mEmptyView.findViewById(R.id.tip);
        mImageView = (ImageView)mEmptyView.findViewById(R.id.image);
        mEmptyView.setVisibility(View.INVISIBLE);
        mParentActivity.addContentView(mEmptyView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }

    public void update(final String text, final int resId) {
        mTextView.setText(text);
        mImageView.setImageResource(resId);
    }

    public void showEmptyView(boolean show) {
        if(show) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.INVISIBLE);
        }
    }
}
