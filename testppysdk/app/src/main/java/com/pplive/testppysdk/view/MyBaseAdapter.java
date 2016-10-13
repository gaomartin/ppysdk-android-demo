package com.pplive.testppysdk.view;

import android.database.DataSetObserver;
import android.widget.BaseAdapter;

public abstract class MyBaseAdapter extends BaseAdapter {

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }
}