package com.majia.guitar.receiver;
import com.majia.guitar.data.GuitarData;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */

/**
 * 
 * @author panxu
 * @since 2014-7-1
 */
public class DownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadReceiver";
    

    @Override
    public void onReceive(Context context, Intent intent) {
        
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long downloadedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            
            Log.d(TAG, "Download is ok ");
            
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadedId);
            
            Cursor cursor = null;
            int status = 0;
            int reason = 0;
            String downloadedFile = "";
            
            try {
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                
                cursor = downloadManager.query(query);
                while (cursor.moveToNext()) {

                    status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                    downloadedFile = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            
            
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                GuitarData.getInstance().updateVideoLocalUrl(downloadedId, downloadedFile);
            }
        }
    }

}
