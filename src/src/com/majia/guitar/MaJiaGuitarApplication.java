/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar;


import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class MaJiaGuitarApplication extends Application {
    private static volatile MaJiaGuitarApplication INSTANCE = null;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        INSTANCE = this;
       
    }
    
    public static final MaJiaGuitarApplication getInstance() {
        return INSTANCE;
    }
    
    public int getVersionCode() {
        int versionCode = 0;
        try {
            PackageInfo packageInfo = getPackageManager().
                          getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        return versionCode;
    }
    
    public String getVersionName() {
        String versionName = "";
        try {
            PackageInfo packageInfo = getPackageManager().
                          getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        return versionName;
    }
}
