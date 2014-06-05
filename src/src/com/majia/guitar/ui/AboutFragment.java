/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.ApkVersion;
import com.majia.guitar.data.UpdateApkVersion;
import com.majia.guitar.data.download.DownloadInfo;
import com.majia.guitar.data.download.IDownloadData.IDownloadListener;
import com.majia.guitar.data.download.MemoryDownloadData;
import com.majia.guitar.service.DownloadService;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * @author panxu
 * @since 2014-5-25
 */
public class AboutFragment extends Fragment implements IDownloadListener {
    
    private static final String TAG = "AboutFragment";
    
    private TextView mCurrentVersionTextView;
    private ViewGroup mUpdateLayout;
    private TextView mNewVersionTextView;
    private TextView mChangeLogTextView;
    private Button mUpdateVersionButton;
    
    private ProgressDialog mProgressDialog;

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);
        
        mCurrentVersionTextView = (TextView) view.findViewById(R.id.currentVersionTextView);
        
        String versionName = MaJiaGuitarApplication.getInstance().getVersionName();
        String currentVersion = getString(R.string.about_current_version, versionName);
        mCurrentVersionTextView.setText(currentVersion);
        
        mUpdateLayout = (ViewGroup) view.findViewById(R.id.updateLayout);
        
        mNewVersionTextView = (TextView) view.findViewById(R.id.newVersionTextView);
        mChangeLogTextView = (TextView) view.findViewById(R.id.changeLogTextView);
        
        mUpdateVersionButton = (Button) view.findViewById(R.id.updateApkButton);
        
        int currentVersionCode = MaJiaGuitarApplication.getInstance().getVersionCode();
        
        ApkVersion apkVersion = UpdateApkVersion.getInstance().getApkVersion();
        
        if (apkVersion.versionCode > currentVersionCode) {
            mUpdateLayout.setVisibility(View.VISIBLE);
            
            mUpdateVersionButton.setOnClickListener(new UpdateApkOnClickListener());
            mUpdateVersionButton.setText(R.string.about_update_apk);
            mNewVersionTextView.setText(apkVersion.versionName);
            mChangeLogTextView.setText(apkVersion.changeLog);
        } else {
            mUpdateLayout.setVisibility(View.GONE);
            mUpdateVersionButton.setOnClickListener(new CheckApkOnClickListener());
            mUpdateVersionButton.setText(R.string.about_check_update_apk);
        }
        
        return view;
    }
    
    private class UpdateApkOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
                mProgressDialog.setTitle(R.string.about_downloading_apk);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMax(100);
                
                
                mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getText(R.string.about_apk_install), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked Yes so do some stuff */
                    }
                });
                
                
                
                mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                        getText(R.string.cancle), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //getActivity().stopService(name);
                    }
                });
                
                mProgressDialog.setCancelable(false);
            }
            
            
            
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        
            
            MemoryDownloadData.getInstance().addListener(AboutFragment.this);
            
            ApkVersion apkVersion = UpdateApkVersion.getInstance().getApkVersion();
            Intent downloadApkIntent = new Intent(getActivity(), DownloadService.class);
            downloadApkIntent.setAction(DownloadService.DOWNLOAD_BCS_ACTION);
            downloadApkIntent.putExtra(DownloadService.DOWNLOAD_VERSION_CODE, apkVersion.versionCode);
            downloadApkIntent.putExtra(DownloadService.DOWLOAD_BUCKET_PATH, apkVersion.internalPath);
            
            getActivity().startService(downloadApkIntent);
        }
        
    }
    
    private class CheckApkOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            
        }
        
    }

    @Override
    public void onDownload(long id, DownloadInfo downloadInfo) {
        Log.d(TAG, "downloadInfo is: " + downloadInfo.getStatus());
    }
}
