/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import java.lang.ref.WeakReference;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.ApkVersion;
import com.majia.guitar.data.UpdateApkVersion;
import com.majia.guitar.data.download.DownloadInfo;
import com.majia.guitar.data.download.IDownloadData.IDownloadListener;
import com.majia.guitar.data.download.MemoryDownloadData;
import com.majia.guitar.service.AbstractRequestListener;
import com.majia.guitar.service.ApkUpdateService;
import com.majia.guitar.service.ApkUpdateService.ApkUpdateServiceBinder;
import com.majia.guitar.service.DownloadService;
import com.majia.guitar.service.RequestResult;
import com.majia.guitar.util.Assert;

import android.app.ProgressDialog;
import android.app.Service;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author panxu
 * @since 2014-5-25
 */
public class VersionInfoFragment extends Fragment implements IDownloadListener {
    
    private static final String TAG = "AboutFragment";
    
    private TextView mCurrentVersionTextView;
    private ViewGroup mUpdateLayout;
    private TextView mNewVersionTextView;
    private TextView mChangeLogTextView;
    private Button mUpdateVersionButton;
    
    private ProgressDialog mProgressDialog;
    
    private final Handler mUiHandler = new Handler(MaJiaGuitarApplication.getInstance().getMainLooper());

    private ApkUpdateService mApkUpdateService;
    private ApkUpdateServiceConnection mApkUpdateServiceConnection;
    
    public static VersionInfoFragment newInstance() {
        return new VersionInfoFragment();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onDestroy() {
    	
    	if (mApkUpdateServiceConnection != null) {
    		getActivity().unbindService(mApkUpdateServiceConnection);
    		mApkUpdateService = null;
    		mApkUpdateServiceConnection = null;
    	}
    	super.onDestroy();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.versin_info_fragment, container, false);
        
        mCurrentVersionTextView = (TextView) view.findViewById(R.id.currentVersionTextView);
        
        String versionName = MaJiaGuitarApplication.getInstance().getVersionName();
        String currentVersion = getString(R.string.about_current_version, versionName);
        mCurrentVersionTextView.setText(currentVersion);
        
        mUpdateLayout = (ViewGroup) view.findViewById(R.id.updateLayout);
        
        mNewVersionTextView = (TextView) view.findViewById(R.id.newVersionTextView);
        mChangeLogTextView = (TextView) view.findViewById(R.id.changeLogTextView);
        
        mUpdateVersionButton = (Button) view.findViewById(R.id.updateApkButton);
        
        setApkUpdateUI();
        
        return view;
    }
    
    private void setApkUpdateUI() {
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
    }
    
    private class UpdateApkOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
            mProgressDialog.setTitle(R.string.about_downloading_apk);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            
            
            mProgressDialog.setCancelable(false);
            
            ApkVersion apkVersion = UpdateApkVersion.getInstance().getApkVersion();
            
            mProgressDialog.setMax(apkVersion.apkSize);
            mProgressDialog.show();
            
        
            
            MemoryDownloadData.getInstance().addListener(VersionInfoFragment.this);
            
           
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
        	mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle(R.string.about_checking_update_apk);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            
            
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
            
            if (mApkUpdateService == null) {
            	mApkUpdateServiceConnection = new ApkUpdateServiceConnection();
            	getActivity().bindService(new Intent(getActivity(), ApkUpdateService.class), 
            							  mApkUpdateServiceConnection, 
            							  Service.BIND_AUTO_CREATE);
            } else {
            	doCheckApkUpdate();
            }
        }
        
    }

    @Override
    public void onDownload(long id, final DownloadInfo downloadInfo) {
        mUiHandler.post(new Runnable() {
            
            @Override
            public void run() {
                Log.d(TAG, "downloadInfo is: " + downloadInfo.getStatus());
                mProgressDialog.setProgress((int) downloadInfo.getDownloadSize());
                
                switch (downloadInfo.getStatus()) {
                case DownloadInfo.DOWNLOAD_IS_ONGOING:
                    
                    break;
                    
                case DownloadInfo.DOWNLOAD_FINISH_ERROR:
                    mProgressDialog.dismiss();
                    Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.about_download_error, Toast.LENGTH_LONG).show();
                    break;

                case DownloadInfo.DOWNLOAD_FINISH_IO_ERROR:
                    mProgressDialog.dismiss();
                    Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.about_download_error_io, Toast.LENGTH_LONG).show();
                    break;
                
                case DownloadInfo.DOWNLOAD_FINISH_NET_ERROR:
                    mProgressDialog.dismiss();
                    Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.about_download_error_net, Toast.LENGTH_LONG).show();
                    break;
                    
                case DownloadInfo.DOWNLOAD_FINISH_SUCCESS:
                    mProgressDialog.dismiss();
                    
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    installIntent.setDataAndType(Uri.parse("file://" + downloadInfo.getDownloadPath()), "application/vnd.android.package-archive");

                    startActivity(installIntent);
                    break;
                    

                default:
                    Assert.assertOnly("download status error: " + downloadInfo.getStatus());
                    break;
                }
                
            }
        });
        
       
    }
    
    private void doCheckApkUpdate() {
    	mApkUpdateService.checkApkUpdate(new CheckApkUpdateListener(this));
    }
    
    private class ApkUpdateServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			ApkUpdateServiceBinder apkUpdateServiceBinder = (ApkUpdateServiceBinder) binder;
			mApkUpdateService = apkUpdateServiceBinder.getApkUpdateService();
			
			doCheckApkUpdate();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mApkUpdateService = null;
			mApkUpdateServiceConnection = null;
			
		}
    	
    }
    
    private static class CheckApkUpdateListener extends AbstractRequestListener<ApkVersion> {

    	private final WeakReference<VersionInfoFragment> mWeakReference;
    	
    	public CheckApkUpdateListener(VersionInfoFragment fragment) {
    		mWeakReference = new WeakReference<VersionInfoFragment>(fragment);
    	}
    	
		@Override
		public void onResponse(RequestResult result, ApkVersion content) {
			VersionInfoFragment fragment = mWeakReference.get();
			
			if (fragment != null && fragment.isAdded()) {
				fragment.mProgressDialog.dismiss();
				
				if (content.versionCode == MaJiaGuitarApplication.getInstance().getVersionCode()) {
					Toast.makeText(MaJiaGuitarApplication.getInstance(), R.string.current_is_newest_version, Toast.LENGTH_SHORT).show();
				}
				fragment.setApkUpdateUI();
				
			}
		}
    	
    }
}
