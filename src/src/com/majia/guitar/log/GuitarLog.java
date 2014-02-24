/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.log;


/**
 * 打印log的工具
 * @author panxu
 * @since 2014-1-12
 */
public class GuitarLog {
    private static final boolean mIsEnable = true;
    
    public static void d(String tag, String message) {
        if (mIsEnable) {
            android.util.Log.d(tag, message 
                    + ", threadId name:" + Thread.currentThread().getId() 
                    + "id: " + Thread.currentThread().getId());
        }
    }
    
    private GuitarLog() {
        
    }
}
