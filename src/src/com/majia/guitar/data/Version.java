/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class Version {
    private final String mVersionName;
    private final long mVersionCode;
    
    public Version(String versionName, long versionCode) {
        mVersionName = versionName;
        mVersionCode = versionCode;
    }
    
    public Version(String versionName, String versionCode) {
        this(versionName, Long.parseLong(versionCode));
    }
    
    public final String getVersionName() {
        return mVersionName;
    }
    
    public long versionCode() {
        return mVersionCode;
    }
}
