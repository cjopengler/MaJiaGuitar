/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.baidu.inf.iis.bcs.BaiduBCS;
import com.baidu.inf.iis.bcs.auth.BCSCredentials;
import com.baidu.inf.iis.bcs.model.BCSClientException;
import com.baidu.inf.iis.bcs.model.BCSServiceException;
import com.baidu.inf.iis.bcs.model.DownloadObject;
import com.baidu.inf.iis.bcs.request.GetObjectRequest;
import com.baidu.inf.iis.bcs.response.BaiduBCSResponse;
import com.majia.guitar.data.MusicEntity;
import com.majia.guitar.data.bcs.BCSConfiguration;
import com.majia.guitar.data.download.IDownloadData;
import com.majia.guitar.data.download.MemoryDownloadData;
import com.majia.guitar.data.download.MusicDownloadData;
import com.majia.guitar.util.SDCardUtil;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author panxu
 * @since 2014-6-26
 */
public class MusicDownloadService extends IntentService {

    public static final String INTENT_MUSIC_ENTITY = "intent_music_entity";
    
    private static final String NAME = "MusicDownloadService";
    private static final String TAG = "MusicDownloadService";

    private IDownloadData mDownloadData = MusicDownloadData.getInstance();
    
    private static final int BUFFER_MAX_SIZE = 1024 * 64;
    
    private static final int MAX_ALIVALIBLE_SPACE = 30 * 1024 * 1024;
    /**
     * @param name
     */
    public MusicDownloadService() {
        super(NAME);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handle(intent);
    }
    
    private void handle(Intent intent) {
        
       
        
        MusicEntity musicEntity = intent.getParcelableExtra(INTENT_MUSIC_ENTITY);
        
        Log.d(TAG, "musicEntity" + musicEntity);
        
        long downloadVersionCode = musicEntity.getId();
        
        if (!SDCardUtil.existSDCard()) {
            mDownloadData.finish(downloadVersionCode, IDownloadData.ERROR_NO_SDCARD, "");
            return;
        }
        
        if (SDCardUtil.getSDFreeSize() < MAX_ALIVALIBLE_SPACE) {
            mDownloadData.finish(downloadVersionCode, IDownloadData.ERROR_NO_ENOUGH_SPCACE, "");
            return;
        }
        
        String downloadBucketPath = musicEntity.getSoundUrl();
        

        RandomAccessFile apkRandomAccessFile = null;
        InputStream inputStream = null;
        String path = "";
        String tempPath = "";
        int errorCode = IDownloadData.ERROR;
        
        

        try {
            BCSCredentials credentials = new BCSCredentials(BCSConfiguration.accessKey, BCSConfiguration.secretKey);
            BaiduBCS baiduBCS = new BaiduBCS(credentials, BCSConfiguration.host);
            baiduBCS.setDefaultEncoding("UTF-8");
            
            int bucketIndex = downloadBucketPath.indexOf("/");
            String bucket = downloadBucketPath.substring(0, bucketIndex);
            String pathInBucket = downloadBucketPath.substring(bucketIndex + 1);
            
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucket,
                                                                     "/" + pathInBucket);
            BaiduBCSResponse<DownloadObject> downloadRsp = baiduBCS.getObject(getObjectRequest);
            long totalSize = downloadRsp.getResult().getObjectMetadata().getContentLength();

            mDownloadData.update(downloadVersionCode, 0, totalSize);
            
            inputStream = downloadRsp.getResult().getContent();
            
            File file = this.getExternalFilesDir(null);
            
            if (file != null) {
                path = file.getPath() + "/" + "yogaguitar_" + musicEntity.getMusicId() + ".mp3";
                tempPath = path + ".tmp";

                apkRandomAccessFile = new RandomAccessFile(tempPath, "rw");


                apkRandomAccessFile.setLength(totalSize);

                byte[] buffer = new byte[BUFFER_MAX_SIZE];

                int readLength = 0;

                long totalReadLenth = 0;

                while ((readLength = inputStream.read(buffer, 0, BUFFER_MAX_SIZE)) != -1) {
                    apkRandomAccessFile.write(buffer, 0, readLength);

                    totalReadLenth += readLength;

                    mDownloadData.update(downloadVersionCode, totalReadLenth, totalSize);
                }

                if (totalReadLenth != totalSize) {
                    throw new RuntimeException("offset = " + totalReadLenth + ", totalSize = " + totalSize);
                } else {
                    errorCode = IDownloadData.SUCCESS;

                }

            } else {
                errorCode = IDownloadData.ERROR_IO;
               
            }

        } catch (BCSServiceException e) {
            Log.e(TAG,
                    "Bcs return:" + e.getBcsErrorCode() + ", " + e.getBcsErrorMessage() + ", RequestId="
                            + e.getRequestId());
            
            errorCode = IDownloadData.ERROR_NETWORK;
        } catch (BCSClientException e) {

            errorCode = IDownloadData.ERROR_NETWORK;
        } catch (FileNotFoundException e) {
            errorCode = IDownloadData.ERROR_IO;
        } catch (IOException e) {
            errorCode = IDownloadData.ERROR_IO;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    errorCode = IDownloadData.ERROR_IO;
                }
            }

            if (apkRandomAccessFile != null) {

                try {
                    
                    apkRandomAccessFile.close();
                    File downloadFile = new File(tempPath);
                    
                    if (errorCode == IDownloadData.SUCCESS) {
                        
                        downloadFile.renameTo(new File(path));
                    } else {
                        downloadFile.delete();
                    }
                    
                } catch (IOException e) {
                    Log.e(TAG, "close failed for io exception " + e.getMessage());
                    errorCode = IDownloadData.ERROR_IO;
                }
            }
        }
        
        mDownloadData.finish(downloadVersionCode, errorCode, path);
        
    
        
    }



}
