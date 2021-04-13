package com.huaqin.macallan18a.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import java.text.DecimalFormat;


public class Utils {

    private static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothAdapter defaultAdapter;
    private static WifiManager wifiManager;
    private static LocationManager locationManager;

    // 蓝牙开启
    @SuppressLint("WrongConstant")
    public static void openBluetooth(final Activity activity){
        defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultAdapter.enable();
        if (defaultAdapter == null || !defaultAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
        try{
            Thread.sleep(2000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        if(defaultAdapter.getState() == 12){
            Log.i("openBluetooth", "bluetooth is already on");
        }
    }

    // 蓝牙关闭
    @SuppressLint("WrongConstant")
    public static void closeBluetooth(final Activity activity){
        defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        defaultAdapter.disable();
        Log.d("FirstActivity:Bluetooth", String.valueOf(defaultAdapter.getState()));
        if(defaultAdapter.getState() == 10){
            Log.i("closeBluetooth", "bluetooth is alread off");
//            Toast.makeText(activity, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
        }
    }

    // wifi开启
    public static void openWifi(final Activity activity){
        wifiManager = (WifiManager)activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED){
            wifiManager.setWifiEnabled(true);
            Log.i("WifiStatus", String.valueOf(wifiManager.getWifiState()));
        }
    }

    // wifi关闭
    public static void closeWifi(final Activity activity){
        wifiManager = (WifiManager)activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }

    // GPS开启
    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void openGPS(final Activity activity){
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean isEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isEnable){
            Intent GPSIntent = new Intent();
            GPSIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
            GPSIntent.setData(Uri.parse("custom:3"));
            Log.i("GPSStatus",String.valueOf(locationManager.isLocationEnabled()));
            try {
                PendingIntent.getBroadcast(activity, 0, GPSIntent, 0).send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }
    // TODO:关闭GPS
    public static void closeGPS(Activity activity) {

    }

    // 单次振动
    public static void openVibrate1(final Context activity, long milliseconds){
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    // 循环振动
    public static void openVibrate(final Activity activity, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        //// 下边是可以使震动有规律的震动   -1：表示不重复 0：循环的震动
        // long[] pattern = { 200, 2000, 2000, 200, 200, 200 };
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

    //取消振动
    public static void cancelVibrate(final Activity activity){
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.cancel();
    }

    //获取具备闪光灯功能的后置摄像头ID
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String getCameraId(final Activity activity) throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        String[] ids = cameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    // 根据cameraId打开闪光灯
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void openFlashLight(final Activity activity){
        try{
            CameraManager cameraManager = (CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = getCameraId(activity);
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId,true);
            }
        }catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void closeFlashLight(final Activity activity){
        try{
            CameraManager cameraManager = (CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = getCameraId(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                assert cameraId != null;
                cameraManager.setTorchMode(cameraId, false);
            }
        }catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public static Double calculateDuration(long startTime) throws InterruptedException {

        DecimalFormat decimalFormat = new DecimalFormat();

        long currentTime = System.currentTimeMillis();
        Log.i("Utils", "currentStamp:" + currentTime);

        Double durationTime = Double.valueOf(decimalFormat.format(((currentTime - startTime) / (1000 * 60.00 * 60.00))));
        Log.i("Utils", "The number of hours between：" + durationTime);
        return durationTime;
    }

    public static void setMediaVolumeMax(Context context){
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        Log.i("setMediaVolumeMax:", "set the maximum volume");
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100,
                AudioManager.FLAG_PLAY_SOUND);
    }

    // TODO:音量恢复到系统默认
    public static void setMediaVolumeReset(Context context){
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

    }

    // 设置当前屏幕亮度最大
    public static void setBrightnessMax(Activity activity){
        setWindowBrightness(activity, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
    }

    private static void setWindowBrightness(Activity activity, float brightness) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness;
        window.setAttributes(lp);
    }

}

