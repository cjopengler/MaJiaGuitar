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
    
    
    DownloadInfo getCurDownloadInfo();
    
    /**
     * 更新文件的总大小和软件版本号
     * @param totalSize 总大小
     * @param softwareVersion 软件版本号
     * @return 当前的id 作为这个软件版本的唯一标识
     */
    long update(long totalSize, String softwareVersion);
    
    /**
     * 更新下载的进度大小
     * @param downloadSize 已经下载的大小
     * @return 当前更新的id 作为软件版本的唯一标识
     */
    long update(long id, long downloadSize);
    

    long finish(long id);
    
    void addListener(IDownloadListener listener);
    void removeListener(IDownloadListener listener);

    public interface IDownloadListener {
        void onDownload(long id, DownloadInfo downloadInfo);
    }
    
}
