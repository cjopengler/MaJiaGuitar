/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data.download;

import java.util.ListIterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.majia.guitar.data.GuitarData;
import com.majia.guitar.data.IGuitarData;
import com.majia.guitar.util.Assert;

/**
 * 
 * @author panxu
 * @since 2014-6-26
 */
public class MusicDownloadData extends AbstractDownloadData {

    private static final class Holder {
        private static final MusicDownloadData INSTANCE = new MusicDownloadData();
    }
    
    private Lock mLock;
    private volatile DownloadInfo mDownloadInfo;
    
    private MusicDownloadData() {
        mLock = new ReentrantLock();
        mDownloadInfo = new DownloadInfo(DownloadInfo.DOWNLOAD_FINISH_SUCCESS, 
                0, 
                0, 
                0,
                "");
    }
    
    public static MusicDownloadData getInstance() {
        return Holder.INSTANCE;
    }
    @Override
    public DownloadInfo getCurDownloadInfo() {
        return mDownloadInfo;
    }

    @Override
    public long update(long id, long downloadSize, long totalSize) {
        mLock.lock();
        mDownloadInfo = new DownloadInfo(DownloadInfo.DOWNLOAD_IS_ONGOING, 
                                         downloadSize, 
                                         totalSize, 
                                         id,
                                         "");
        mLock.unlock();
        
        notifyListeners(id);
        
        return 0;
    }

    @Override
    public long finish(long id, int error, String downloadPath) {
        
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
            
        case IDownloadData.ERROR_NO_SDCARD:
            downloadStatus = DownloadInfo.DOWNLOAD_FINISH_NO_SDCARD_ERROR;
            break;
            
        case IDownloadData.ERROR_NO_ENOUGH_SPCACE:
            downloadStatus = DownloadInfo.DOWNLOAD_FINISH_NO_ENOUGH_ERROR;
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
        
        
        GuitarData.getInstance().updateMusicLocalUrl(id, downloadPath);
        notifyListeners(id);
        return 0;
    }

    private void notifyListeners(long id) {
        
        ListIterator<IDownloadListener> iterator = mDownloadListeners.listIterator();

        while (iterator.hasNext()) {
            IDownloadData.IDownloadListener downloadListener = (IDownloadData.IDownloadListener) iterator.next();
            downloadListener.onDownload(id, mDownloadInfo);
        }

    }

}
