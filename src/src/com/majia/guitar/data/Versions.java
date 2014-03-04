/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public final class Versions {
    private final Version mMusicVersion;
    private final Version mApkVersion;
    
    public Versions(VersionJson versionJson) {
        mMusicVersion = new Version(versionJson.musics_version_name, 
                                    Long.parseLong(versionJson.musics_version_code));
        
        mApkVersion = new Version(versionJson.apk_version_name, 
                                  Long.parseLong(versionJson.apk_version_code));
        
    }
    
    public Versions(Version musicVersion, Version apkVersion) {
        mMusicVersion = musicVersion;
        mApkVersion = apkVersion;
    }
    
    public Version getMusicVersion() {
        return mMusicVersion;
    }
    
    public Version getApkVersion() {
        return mApkVersion;
    }
}
