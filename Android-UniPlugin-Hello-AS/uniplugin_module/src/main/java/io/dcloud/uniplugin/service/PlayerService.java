package io.dcloud.uniplugin.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.dcloud.uniplugin.NativePageActivity;
import io.dcloud.uniplugin.entity.CurrentInfo;
import io.dcloud.uniplugin.entity.PlayerState;
import io.dcloud.uniplugin.entity.Song;
import uni.dcloud.io.uniplugin_module.R;

/**
 * 常驻服务+仿网易云音乐通知栏操作效果
 * 通知栏自定义样式，控件点击+动态变更效果
 * 一堆比你粗名还比你勤奋的人在前面跑，不学习还怎么混?
 *
 * @author 马晓勇
 */
public class PlayerService extends Service {

    private NotificationCompat.Builder notificationBuilder;
    private Notification notice;
    private RemoteViews smallRemoteView;
    private RemoteViews bigRemoteViews;
    private NotificationReceiver notificationReceiver;
    private MyReceiver myReceiver;
    private NotificationManager notificationManager;
    private static final int NOTICEID = 121444;

    private MyBinder binder;
    private static final String NOTIFICATION_CHANNEL_ID = "1";
    private static final String NOTIFICATION_CHANNEL_NAME = "foregroundNotificationChannel";

    /**
     * 播放列表
     */
    private List<Song> playList = new ArrayList<>();
    private final List<Song> playHistory = new ArrayList<>();
    private CurrentInfo currentInfo;
    private MediaPlayer player;
    private PlayerState playerState = PlayerState.NONE;

    private boolean isPlaying = false;

    /**
     * 定时器
     */
    private final Handler timerHandler = new Handler();
    private Runnable myTimerRun;

    private String host;
    private String token;

    private Long screenOffTime = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            // 通知栏广播
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("io.dcloud.uniplugin.wd.play");
            intentFilter.addAction("io.dcloud.uniplugin.wd.pause");
            intentFilter.addAction("io.dcloud.uniplugin.wd.previoussong");
            intentFilter.addAction("io.dcloud.uniplugin.wd.nextsong");
            intentFilter.addAction("io.dcloud.uniplugin.wd.follow");
            intentFilter.addAction("io.dcloud.uniplugin.wd.close");
            intentFilter.addAction("android.intent.action.PHONE_STATE");
            intentFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            notificationReceiver = new NotificationReceiver();
            this.registerReceiver(notificationReceiver, intentFilter);

            // 来自ui的广播
            myReceiver = new MyReceiver();
            intentFilter = new IntentFilter();
            intentFilter.addAction("io.dcloud.uniplugin.wd.toNb");
            this.registerReceiver(myReceiver, intentFilter);
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "onCreate：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "onCreate：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            preNotification();

            // 开始定时器
            startTimer();

            // 发送服务启动成功通知
            JSONObject content = new JSONObject();
            content.put("action", "serviceRunning");
            content.put("time", System.currentTimeMillis());
            broadcastToUi(JSON.toJSONString(content));

            if (intent != null) {
                String playList = intent.getStringExtra("playList");
                if (playList != null && !playList.equals("")) {
                    JSONArray jsonArray = JSON.parseArray(playList);
                    if (jsonArray != null && jsonArray.size() > 0) {
                        setPlayList(jsonArray);
                    }
                }

                String currInfoStr = intent.getStringExtra("currentInfo");
                if (currInfoStr != null && !currInfoStr.equals("")) {
                    JSONObject jsonObject = JSON.parseObject(currInfoStr);
                    CurrentInfo ci = jsonToCurrentInfo(jsonObject);
                    if (ci == null || ci.getCurrentSong() == null) {
                        Toast.makeText(PlayerService.this, "资源文件不存在", Toast.LENGTH_LONG).show();
                        return Service.START_REDELIVER_INTENT;
                    } else {
                        currentInfo = ci;
                    }
                }
                boolean playNow = intent.getBooleanExtra("playNow", false);
                if (playNow) {
                    startPlay();
                }
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "onStartCommand：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "onStartCommand：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        // START_REDELIVER_INTENT 重投递intent,当service被kill后，会重启service,重新传递intent
        // START_STICKY 也会重启service但是intent会传入null
        return Service.START_REDELIVER_INTENT;
    }

    /**
     * 用定时器广播 播放器信息
     */
    private void startTimer() {
        myTimerRun = new Runnable()                                         // 创建一个runnable对象
        {
            @Override
            public void run() {
                try {
                    // 熄屏一个小时自动暂停
                    if (screenOffTime > 0 && System.currentTimeMillis() > screenOffTime + 60 * 60 * 1000) {
                        pausePlay();
                    }

                    // 推送当前播放器状态
                    JSONObject content = new JSONObject();
                    content.put("action", "player_info");
                    content.put("time", System.currentTimeMillis());

                    JSONObject data = new JSONObject();
                    if (player != null) {
                        data.put("is_playing", player.isPlaying());
                        data.put("player_state", playerState);

                        if (playerState != PlayerState.ERROR) {
                            data.put("current_position", player.getCurrentPosition());
                        } else {
                            data.put("current_position", 0);
                        }
                        if (playerState == PlayerState.STARTED ||
                                playerState == PlayerState.STOPPED ||
                                playerState == PlayerState.PAUSED ||
                                playerState == PlayerState.PLAYBACK_COMPLETED ||
                                playerState == PlayerState.END) {
                            data.put("duration", player.getDuration());
                        } else {
                            data.put("duration", 0);
                        }
                    } else {
                        data.put("is_playing", false);
                        data.put("player_state", PlayerState.NONE);
                        data.put("current_position", 0);
                        data.put("duration", 0);
                    }
                    data.put("has_previous", hasPrevious());
                    data.put("has_next", hasNext());
                    if (currentInfo != null && currentInfo.getCurrentSong() != null) {
                        data.put("speed", currentInfo.getSpeed());
                        data.put("current_song", currentInfo.getCurrentSong());
                    } else {
                        data.put("speed", 1);
                        data.put("current_song", null);
                    }

                    content.put("data", data);
                    broadcastToUi(JSON.toJSONString(content));
                } catch (Exception e) {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    String message = "";
                    StackTraceElement[] ste = e.getStackTrace();
                    if (ste != null && ste.length > 0) {
                        message = "startTimer：" + ste[0].getLineNumber() + " - " + e.getMessage();
                    } else {
                        message = "startTimer：" + e.getMessage();
                    }
                    Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
                timerHandler.postDelayed(this, 500);        // 再次调用myTimerRun对象，实现每两秒一次的定时器操作
            }
        };
        timerHandler.postDelayed(myTimerRun, 500);                  // 使用postDelayed方法，两秒后再调用此myTimerRun对象
    }

    @Override
    public void onDestroy() {
        try {
            this.stopForeground(true);
            this.unregisterReceiver(notificationReceiver);
            this.unregisterReceiver(myReceiver);
            timerHandler.removeCallbacks(myTimerRun);   // 调用此方法，以关闭此定时器

            if (this.player != null) {
                this.player.release();
                playerState = PlayerState.END;
                this.player = null;
                playerState = PlayerState.NONE;
            }
            super.onDestroy();
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "onDestroy：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "onDestroy：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        try {
            preNotification();
            return binder = new MyBinder(this);
        } catch (Exception e) {
            Toast.makeText(this, "onBind：" + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            return super.onUnbind(intent);
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "onUnbind：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "onUnbind：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * 通知栏按钮响应  接收器
     */
    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action != null && !action.equals("")) {
                    switch (action) {
                        case "io.dcloud.uniplugin.wd.play":             // 播放
                            startPlay();
                            break;
                        case "io.dcloud.uniplugin.wd.pause":            // 暂停
                        case "android.intent.action.PHONE_STATE":            // 暂停
                        case "android.intent.action.NEW_OUTGOING_CALL":            // 暂停
                            pausePlay();
                            break;
                        case "io.dcloud.uniplugin.wd.previoussong":     // 上一首歌
                            previousSong();
                            break;
                        case "io.dcloud.uniplugin.wd.nextsong":         // 下一首歌
                            nextSong();
                            break;
                        case "io.dcloud.uniplugin.wd.follow":           // 收藏
                            // bind方式启动的service不能弹出Toast
                            // doFavorites();
                            break;
                        case "io.dcloud.uniplugin.wd.close":            // 关闭通知栏
                            // startService方式启动才有效
                            stopSelf();
                            break;
                        case "android.intent.action.SCREEN_OFF":
                            screenOffTime = System.currentTimeMillis();
                            break;
                        case "android.intent.action.SCREEN_ON":
                            screenOffTime = 0L;
                            break;
                    }
                }
            } catch (Exception e) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                String message = "";
                StackTraceElement[] ste = e.getStackTrace();
                if (ste != null && ste.length > 0) {
                    message = "NotificationReceiver：" + ste[0].getLineNumber() + " - " + e.getMessage();
                } else {
                    message = "NotificationReceiver：" + e.getMessage();
                }
                Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }
    }

    /**
     * 开始播放
     */
    private void startPlay() {
        try {
            if (playList.size() <= 0) {
                currentInfo = null;
                isPlaying = false;
            } else {
                if (currentInfo == null) {
                    setCurrentPlayInfo(playList.get(0));
                }
                if (currentInfo == null || currentInfo.getCurrentSong() == null) {
                    Toast.makeText(this, "资源文件不存在", Toast.LENGTH_LONG).show();
                    return;
                }
                // 最后播放的曲目是不是当前曲目
                boolean isLast = playHistory.size() > 0 && playHistory.get(playHistory.size() - 1).getId().equals(currentInfo.getCurrentSong().getId());
                MediaPlayer player = getMediaPlayer();
                isPlaying = true;

                if (isLast && playerState == PlayerState.PAUSED) {
                    player.start();
                    playerState = PlayerState.STARTED;
                } else {
                    if (playerState != PlayerState.IDLE) {
                        // player.reset();
                        // playerState = PlayerState.IDLE;

                        this.player.release();
                        playerState = PlayerState.END;
                        this.player = null;
                        playerState = PlayerState.NONE;
                        player = getMediaPlayer();

                        // 发送收听记录请求
                        // addMemberPlayHistory();
                    }

                    try {
                        String path1 = Environment.getExternalStoragePublicDirectory("apps").getAbsolutePath();

                        player.setDataSource(currentInfo.getCurrentSong().getUrl());
                        playerState = PlayerState.INITIALIZED;
                        player.prepareAsync();
                        playerState = PlayerState.PREPARING;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                smallRemoteView.setViewVisibility(R.id.pouse, View.VISIBLE);
                smallRemoteView.setViewVisibility(R.id.play, View.INVISIBLE);

                bigRemoteViews.setViewVisibility(R.id.pouse, View.VISIBLE);
                bigRemoteViews.setViewVisibility(R.id.play, View.INVISIBLE);

                notificationBuilder.setCustomContentView(smallRemoteView);
                notificationBuilder.setCustomBigContentView(bigRemoteViews);
                notificationManager.notify(NOTICEID, notificationBuilder.build());

                // 如果当前曲目不是最后一个历史播放曲目
                if (!isLast) {
                    playHistory.add(currentInfo.getCurrentSong());
                }
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "startPlay：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "startPlay：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * service里面自定义的方法
     */
    private void methodInMyService() {

    }

    /**
     * 设置当前播放节目
     *
     * @param songItem 节目
     */
    private void setCurrentPlayInfo(Song songItem) {
        try {
            CurrentInfo currInfo = new CurrentInfo();
            if (currentInfo != null) {
                currInfo.setPlayMode(currentInfo.getPlayMode());
            }
            currInfo.setCurrentSong(songItem);
            setCurrentPlayInfo(currInfo);
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "setCurrentPlayInfo：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "setCurrentPlayInfo：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 设置当前播放信息
     *
     * @param currInfo 播放信息
     */
    private void setCurrentPlayInfo(CurrentInfo currInfo) {
        try {
            if (currInfo == null || currInfo.getCurrentSong() == null) {
                Toast.makeText(this, "资源文件不存在", Toast.LENGTH_LONG).show();
                return;
            }
            currentInfo = currInfo;
            String name = currentInfo.getCurrentSong().getName();
            String description = currentInfo.getCurrentSong().getDescription() != null && !currentInfo.getCurrentSong().getDescription().isEmpty() ?
                    currentInfo.getCurrentSong().getDescription() : " ~ ";

            smallRemoteView.setTextViewText(R.id.songname, name);
            smallRemoteView.setTextViewText(R.id.songer, description);

            bigRemoteViews.setTextViewText(R.id.songname, name);
            bigRemoteViews.setTextViewText(R.id.songer, description);

            // 收藏状态
            if (currInfo.getCurrentSong().getFollow() == 1) {
                smallRemoteView.setImageViewResource(R.id.follow, R.drawable.follow_yes);
                bigRemoteViews.setImageViewResource(R.id.follow, R.drawable.follow_yes);
            } else {
                smallRemoteView.setImageViewResource(R.id.follow, R.drawable.follow);
                bigRemoteViews.setImageViewResource(R.id.follow, R.drawable.follow);
            }

            notificationBuilder.setCustomContentView(smallRemoteView);
            notificationBuilder.setCustomBigContentView(bigRemoteViews);
            notificationManager.notify(NOTICEID, notificationBuilder.build());

            if (currentInfo.getCurrentSong().getCover() != null && !currentInfo.getCurrentSong().getCover().equals("")) {
                setSongLogoImages(currentInfo.getCurrentSong().getCover());
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "setCurrentPlayInfo：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "setCurrentPlayInfo：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 从头开始播放节目
     *
     * @param songItem 播放节目
     */
    private void replaySong(Song songItem) {
        try {
            MediaPlayer player = getMediaPlayer();
            player.release();
            playerState = PlayerState.IDLE;
            setCurrentPlayInfo(songItem);
            startPlay();
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "replaySong：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "replaySong：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 从播放列表移除
     *
     * @param jsonObject json对象
     */
    private void removeFromPlayList(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString("data");
            List<Song> indexList = new ArrayList<>();
            for (int i = 0; i < playList.size(); i++) {
                if (playList.get(i).getId().equals(id)) {
                    indexList.add(playList.get(i));
                }
            }

            if (indexList.size() > 0) {
                playList.removeAll(indexList);
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "removeFromPlayList：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "removeFromPlayList：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 拖动到指定位置
     *
     * @param position 位置
     */
    private void seekTo(int position) {
        try {
            if (playerState == PlayerState.PREPARED ||
                    playerState == PlayerState.STARTED ||
                    playerState == PlayerState.PAUSED ||
                    playerState == PlayerState.PLAYBACK_COMPLETED) {
                player.seekTo(position);
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "seekTo：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "seekTo：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 改变播放速率
     *
     * @param speed 播放速率
     */
    private void changePlayerSpeed(float speed) {
        try {
            // this checks on API 23 and up
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (player.isPlaying()) {
                    player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));
                } else {
                    player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));
                    player.pause();
                }
                if (currentInfo != null) {
                    currentInfo.setSpeed(speed);
                }
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "changePlayerSpeed：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "changePlayerSpeed：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 播放列表中的曲目
     *
     * @param id 曲目id
     */
    private void playListSong(String id) {
        try {
            int index = getSongIndex(id);
            if (index >= 0) {
                setCurrentPlayInfo(playList.get(index));
                startPlay();
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "playListSong：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "playListSong：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 设置播放列表
     *
     * @param jsonObject json对象
     */
    private void setPlayList(JSONObject jsonObject) {
        try {
            JSONArray sl = jsonObject.getJSONArray("data");
            setPlayList(sl);
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "setPlayList：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "setPlayList：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 设置播放列表
     *
     * @param jsonArray json对象
     */
    private void setPlayList(JSONArray jsonArray) {
        try {
            List<Song> sil = new ArrayList<>();
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    sil.add(jsonToSong(jo));
                }
            }
            if (sil.size() > 0) {
                playList = sil;
                if (currentInfo == null) {
                    setCurrentPlayInfo(sil.get(0));
                }
            } else {
                stopSelf();
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "setPlayList：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "setPlayList：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 添加到播放列表
     *
     * @param jsonObject json对象
     */
    private Song addToPlayList(JSONObject jsonObject) {
        try {
            JSONObject jo = jsonObject.getJSONObject("data");
            Song si = jsonToSong(jo);

            return addToPlayList(si);
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "addToPlayList：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "addToPlayList：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * 设置服务信息
     *
     * @param host 域名
     */
    private void setServerConfig(String host) {
        this.host = host;
    }

    /**
     * 设置token
     *
     * @param token token
     */
    private void setUserToken(String token) {
        this.token = token;
    }

    /**
     * 添加到播放列表
     *
     * @param songItem 曲目
     * @return 曲目
     */
    private Song addToPlayList(Song songItem) {
        try {
            for (Song s : playList) {
                if (s.getId().equals(songItem.getId())) {
                    return s;
                }
            }
            playList.add(songItem);
            return songItem;
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "addToPlayList：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "addToPlayList：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * 添加到播放列表并播放
     *
     * @param jsonObject json对象
     */
    private void addToPlayListAndPlay(JSONObject jsonObject) {
        try {
            Song si = addToPlayList(jsonObject);
            setCurrentPlayInfo(si);
            startPlay();
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "addToPlayListAndPlay：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "addToPlayListAndPlay：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 向ui发送数据
     *
     * @param content 广播内容
     */
    private void broadcastToUi(String content) {
        Intent uiiIntent = new Intent();
        //指定广播的名字
        uiiIntent.setAction("io.dcloud.uniplugin.wd.toUi");
        //指定广播的内容
        uiiIntent.putExtra("content", content);
        //发送广播
        this.sendBroadcast(uiiIntent);
    }

    /**
     * 暂停播放
     */
    private void pausePlay() {
        try {
            isPlaying = false;
            getMediaPlayer().pause();
            playerState = PlayerState.PAUSED;

            smallRemoteView.setViewVisibility(R.id.pouse, View.INVISIBLE);
            smallRemoteView.setViewVisibility(R.id.play, View.VISIBLE);

            bigRemoteViews.setViewVisibility(R.id.pouse, View.INVISIBLE);
            bigRemoteViews.setViewVisibility(R.id.play, View.VISIBLE);
            notificationBuilder.setCustomContentView(smallRemoteView);
            notificationBuilder.setCustomBigContentView(bigRemoteViews);
            notificationManager.notify(NOTICEID, notificationBuilder.build());
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "pausePlay：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "pausePlay：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 上一首
     */
    private void previousSong() {
        try {
            int index = getCurrentSongIndex();
            if (index > 0) {
                setCurrentPlayInfo(playList.get(index - 1));
                startPlay();
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "previousSong：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "previousSong：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 是否有上一首
     *
     * @return 是否有
     */
    private boolean hasPrevious() {
        try {
            int index = getCurrentSongIndex();
            return index > 0 && playList.size() > 0;
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "hasPrevious：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "hasPrevious：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * 下一首
     */
    private void nextSong() {
        try {
            int index = getCurrentSongIndex();
            if (index < playList.size() - 1) {
                setCurrentPlayInfo(playList.get(index + 1));
                startPlay();
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "nextSong：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "nextSong：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 是否有下一首
     *
     * @return 是否有
     */
    private boolean hasNext() {
        try {
            int index = getCurrentSongIndex();
            return index < playList.size() - 1;
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "hasNext：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "hasNext：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * 获取当前曲目下标
     *
     * @return 下标 -1表示当前曲目不存在
     */
    private int getCurrentSongIndex() {
        int currIndex = -1;
        try {
            if (currentInfo != null && currentInfo.getCurrentSong() != null) {
                for (int i = 0; i < playList.size(); i++) {
                    if (playList.get(i).getId().equals(currentInfo.getCurrentSong().getId())) {
                        currIndex = i;
                    }
                }
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "getCurrentSongIndex：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "getCurrentSongIndex：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        return currIndex;
    }

    /**
     * 获取曲目下标
     *
     * @param id id
     * @return 下标
     */
    private int getSongIndex(String id) {
        int currIndex = -1;
        try {
            for (int i = 0; i < playList.size(); i++) {
                if (playList.get(i).getId().equals(id)) {
                    currIndex = i;
                }
            }
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "getSongIndex：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "getSongIndex：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        return currIndex;
    }

    /**
     * 获取播放器
     *
     * @return 返回值
     */
    private MediaPlayer getMediaPlayer() {
        if (player == null) {
            try {
                currentInfo.setCurrentPosition(0);
                // 如果为空就new一个
                final MediaPlayer mediaPlayer = new MediaPlayer();
                // 添加准备好的监听
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        try {
                            // 如果准备好了，就会进行这个方法
                            playerState = PlayerState.PREPARED;
                            mp.seekTo(currentInfo.getCurrentPosition());

                            // 如果准备好了，就会进行这个方法
                            if (isPlaying) {
                                mp.start();
                                playerState = PlayerState.STARTED;
                            }
                        } catch (Exception e) {
                            if (Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            String message = "";
                            StackTraceElement[] ste = e.getStackTrace();
                            if (ste != null && ste.length > 0) {
                                message = "onPrepared：" + ste[0].getLineNumber() + " - " + e.getMessage();
                            } else {
                                message = "onPrepared：" + e.getMessage();
                            }
                            Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }
                });
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        try {
                            playerState = PlayerState.ERROR;
                            mp.release();
                            playerState = PlayerState.NONE;
                            player = null;

                            smallRemoteView.setViewVisibility(R.id.pouse, View.INVISIBLE);
                            smallRemoteView.setViewVisibility(R.id.play, View.VISIBLE);
                            bigRemoteViews.setViewVisibility(R.id.pouse, View.INVISIBLE);
                            bigRemoteViews.setViewVisibility(R.id.play, View.VISIBLE);

                            notificationBuilder.setCustomContentView(smallRemoteView);
                            notificationBuilder.setCustomBigContentView(bigRemoteViews);
                            notificationManager.notify(NOTICEID, notificationBuilder.build());

                            // 提示
                            if (Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            Toast.makeText(PlayerService.this, "播放错误", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        } catch (Exception e) {
                            if (Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            String message = "";
                            StackTraceElement[] ste = e.getStackTrace();
                            if (ste != null && ste.length > 0) {
                                message = "onError：" + ste[0].getLineNumber() + " - " + e.getMessage();
                            } else {
                                message = "onError：" + e.getMessage();
                            }

                            Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                        return true;
                    }
                });
                mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        System.out.println(percent);
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            playerState = PlayerState.PLAYBACK_COMPLETED;
                            int cp = mp.getCurrentPosition();
                            int dt = mp.getDuration();

                            if (dt - cp < 1000) {
                                if (currentInfo != null && currentInfo.getPlayMode() == 1) {
                                    nextSong();
                                }
                            } else {
                                currentInfo.setCurrentPosition(cp);
                                mp.stop();
                                playerState = PlayerState.STOPPED;
                                mp.prepareAsync();
                                playerState = PlayerState.PREPARING;
                            }
                        } catch (Exception e) {
                            if (Looper.myLooper() == null) {
                                Looper.prepare();
                            }
                            String message = "";
                            StackTraceElement[] ste = e.getStackTrace();
                            if (ste != null && ste.length > 0) {
                                message = "onCompletion：" + ste[0].getLineNumber() + " - " + e.getMessage();
                            } else {
                                message = "onCompletion：" + e.getMessage();
                            }
                            Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }
                });
                playerState = PlayerState.IDLE;
                player = mediaPlayer;
            } catch (Exception e) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                String message = "";
                StackTraceElement[] ste = e.getStackTrace();
                if (ste != null && ste.length > 0) {
                    message = "getPLMediaPlayer：" + ste[0].getLineNumber() + " - " + e.getMessage();
                } else {
                    message = "getPLMediaPlayer：" + e.getMessage();
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }
        return player;
    }

    /**
     * 通过json创建SongItem
     *
     * @param jo json
     * @return songItem
     */
    private Song jsonToSong(JSONObject jo) {
        try {
            Song si = new Song();
            if (jo != null && jo.containsKey("id") && jo.getString("id") != null && !jo.getString("id").equals("")) {
                si.setId(jo.getString("id"));
                si.setName(jo.getString("name"));
                si.setAuthor(jo.getString("author"));
                si.setDescription(jo.getString("description"));
                si.setCover(jo.getString("cover"));
                si.setUrl(jo.getString("url"));
                si.setFollow(jo.getInteger("follow"));
            }
            return si;
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "jsonToSong：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "jsonToSong：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * 解析json成为CurrentInfo
     *
     * @param jsonObject json
     * @return CurrentInfo
     */
    private CurrentInfo jsonToCurrentInfo(JSONObject jsonObject) {
        try {
            CurrentInfo currInfo = new CurrentInfo();
            if (jsonObject.containsKey("currentSong")) {
                JSONObject csJsonObject = jsonObject.getJSONObject("currentSong");
                if (csJsonObject != null) {
                    currInfo.setCurrentSong(jsonToSong(csJsonObject));
                }
            }
            if (jsonObject.containsKey("currentPosition")) {
                currInfo.setCurrentPosition(jsonObject.getInteger("currentPosition"));
            }
            if (jsonObject.containsKey("playMode")) {
                currInfo.setCurrentPosition(jsonObject.getInteger("playMode"));
            }
            if (jsonObject.containsKey("speed")) {
                currInfo.setSpeed(jsonObject.getFloat("speed"));
            }
            return currInfo;
        } catch (Exception e) {
            String message = "";
            StackTraceElement[] ste = e.getStackTrace();
            if (ste != null && ste.length > 0) {
                message = "jsonToCurrentInfo：" + ste[0].getLineNumber() + " - " + e.getMessage();
            } else {
                message = "jsonToCurrentInfo：" + e.getMessage();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * 更改图片logo
     *
     * @param url 图片url
     */
    private void setSongLogoImages(final String url) {
        new Thread() {
            @Override
            public void run() {
                try {
                    // 把传过来的路径转成URL
                    URL u = new URL(url + "?imageMogr2/thumbnail/!360x360r");
                    //获取连接
                    HttpURLConnection connection = (HttpURLConnection) u.openConnection();
                    // 使用GET方法访问网络
                    connection.setRequestMethod("GET");
                    // 超时时间为10秒
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = connection.getInputStream();

                        // 使用工厂把网络的输入流生产Bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        int ii = bitmap.getByteCount();
                        inputStream.close();

                        if (currentInfo != null
                                && currentInfo.getCurrentSong() != null
                                && currentInfo.getCurrentSong().getCover() != null
                                && currentInfo.getCurrentSong().getCover().equals(url)) {
                            smallRemoteView.setImageViewBitmap(R.id.songlogo, bitmap);
                            bigRemoteViews.setImageViewBitmap(R.id.songlogo, bitmap);

                            notificationBuilder.setCustomContentView(smallRemoteView);
                            notificationBuilder.setCustomBigContentView(bigRemoteViews);
                            notificationManager.notify(NOTICEID, notificationBuilder.build());
                        }
                    } else {
                        smallRemoteView.setImageViewResource(R.id.songlogo, R.drawable.no_song);
                        bigRemoteViews.setImageViewResource(R.id.songlogo, R.drawable.no_song);
                    }
                    notificationBuilder.setCustomContentView(smallRemoteView);
                    notificationBuilder.setCustomBigContentView(bigRemoteViews);
                    notificationManager.notify(NOTICEID, notificationBuilder.build());
                } catch (Exception e) {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    String message = "";
                    StackTraceElement[] ste = e.getStackTrace();
                    if (ste != null && ste.length > 0) {
                        message = "setSongLogoImages：" + ste[0].getLineNumber() + " - " + e.getMessage();
                    } else {
                        message = "setSongLogoImages：" + e.getMessage();
                    }
                    Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        }.start();
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取广播的名字
            String action = intent.getAction();
            if ("io.dcloud.uniplugin.wd.toNb".equals(action)) {
                try {
                    //获取广播内容
                    String content = intent.getStringExtra("content");
                    JSONObject jsonObject = JSON.parseObject(content);

                    String a = jsonObject.getString("action");
                    switch (a) {
                        case "setPlayList":
                            setPlayList(jsonObject);
                            break;
                        case "addToPlayList":
                            addToPlayList(jsonObject);
                            break;
                        case "addToPlayListAndPlay":
                            addToPlayListAndPlay(jsonObject);
                            break;
                        case "removeFromPlayList":
                            removeFromPlayList(jsonObject);
                            break;
                        case "seekTo":
                            if (jsonObject.containsKey("data") && jsonObject.getJSONObject("data").containsKey("position")) {
                                int position = jsonObject.getJSONObject("data").getInteger("position");
                                seekTo(position);
                            }
                            break;
                        case "changePlayerSpeed":
                            if (jsonObject.containsKey("data") && jsonObject.getJSONObject("data").containsKey("speed")) {
                                float speed = jsonObject.getJSONObject("data").getFloat("speed");
                                changePlayerSpeed(speed);
                            }
                            break;
                        case "playListSong":
                            if (jsonObject.containsKey("data") && jsonObject.getJSONObject("data").containsKey("id")) {
                                String id = jsonObject.getJSONObject("data").getString("id");
                                playListSong(id);
                            }
                            break;
                        case "setCurrentInfo":
                            if (jsonObject.containsKey("data")) {
                                CurrentInfo currInfo = jsonToCurrentInfo(jsonObject.getJSONObject("data"));
                                setCurrentPlayInfo(currInfo);
                            }
                            break;
                        case "setServerConfig":
                            if (jsonObject.containsKey("data")) {
                                String serverUrl = jsonObject.getJSONObject("data").getString("serverUrl");
                                setServerConfig(serverUrl);
                            }
                            break;
                        case "setUserToken":
                            if (jsonObject.containsKey("data")) {
                                String token = jsonObject.getJSONObject("data").getString("token");
                                setUserToken(token);
                            }
                            break;
                    }
                } catch (Exception e) {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    String message = "";
                    StackTraceElement[] ste = e.getStackTrace();
                    if (ste != null && ste.length > 0) {
                        message = "MyReceiver：" + ste[0].getLineNumber() + " - " + e.getMessage();
                    } else {
                        message = "MyReceiver：" + e.getMessage();
                    }
                    Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        }
    }

    public class MyBinder extends Binder implements IMyBinder {
        /**
         * 避免activity结束时,由于service中强引用activity导致内存泄漏
         */
        private WeakReference<NativePageActivity> mainActivityWeakReference;

        private PlayerService myService;

        private MyBinder(PlayerService myService) {
            this.myService = myService;
        }

        public PlayerService getMyService() {
            return myService;
        }

        @Override
        public void setActivity(Activity activity) {
            try {
                if (activity instanceof NativePageActivity) {
                    mainActivityWeakReference = new WeakReference<>((NativePageActivity) activity);
                }
            } catch (Exception e) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                String message = "";
                StackTraceElement[] ste = e.getStackTrace();
                if (ste != null && ste.length > 0) {
                    message = "MyBinder：" + ste[0].getLineNumber() + " - " + e.getMessage();
                } else {
                    message = "MyBinder：" + e.getMessage();
                }
                Toast.makeText(PlayerService.this, message, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }

        @Override
        public void callMethodOfService() {
            methodInMyService();
        }
    }

    /**
     * 准备启动通知栏
     * <p>
     * <p>
     * Android 8.0 新的重要特性：NotificationChannel 用于桌面图标提示，长安弹出消息内容
     * （1）创建NotificationChannel
     * <p>
     * 如果你需要发送属于某个自定义渠道的通知，你需要在发送通知前创建自定义通知渠道，示例如下：
     * <p>
     * //ChannelId为"1",ChannelName为"Channel1"
     * NotificationChannel channel = new NotificationChannel("1","Channel1", NotificationManager
     * .IMPORTANCE_DEFAULT);
     * channel.setLightColor(Color.BLUE); //应用图标提示点颜色
     * channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
     * channel.enableLights(true); //是否允许控制手机通知灯
     * channel.enableVibration(true);//是否允许震动
     * notificationManager.createNotificationChannel(channel);
     * <p>
     * （2）向NotificationChannel发送通知
     * <p>
     * public static void showChannel1Notification(Context context) {
     * int notificationId = 0x1234;
     * Notification.Builder builder = new Notification.Builder(context,"1"); //与channelId对应
     * //icon title text必须包含，不然影响桌面图标小红点的展示
     * builder.setSmallIcon(android.R.drawable.stat_notify_chat)
     * .setContentTitle("xxx")
     * .setContentText("xxx")
     * .setNumber(3); //久按桌面图标时允许的此条通知的数量
     * NotificationManager notificationManager = (NotificationManager) context.getSystemService
     * (Context.NOTIFICATION_SERVICE);
     * notificationManager.notify(notificationId, builder.build());
     * }
     * <p>
     * （3）删除NotificationChannel
     * notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID);
     */
    private void preNotification() {
        synchronized (PlayerService.this) {
            //避免重复创建通知
            if (this.notificationBuilder == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = createNotificationChannel(false);
                    if (notificationChannel != null) {
                        this.notificationBuilder = new NotificationCompat.Builder(this, notificationChannel.getId());
                    } else {
                        this.notificationBuilder = new NotificationCompat.Builder(this);
                    }
                } else {
                    this.notificationBuilder = new NotificationCompat.Builder(this);
                }

                this.startForeground(NOTICEID, getNotification());
            }
        }
    }

    /**
     * 创建NotificationChannel
     * <p>
     * 在Android 8.0以上版本中，桌面图标可以在有消息通知时出现一个小红点，用户可以长安图标，弹出消息提示
     *
     * @param importance 消息级别  true:高级(通知灯+震动+提示音)  false:低级(不打扰)
     */
    private NotificationChannel createNotificationChannel(boolean importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, importance ? NotificationManager.IMPORTANCE_HIGH :
                    NotificationManager.IMPORTANCE_LOW);
            //应用图标提示点颜色
            channel.setLightColor(Color.BLUE);
            //是否在久按桌面图标时显示此渠道的通知
            channel.setShowBadge(true);
            //是否允许震动
            channel.enableVibration(true);
            //是否允许控制手机通知灯
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
            return channel;
        } else {
            return null;
        }
    }

    /**
     * 自定义通知栏常编辑
     *
     * @return 通知栏
     */
    private Notification getNotification() {
        // android 6.0以上，SmallIcon需要使用白色图标，否则android会把不透明的部分全部绘制成白色
        notificationBuilder.setSmallIcon(R.drawable.notificationlogomusic);
        // notificationBuilder.setTicker("动漫之家");//状态栏消息滚动说明
        notificationBuilder.setContentTitle("耽听");
        notificationBuilder.setContentText("耽听正在运行");
        // 时间
        notificationBuilder.setWhen(System.currentTimeMillis());
        // Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        // notificationBuilder.setLargeIcon(bitmap);

        // 设置带进度条的通知栏 前提是使用默认通知栏布局
        // 第三个参数为设置进度条样式,true : 不确定式进度条，false : 精确自增式进度条
        // notificationBuilder.setProgress(100,10,false);

        // 5.0以上横幅消息推送
        // 情况1:设置消息优先级 从Android 5.0开始，如果notification  priority设置为HIGH, MAX,
        // 并且设置了notificationbuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
        // 情况2:设置setfullscreenintent();
        // 声音+震动+呼吸灯
        // notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        // 设置点击后不取消
        // notificationBuilder.setAutoCancel(false);
        //设置为前台常驻服务通知 setAutoCancel和setOngoing调用一个即可
        notificationBuilder.setOngoing(true);

        notificationBuilder.setCustomContentView(getSmallRemoteView());
        notificationBuilder.setCustomBigContentView(getBigRemoteViews());
        /*
         设置可见性

         您的应用可以控制在安全锁定屏幕上显示的通知中可见的详细级别。 调用 setVisibility() 并指定以下值之一：

         VISIBILITY_PUBLIC 显示通知的完整内容。
         VISIBILITY_SECRET 不会在锁定屏幕上显示此通知的任何部分。
         VISIBILITY_PRIVATE 显示通知图标和内容标题等基本信息，但是隐藏通知的完整内容。
         设置 VISIBILITY_PRIVATE 后，您还可以提供其中隐藏了某些详细信息的替换版本通知内容。
         例如，短信 应用可能会显示一条通知，指出“您有 3 条新短信”，但是隐藏了短信内容和发件人。
         要提供此替换版本的通知，请先使用 NotificationCompat.Builder 创建替换通知。创建专用通知对象时，请通过 setPublicVersion()
         方法为其附加替换通知。
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 5.0系统以上，锁屏界面显示
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        notificationBuilder.setContentIntent(notificationClick());
        return notice = notificationBuilder.build();
    }

    /**
     * @return 返回高度64dp标准通知栏样式
     */
    private RemoteViews getSmallRemoteView() {
        smallRemoteView = new RemoteViews(getPackageName(),
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
                        R.layout.notification_small_layout_api24 :
                        R.layout.notification_small_layout);
        // Android4.X
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            smallRemoteView.setTextColor(R.id.songname, Color.WHITE);
        }
        smallRemoteView.setTextViewText(R.id.songname, "无节目");
        smallRemoteView.setTextViewText(R.id.songer, "~");
        smallRemoteView.setImageViewResource(R.id.songlogo, R.drawable.no_song);

        smallRemoteView.setOnClickPendingIntent(R.id.play,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1002,
                        new Intent("io.dcloud.uniplugin.wd.play"), PendingIntent.FLAG_UPDATE_CURRENT));
        smallRemoteView.setOnClickPendingIntent(R.id.pouse,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1003,
                        new Intent("io.dcloud.uniplugin.wd.pause"), PendingIntent.FLAG_UPDATE_CURRENT));
        smallRemoteView.setOnClickPendingIntent(R.id.nextsong,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1004,
                        new Intent("io.dcloud.uniplugin.wd.nextsong"), PendingIntent.FLAG_UPDATE_CURRENT));
        smallRemoteView.setOnClickPendingIntent(R.id.follow,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1005,
                        new Intent("io.dcloud.uniplugin.wd.follow"), PendingIntent.FLAG_UPDATE_CURRENT));
        smallRemoteView.setOnClickPendingIntent(R.id.close,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1006,
                        new Intent("io.dcloud.uniplugin.wd.close"), PendingIntent.FLAG_UPDATE_CURRENT));
        smallRemoteView.setOnClickPendingIntent(R.id.lastsong,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1013,
                        new Intent("io.dcloud.uniplugin.wd.previoussong"), PendingIntent.FLAG_UPDATE_CURRENT));

        return smallRemoteView;
    }

    /**
     * @return 返回高度大于64dp的自定义通知栏样式
     */
    private RemoteViews getBigRemoteViews() {
        bigRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_big_layout);
        //Android4.X
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            bigRemoteViews.setTextColor(R.id.songname, Color.WHITE);
            bigRemoteViews.setViewVisibility(R.id.line, View.INVISIBLE);
        }
        bigRemoteViews.setTextViewText(R.id.songname, "无节目");
        bigRemoteViews.setTextViewText(R.id.songer, "~");
        bigRemoteViews.setImageViewResource(R.id.songlogo, R.drawable.no_song);

        bigRemoteViews.setOnClickPendingIntent(R.id.play,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1007,
                        new Intent("io.dcloud.uniplugin.wd.play"), PendingIntent.FLAG_UPDATE_CURRENT));
        bigRemoteViews.setOnClickPendingIntent(R.id.pouse,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1008,
                        new Intent("io.dcloud.uniplugin.wd.pause"), PendingIntent.FLAG_UPDATE_CURRENT));
        bigRemoteViews.setOnClickPendingIntent(R.id.lastsong,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1009,
                        new Intent("io.dcloud.uniplugin.wd.previoussong"), PendingIntent.FLAG_UPDATE_CURRENT));
        bigRemoteViews.setOnClickPendingIntent(R.id.nextsong,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1010,
                        new Intent("io.dcloud.uniplugin.wd.nextsong"), PendingIntent.FLAG_UPDATE_CURRENT));
        bigRemoteViews.setOnClickPendingIntent(R.id.follow,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1011,
                        new Intent("io.dcloud.uniplugin.wd.follow"), PendingIntent.FLAG_UPDATE_CURRENT));
        bigRemoteViews.setOnClickPendingIntent(R.id.close,
                PendingIntent.getBroadcast(this.getApplicationContext(), 1012,
                        new Intent("io.dcloud.uniplugin.wd.close"), PendingIntent.FLAG_UPDATE_CURRENT));

        return bigRemoteViews;
    }

    /**
     * @return 通知栏执行点击事件
     */
    private PendingIntent notificationClick() {
        Intent i = new Intent();
        i.setClassName("com.xiaohuixiong.music", "io.dcloud.PandoraEntryActivity");
        return PendingIntent.getActivity(this.getApplicationContext(), 1001, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}