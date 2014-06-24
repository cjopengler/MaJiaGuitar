/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

/**
 * 
 * @author panxu
 * @since 2014-6-6
 */
public class Config {
    
    private static final String EXTERANL_STORE_FOLDER_NAME = "YogaGuitar";
    private Config() {
        
    }
    
    public static String getExternalStoragePath() throws FailCreateExteranlPathIOException {
        File yogaGuitarFile = new File(Environment.getExternalStorageDirectory(), EXTERANL_STORE_FOLDER_NAME);
        
        if (!yogaGuitarFile.exists()) {
            if (!yogaGuitarFile.mkdir()) {
                throw new FailCreateExteranlPathIOException("无法创建存储路径");
            }
        }
        return yogaGuitarFile.getPath();
    }
    
    public static class FailCreateExteranlPathIOException extends IOException {
        
        public FailCreateExteranlPathIOException(String detailMessage) {
            super(detailMessage);
        }
    }
    
    
}
