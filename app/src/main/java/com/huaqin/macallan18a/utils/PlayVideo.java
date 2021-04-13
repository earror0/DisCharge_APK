package com.huaqin.macallan18a.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.widget.MediaController;
import android.widget.VideoView;

import com.huaqin.macallan18a.R;
import com.huaqin.macallan18a.VideoActivity;


public class PlayVideo {

    @SuppressLint("StaticFieldLeak")
    private static MediaController mediaController;

    public static void initVideoPlayer(VideoView videoView, Activity activity){
//        在缓存目录中读取视频资源
//        File directory = contextWrapper.getExternalFilesDir(null);
//        File file = new File(directory, "video_test.mp4");
//        videoView.setVideoPath(file.getPath());
        // 读取raw目录下的视频资源
        String videoPath = "android.resource://" + activity.getPackageName() + "/" + R.raw.video_test;
        videoView.setVideoPath(videoPath);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
    }

    // 单次播放
    public static void playVideo(VideoView videoView){
        if (!videoView.isPlaying()){
            videoView.start();
        }
    }

    // 循环播放视频
    public static void LoopPlay(VideoView videoView){
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
    }

    public static void stopPlay(VideoView videoView){
        videoView.stopPlayback();
    }

    public static void pauseVideo(VideoView videoView){
        videoView.pause();
    }

    public static void setMediaController(MediaController mediaController) {
        PlayVideo.mediaController = mediaController;
    }

    public static void stopVideoActivity(){
        
        VideoActivity.activity_video.finish();
    }
}
