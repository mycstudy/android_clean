package com.xminnov.bu01.demo;

import android.app.Activity;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by liym on 2018/8/3.
 */

class ActivityCollector {
    private static List<BleBaseActivity>  sActivities = new ArrayList<>();

    static void addActivity(BleBaseActivity activity){
        sActivities.add(activity);
    }

    static void removeActivity(BleBaseActivity activity){
        sActivities.remove(activity);
    }

    static void invalidateOptionsMenu(){
        for (BleBaseActivity activity: sActivities){
            activity.invalidateOptionsMenu();
        }
    }

    static void onReaderConnect(){
        for (BleBaseActivity activity : sActivities){
            activity.onReaderConnect();
        }
    }

    static void onReaderDisconnect(){
        for (BleBaseActivity activity : sActivities){
            activity.onReaderDisconnect();
        }
    }
}
