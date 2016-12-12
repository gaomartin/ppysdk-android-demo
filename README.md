# PP云直播推流播放Android SDK使用说明

PPY Android SDK是pp云推出的 Android 平台上使用的软件开发工具包(SDK),包括直播SDK和播放SDK两部分组成。

# PP云直播推流SDK
直播推流SDK主要负责视频直播的采集、预处理、编码和推流，及直播，点播的播放。  
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

# PP云播放SDK
PP云播放SDK主要负责直播流的播放。  
## 一. 功能特点

* [x] [支持软解]
* [x] 支持播放协议：RTMP，HLS, HTTP-FLV


## 二. 运行环境

* 最低支持版本为Android 4.0 (API level 15)
* 支持的cpu架构：armv7, arm64, x86

# 三 详细SDK使用文档地址
github上文档地址：https://github.com/pptvyun/ppysdk-android-demo

# 三 下载地址
从github下载SDK及demo工程： https://github.com/pptvyun/ppysdk-android-demo.git



