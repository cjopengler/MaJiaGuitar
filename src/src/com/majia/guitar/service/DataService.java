/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

import java.io.FileDescriptor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.majia.guitar.data.Versions;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * 
 * @author panxu
 * @since 2014-1-12
 */
public class DataService extends Service {
    
    
    
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final long KEEP_ALIVE_TIME = 0;
    private ExecutorService mExecutorService;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();
        mExecutorService = new ThreadPoolExecutor(
                                CORE_POOL_SIZE, 
                                MAX_POOL_SIZE, 
                                KEEP_ALIVE_TIME, 
                                TimeUnit.MILLISECONDS, 
                                blockingQueue);
    }
    

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public class DataBinder extends Binder {
        public DataService getDataService() {
            return DataService.this;
        }
    }
    
    @Override
    public void onDestroy() {
        mExecutorService.shutdown();
        super.onDestroy();
    }

    
}
