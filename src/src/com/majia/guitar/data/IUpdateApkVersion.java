/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

/**
 * 
 * @author panxu
 * @since 2014-5-25
 */
public interface IUpdateApkVersion {
    ApkVersion getApkVersion();
    void setApkVersion(ApkVersion apkVersion);
    
    void registListener(UpdateListener updateListener);
    void unregistListener(UpdateListener updateListener);
    
    public interface UpdateListener {
        void onUpdate(ApkVersion apkVersion);
    }
}
