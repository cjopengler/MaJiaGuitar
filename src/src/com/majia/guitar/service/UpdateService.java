/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

import com.majia.guitar.data.GuitarData;
import com.majia.guitar.data.RemoteMusicServer;
import com.majia.guitar.data.Versions;

import android.app.IntentService;
import android.content.Intent;

/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class UpdateService extends IntentService {
    
    private static final String NAME = "update service";
    
    public static String UPDATE_GUITAR_MUSIC_ACTION = "com.majia.guitar.service.update_guitar_music_action";

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

}
