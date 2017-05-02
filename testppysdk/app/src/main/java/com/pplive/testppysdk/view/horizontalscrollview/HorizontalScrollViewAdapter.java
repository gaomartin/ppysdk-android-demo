package com.pplive.testppysdk.view.horizontalscrollview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pplive.testppysdk.Ft;
import com.pplive.testppysdk.R;

import java.util.ArrayList;

public class HorizontalScrollViewAdapter
{
	private LayoutInflater mInflater;
	private ArrayList<Ft> mFts = new ArrayList<>();
	private int mCurrentFt = 0;

	public HorizontalScrollViewAdapter(Context context)
	{
		mInflater = LayoutInflater.from(context);
	}

	public void updateData(ArrayList<Ft> fts)
	{
		mFts = fts;
		if (mFts == null)
			mFts = new ArrayList<>();
	}
	public void updateCurrentFt(int ft)
	{
		mCurrentFt = ft;
	}
	public int getCount()
	{
		return mFts.size();
	}

	public Object getItem(int position)
	{
		return mFts.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.layout_ft_item, parent, false);
			viewHolder.checkBox = (CheckBox) convertView
					.findViewById(R.id.ft_checkbox);
			convertView.setTag(viewHolder);
		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.checkBox.setText(mFts.get(position).getText());
		viewHolder.checkBox.setChecked(mCurrentFt == mFts.get(position).getFt());

		return convertView;
	}

	private class ViewHolder
	{
		CheckBox checkBox;
	}

}
