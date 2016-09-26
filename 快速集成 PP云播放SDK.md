# PP云播放Android SDK使用说明

PP云播放Android SDK是pp云推出的 Android 平台上使用的软件开发工具包(SDK), 负责直播流的播放。  
## 一. 功能特点

* [x] [支持软解]
* [x] 支持播放协议：RTMP，HLS, HTTP-FLV


## 二. 运行环境

* 最低支持版本为Android 4.0 (API level 15)
* 支持的cpu架构：armv7, arm64, x86
  
## 三. 快速集成

本章节提供一个快速集成PP云播放SDK基础功能的示例。
具体可以参考testppysdk工程中的相应文件。

### 配置项目

引入目标库, 将播放SDK中libs目录下的库文件引入到目标工程中并添加依赖。

可参考下述配置方式（以Android Studio为例）：
- 将播放SDK中mediaPlayer.jar拷贝到app的libs目录下；
- 将播放SDK中lib中的armeabi中的so文件拷贝到app的/src/main/jniLibs/armeabi目录下；
- 修改目标工程的build.gradle文件，配置repositories路径：
````gradle

    sourceSets {
        main {
            java.srcDirs = ["/src/main/java"]
            jniLibs.srcDirs = ["/src/main/jniLibs"]
        }
    }
    
    repositories {
        flatDir {
            dirs 'libs'
        }
    }
    
````

### 简单播放示例

具体可参考testppysdk工程中的`WatchStreamingActivity`类

- 在布局文件中加入播放View
````xml
<android.slkmedia.mediaplayer.VideoView
        android:id="@+id/live_player_videoview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_centerInParent="true" />
````
- VideoView
````java
VideoView mVideoView = (VideoView)findViewById(R.id.live_player_videoview);
````

- 初始化VideoView。

````java
// 初始化
mVideoView.initialize();

// 设置状态监听器
mVideoView.setListener(new VideoViewListener() {
    @Override
    public void onPrepared() {
        Log.d(ConstInfo.TAG, "play onPrepared");
    }

    @Override
    public void onError(int i, int i1) {
        if(i == VideoView.ERROR_DEMUXER_READ_FAIL){
            Log.d(ConstInfo.TAG, "fail to read data from network");
        }else if(i == VideoView.ERROR_DEMUXER_PREPARE_FAIL){
            Log.d(ConstInfo.TAG, "fail to connect to media server");
        }else{
            Log.d(ConstInfo.TAG, "onError : "+String.valueOf(i));
        }
    }

    @Override
    public void onInfo(int what, int extra) {
        if(what == VideoView.INFO_BUFFERING_START)
        {
            Log.d(ConstInfo.TAG, "onInfo buffering start");
        }

        if(what == VideoView.INFO_BUFFERING_END)
        {
            Log.d(ConstInfo.TAG, "onInfo buffering end");
        }

        if(what == VideoView.INFO_VIDEO_RENDERING_START)
        {
            Log.d(ConstInfo.TAG, "onInfo video rendering start");
        }

        if(what == VideoView.INFO_REAL_BITRATE)
        {
            Log.d(ConstInfo.TAG, "onInfo real bitrate : "+String.valueOf(extra));
        }

        if(what == VideoView.INFO_REAL_FPS)
        {
            Log.d(ConstInfo.TAG, "onInfo real fps : "+String.valueOf(extra));
        }

        if(what == VideoView.INFO_REAL_BUFFER_DURATION)
        {
            Log.d(ConstInfo.TAG, "onInfo real buffer duration : "+String.valueOf(extra));
        }

        if(what == VideoView.INFO_CONNECTED_SERVER)
        {
            Log.d(ConstInfo.TAG, "connected to media server");
        }

        if(what == VideoView.INFO_DOWNLOAD_STARTED)
        {
            Log.d(ConstInfo.TAG, "start download media data");
        }

        if(what == VideoView.INFO_GOT_FIRST_KEY_FRAME)
        {
            Log.d(ConstInfo.TAG, "got first key frame");
        }
    }

    @Override
    public void onCompletion() {
        Log.d(ConstInfo.TAG, "onCompletion");
    }

    @Override
    public void onVideoSizeChanged(int i, int i1) {
        Log.d(ConstInfo.TAG, "play setOnVideoSizeChanged w="+i+" h="+i1);
    }
});

````

- 开始播放  
**注意：播放器在setListener的回调中收到onPrepared中调用start接口开始播放**
````java
mVideoView.setListener(new VideoViewListener() {
    @Override
    public void onPrepared() {
        Log.d(ConstInfo.TAG, "play onPrepared");
        
         mVideoView.start();
    }

  ...
});
````
- 播放结束
````java
mVideoView.stop(false);
````
- 释放播放资源
  在Activity的onDestroy加入释放代码
````java
@Override
protected void onDestroy()
{
    super.onDestroy();

    mVideoView.stop(false);
    mVideoView.release();
}
````

