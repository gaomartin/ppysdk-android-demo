package com.pplive.testppysdk;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by ballackguan on 2016/8/4.
 */
public class PPYRestApi {

    public static final String PPYUN_HOST           = "http://115.231.44.26:8081/";
//    public static final String PPYUN_HOST           = "http://10.200.48.32:8080/";

    public static final String STREAM_CREATE        = "live/create/";
    public static final String STREAM_START         = "live/start/";
    public static final String STREAM_STOP          = "live/stop/";
    public static final String STREAM_WATCH         = "live/watch/";
    public static final String STREAM_STATUS        = "live/status/";
    public static final String LIVE_LIST            = "live/living/list";
    public static final String VIDEO_LIST            = "live/vod/list";
    public interface StringResultCallack
    {
        void result(int errcode, String data);
    }
    public interface StringResultWatchCallack
    {
        void result(int errcode, String rtmpurl, String live2url);
    }
    public interface StringResultMapCallack
    {
        void result(int errcode, Bundle result);
    }
    public interface StringResultStatusCallack
    {
        void result(int errcode, String livestatus, String streamstatus);
    }
    private static String sync_http_get(String strUrl)
    {
        Log.d(ConstInfo.TAG, "get url: " + strUrl);
        String strResult = "";
        try {
            // HttpClient对象
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpRequest = new HttpGet(strUrl);

            // 获得HttpResponse对象
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 取得返回的数据
                strResult = EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(ConstInfo.TAG, "reply: " + strResult);
        return strResult;
    }
    public static void asyn_http_get(String relative_url, String liveid, final PPYRestApi.StringResultCallack callack)
    {
        new AsyncTaskHttpClient(getAbsoluteUrl(relative_url)+liveid, callack).execute();
    }
    public static void asyn_http_get(String url, final PPYRestApi.StringResultCallack callack)
    {
        new AsyncTaskHttpClient(url, callack).execute();
    }
    private static class AsyncTaskHttpClient extends AsyncTask<Integer, Integer, String> {
        private String mUrl;
        private PPYRestApi.StringResultCallack mCallback;

        public AsyncTaskHttpClient(String url, final PPYRestApi.StringResultCallack callack) {
            super();
            mUrl = url;
            mCallback = callack;
        }


        /**
         * 这里的Integer参数对应AsyncTask中的第一个参数
         * 这里的String返回值对应AsyncTask的第三个参数
         * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
         * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
         */
        @Override
        protected String doInBackground(Integer... params) {
            return sync_http_get(mUrl);
        }


        /**
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPostExecute(String result) {
            if (mCallback != null)
                mCallback.result(-1, result);
        }


        //该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
        @Override
        protected void onPreExecute() {
        }


        /**
         * 这里的Intege参数对应AsyncTask中的第二个参数
         * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
         * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return PPYUN_HOST + relativeUrl;
    }

    private static HashMap<String, StringResultCallack> mMapResult = new HashMap<>();
    public static void stream_create(String liveid, final StringResultCallack resultCallack)
    {
        synchronized (mMapResult)
        {
            for (String id : mMapResult.keySet())
            {
                mMapResult.put(id, null);
            }
        }
        final String uuid = UUID.randomUUID().toString();
        mMapResult.put(uuid, resultCallack);

        PPYRestApi.asyn_http_get(STREAM_CREATE, liveid, new StringResultCallack() {
            @Override
            public void result(int errcode, String response) {
                if (response != null && !response.isEmpty()) {
                    JSONObject s = JSON.parseObject(response);
                    if (s != null) {
                        int err = s.getIntValue("err");
                        if (err == 0) {
                            //Class<T> clazz = T;
                            JSONObject data = s.getJSONObject("data");

                            String publicUrl = data.getString("pushUrl");
                            String token = data.getString("token");

                            StringResultCallack targetResult = mMapResult.get(uuid);
                            if (targetResult != null)
                                targetResult.result(0, publicUrl + "/" + token);
                            mMapResult.remove(uuid);
                            return;
                        }
                        else
                        {
                            String msg = s.getString("msg");
                            StringResultCallack targetResult = mMapResult.get(uuid);
                            if (targetResult != null)
                                targetResult.result(err, msg);
                            mMapResult.remove(uuid);
                            return;
                        }
                    }
                }
                StringResultCallack targetResult = mMapResult.get(uuid);
                if (targetResult != null)
                    targetResult.result(errcode, "");
                mMapResult.remove(uuid);
            }
        });
    }

    public static void stream_start(String liveid, final StringResultCallack resultCallack)
    {
        PPYRestApi.asyn_http_get(STREAM_START, liveid, new StringResultCallack() {
            @Override
            public void result(int errcode, String response) {
                if (response != null && !response.isEmpty()) {
                    JSONObject s = JSON.parseObject(response);
                    if (s != null) {
                        int err = s.getIntValue("err");
                        if (err == 0) {
                            if (resultCallack != null)
                                resultCallack.result(0, "");
                            return;
                        }
                        else
                        {
                            String msg = s.getString("msg");
                            if (resultCallack != null)
                                resultCallack.result(err, msg);
                            return;
                        }
                    }
                }
                if (resultCallack != null)
                    resultCallack.result(errcode, "");
            }
        });
    }
    public static void stream_stop(String liveid, final StringResultCallack resultCallack)
    {
        PPYRestApi.asyn_http_get(STREAM_STOP, liveid, new StringResultCallack() {
            @Override
            public void result(int errcode, String response) {
                if (response != null && !response.isEmpty()) {
                    JSONObject s = JSON.parseObject(response);
                    if (s != null) {
                        int err = s.getIntValue("err");
                        if (err == 0) {
                            if (resultCallack != null)
                                resultCallack.result(0, "");
                            return;
                        }
                        else
                        {
                            String msg = s.getString("msg");
                            if (resultCallack != null)
                                resultCallack.result(err,msg);
                            return;
                        }
                    }
                }
                if (resultCallack != null)
                    resultCallack.result(errcode, "");
            }
        });
    }
    public static void stream_watch(String liveid, final StringResultMapCallack resultCallack)
    {
        PPYRestApi.asyn_http_get(STREAM_WATCH, liveid, new StringResultCallack() {
            @Override
            public void result(int errcode, String response) {
                if (response != null && !response.isEmpty()) {
                    JSONObject s = JSON.parseObject(response);
                    if (s != null) {
                        int err = s.getIntValue("err");
                        if (err == 0) {
                            JSONObject data = s.getJSONObject("data");

                            Bundle bundle = new Bundle();
                            bundle.putString("rtmpUrl", data.getString("rtmpUrl"));
                            bundle.putString("m3u8Url", data.getString("m3u8Url"));
                            bundle.putString("hdlUrl", data.getString("hdlUrl"));

                            JSONArray rtmpArray = data.getJSONArray("rtmpsUrl");
                            if (rtmpArray != null)
                            {
                                ArrayList<String> rtmpsUrl = new ArrayList<String>();
                                for (int i=0; i<rtmpArray.size(); i++)
                                {
                                    rtmpsUrl.add(rtmpArray.getString(i));
                                }
                                bundle.putStringArrayList("rtmpsUrl", rtmpsUrl);
                            }

                            if (resultCallack != null)
                                resultCallack.result(0, bundle);
                            return;
                        }
                        else
                        {
//                            String msg = s.getString("msg");
                            if (resultCallack != null)
                                resultCallack.result(err, null);
                            return;
                        }
                    }
                }
                if (resultCallack != null)
                    resultCallack.result(errcode, null);
            }
        });
    }

    public static void stream_status(String liveid, final StringResultStatusCallack resultCallack)
    {
        PPYRestApi.asyn_http_get(STREAM_STATUS, liveid, new StringResultCallack() {
            @Override
            public void result(int errcode, String response) {
                if (response != null && !response.isEmpty()) {
                    JSONObject s = JSON.parseObject(response);
                    if (s != null) {
                        int err = s.getIntValue("err");
                        if (err == 0) {

                            JSONObject data = s.getJSONObject("data");
                            String liveStatus = data.getString("liveStatus");
                            String streamStatus = data.getString("streamStatus");
                            if (resultCallack != null)
                            {
                                resultCallack.result(0, liveStatus, streamStatus);
                            }
                            return;
                        }
                        else
                        {
                            String msg = s.getString("msg");
                            if (resultCallack != null)
                                resultCallack.result(err,"", "");
                            return;
                        }
                    }
                }
                if (resultCallack != null)
                    resultCallack.result(errcode, "", "");
            }
        });
    }

    public static void get_watch_list(final int type, int page_index, int page_size, final ArrayListResultCallack<VideoItemInfo> resultCallack)
    {
        String url;
        if (type == 1)
        {
            // live
            url = PPYUN_HOST+LIVE_LIST+"?page_num="+page_index+"&page_size="+page_size;
        }
        else
            url = PPYUN_HOST+VIDEO_LIST+"?page_num="+page_index+"&page_size="+page_size;


        PPYRestApi.asyn_http_get(url, new StringResultCallack() {
            @Override
            public void result(int errcode, String response) {
                if (response != null && !response.isEmpty()) {
                    JSONObject s = JSON.parseObject(response);
                    if (s != null) {
                        int err = s.getIntValue("err");
                        if (err == 0) {
                            JSONArray data = s.getJSONArray("data");
                            ArrayList<VideoItemInfo> itemInfos = new ArrayList<VideoItemInfo>();
                            if (data != null)
                            {
                                for (int i=0; i<data.size(); i++)
                                {
                                    JSONObject object = data.getJSONObject(i);
                                    if (object == null)
                                        continue;

                                    String liveid = object.getString("room_name");
                                    if (liveid == null || liveid.isEmpty())
                                        continue;
                                    if (type == 2) // vod
                                    {
                                        int duration = object.getIntValue("duration");
                                        if (duration < 10)
                                            continue;
                                    }

                                    itemInfos.add(new VideoItemInfo(object.getString("channel_web_id"), object.getString("screen_shot"), liveid, type));
                                }
                            }

                            if (resultCallack != null)
                                resultCallack.result(0, itemInfos);
                            return;
                        }
                        else
                        {
//                            String msg = s.getString("msg");
                            if (resultCallack != null)
                                resultCallack.result(err, null);
                            return;
                        }
                    }
                }
                if (resultCallack != null)
                    resultCallack.result(errcode, null);
            }
        });
    }

    public static String get_m3u8Url(String channel_web_id)
    {
        return "http://player.pptvyun.com/svc/m3u8player/pl/"+channel_web_id+".m3u8";
    }
}
