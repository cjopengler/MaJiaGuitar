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

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author panxu
 * @since 2013-12-30
 */
public class DownloadService extends IntentService {
    
    public static final String DOWNLOAD_ACTION = "com.majia.guitar.service.download";
    
    private static final String TAG = "DownloadService";
    private static final String SERVICE_NANME = "DownloadService";
    
    private static final int BUFFER_MAX_SIZE = 1024 * 2;
    
    private IDownloadData mDownloadData = MemoryDownloadData.getInstance();
    

    /**
     * @param name
     */
    public DownloadService() {
        super(SERVICE_NANME);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Uri donwloadUri = intent.getData();
        String action = intent.getAction();
        
        if (action.equals(DOWNLOAD_ACTION)) {
            handleDownload();
        }
    }
    
    private void handleDownload() {

        boolean isDownloadSuccess = false;
        RandomAccessFile apkRandomAccessFile = null;
        InputStream inputStream = null;
        

        try {
            BCSCredentials credentials = new BCSCredentials(BCSConfiguration.accessKey, BCSConfiguration.secretKey);
            BaiduBCS baiduBCS = new BaiduBCS(credentials, BCSConfiguration.host);
            baiduBCS.setDefaultEncoding("UTF-8");
            
            GetObjectRequest getObjectRequest = new GetObjectRequest(BCSConfiguration.bucket,
                    BCSConfiguration.APK_PATH);
            BaiduBCSResponse<DownloadObject> downloadRsp = baiduBCS.getObject(getObjectRequest);
            long totalSize = downloadRsp.getResult().getObjectMetadata().getContentLength();

            mDownloadData.update(totalSize, "0");
            
            inputStream = downloadRsp.getResult().getContent();

            File file = this.getExternalFilesDir(null);

            if (file == null) {
                // 给出提示
            } else {
                String path = file.getPath() + "/" + "yogaguitar.apk";

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
                    
                    mDownloadData.update(0, totalReadLenth);
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
            mDownloadData.finish(0);
            
            File file = this.getExternalFilesDir(null);

            String path = file.getPath() + "/" + "yogaguitar.apk";

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");

            startActivity(installIntent);
        }
    
    }

}
