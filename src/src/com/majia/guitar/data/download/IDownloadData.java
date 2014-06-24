/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data.download;

/**
 * 
 * @author panxu
 * @since 2013-12-30
 */
public interface IDownloadData {
    
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final int ERROR_IO = -2;
    public static final int ERROR_NETWORK = -3;
    
    
    DownloadInfo getCurDownloadInfo();
    
   
    
    /**
     * 更新下载的进度大小
     * @param downloadSize 已经下载的大小
     * @return 当前更新的id 作为软件版本的唯一标识
     */
    long update(long id, long downloadSize, long totalSize);
    

    long finish(long id, int error, String downloadPath);
    
    void addListener(IDownloadListener listener);
    void removeListener(IDownloadListener listener);

    public interface IDownloadListener {
        void onDownload(long id, DownloadInfo downloadInfo);
    }
    
}
