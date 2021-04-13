package com.huaqin.macallan18a;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.macallan18a.utils.BatteryUtils;
import com.huaqin.macallan18a.utils.PlayMusic;
import com.huaqin.macallan18a.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnCheckedChangeListener {

    private static final MediaPlayer musicPlayer = new MediaPlayer();
    private Map<String, String> function = new HashMap<>();

    private TextView targetTextView;
    private EditText editText;

    private Button disChargeByLevel;
    private Button disChargeByTime;
    private Button disChargeByScene;

    private int targetLevel = 0;
    private Double targetHours = 0.00;
    // 增加标志位用于适用手动终止放电
    private Boolean isContinue = true;
    // 标志位用于按百分比开始耗电的准入条件，当成功开始放电后会更新为true
    private Boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 控件获取
        CheckBox music = findViewById(R.id.play_music);
        CheckBox video = findViewById(R.id.play_video);
        CheckBox recorder = findViewById(R.id.record_video);
        CheckBox hardware = findViewById(R.id.hardware_control);
        disChargeByLevel = (Button) findViewById(R.id.discharge_by_level);
        disChargeByTime = (Button)findViewById(R.id.discharge_by_time);
        disChargeByScene = (Button)findViewById(R.id.discharge_by_scene);
        // 控件初始化
        music.setOnCheckedChangeListener(this);
        video.setOnCheckedChangeListener(this);
        recorder.setOnCheckedChangeListener(this);
        hardware.setOnCheckedChangeListener(this);

        // 媒体初始化
//        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        PlayMusic.initMediaPlayer(musicPlayer, MainActivity.this);

        // 监听滑动条
        targetTextView = (TextView) findViewById(R.id.target_level);
        SeekBar seekBar = findViewById(R.id.seek_bar);
        // 监听输入框
        editText = findViewById(R.id.target_time);

        // 设置SeekBar 监听setOnSeekBarChangeListener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // 拖动条停止拖动时调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("SeekBar", "拖动停止");
                Log.i("目标放电量：", String.valueOf(targetTextView.getText()));
                targetLevel = Integer.parseInt(targetTextView.getText().toString().replace("%",""));
            }
            // 拖动条开始拖动时调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i("SeekBar", "开始拖动");
            }
            // 拖动条进度改变时调用
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                targetTextView.setText(String.format("%d%%", progress));
            }
        });

        disChargeByLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonToDisable();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("TargetBatteryLevel:", targetTextView.getText().toString());
                        try {
                            int currentLevel = 0;
                            String TAG = "DisChargeByLevel";

                            // 获取目标耗电量
                            Log.i(TAG,"targetLevel:" + targetTextView.getText());
                            // 获取当前电量
                            BatteryUtils.getBatteryLevel(MainActivity.this);
                            currentLevel = Integer.parseInt(Objects.
                                    requireNonNull(BatteryUtils.map.get("level")).
                                    replace("%", ""));
                            targetLevel = Integer.parseInt(targetTextView.getText().toString()
                                    .replace("%",""));

                            if(currentLevel < targetLevel || currentLevel == targetLevel){
                                Log.i(TAG,"The currentLevel is less than or equal " +
                                        "to the targetLevel, do not disCharge!");
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                                MainActivity.this,
                                                "目标电量大于当前电量无需放电",
                                                Toast.LENGTH_SHORT).show();
                                        buttonToEnable();
                                    }
                                });
                            }else {
                                isContinue = true;
                                // 根据选择的场景耗电
                                disCharge();
                            }
                            while(flag && isContinue){
                                BatteryUtils.getBatteryLevel(MainActivity.this);
                                currentLevel = Integer.parseInt(
                                        Objects.requireNonNull(BatteryUtils.map.get("level")).
                                        replace("%", ""));

                                if (targetLevel < currentLevel){
                                    Thread.sleep(20000);
                                    Log.i("discharge thread", "continue discharge" +
                                            "  currentLevel:" + BatteryUtils.map.get("level") +
                                            "  targetLevel:" + targetLevel);
                                }else{
                                    stopDisCharge();
                                    Log.i("discharge thread", "stop discharge");
                                    flag = false;
                                    break;
                                }
                            }
                            if (!flag){
                                // 实现在子线程中更新主线程的UI
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        buttonToEnable();
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        disChargeByTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonToDisable();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("MainActivity", "start discharge by time");
                        try{
                            /* 输入的目标放电时间, 需要判空 */
                            targetHours = Double.parseDouble(editText.getText().toString());
                            long startTime = System.currentTimeMillis();
                            if(targetHours.equals(0.00)){
                                isContinue = false;
                                // 实现子线程更新UI线程中的UI
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,
                                                "耗电时长不能为0，请重新输入！！！",
                                                Toast.LENGTH_SHORT).show();
                                        buttonToEnable();
                                    }
                                });
                            }else{
                                isContinue = true;
                                // 根据选择的场景耗电
                                disCharge();
                            }
                            // 已经放电的时长
                            double durationTime = 0.00;
                            while (isContinue){
                                durationTime = Utils.calculateDuration(startTime);
                                if (durationTime < targetHours){
                                    Thread.sleep(20 * 1000);
                                    Log.i("MainActivity", "Discharge time is " +
                                            "insufficient, continue to discharge");
                                } else{
                                    stopDisCharge();
                                    Log.i("MainActivity", "stop discharge");
                                    // 实现子线程更新UI线程中的UI
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            buttonToEnable();
                                        }
                                    });
                                    break;
                                }
                            }
                        }catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        // 监听输入框，避免接收到非法字符（空格/回车）】
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                // 处理输入的空格
                if (charSequence.toString().contains(" ")){
                    String[] str = charSequence.toString().split(" ");
                    String str1 = "";
                    for (int i = 0; i < str.length; i++){
                        str1 += str[i];
                    }
                    editText.setText(str1);
                    editText.setSelection(start);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    // 复选框中事件选中与否
    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
        switch(checkBox.getId()){
            case R.id.play_music:
                if (checked) {// 选音乐播放
                    function.put("music", "音乐播放");
                } else {
                    function.remove("music");
                }
                break;
            case R.id.play_video:
                if (checked) {// 选视频播放
                    function.put("video", "视频播放");
                } else {
                    function.remove("video");
                }
                break;
            case R.id.record_video:
                if (checked) {// 选视频录像
                    function.put("recorder", "视频录像");
                } else {
                    function.remove("recorder");
                }
                break;
            case R.id.hardware_control:
                if (checked) {// 选硬件开关
                    function.put("hardware", "硬件开关");
                } else {
                    function.remove("hardware");
                }
                break;
        }
    }

    /*
    根据选择的功能进行放电操作
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startDisCharge(View view){
        buttonToDisable();
        disCharge();

    }

    // 获取电量百分比
    public void getBatteryLevel_main(View view){
        BatteryUtils.getBatteryLevel(MainActivity.this);
        String level = BatteryUtils.map.get("level");
        // 更新xml文件中的电量值
        TextView textView = findViewById(R.id.battery_level);
        textView.setText(level);
        assert level != null;
        Log.i("Battery", level);
        Toast.makeText(MainActivity.this, "BatteryLevel:" + level, Toast.LENGTH_SHORT).show();
    }

    // 耗电
    public void disCharge() {

        String TAG = "ExecutiveFunction";
        final StringBuilder sb = new StringBuilder();
        String ret = "选中的功能有:";
        // 进行已选功能的检查，如果没有选择任何功能，提示选择
        if (function.size() == 0) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String no = "请至少选择一项功能！";
                    Toast.makeText(MainActivity.this, no, Toast.LENGTH_SHORT).show();
                    buttonToEnable();
                }
            });
            flag = false;

        } else {
            Log.i(TAG, "start disCharge!");
            sb.append(ret);
            for (String key : function.keySet()) {
                sb.append(function.get(key)).append("\t");
            }
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, sb, Toast.LENGTH_SHORT).show();
                }
            });
            flag = true;
        }

        // 循环读取已选择的功能
        for (String i : function.keySet()){
            Log.d("function:", i);
            switch(i){
                case "music":
                    Log.i(TAG, "play music");
                    // 设置最大音量
                    Utils.setMediaVolumeMax(MainActivity.this);
                    PlayMusic.loopPlayMusic(musicPlayer);
                    break;
                case "video":
                    Log.i(TAG, "play video");
                    Intent videoIntent = new Intent(MainActivity.this, VideoActivity.class);
                    startActivity(videoIntent);
                    break;
                case "recorder":
                    Log.i(TAG,"start recorder");
                    Intent recorderIntent = new Intent(MainActivity.this, RecorderActivity.class);
                    startActivity(recorderIntent);
                    break;
                case "hardware":
                    Log.i(TAG,"openFlashLight");
                    Utils.openFlashLight(this);
                    Log.i(TAG,"openBluetooth");
                    Utils.openBluetooth(this);
                    Log.i(TAG,"openVibrate");
                    long[] pattern = { 200, 2000, 2000, 200, 200, 200 };
                    Utils.openVibrate(MainActivity.this, pattern, true);
                    Log.i(TAG,"openGPS");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Utils.openGPS(this);
                    }
            }
        }
    }

    public void stopDisCharge(){
        String TAG = "ExecutiveFunction";
        // 循环读取已选择的功能
        for (String i : function.keySet()){
            Log.d("function:", i);
            switch(i){
                case "music":
                    Log.i(TAG, "stop music");
                    PlayMusic.pause_music(musicPlayer);
                    break;
                case "video":
                    Log.i(TAG, "stop video");
                    VideoActivity.activity_video.finish();
                    break;
                case "recorder":
                    Log.i(TAG,"stop recorder");
                    RecorderActivity.recorder_activity.finish();
                    break;
                case "hardware":
                    Log.i(TAG,"closeFlashLight");
                    Utils.closeFlashLight(this);
                    Log.i(TAG,"closeBluetooth");
                    Utils.closeBluetooth(this);
                    Log.i(TAG,"closeVibrate");
                    Utils.cancelVibrate(MainActivity.this);
                    Log.i(TAG,"closeGPS");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Utils.closeGPS(this);
                    }
            }
        }

    }

    // 在主线程停止放电
    public void stopMainThreadDisCharge(View view){
        buttonToEnable();
        stopDisCharge();
        // 设置停止循环标志位，中断子线程中的循环
        isContinue = false;
    }

    public void buttonToEnable(){
        // 启用耗电按钮
        disChargeByScene.setEnabled(true);
        disChargeByLevel.setEnabled(true);
        disChargeByTime.setEnabled(true);
    }

    public void buttonToDisable(){
        // 将所有耗电按钮置灰
        disChargeByScene.setEnabled(false);
        disChargeByLevel.setEnabled(false);
        disChargeByTime.setEnabled(false);
    }

}