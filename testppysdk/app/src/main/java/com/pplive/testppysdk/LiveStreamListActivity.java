package com.pplive.testppysdk;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.pplive.testppysdk.view.EmptyViewHelper;
import com.pplive.testppysdk.view.MyBaseAdapter;
import com.pplive.testppysdk.view.NoScrollbarGridView;

public class LiveStreamListActivity extends BaseActivity
{
	private PullToRefreshGridView mPullRefreshListView;
	private PictureAdapter mVideoListAdapter;
	private EmptyViewHelper mEmptyViewHelper;
	private ArrayList<VideoItemInfo> mVideoArrayList = new ArrayList<VideoItemInfo>();
	private int mType = 1;
	private int mPageIndex = 1;
	private final int PAGE_PER_SIZE = 10;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_stream_list_activity);

		mType = getIntent().getIntExtra("type", 1);
		mEmptyViewHelper = new EmptyViewHelper(this);
		mEmptyViewHelper.update((mType == 1)?getString(R.string.empty_live_tip):getString(R.string.empty_video_tip), R.drawable.empty);

		// 得到控件
		mPullRefreshListView = (PullToRefreshGridView) findViewById(R.id.pull_refresh_grid);

		ImageView imageview_back = (ImageView)findViewById(R.id.imageview_back);
		imageview_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		ImageView imageview_watch_video = (ImageView)findViewById(R.id.imageview_watch_video);
		imageview_watch_video.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(LiveStreamListActivity.this, LiveStreamListActivity.class);
				intent.putExtra("type", 2); // 1: live, 2: video
				startActivity(intent);
			}
		});
		imageview_watch_video.setVisibility((mType == 1)?View.VISIBLE:View.GONE);

		initIndicator();

//		if (mType == 1)
//		{
//			mVideoArrayList.add(new VideoItemInfo("http://player.pptvyun.com/svc/m3u8player/pl/0a2dnq6apaOdn6eL4K2dnqfhoaegoKqVpQ.m3u8",
//					"http://v.img.pplive.cn/sp300/66/01/66014e99ef513a79722ee47158554089/3.jpg", "12345", 1));
//			mVideoArrayList.add(new VideoItemInfo("http://player.pptvyun.com/svc/m3u8player/pl/0a2dnq6apaOdoK-L4K2dnqfhoaegoKuVpQ.m3u8",
//					"http://v.img.pplive.cn/sp300/01/b2/01b2c2529962cce74efae93158554191/3.jpg", "asdfa", 1));
//			mVideoArrayList.add(new VideoItemInfo("http://player.pptvyun.com/svc/m3u8player/pl/0a2dnq6apaOknKmL4K2dnqfhoaegoa2ZpQ.m3u8",
//					"http://e.hiphotos.baidu.com/image/h%3D200/sign=c898bddf19950a7b6a3549c43ad0625c/14ce36d3d539b600be63e95eed50352ac75cb7ae.jpg", "6789", 1));
//		}
//		else
//		{
//			mVideoArrayList.add(new VideoItemInfo("http://player.pptvyun.com/svc/m3u8player/pl/0a2dnq6apaGkn6aL4K2dnqfhoaegna-dpw.m3u8",
//					"http://c.hiphotos.baidu.com/image/h%3D200/sign=60a2c5d92c738bd4db21b531918b876c/6a600c338744ebf8a05ade3bdbf9d72a6059a78f.jpg", "23455", 2));
//
//			mVideoArrayList.add(new VideoItemInfo("http://player.pptvyun.com/svc/m3u8player/pl/0a2dnq6apaGkn6aL4K2dnqfhoaegna-dpw.m3u8",
//					"http://g.hiphotos.baidu.com/image/h%3D200/sign=2934ec60272dd42a400906ab333b5b2f/e61190ef76c6a7ef3005112ffffaaf51f3de669c.jpg", "54678", 2));
//		}

		mVideoListAdapter = new PictureAdapter(mVideoArrayList);
		mPullRefreshListView.setAdapter(mVideoListAdapter);

		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<GridView>()
			{

				@Override
				public void onPullDownToRefresh(
						PullToRefreshBase<GridView> refreshView)
				{
					Log.e("TAG", "onPullDownToRefresh"); // Do work to
					String label = DateUtils.formatDateTime(
							getApplicationContext(),
							System.currentTimeMillis(),
							DateUtils.FORMAT_SHOW_TIME
									| DateUtils.FORMAT_SHOW_DATE
									| DateUtils.FORMAT_ABBREV_ALL);

					// Update the LastUpdatedLabel
					refreshView.getLoadingLayoutProxy()
							.setLastUpdatedLabel(label);

					getDatas(true);
				}

				@Override
				public void onPullUpToRefresh(
						PullToRefreshBase<GridView> refreshView)
				{
					Log.e("TAG", "onPullUpToRefresh"); // Do work to refresh
														// the list here.
					getDatas(false);
				}
			});

		// 初始化数据和数据源
		showLoading("");
		getDatas(true);
	}

	public class PictureAdapter extends MyBaseAdapter {
		private ArrayList<VideoItemInfo> mVideoList = new ArrayList<VideoItemInfo>();

		public PictureAdapter(ArrayList<VideoItemInfo> videoList) {
			mVideoList = videoList;
		}

		public void update(ArrayList<VideoItemInfo> videoList)
		{
			mVideoList = videoList;
		}

		@Override
		public int getCount() {
			return mVideoList.size();
		}

		@Override
		public Object getItem(int position) {
			return mVideoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			PictureHolder holder = null;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.layout_grid_item, null);
				holder = new PictureHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.type = (ImageView) convertView.findViewById(R.id.type);
				holder.liveid = (TextView) convertView.findViewById(R.id.liveid);

				convertView.setTag(holder);
			}
			else
				holder = (PictureHolder)convertView.getTag();

			final VideoItemInfo videoItemInfo = mVideoList.get(position);
			holder.type.setImageResource((videoItemInfo.getType()==1)?R.drawable.live:R.drawable.video);
			ConstInfo.loadImage(videoItemInfo.getImageurl(), R.drawable.default2, new ImageSize(100, 100), holder.image, mPullRefreshListView);
			holder.liveid.setText(getString(R.string.liveid_tip, videoItemInfo.getLiveid()));

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (videoItemInfo.getType() == 1)
					{
						// live
						showLoading("");
						PPYRestApi.stream_watch(videoItemInfo.getLiveid(), new PPYRestApi.StringResultMapCallack() {
							@Override
							public void result(int errcode, final Bundle data) {
								hideLoading();
								if (errcode==0 && data != null) {
									final String rtmpurl = data.getString("rtmpUrl");
									if (rtmpurl != null && !rtmpurl.isEmpty()) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (!FloatWindowService.mIsFloatWindowShown)
												{
													Intent intent = new Intent(LiveStreamListActivity.this, WatchStreamingActivity.class);
													intent.putExtra("liveurl", data);
													intent.putExtra("liveid", videoItemInfo.getLiveid());
													//intent.putExtra("type", 1); // rtmp
													startActivity(intent);
												}
												else
												{
													Intent intent = new Intent(LiveStreamListActivity.this, FloatWindowService.class);
													Bundle bundle = new Bundle();
													bundle.putBundle("liveurl", data);
													bundle.putString("liveid", videoItemInfo.getLiveid());
													bundle.putInt(FloatWindowService.PLAY_TYPE, 1); // 1: live, 0: vod
													intent.putExtra(FloatWindowService.ACTION_PLAY, bundle);
													startService(intent);
												}

											}
										});
									}
								}
								else
								{
									if (errcode == 98)
									{
										Toast.makeText(getApplication(), "直播已结束，请至回放页面观看", Toast.LENGTH_SHORT).show();
										getDatas(true);
									}
									else
										Toast.makeText(getApplication(), "观看直播失败: 网络错误", Toast.LENGTH_SHORT).show();
								}

							}
						});
					}
					else
					{
						// vod
						if (!FloatWindowService.mIsFloatWindowShown)
						{
							Intent intent = new Intent(LiveStreamListActivity.this, WatchVideoActivity.class);
							intent.putExtra("m3u8Url", PPYRestApi.get_m3u8Url(videoItemInfo.getPlayurl()));
							startActivity(intent);
						}
						else
						{
							Intent intent = new Intent(LiveStreamListActivity.this, FloatWindowService.class);
							Bundle bundle = new Bundle();
							bundle.putString("m3u8Url", PPYRestApi.get_m3u8Url(videoItemInfo.getPlayurl()));
							bundle.putInt(FloatWindowService.PLAY_TYPE, 0); // 1: live, 0: vod
							intent.putExtra(FloatWindowService.ACTION_PLAY, bundle);
							startService(intent);
						}

					}
				}
			});

			return convertView;
		}

		class PictureHolder {
			ImageView image;
			ImageView type;
			TextView liveid;
		}
	}

	private void initIndicator()
	{
		ILoadingLayout startLabels = mPullRefreshListView.getLoadingLayoutProxy(true, false);
		startLabels.setPullLabel("往下拉");// 刚下拉时，显示的提示
		startLabels.setRefreshingLabel("正在刷新...");// 刷新时
		startLabels.setReleaseLabel("松开");// 下来达到一定距离时，显示的提示

		ILoadingLayout endLabels = mPullRefreshListView.getLoadingLayoutProxy(false, true);
		endLabels.setPullLabel("往上拉.");// 刚下拉时，显示的提示
		endLabels.setRefreshingLabel("正在加载...");// 刷新时
		endLabels.setReleaseLabel("松开");// 下来达到一定距离时，显示的提示
	}

	private void getDatas(boolean isRefresh)
	{
		if (isRefresh)
		{
			mPageIndex = 1;
			PPYRestApi.get_watch_list(mType, mPageIndex, PAGE_PER_SIZE, new ArrayListResultCallack<VideoItemInfo>() {
				@Override
				public void result(final int errcode, final ArrayList<VideoItemInfo> result) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							hideLoading();
							mPullRefreshListView.onRefreshComplete();
							if (errcode == 0 && result != null)
							{
								mPageIndex++;
								mVideoArrayList = result;
								mVideoListAdapter.update(mVideoArrayList);
								mVideoListAdapter.notifyDataSetChanged();
							}
							mEmptyViewHelper.showEmptyView(mVideoArrayList.isEmpty());
						}
					});

				}
			});
		}
		else
		{
			PPYRestApi.get_watch_list(mType, mPageIndex, PAGE_PER_SIZE, new ArrayListResultCallack<VideoItemInfo>() {
				@Override
				public void result(final int errcode, final ArrayList<VideoItemInfo> result) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							hideLoading();
							mPullRefreshListView.onRefreshComplete();

							if (errcode == 0 && result != null && !result.isEmpty())
							{
								mPageIndex++;
								mVideoArrayList.addAll(result);
								mVideoListAdapter.update(mVideoArrayList);
								mVideoListAdapter.notifyDataSetChanged();
							}
							mEmptyViewHelper.showEmptyView(mVideoArrayList.isEmpty());
						}
					});
				}
			});
		}
	}

}
