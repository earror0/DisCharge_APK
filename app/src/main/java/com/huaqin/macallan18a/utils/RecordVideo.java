package com.huaqin.macallan18a.utils;

import android.content.ContextWrapper;
import android.media.MediaRecorder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;

public class RecordVideo {

    public static void initRecord(MediaRecorder mediaRecorder, ContextWrapper contextWrapper,
                                  SurfaceView surfaceView){
        // 创建保存录制视频的视频文件
        File directory = contextWrapper.getExternalFilesDir(null);
        assert directory != null;
        File videoFile = new File(directory + "/test_video.3gp");
        mediaRecorder.reset();
        // 设置从麦克风采集声音(或来自录像机的声音AudioSource.CAMCORDER)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置从摄像头采集图像
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // 设置视频文件的输出格式
        // 必须在设置声音编码格式、图像编码格式之前设置
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        // 设置声音编码的格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 设置图像编码的格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoSize(1280, 720);
        // 每秒 4帧
        mediaRecorder.setVideoFrameRate(20);
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
        // 指定使用SurfaceView来预览视频
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

    }

    public static Boolean startRecord(MediaRecorder mediaRecorder){
        try{
            mediaRecorder.prepare();
            //开始录制
            mediaRecorder.start();
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    public static void stopRecord(MediaRecorder mediaRecorder, Boolean isRecording){
        // 如果正在进行录制
        if (isRecording) {
            // 停止录制
            mediaRecorder.stop();
            // 释放资源
            mediaRecorder.release();
        }
    }
}
