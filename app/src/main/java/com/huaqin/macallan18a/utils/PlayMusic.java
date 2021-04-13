package com.huaqin.macallan18a.utils;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class PlayMusic {

    public static final String TAG = "FirstActivity";

    public static void initMediaPlayer(MediaPlayer mediaPlayer, Activity activity) {

        mediaPlayer.reset();
        try {
            AssetFileDescriptor fileDescriptor = activity.getAssets().openFd("music_test.wav");
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            // 让MediaPlayer进入准备状态
            mediaPlayer.prepare();
            Log.d(TAG, "initMediaPlayer: out");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void play_music(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onClick: play");
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();  // 开始播放
            mediaPlayer.setVolume(1, 1);
        }
    }

    public static void pause_music(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onClick: pause");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();  // 暂停播放
        }
    }

    public static void stop_music(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onClick: stop");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public static void loopPlayMusic(MediaPlayer mediaPlayer) {
        mediaPlayer.setLooping(true);
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
    }
}
