/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;


import com.majia.guitar.R;
import com.majia.guitar.data.download.DownloadInfo;
import com.majia.guitar.data.download.IDownloadData;
import com.majia.guitar.data.download.IDownloadData.IDownloadListener;
import com.majia.guitar.data.download.MemoryDownloadData;
import com.majia.guitar.log.GuitarLog;
import com.majia.guitar.service.DownloadService;
import com.majia.guitar.data.GuitarData;
import com.majia.guitar.data.Version;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;



/**
 * 
 * @author panxu
 * @since 2013-12-22
 */
public class UpdateFragment extends Fragment implements IDownloadListener,
                                                        GuitarData.IGuitarDataListener {
    private static final String TAG = "UpdateFragment";
    
    private TextView mDownloadPercentTextView;
    private ProgressBar mDownloadProgressBar;
    private Button mApkUpdateButton;
    private Handler mUiHandler;
    private Button mTestApkDownloadButton;
    
    public static UpdateFragment newInstance() {
        return new UpdateFragment();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        GuitarData.getInstance().addListener(this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View updateView = inflater.inflate(R.layout.update_fragment, container, false);
        
        mUiHandler = new Handler();
        
        mApkUpdateButton = (Button) updateView.findViewById(R.id.apkUpdatebutton);
        mApkUpdateButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent downloadServiceIntent = new Intent(getActivity(), DownloadService.class);
                downloadServiceIntent.setAction(DownloadService.DOWNLOAD_ACTION);
                getActivity().startService(downloadServiceIntent);
            }
        });
        
        mDownloadPercentTextView = (TextView) updateView.findViewById(R.id.downloadPercentTextView);
        mDownloadProgressBar = (ProgressBar) updateView.findViewById(R.id.downloadProgressBar);
        
        IDownloadData downloadData = MemoryDownloadData.getInstance();
        DownloadInfo downloadInfo = downloadData.getCurDownloadInfo();
        setDownloadUi(downloadInfo);
        
        downloadData.addListener(this);
        
        mTestApkDownloadButton = (Button) updateView.findViewById(R.id.testApkDownloadButton);
        mTestApkDownloadButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent downloadServiceIntent = new Intent(getActivity(), DownloadService.class);
                downloadServiceIntent.setAction(DownloadService.DOWNLOAD_ACTION);
                getActivity().startService(downloadServiceIntent);
                
            }
        });
        
        return updateView;
    }

    @Override
    public void onDestroyView() {
        IDownloadData downloadData = MemoryDownloadData.getInstance();
        downloadData.removeListener(this);
        super.onDestroyView();
    }
    
    @Override
    public void onDetach() {
        GuitarData.getInstance().removeListener(this);
        super.onDetach();
    }
    
    @Override
    public void onDownload(long id, final DownloadInfo downloadInfo) {
        mUiHandler.post(new Runnable() {
            
            @Override
            public void run() {
                setDownloadUi(downloadInfo);
            }
        });
    }
    
    private void setDownloadUi(DownloadInfo downloadInfo) {
        
        GuitarLog.d(TAG, "donwloadInfo: " + downloadInfo);
        
        switch (downloadInfo.getStatus()) {
        
        case DownloadInfo.IDEL_STATUS:
            mApkUpdateButton.setEnabled(false);
            mDownloadPercentTextView.setVisibility(View.GONE);
            mDownloadProgressBar.setVisibility(View.GONE);
            break;
        
        case DownloadInfo.DOWNLOAD_START_STATUS:
            mApkUpdateButton.setEnabled(true);
            mDownloadPercentTextView.setVisibility(View.GONE);
            mDownloadProgressBar.setVisibility(View.GONE);
            break;
            
            
        case DownloadInfo.DOWNLOADING_STATUS:
            long downloadSize = downloadInfo.getDownloadSize();
            long totalSize = downloadInfo.getTotalSize();
            
            mApkUpdateButton.setEnabled(false);
            
            mDownloadPercentTextView.setVisibility(View.VISIBLE);
            mDownloadPercentTextView.setText(downloadSize*100 / totalSize + "%");
            
            mDownloadProgressBar.setVisibility(View.VISIBLE);
            mDownloadProgressBar.setMax((int) totalSize);
            mDownloadProgressBar.setProgress((int) downloadSize);
            break;
            
        case DownloadInfo.DOWNLOAD_FINISH_STATUS:
            mApkUpdateButton.setEnabled(false);
            mDownloadPercentTextView.setVisibility(View.GONE);
            mDownloadProgressBar.setVisibility(View.GONE);
            break;
            
        default:
            break;
        }
    }

    @Override
    public void onUpdateVersion(Version oldMusicVersion, Version newMusicVersion, Version oldApkVersion,
            Version newApkVersion) {
        
    }
}
