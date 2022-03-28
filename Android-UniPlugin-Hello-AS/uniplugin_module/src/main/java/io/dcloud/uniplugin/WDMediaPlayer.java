package io.dcloud.uniplugin;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;
import io.dcloud.uniplugin.entity.CurrentInfo;
import io.dcloud.uniplugin.entity.Song;
import io.dcloud.uniplugin.service.PlayerService;


public class WDMediaPlayer extends UniModule {
    public static int REQUEST_CODE = 1000;

    private final List<UniJSCallback> onPlayCallback = new ArrayList<>();
    private final List<UniJSCallback> onPauseCallback = new ArrayList<>();
    private final List<UniJSCallback> onPreviousSongCallback = new ArrayList<>();
    private final List<UniJSCallback> onNextSongCallback = new ArrayList<>();
    private final List<UniJSCallback> onCloseCallback = new ArrayList<>();
    private final List<UniJSCallback> onFollowCallback = new ArrayList<>();
    private final List<UniJSCallback> onPlayerInfoCallback = new ArrayList<>();

    private MyNotificationReceiver notificationReceiver;
    private MyReceiver myReceiver;

    /**
     * 定时器
     */
    private final Handler timerHandler = new Handler();
    private Runnable myTimerRun;

    private JSONObject serverConfig;
    private JSONObject userToken;


    /**
     * 获取服务运行状态
     *
     * @return 服务运行状态
     */
    @UniJSMethod
    public boolean isServiceRunning() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                ActivityManager am = (ActivityManager) mUniSDKInstance.getContext().getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
                for (ActivityManager.RunningServiceInfo info : services) {
                    if (info.service.getClassName().equals("io.dcloud.uniplugin.service.PlayerService")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            onErrorMessage("isServiceRunning：" + e.getMessage());
        }
        return false;
    }

    /**
     * 列表播放
     *
     * @param list     播放列表
     * @param currInfo 当前信息
     */
    @UniJSMethod
    public void listPlay(List<Song> list, CurrentInfo currInfo, boolean playNow) {
        try {
            if (isServiceRunning()) {
                setPlayList(list);
                setCurrentInfo(currInfo);
                if (playNow) {
                    play();
                }
            } else {
                if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                    Intent intent = new Intent(mUniSDKInstance.getContext(), PlayerService.class);
                    intent.putExtra("playList", JSON.toJSONString(list));
                    intent.putExtra("currentInfo", JSON.toJSONString(currInfo));
                    intent.putExtra("playNow", playNow);

                    mUniSDKInstance.getContext().startService(intent);
                    registerMyReceiver();

                    startTimer();
                }
            }
        } catch (Exception e) {
            onErrorMessage("listPlay：" + e.getMessage());
        }
    }

    /**
     * 用定时器维护播放器状态
     */
    private void startTimer() {
        myTimerRun = new Runnable()                                         // 创建一个runnable对象
        {
            @Override
            public void run() {
                timerHandler.postDelayed(this, 500);           // 再次调用myTimerRun对象，实现每两秒一次的定时器操作
            }
        };
        timerHandler.postDelayed(myTimerRun, 500);                // 使用postDelayed方法，两秒后再调用此myTimerRun对象
    }

    @UniJSMethod
    public void setServerConfig(JSONObject jsonObject) {
        try {
            serverConfig = jsonObject;
            if (isServiceRunning()) {
                JSONObject jo = new JSONObject();
                jo.put("action", "setServerConfig");

                jo.put("data", jsonObject);
                sendContentBroadcast(JSON.toJSONString(jo));
            }
        } catch (Exception e) {
            onErrorMessage("setServerConfig：" + e.getMessage());
        }
    }

    @UniJSMethod
    public void setUserToken(JSONObject jsonObject) {
        try {
            userToken = jsonObject;
            if (isServiceRunning()) {
                JSONObject jo = new JSONObject();
                jo.put("action", "setUserToken");

                jo.put("data", jsonObject);
                sendContentBroadcast(JSON.toJSONString(jo));
            }
        } catch (Exception e) {
            onErrorMessage("setUserToken：" + e.getMessage());
        }
    }

    /**
     * 注册广播监听器
     */
    public void registerMyReceiver() {
        if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            // 来自通知栏的广播
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("io.dcloud.uniplugin.wd.play");
            intentFilter.addAction("io.dcloud.uniplugin.wd.pause");
            intentFilter.addAction("io.dcloud.uniplugin.wd.previoussong");
            intentFilter.addAction("io.dcloud.uniplugin.wd.nextsong");
            intentFilter.addAction("io.dcloud.uniplugin.wd.follow");
            intentFilter.addAction("io.dcloud.uniplugin.wd.close");
            notificationReceiver = new MyNotificationReceiver();
            mUniSDKInstance.getContext().registerReceiver(notificationReceiver, intentFilter);

            // 来自服务的广播
            myReceiver = new MyReceiver();
            intentFilter = new IntentFilter();
            intentFilter.addAction("io.dcloud.uniplugin.wd.toUi");
            mUniSDKInstance.getContext().registerReceiver(myReceiver, intentFilter);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == REQUEST_CODE && data.hasExtra("respond")) {
                Log.e("TestModule", "原生页面返回！" + data.getStringExtra("respond"));
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            onErrorMessage("onActivityResult：" + e.getMessage());
        }
    }

    @UniJSMethod
    public void gotoNativePage() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent(mUniSDKInstance.getContext(), NativePageActivity.class);
                ((Activity) mUniSDKInstance.getContext()).startActivityForResult(intent, REQUEST_CODE);
            }
        } catch (Exception e) {
            onErrorMessage("gotoNativePage：" + e.getMessage());
        }
    }

    @UniJSMethod
    public void seekTo(int position) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "seekTo");

            JSONObject data = new JSONObject();
            data.put("position", position);
            jsonObject.put("data", data);
            sendContentBroadcast(JSON.toJSONString(jsonObject));
        } catch (Exception e) {
            onErrorMessage("seekTo：" + e.getMessage());
        }
    }

    @UniJSMethod
    public void changePlayerSpeed(float speed) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "changePlayerSpeed");

            JSONObject data = new JSONObject();
            data.put("speed", speed);
            jsonObject.put("data", data);
            sendContentBroadcast(JSON.toJSONString(jsonObject));
        } catch (Exception e) {
            onErrorMessage("changePlayerSpeed：" + e.getMessage());
        }
    }

    @UniJSMethod
    public void playListSong(String id) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "playListSong");

            JSONObject data = new JSONObject();
            data.put("id", id);
            jsonObject.put("data", data);
            sendContentBroadcast(JSON.toJSONString(jsonObject));
        } catch (Exception e) {
            onErrorMessage("playListSong：" + e.getMessage());
        }
    }

    /**
     * 设置当前播放器信息
     *
     * @param currentInfo 当前播放器信息
     */
    @UniJSMethod
    public void setCurrentInfo(CurrentInfo currentInfo) {
        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("action", "setCurrentInfo");
            data.put("data", currentInfo);

            sendContentBroadcast(JSON.toJSONString(data));
        } catch (Exception e) {
            onErrorMessage("setCurrentInfo：" + e.getMessage());
        }
    }

    /**
     * 设置播放列表
     *
     * @param jsonArray 播放列表
     */
    @UniJSMethod
    public void setPlayList(List<Song> jsonArray) {
        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("action", "setPlayList");
            data.put("data", jsonArray);

            sendContentBroadcast(JSON.toJSONString(data));
        } catch (Exception e) {
            onErrorMessage("setPlayList：" + e.getMessage());
        }
    }

    /**
     * 播放
     */
    @UniJSMethod
    public void follow() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent();
                // 指定广播的名字
                intent.setAction("io.dcloud.uniplugin.wd.follow");
                // 发送广播
                mUniSDKInstance.getContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            onErrorMessage("follow：" + e.getMessage());
        }
    }

    /**
     * 播放
     */
    @UniJSMethod
    public void play() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent();
                // 指定广播的名字
                intent.setAction("io.dcloud.uniplugin.wd.play");
                // 发送广播
                mUniSDKInstance.getContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            onErrorMessage("play：" + e.getMessage());
        }
    }

    /**
     * 暂停
     */
    @UniJSMethod
    public void pause() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent();
                // 指定广播的名字
                intent.setAction("io.dcloud.uniplugin.wd.pause");
                // 发送广播
                mUniSDKInstance.getContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            onErrorMessage("pause：" + e.getMessage());
        }
    }

    /**
     * 上一首歌
     */
    @UniJSMethod
    public void previousSong() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent();
                // 指定广播的名字
                intent.setAction("io.dcloud.uniplugin.wd.previoussong");
                // 发送广播
                mUniSDKInstance.getContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            onErrorMessage("previousSong：" + e.getMessage());
        }
    }

    /**
     * 下一首歌
     */
    @UniJSMethod
    public void nextSong() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent();
                // 指定广播的名字
                intent.setAction("io.dcloud.uniplugin.wd.nextsong");
                // 发送广播
                mUniSDKInstance.getContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            onErrorMessage("nextSong：" + e.getMessage());
        }
    }

    /**
     * 关闭
     */
    @UniJSMethod
    public void closeService() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent(mUniSDKInstance.getContext(), PlayerService.class);
                mUniSDKInstance.getContext().stopService(intent);

                mUniSDKInstance.getContext().unregisterReceiver(myReceiver);
            }
            timerHandler.removeCallbacks(myTimerRun);   // 调用此方法，以关闭此定时器
        } catch (Exception e) {
            onErrorMessage("closeService：" + e.getMessage());
        }
    }

    /**
     * 关闭
     */
    @UniJSMethod
    public void close() {
        try {
            if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent();
                // 指定广播的名字
                intent.setAction("io.dcloud.uniplugin.wd.close");
                // 发送广播
                mUniSDKInstance.getContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            onErrorMessage("close：" + e.getMessage());
        }
    }

    /**
     * 发送广播
     */
    private void sendContentBroadcast(String content) {
        if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            Intent intent = new Intent();
            //指定广播的名字
            intent.setAction("io.dcloud.uniplugin.wd.toNb");
            //指定广播的内容
            intent.putExtra("content", content);
            //发送广播
            mUniSDKInstance.getContext().sendBroadcast(intent);
        }
    }

    /**
     * 当播放时
     *
     * @param callback 回调
     */
    @UniJSMethod
    public void onPlay(UniJSCallback callback) {
        try {
            if (callback != null) {
                onPlayCallback.add(callback);
            }
        } catch (Exception e) {
            onErrorMessage("onPlay：" + e.getMessage());
        }
    }

    /**
     * 当暂停时
     *
     * @param callback 回调
     */
    @UniJSMethod
    public void onPause(UniJSCallback callback) {
        try {
            if (callback != null) {
                onPauseCallback.add(callback);
            }
        } catch (Exception e) {
            onErrorMessage("onPause：" + e.getMessage());
        }
    }

    /**
     * 当上一首时
     *
     * @param callback 回调
     */
    @UniJSMethod
    public void onPreviousSong(UniJSCallback callback) {
        try {
            if (callback != null) {
                onPreviousSongCallback.add(callback);
            }
        } catch (Exception e) {
            onErrorMessage("onPreviousSong：" + e.getMessage());
        }
    }

    /**
     * 当下一首时
     *
     * @param callback 回调
     */
    @UniJSMethod
    public void onNextSong(UniJSCallback callback) {
        try {
            if (callback != null) {
                onNextSongCallback.add(callback);
            }
        } catch (Exception e) {
            onErrorMessage("onNextSong：" + e.getMessage());
        }
    }

    /**
     * 当收藏时
     *
     * @param callback 回调
     */
    @UniJSMethod
    public void onFollow(UniJSCallback callback) {
        try {
            if (callback != null) {
                onFollowCallback.add(callback);
            }
        } catch (Exception e) {
            onErrorMessage("onFollow：" + e.getMessage());
        }
    }

    /**
     * 当收藏时
     *
     * @param callback 回调
     */
    @UniJSMethod
    public void onClose(UniJSCallback callback) {
        try {
            if (callback != null) {
                onCloseCallback.add(callback);
            }
        } catch (Exception e) {
            onErrorMessage("onClose：" + e.getMessage());
        }
    }

    /**
     * 当播放信息变化时
     *
     * @param callback 回调
     */
    @UniJSMethod
    public void onPlayerInfo(UniJSCallback callback) {
        try {
            if (callback != null) {
                onPlayerInfoCallback.add(callback);
            }
        } catch (Exception e) {
            onErrorMessage("onPlayerInfo：" + e.getMessage());
        }
    }

    /**
     * 广播监听器
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //获取广播的名字
                String action = intent.getAction();
                if ("io.dcloud.uniplugin.wd.toUi".equals(action)) {
                    //获取广播内容
                    String content = intent.getStringExtra("content");
                    if (content != null && !content.equals("")) {
                        JSONObject jsonObject = JSON.parseObject(content);
                        if (jsonObject.containsKey("action")) {
                            String a = jsonObject.getString("action");
                            if (a != null && !a.equals("")) {
                                switch (a) {
                                    case "player_info":
                                        for (UniJSCallback callback : onPlayerInfoCallback) {
                                            if (jsonObject.containsKey("data")) {
                                                callback.invokeAndKeepAlive(jsonObject.getJSONObject("data"));
                                            } else {
                                                callback.invokeAndKeepAlive(null);
                                            }
                                        }
                                        break;
                                    case "serviceRunning":
                                        if (serverConfig != null) {
                                            setServerConfig(serverConfig);
                                        }
                                        if (userToken != null) {
                                            setUserToken(userToken);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                onErrorMessage("MyReceiver：" + e.getMessage());
            }
        }
    }

    /**
     * 通知栏按钮响应  接收器
     */
    private class MyNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action != null && !action.equals("")) {
                    switch (action) {
                        case "io.dcloud.uniplugin.wd.play":             // 播放
                            for (UniJSCallback callback : onPlayCallback) {
                                callback.invokeAndKeepAlive(null);
                            }
                            break;
                        case "io.dcloud.uniplugin.wd.pause":            // 暂停
                            for (UniJSCallback callback : onPauseCallback) {
                                callback.invokeAndKeepAlive(null);
                            }
                            break;
                        case "io.dcloud.uniplugin.wd.previoussong":         // 上一首歌
                            for (UniJSCallback callback : onPreviousSongCallback) {
                                callback.invokeAndKeepAlive(null);
                            }
                            break;
                        case "io.dcloud.uniplugin.wd.nextsong":         // 下一首歌
                            for (UniJSCallback callback : onNextSongCallback) {
                                callback.invokeAndKeepAlive(null);
                            }
                            break;
                        case "io.dcloud.uniplugin.wd.follow":           // 收藏
                            for (UniJSCallback callback : onFollowCallback) {
                                callback.invokeAndKeepAlive(null);
                            }
                            break;
                        case "io.dcloud.uniplugin.wd.close":            // 关闭通知栏
                            for (UniJSCallback callback : onCloseCallback) {
                                callback.invokeAndKeepAlive(null);
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                onErrorMessage("MyNotificationReceiver：" + e.getMessage());
            }
        }
    }

    private void onErrorMessage(String message) {
        if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            Toast.makeText(mUniSDKInstance.getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
