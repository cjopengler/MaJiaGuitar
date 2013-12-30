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
    public static final int DOWNLOADING_STATUS = 1;
    public static final int DOWNLOAD_FINISH_STATUS = 2;
    
    private final int mStatus;
    

    private final long mDownloadSize;
    private final long mTotalSize;
    private final String mVersion;
    
    public DownloadInfo(int status, long downloadSize, long totalSize, final String version) {
        mStatus = status;
        mDownloadSize = downloadSize;
        mTotalSize = totalSize;
        mVersion = version;
    }
    
    public DownloadInfo(DownloadInfo downloadInfo) {
        this(downloadInfo.mStatus, 
                downloadInfo.mDownloadSize, 
                downloadInfo.mTotalSize, 
                downloadInfo.mVersion);
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
    
}
