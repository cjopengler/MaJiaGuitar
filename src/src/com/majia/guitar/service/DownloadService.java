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
import com.majia.guitar.R;
import com.majia.guitar.data.bcs.BCSConfiguration;
import com.majia.guitar.data.download.IDownloadData;
import com.majia.guitar.data.download.MemoryDownloadData;
import com.majia.guitar.util.Assert;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
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

        try {
            BCSCredentials credentials = new BCSCredentials(BCSConfiguration.accessKey, BCSConfiguration.secretKey);
            BaiduBCS baiduBCS = new BaiduBCS(credentials, BCSConfiguration.host);
            baiduBCS.setDefaultEncoding("UTF-8");
            
            GetObjectRequest getObjectRequest = new GetObjectRequest(BCSConfiguration.bucket,
                                                                     downloadBucketPath);
            BaiduBCSResponse<DownloadObject> downloadRsp = baiduBCS.getObject(getObjectRequest);
            long totalSize = downloadRsp.getResult().getObjectMetadata().getContentLength();

            mDownloadData.update(totalSize, downloadVersionCode);
            
            inputStream = downloadRsp.getResult().getContent();

            
            File file = this.getExternalFilesDir(null);

            if (file == null) {
                // 给出提示
                
            } else {
                path = file.getPath() + "/" + "yogaguitar_" + downloadVersionCode + ".apk";

                apkRandomAccessFile = new RandomAccessFile(path, "rw");// new
                                                                       // RandomAccessFile(apkFile,
                                                                       // "rw");

                apkRandomAccessFile.setLength(totalSize);

                byte[] buffer = new byte[BUFFER_MAX_SIZE];

                int readLength = 0;

                long totalReadLenth = 0;

                while ((readLength = inputStream.read(buffer, 0, BUFFER_MAX_SIZE)) != -1) {
                    apkRandomAccessFile.write(buffer, 0, readLength);

                    totalReadLenth += readLength;
                    
                    mDownloadData.update(downloadVersionCode, totalReadLenth);
                }

                if (totalReadLenth != totalSize) {
                    throw new RuntimeException("offset = " + totalReadLenth + ", totalSize = " + totalSize);
                } else {
                    Toast.makeText(this, R.string.download_finish, Toast.LENGTH_LONG).show();
                    isDownloadSuccess = true;
                    
                }

            }

        } catch (BCSServiceException e) {
            Log.e(TAG,
                    "Bcs return:" + e.getBcsErrorCode() + ", " + e.getBcsErrorMessage() + ", RequestId="
                            + e.getRequestId());
        } catch (BCSClientException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (apkRandomAccessFile != null) {

                try {
                    apkRandomAccessFile.close();
                } catch (IOException e) {
                    Log.e(TAG, "close failed for io exception " + e.getMessage());
                }
            }
        }
        
        if (isDownloadSuccess) {
            mDownloadData.finish(downloadVersionCode, IDownloadData.SUCCESS, path);
            
            /*Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");

            startActivity(installIntent);*/
        } else {
            mDownloadData.finish(downloadVersionCode, IDownloadData.ERROR, path);
        }
    
    }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
