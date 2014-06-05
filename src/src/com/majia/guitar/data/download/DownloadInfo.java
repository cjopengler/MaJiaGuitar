/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data.download;

/**
 * 
 * @author panxu
 * @since 2013-12-30
 */
public class DownloadInfo {
    public static final int IDEL_STATUS = 0;
    public static final int DOWNLOAD_START_STATUS = 1;
    public static final int DOWNLOADING_STATUS = 2;
    public static final int DOWNLOAD_FINISH_STATUS = 3;
    
    private final int mStatus;
    

    private final long mDownloadSize;
    private final long mTotalSize;
    private final String mVersion;
    private final String mDownloadPath;
    
    public DownloadInfo(int status, long downloadSize, long totalSize, final String version, final String downloadPath) {
        mStatus = status;
        mDownloadSize = downloadSize;
        mTotalSize = totalSize;
        mVersion = version;
        mDownloadPath = downloadPath;
    }
    
    public DownloadInfo(DownloadInfo downloadInfo) {
        this(downloadInfo.mStatus, 
                downloadInfo.mDownloadSize, 
                downloadInfo.mTotalSize, 
                downloadInfo.mVersion,
                downloadInfo.mDownloadPath);
    }
    public int getStatus() {
        return mStatus;
    }

    public long getDownloadSize() {
        return mDownloadSize;
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    public String getVersion() {
        return mVersion;
    }
    
    public String getDownloadPath() {
        return mDownloadPath;
    }
    
    @Override
    public String toString() {
        return "{status:" + mStatus + 
                ", downloadSize:" + mDownloadSize + 
                ", totalSize:" + mTotalSize + 
                ", version:" + mVersion + "}";
    }
}
