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
    private DownloadInfo mDownloadInfo;
    
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
        mLock.lock();
        DownloadInfo downloadInfo = mDownloadInfo;
        mLock.unlock();
        return downloadInfo;
    }

    @Override
    public long update(long totalSize, String softwareVersion) {
        DownloadInfo downloadInfo = new DownloadInfo(
                                            DownloadInfo.DOWNLOADING_STATUS,
                                            0, 
                                            totalSize, 
                                            softwareVersion);
        mLock.lock();
        mDownloadInfo = new DownloadInfo(downloadInfo);
        mLock.unlock();
        
        notifyListeners();
        
        return 0;
    }

    @Override
    public long update(Long id, long downloadSize) {
        mLock.lock();
        mDownloadInfo = new DownloadInfo(DownloadInfo.DOWNLOADING_STATUS, 
                                         downloadSize, 
                                         mDownloadInfo.getTotalSize(), 
                                         mDownloadInfo.getVersion());
        mLock.unlock();
        
        notifyListeners();
        
        return 0;
    }

}
