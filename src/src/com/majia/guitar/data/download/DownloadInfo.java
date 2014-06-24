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

    public static final int DOWNLOAD_IS_ONGOING = 2;
    public static final int DOWNLOAD_FINISH_SUCCESS = 3;
    public static final int DOWNLOAD_FINISH_IO_ERROR = 4;
    public static final int DOWNLOAD_FINISH_NET_ERROR = 5;
    public static final int DOWNLOAD_FINISH_ERROR = 6;
    
    private final int mStatus;
    

    private final long mDownloadSize;
    private final long mTotalSize;
    private final long mVersion;
    private final String mDownloadPath;
    
    public DownloadInfo(int status, long downloadSize, long totalSize, long version, final String downloadPath) {
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

    public long getVersion() {
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
