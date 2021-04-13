package com.huaqin.macallan18a;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import com.huaqin.macallan18a.utils.Utils;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RecorderActivity extends Activity implements OnClickListener {
    // 程序中的两个按钮
    private Button record, stop;
    // 系统的视频文件
    private File videoFile;
    private MediaRecorder mRecorder;
    // 显示视频预览的SurfaceView
    private SurfaceView sView;
    // 记录是否正在进行录制
    private boolean isRecording = false;
    private String TAG = "RecorderActivity";

    @SuppressLint("StaticFieldLeak")
    public static Activity recorder_activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        recorder_activity = this;

        super.onCreate(savedInstanceState);
        // 去掉标题栏 ,必须放在setContentView之前
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recorder);
        // 设置横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        // 获取程序界面中的两个按钮
        record = findViewById(R.id.record);
        stop = findViewById(R.id.stop);
        // 让stop按钮不可用。
        stop.setEnabled(false);
        // 为两个按钮的单击事件绑定监听器
        record.setOnClickListener(this);
        stop.setOnClickListener(this);
        // 获取程序界面中的SurfaceView
        sView = this.findViewById(R.id.sView);
        // 设置分辨率
        sView.getHolder().setFixedSize(1080, 2230);
        // 设置该组件让屏幕不会自动关闭
        sView.getHolder().setKeepScreenOn(true);
        // Activity启动后自动触发视频录制按钮
        sView.post(new Runnable() {
            @Override
            public void run() {
                record.performClick();
                isRecording = true;
            }
        });
        Log.i(TAG, "screen setting for maximum brightness");
        Utils.setBrightnessMax(RecorderActivity.this);

    }

    @Override
    public void onClick(View source) {
        switch (source.getId()) {
            // 单击录制按钮
            case R.id.record:
                try {
                    recorder();
                    Log.i(TAG, "start of video recording");
                    // 让record按钮不可用。
                    record.setEnabled(false);
                    // 让stop按钮可用。
                    stop.setEnabled(true);
                    isRecording = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            // 单击停止按钮
            case R.id.stop:
                Log.i(TAG, "stop video recording");
                stopRecord();
                // 让record按钮可用。
                record.setEnabled(true);
                // 让stop按钮不可用。
                stop.setEnabled(false);
                break;
        }
    }

    public void recorder(){
        try{
            // 创建保存录制视频的视频文件
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getExternalFilesDir(null);
            assert directory != null;
            Log.i("initRecorder", directory.getAbsolutePath());
            videoFile = new File(directory, "test_video" + ".3gp");
            // 创建MediaPlayer对象
            mRecorder = new MediaRecorder();
            mRecorder.reset();

            // 设置从麦克风采集声音(或来自录像机的声音AudioSource.CAMCORDER)
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            // 设置从摄像头采集图像
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            // 设置视频文件的输出格式
            // 必须在设置声音编码格式、图像编码格式之前设置
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置声音编码的格式
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // 设置图像编码的格式
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            // 竖屏录制
            mRecorder.setOrientationHint(90);
            // 分辨率
//                    mRecorder.setVideoSize(720, 1280);
            mRecorder.setVideoFrameRate(30);
            mRecorder.setOutputFile(videoFile.getAbsolutePath());
            // 指定使用SurfaceView来预览视频
            mRecorder.setPreviewDisplay(sView.getHolder().getSurface());
            mRecorder.prepare();
            // 开始录制
            mRecorder.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void stopRecord(){
        // 如果正在进行录制
        if (isRecording) {
            // 停止录制
            mRecorder.stop();
            // 释放资源
            mRecorder.release();
            mRecorder = null;
        }
        RecorderActivity.this.finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String getCameraId(final Activity activity) throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        String[] ids = cameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return null;
    }

}