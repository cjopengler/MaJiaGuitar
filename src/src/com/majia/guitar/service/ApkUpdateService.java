package com.majia.guitar.service;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.JsonSyntaxException;
import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.data.ApkVersion;
import com.majia.guitar.data.MusicRemoteServer;
import com.majia.guitar.data.UpdateApkVersion;
import com.majia.guitar.data.json.ApkVersionJson;
import com.majia.guitar.util.Assert;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;

public class ApkUpdateService extends MultiThreadIntentService {

	private static final String SERVICE_NAME = "ApkUpdateService";
	
	public static String CHECK_APK_UPDATE_ACTION = "com.majia.guitar.service.ApkUpdateService.check_apk_update_action";
	
	private static final String TAG = "ApkUpdateService";
	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	
	public ApkUpdateService() {
		super(SERVICE_NAME);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new ApkUpdateServiceBinder();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		
		if (action.equals(CHECK_APK_UPDATE_ACTION)) {
			int currentVersionCode = MaJiaGuitarApplication.getInstance().getVersionCode();
			try {
				
				ApkVersionJson apkVersionJson = MusicRemoteServer.getInstance().
																  queryApkVersion(currentVersionCode);
			
				ApkVersion apkVersion = new ApkVersion(apkVersionJson);
				UpdateApkVersion.getInstance().setApkVersion(apkVersion);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	public Future<ApkVersion> checkApkUpdate(final AbstractRequestListener<ApkVersion> listener) {
		Future<ApkVersion> future = getExecutorService().submit(new Callable<ApkVersion>() {

			@Override
			public ApkVersion call() throws Exception {
				
				RequestResult result = RequestResult.FAIL;
				ApkVersion apkVersion = null;
				int currentVersionCode = MaJiaGuitarApplication.getInstance().getVersionCode();
				try {
					
					ApkVersionJson apkVersionJson = MusicRemoteServer.getInstance().
																	  queryApkVersion(currentVersionCode);
					apkVersion = new ApkVersion(apkVersionJson);
					UpdateApkVersion.getInstance().setApkVersion(apkVersion);
					
					result = RequestResult.SUCCESS;
				} catch (JsonSyntaxException e) {
				    result = RequestResult.FAIL_HTTP;
				} catch (ClientProtocolException e) {
					result = RequestResult.FAIL_HTTP;
				} catch (IOException e) {
					result = RequestResult.FAIL_HTTP;
				}
				
				
				final RequestResult retRequestResult = result;
				final ApkVersion retApkVersion = apkVersion;
				mUIHandler.post(new Runnable() {
					
					@Override
					public void run() {
						listener.onResponse(retRequestResult, retApkVersion);
					}
				});
				
				
				
				return apkVersion;
				
			}
		});
		
		return future;
	}
	
	public class ApkUpdateServiceBinder extends Binder {
		
		public ApkUpdateService getApkUpdateService() {
			return ApkUpdateService.this;
		}
	}
	
	
	
	
	
}
