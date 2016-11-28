# PP云直播推流Android SDK使用说明

PPY Streamer Android SDK是pp云推出的 Android 平台上使用的软件开发工具包(SDK), 负责视频直播的采集、预处理、编码和推流。  
## 一. 功能特点

* [x] [支持软编]
* [x] [网络自适应]：可根据实际网络情况动态调整目标码率，保证流畅性
* [x] 音频编码：AAC
* [x] 视频编码：H.264
* [x] 推流协议：RTMP
* [x] [视频分辨率]：支持360P, 480P, 540P和720P
* [x] 音视频目标码率：可设
* [x] 支持固定竖屏推流
* [x] 支持前、后置摄像头动态切换
* [x] 闪光灯：开/关
* [x] [内置美颜功能]
* [x] [支持手动指定自动对焦测光区域]


## 二. 运行环境

* 最低支持版本为Android 4.0 (API level 15)
* 支持的cpu架构：armv7, arm64, x86

软硬编部分功能版本需求列表:

|           |软编       |
|-----------|-----------|
|基础推流   |4.0 (15)   |
|网络自适应 |4.0 (15)   |
  
## 三. 快速集成

本章节提供一个快速集成PP云推流SDK基础功能的示例。
具体可以参考testppysdk工程中的相应文件。

### 配置项目

引入目标库, 将推流SDK中libs目录下的库文件引入到目标工程中并添加依赖。

可参考下述配置方式（以Android Studio为例）：
- 将ppcloud-sdk.aar拷贝到app的libs目录下；
- 修改目标工程的build.gradle文件，配置repositories路径：
````gradle

     repositories {
        flatDir {
            dirs 'libs'
        }
    }
    
dependencies {
    ...
    compile(name: 'ppcloud-sdk', ext: 'aar')
    ...
}
````

### 简单推流示例

- 初始化SDK 
````java
// 在app的application里调用初始化函数
PPYStream.getInstance().init(this);
````
````java
public class TestApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();

        PPYStream.getInstance().init(this);
    }
}
````

具体可参考demo工程中的`LiveStreamingActivity`类

- 在布局文件中加入预览View
````xml
<com.pplive.ppysdk.PPYSurfaceView
        android:id="@+id/lsq_cameraView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" >
    </com.pplive.ppysdk.PPYSurfaceView>
````
- PPYSurfaceView
````java
PPYSurfaceView mCameraView = (PPYSurfaceView)findViewById(R.id.lsq_cameraView);
````

- 创建并配置PPYStreamerConfig。
推流过程中不可动态改变的参数需要在创建该类的对象时指定。
````java
PPYStreamerConfig builder = new PPYStreamerConfig(); // use default param
// 设置推流url
builder.setPublishurl(url);
/**
 * 设置推流分辨率，支持以下值：
 * VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_360P,
 * VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_480P,
 * VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_540P,
 * VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_720P;
 */
builder.setVideoResolution(VIDEO_RESOLUTION_TYPE.VIDEO_RESOLUTION_480P);
// 设置视频帧率
builder.setFrameRate(15);
// 设置视频码率(分别初始码率, 单位为kbps)
builder.setVideoBitrate(400);
// 设置音频码率(单位为kbps)
builder.setAudioBitrate(32);
// 设置音频采样率
builder.setSampleAudioRateInHz(44100);
// 设置是否默认使用前置摄像头
builder.setDefaultFront(true);
// 设置是否采用横屏模式
builder.setDefaultLandscape(false);


````
- 创建推流事件监听，可以收到推流过程中的异步事件。

**注意：所有回调直接运行在产生事件的各工作线程中，不要在该回调中做任何耗时的操作，或者直接调用推流API。**
````java
PPYStream.getInstance().setPPYStatusListener(new PPYStatusListener() {
                @Override
                public void onStateChanged(int type, Object o) {
                    if (type == PPY_SDK_INIT_SUCC)
                        PPYStream.getInstance().StartStream();
                }
            });

````
- 创建PPYStreamer对象
````java
PPYStream.getInstance().CreateStream(getApplicationContext(), config, mCameraView);

PPYStream.getInstance().setPPYStatusListener(new PPYStatusListener() {
                @Override
                public void onStateChanged(int type, Object o) {
                    if (type == PPY_SDK_INIT_SUCC)
                        PPYStream.getInstance().StartStream();
                }
            });
````
- 开始推流  
**注意：初次开启预览后需要在setPPYStatusListener回调中收到PPY_SDK_INIT_SUCC
事件后调用方才有效。**
````java
PPYStream.getInstance().StartStream();
````
- 推流过程中可动态设置的常用方法
````java
// 切换前后摄像头
PPYStream.getInstance().SwitchCamera();
// 开关闪光灯
PPYStream.getInstance().setFlashLightState(true);
// 是否支持打开闪光灯
PPYStream.getInstance().IsSupportFlashlight();
// 设置是否开启美颜
PPYStream.getInstance().EnableBeauty(true);
// 设置美颜的美白，亮度，色调参数(0-1.0 默认都是0.5)
PPYStream.getInstance().SetBeautyParam(mBeautyWhite, mBeautyBright, mBeautyTone);

//推流过程中获取音视频信息
// 获取当前视频宽高
PPYStream.getInstance().getVideoWdith();
PPYStream.getInstance().getVideoHeight();
// 获取当前视频码率
PPYStream.getInstance().getVideoBitrate();
// 获取当前音频码率
PPYStream.getInstance().getAudioBitrate();
// 获取当前FPS
PPYStream.getInstance().getVideoFrameRate();
````
- 停止推流
````java
PPYStream.getInstance().StopStream();
````
- Activity的生命周期回调处理  
**采集的状态依赖于Activity的生命周期，所以必须在Activity的生命周期中也调用SDK相应的接口。**
```java
public class LiveStreamingActivity extends Activity {

    // ...

    @Override
    public void onResume() {
        super.onResume();
        PPYStream.getInstance().OnResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        PPYStream.getInstance().OnPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PPYStream.getInstance().OnDestroy();
    }
}
```
如需测试用的推流地址，请联系我们。

