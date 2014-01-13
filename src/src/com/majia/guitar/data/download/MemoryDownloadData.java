/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data.download;

import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author panxu
 * @since 2013-12-30
 */
public class MemoryDownloadData extends AbstractDownloadData {
    
    private static class InstanceHodler {
        private static MemoryDownloadData sInstance = new MemoryDownloadData();
    }
    
    
    private Lock mLock;
    private volatile DownloadInfo mDownloadInfo;
    
    public static MemoryDownloadData getInstance() {
        return InstanceHodler.sInstance;
    }

    
    private MemoryDownloadData() {
        super();
        
        mLock = new ReentrantLock();
        mDownloadInfo = new DownloadInfo(DownloadInfo.IDEL_STATUS, 
                0, 
                0, 
                "");
        
    }
    
    private void notifyListeners() {
        
        ListIterator<IDownloadListener> iterator = mDownloadListeners.listIterator();

        while (iterator.hasNext()) {
            IDownloadData.IDownloadListener downloadListener = (IDownloadData.IDownloadListener) iterator.next();
            downloadListener.onDownload(0, mDownloadInfo);
        }

    }
    
    
    @Override
    public DownloadInfo getCurDownloadInfo() {
        DownloadInfo downloadInfo = mDownloadInfo;
        return downloadInfo;
    }

    @Override
    public long update(long totalSize, String softwareVersion) {
        DownloadInfo downloadInfo = new DownloadInfo(
                                            DownloadInfo.DOWNLOAD_START_STATUS,
                                            0, 
                                            totalSize, 
                                            softwareVersion);
        mDownloadInfo = new DownloadInfo(downloadInfo);
        
        notifyListeners();
        
        return 0;
    }

    @Override
    public long update(long id, long downloadSize) {
        mLock.lock();
        mDownloadInfo = new DownloadInfo(DownloadInfo.DOWNLOADING_STATUS, 
                                         downloadSize, 
                                         mDownloadInfo.getTotalSize(), 
                                         mDownloadInfo.getVersion());
        mLock.unlock();
        
        notifyListeners();
        
        return 0;
    }


    @Override
    public long finish(long id) {
        
        mDownloadInfo = new DownloadInfo(DownloadInfo.DOWNLOAD_FINISH_STATUS, 
                                         mDownloadInfo.getDownloadSize(), 
                                         mDownloadInfo.getTotalSize(), 
                                         mDownloadInfo.getVersion());
        
        notifyListeners();
        return 0;
    }



}
