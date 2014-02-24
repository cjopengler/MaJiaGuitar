/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar;

import com.majia.guitar.service.UpdateService;

import android.app.Application;
import android.content.Intent;

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
        //启动更新
        this.startService(new Intent(UpdateService.UPDATE_GUITAR_MUSIC_ACTION));
    }
    
    public static final Application getInstance() {
        return INSTANCE;
    }
}
