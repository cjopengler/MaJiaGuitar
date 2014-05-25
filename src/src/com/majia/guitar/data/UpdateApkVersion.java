/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.util.concurrent.CopyOnWriteArrayList;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.majia.guitar.MaJiaGuitarApplication;

/**
 * 
 * @author panxu
 * @since 2014-5-25
 */
public class UpdateApkVersion implements IUpdateApkVersion {

    private final String UPDATE_APK_SHARE_PREFRENCE_NAME = "apk_version";
    private final String VERSION_CODE = "apk_version_code";
    private final String VERSION_NAME = "apk_version_name";
    private final String CHANGEL_LOG = "apk_version_change_log";
    private final String INTERNAL_PATH = "internal_path";
    private final String EXTERNAL_ULR = "external_url";
    
    private final CopyOnWriteArrayList<UpdateListener> mUpdateListeners = new CopyOnWriteArrayList<IUpdateApkVersion.UpdateListener>();
    
    private volatile ApkVersion mApkVersion;
    
    private static final class Holder {
        public static final UpdateApkVersion INSTANCE = new UpdateApkVersion();
    }
    
    public static UpdateApkVersion getInstance() {
        return Holder.INSTANCE;
    }
    private UpdateApkVersion() {
        
    }

    @Override
    public ApkVersion getApkVersion() {
        
        if (mApkVersion == null) {
            SharedPreferences dataSharedPreferences = MaJiaGuitarApplication.getInstance().getSharedPreferences(
                    UPDATE_APK_SHARE_PREFRENCE_NAME, 0);
            int versionCode = dataSharedPreferences.getInt(VERSION_CODE, 0);
            String versionName = "";
            String changeLog = "";
            String internalPath = "";
            String externalUrl = "";
            
            if (versionCode > 0) {
                versionName = dataSharedPreferences.getString(VERSION_NAME, "");
                changeLog = dataSharedPreferences.getString(CHANGEL_LOG, "");
                internalPath = dataSharedPreferences.getString(INTERNAL_PATH, "");
                externalUrl = dataSharedPreferences.getString(EXTERNAL_ULR, "");
            } 
            
            mApkVersion = new ApkVersion(versionName, versionCode, changeLog, internalPath, externalUrl);
        }
        
        return mApkVersion;
    }

    @Override
    public void setApkVersion(ApkVersion apkVersion) {
        
        int versionCode = MaJiaGuitarApplication.getInstance().getVersionCode();

        
        if (versionCode < apkVersion.versionCode) { //表示可以更新
        
            SharedPreferences dataSharedPreferences = MaJiaGuitarApplication.getInstance().getSharedPreferences(
                    UPDATE_APK_SHARE_PREFRENCE_NAME, 0);
         
            mApkVersion = apkVersion;
    
            SharedPreferences.Editor dataEditor = dataSharedPreferences.edit();
    
            dataEditor.putString(VERSION_NAME, apkVersion.versionName);
            dataEditor.putInt(VERSION_CODE, apkVersion.versionCode);
            dataEditor.putString(CHANGEL_LOG, apkVersion.changeLog);
            dataEditor.putString(INTERNAL_PATH, apkVersion.internalPath);
            dataEditor.putString(EXTERNAL_ULR, apkVersion.externalUrl);
            
            dataEditor.commit();
            
            notifyListeners(apkVersion);
        }
    }

    @Override
    public void registListener(UpdateListener updateListener) {
        mUpdateListeners.addIfAbsent(updateListener);
    }

    @Override
    public void unregistListener(UpdateListener updateListener) {
        mUpdateListeners.remove(updateListener);
    }
    
    private void notifyListeners(ApkVersion apkVersion) {
        for (UpdateListener updateListener : mUpdateListeners) {
            updateListener.onUpdate(apkVersion);
        }
    }

}
