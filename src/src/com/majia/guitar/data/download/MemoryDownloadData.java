/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data.download;

import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.majia.guitar.util.Assert;

import android.R.integer;

/**
 * 这里不支持断点续传 如果要支持断点续传需要使用数据库来维护下载数据
 * 所以称之为MemoryDoanloadData
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
        mDownloadInfo = new DownloadInfo(DownloadInfo.DOWNLOAD_FINISH_SUCCESS, 
                0, 
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



    /**
     * 这里的id就是version code
     */
    @Override
    public long update(long versionCode, long downloadSize, long totalSize) {
        mLock.lock();
        mDownloadInfo = new DownloadInfo(DownloadInfo.DOWNLOAD_IS_ONGOING, 
                                         downloadSize, 
                                         mDownloadInfo.getTotalSize(), 
                                         mDownloadInfo.getVersion(),
                                         "");
        mLock.unlock();
        
        notifyListeners();
        
        return 0;
    }


    @Override
    public long finish(long versionCode, int error, String downloadPath) {
        
        int downloadStatus = DownloadInfo.DOWNLOAD_FINISH_SUCCESS;
        
        switch (error) {
        case IDownloadData.ERROR:
            downloadStatus = DownloadInfo.DOWNLOAD_FINISH_ERROR;
            break;
            
        case IDownloadData.ERROR_IO:
            downloadStatus = DownloadInfo.DOWNLOAD_FINISH_IO_ERROR;
            break;
        case IDownloadData.ERROR_NETWORK:
            downloadStatus = DownloadInfo.DOWNLOAD_FINISH_NET_ERROR;
            break;
            
        case IDownloadData.ERROR_NO_ENOUGH_SPCACE:
            downloadStatus = DownloadInfo.DOWNLOAD_FINISH_NO_ENOUGH_ERROR;
            break;
            
        case IDownloadData.ERROR_NO_SDCARD:
            downloadStatus = DownloadInfo.DOWNLOAD_FINISH_NO_SDCARD_ERROR;
            break;
            
        case IDownloadData.SUCCESS:
            downloadStatus = DownloadInfo.DOWNLOAD_FINISH_SUCCESS;
            break;

        default:
            Assert.assertOnly("error code is not exist " + error);
            break;
        }
        mDownloadInfo = new DownloadInfo(downloadStatus, 
                                         mDownloadInfo.getDownloadSize(), 
                                         mDownloadInfo.getTotalSize(), 
                                         mDownloadInfo.getVersion(),
                                         downloadPath);
        
        notifyListeners();
        return 0;
    }



}
