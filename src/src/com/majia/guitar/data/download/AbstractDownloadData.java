/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data.download;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author panxu
 * @since 2013-12-30
 */
public abstract class AbstractDownloadData implements IDownloadData {

    protected final CopyOnWriteArrayList<IDownloadListener> mDownloadListeners;
    
    public AbstractDownloadData() {
        mDownloadListeners = new CopyOnWriteArrayList<IDownloadData.IDownloadListener>();
    }
    
    @Override
    public void addListener(IDownloadListener listener) {
        mDownloadListeners.addIfAbsent(listener);
    }

    @Override
    public void removeListener(IDownloadListener listener) {
        mDownloadListeners.remove(listener);
    }

}
