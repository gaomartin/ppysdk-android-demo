package com.pplive.testppysdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
public class NoScrollbarGridView extends GridView {
    private boolean mIsFromMeasure = false;
    public NoScrollbarGridView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public NoScrollbarGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollbarGridView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mIsFromMeasure = true;
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int position = pointToPosition((int) event.getX(), (int) event.getY());
        if (position == -1)
            return false;
        return super.onTouchEvent(event);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mIsFromMeasure = false;
        super.onLayout(changed, l, t, r, b);
    }

    public boolean isFromMeasure() {return mIsFromMeasure;}



    public static class LayoutParams extends RelativeLayout.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}