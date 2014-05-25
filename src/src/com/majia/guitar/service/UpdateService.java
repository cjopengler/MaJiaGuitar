/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.JsonSyntaxException;
import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.data.ApkVersion;
import com.majia.guitar.data.GuitarData;
import com.majia.guitar.data.RemoteMusicServer;
import com.majia.guitar.data.UpdateApkVersion;
import com.majia.guitar.data.Versions;
import com.majia.guitar.data.json.ApkVersionJson;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class UpdateService extends IntentService {
    
    private static final String NAME = "update service";
    
    public static String UPDATE_GUITAR_MUSIC_ACTION = "com.majia.guitar.service.UpdateService.update_guitar_music_action";

    public static String QUERY_APK_UPDATE_ACTION = "com.majia.guitar.service.UpdateService.query_apk_update_action";
    /**
     * @param name
     */
    public UpdateService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(UPDATE_GUITAR_MUSIC_ACTION)) {
            handleUpdateGuitarMusic();
        } else if (action.equals(QUERY_APK_UPDATE_ACTION)) {
            
            handleApkUpdate();
        }
    }
    
    private void handleUpdateGuitarMusic() {
        Versions versions = RemoteMusicServer.getVersions();
        
        if (versions == null) {
            //
        } else {
            //写入数据
            GuitarData.getInstance().updateVersion(versions, true);
        }
    }
    
    private void handleApkUpdate() {
        
        ApkVersionJson apkVersionJson = null;
        
        try {
            
            
            int versionCode = MaJiaGuitarApplication.getInstance().getVersionCode();
            
            apkVersionJson = RemoteMusicServer.getInstance().queryApkVersion(versionCode);
            
            
            if (apkVersionJson.update == ApkVersionJson.UPDATE_TRUE) {
                ApkVersion apkVersion = new ApkVersion(apkVersionJson);
                
                UpdateApkVersion.getInstance().setApkVersion(apkVersion);
            }
            
            
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    

}
