/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.data.json.ApkVersionJson;

/**
 * 
 * @author panxu
 * @since 2014-5-25
 */
public class ApkVersion {

    public final String versionName;
    public final int versionCode;
    public final String changeLog;
    public final String internalPath;
    public final String externalUrl;
    
    public final int apkSize;
    
    public ApkVersion(ApkVersionJson apkVersionJson) {
        versionName = apkVersionJson.description == null ? MaJiaGuitarApplication.getInstance().getVersionName() : apkVersionJson.description.version_name;
        versionCode = apkVersionJson.description == null ? MaJiaGuitarApplication.getInstance().getVersionCode() : Integer.valueOf(apkVersionJson.description.version_code);
    
	        changeLog = apkVersionJson.description == null ? "" : apkVersionJson.description.change_log;
	        internalPath = apkVersionJson.description == null ? "" :apkVersionJson.description.internal_path;
	        externalUrl = apkVersionJson.description == null ? "" :apkVersionJson.description.external_url;
	        apkSize = apkVersionJson.description == null ? 0 :Integer.valueOf(apkVersionJson.description.apk_size);
        
    }
    
    public ApkVersion(String versionName, 
                      int versionCode,
                      String changeLog,
                      String internalPath,
                      String externalUrl,
                      int apkSize) {
        
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.changeLog = changeLog;
        this.internalPath = internalPath;
        this.externalUrl = externalUrl;
        this.apkSize = apkSize;
        
    }

}
