package com.huaqin.macallan18a.utils;

import android.app.Activity;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

public class BatteryUtils {
    // 存储获取的电池信息
    public static Map<String, String> map = new HashMap<String, String>();

    // 获取电池的百分比电量
    public static void getBatteryLevel(Activity activity){
        // 获取电池服务
        BatteryManager batteryManager = (BatteryManager)activity.getSystemService(Context.BATTERY_SERVICE);

        try{
            // 获取电量百分比
            String level = String.valueOf(batteryManager.getIntProperty(BatteryManager.
                    BATTERY_PROPERTY_CAPACITY)) + "%";
            map.put("level", level);
            // 获取充电状态
            String status = String.valueOf(batteryManager.getIntProperty(BatteryManager.
                    BATTERY_PROPERTY_STATUS));
            map.put("status", status);
            // 平均电流
            String averageCurrents = String.valueOf(batteryManager.getIntProperty(BatteryManager.
                    BATTERY_PROPERTY_CURRENT_AVERAGE));
            map.put("averageCurrents", averageCurrents);
            // 瞬时电流
            String nowCurrents = String.valueOf(batteryManager.getIntProperty(BatteryManager.
                    BATTERY_PROPERTY_CURRENT_AVERAGE));
            map.put("nowCurrents", nowCurrents);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
