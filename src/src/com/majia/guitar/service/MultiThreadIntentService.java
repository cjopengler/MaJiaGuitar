/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 设置http的线程数量 来达到 多个线程请求的http目的.
 * 避免被一个http请求block 无法后续进行.
 * @author panxu
 * @since 2014-2-17
 */
public abstract class MultiThreadIntentService extends IntentService {
    
    public MultiThreadIntentService(String name) {
		super(name);
	}

	/**线程池 core size*/
    private static final int CORE_POOL_SIZE = 5;
    
    /**线程池最大size*/
    private static final int MAX_POOL_SIZE = 10;
    
    /**线程alive时间 */
    private static final long KEEP_ALIVE_TIME = 0;
    
    /**线程池*/
    private volatile ExecutorService mExecutorService;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        final BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();
        mExecutorService = new ThreadPoolExecutor(
                                CORE_POOL_SIZE, 
                                MAX_POOL_SIZE, 
                                KEEP_ALIVE_TIME, 
                                TimeUnit.MILLISECONDS, 
                                blockingQueue);

    }
    
    protected final ExecutorService getExecutorService() {
        return mExecutorService;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        mExecutorService.shutdown();
        super.onDestroy();
    }

}
