package com.huaqin.macallan18a;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import com.huaqin.macallan18a.utils.PlayVideo;
import com.huaqin.macallan18a.utils.Utils;


public class VideoActivity extends AppCompatActivity{

    @SuppressLint("StaticFieldLeak")
    public static Activity activity_video = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        activity_video = this;

        super.onCreate(savedInstanceState);
        // 去掉标题栏 ,必须放在setContentView之前
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        VideoView videoview = findViewById(R.id.video_view);
        // 保持屏幕常亮
        videoview.setKeepScreenOn(true);
        // 设置屏幕亮度最大
        Utils.setBrightnessMax(VideoActivity.this);
        // 设置音量最大
        Utils.setMediaVolumeMax(VideoActivity.this);
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        // 初始化播放器
        PlayVideo.initVideoPlayer(videoview, VideoActivity.this);
        // 默认循环播放
        PlayVideo.LoopPlay(videoview);

        Log.i("Video", String.valueOf(videoview.isPlaying()));
    }

    public void playVideo(View view){
        PlayVideo.playVideo((VideoView) findViewById(R.id.video_view));
    }

    public void pauseVideo(View view){
        PlayVideo.pauseVideo((VideoView) findViewById(R.id.video_view));
    }

    public void stopVideo(View view){
        PlayVideo.stopPlay((VideoView) findViewById(R.id.video_view));
    }
}