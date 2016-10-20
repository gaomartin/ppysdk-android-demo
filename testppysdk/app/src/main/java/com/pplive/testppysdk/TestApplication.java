package com.pplive.testppysdk;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.pplive.ppysdk.PPYStream;

import java.io.File;

/**
 * Created by ballackguan on 2016/8/23.
 */
public class TestApplication extends Application {

    public static DisplayImageOptions mOptions = null;
    public static HashCodeFileNameGenerator mFileNameGen = new HashCodeFileNameGenerator();
    private static final int MAX_DISK_CACHE_SIZE = 200 * 1024 * 1024; // 30Mb
    private static final int MAX_MEMORY_CACHE_SIZE = 30 * 1024 * 1024; // 30Mb
    private String mImageCachePath;
    public static int SCREEN_WIDTH = 0;
    public static int SCREEN_HEIGHT = 0;
//    public static int SCREEN_WIDTH_DP = 0;
//    public static int SCREEN_HEIGHT_DP = 0;
    @Override
    public void onCreate()
    {
        super.onCreate();

        PPYStream.getInstance().init(this);


        String path = getCacheDir().getAbsolutePath() + "/log";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        mImageCachePath = getCacheDir().getAbsolutePath() + "/imagecache";
        File file2 = new File(mImageCachePath);
        if (!file2.exists()) {
            file2.mkdirs();
        }
        initImageLoader(this);

        DisplayMetrics localDisplayMetrics = getResources().getDisplayMetrics();

        SCREEN_WIDTH = localDisplayMetrics.widthPixels;
        SCREEN_HEIGHT = localDisplayMetrics.heightPixels;

        // 屏幕密度:指每平方英寸中的像素数,在DisplayMetrics类中，该密度值为dpi/160
        float density = localDisplayMetrics.density;
        // 屏幕密度(dpi):指每英寸中的像素数
        float densityDpi = localDisplayMetrics.densityDpi;

//        // 屏幕宽度(dip)
//        SCREEN_WIDTH_DP = CommonFunction.px2dip(this, SCREEN_WIDTH);
//        // 屏幕高度(dip)
//        SCREEN_HEIGHT_DP = CommonFunction.px2dip(this, SCREEN_HEIGHT);

    }

    private void initImageLoader(Context context)
    {
        mOptions = new DisplayImageOptions.Builder()
                //.showImageOnLoading(R.drawable.loading)
                //.showImageForEmptyUri(R.drawable.ic_empty)
                // .showImageOnFail(R.drawable.ic_error)
                // .resetViewBeforeLoading(true)
                .considerExifParams(true).cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                // .displayer(new FadeInBitmapDisplayer(1000))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .diskCache(new UnlimitedDiskCache(new File(mImageCachePath)))
                .threadPriority(Thread.NORM_PRIORITY-1)
                .threadPoolSize(4)
                //.denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(mFileNameGen)
                .diskCacheSize(MAX_DISK_CACHE_SIZE)
                .memoryCacheSizePercentage(15)
                //.memoryCacheSize(MAX_MEMORY_CACHE_SIZE)
                .memoryCache(new WeakMemoryCache())
                .defaultDisplayImageOptions(mOptions)
                //.writeDebugLogs()
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

}
