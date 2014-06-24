/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
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
import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.bcs.BCSConfiguration;
import com.majia.guitar.data.download.IDownloadData;
import com.majia.guitar.data.download.MemoryDownloadData;
import com.majia.guitar.util.Assert;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author panxu
 * @since 2013-12-30
 */
public class DownloadService extends IntentService {
    
    public static final String DOWNLOAD_BCS_ACTION = "com.majia.guitar.service.download";
    public static final String DOWLOAD_BUCKET_PATH = "com.majia.guitar.service.download_url";
    
    /**用来生成Apk的名字*/
    public static final String DOWNLOAD_VERSION_CODE = "com.majia.guitar.service.download_version_code";
    
    private static final String TAG = "DownloadService";
    private static final String SERVICE_NAME = "DownloadService";
    
    private static final int BUFFER_MAX_SIZE = 1024 * 2;
    
    
    
    private IDownloadData mDownloadData = MemoryDownloadData.getInstance();
    

    /**
     * @param name
     */
    public DownloadService() {
        super(SERVICE_NAME);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Uri donwloadUri = intent.getData();
        String action = intent.getAction();
        
        if (action.equals(DOWNLOAD_BCS_ACTION)) {
            String bucketPath = intent.getStringExtra(DOWLOAD_BUCKET_PATH);
            int downloadVersionCode = intent.getIntExtra(DOWNLOAD_VERSION_CODE, -1);
            
            Assert.assertTrue(!TextUtils.isEmpty(bucketPath));
            Assert.assertTrue(downloadVersionCode != -1);
            
            handleDownload(bucketPath, downloadVersionCode);
        }
    }
    
    private void handleDownload(String downloadBucketPath, int downloadVersionCode) {

        boolean isDownloadSuccess = false;
        RandomAccessFile apkRandomAccessFile = null;
        InputStream inputStream = null;
        String path = "";
        int errorCode = IDownloadData.ERROR;

        try {
            BCSCredentials credentials = new BCSCredentials(BCSConfiguration.accessKey, BCSConfiguration.secretKey);
            BaiduBCS baiduBCS = new BaiduBCS(credentials, BCSConfiguration.host);
            baiduBCS.setDefaultEncoding("UTF-8");
            
            GetObjectRequest getObjectRequest = new GetObjectRequest(BCSConfiguration.bucket,
                                                                     "/" + downloadBucketPath);
            BaiduBCSResponse<DownloadObject> downloadRsp = baiduBCS.getObject(getObjectRequest);
            long totalSize = downloadRsp.getResult().getObjectMetadata().getContentLength();

            mDownloadData.update(downloadVersionCode, 0, totalSize);
            
            inputStream = downloadRsp.getResult().getContent();
            
            File file = this.getExternalFilesDir(null);
            
            if (file != null) {
                path = file.getPath() + "/" + "yogaguitar_" + downloadVersionCode + ".apk";

                apkRandomAccessFile = new RandomAccessFile(path, "rw");


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
                } catch (IOException e) {
                    Log.e(TAG, "close failed for io exception " + e.getMessage());
                    errorCode = IDownloadData.ERROR_IO;
                }
            }
        }
        
        mDownloadData.finish(downloadVersionCode, errorCode, path);
        
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
