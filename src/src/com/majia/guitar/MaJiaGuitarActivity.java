package com.majia.guitar;


import java.io.File;
import java.io.FileInputStream;

import com.majia.guitar.log.GuitarLog;
import com.majia.guitar.service.ApkUpdateService;
import com.majia.guitar.ui.MusicFragment;
import com.majia.guitar.ui.MainTitleBarFragment;
import com.majia.guitar.ui.MainTitleBarFragment.Args;
import com.majia.guitar.util.MusicLog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

public class MaJiaGuitarActivity extends FragmentActivity {
    private static final String TAG = "MaJiaGuitarActivity";
    
    private static final long DISPLAY_TIME_OUT = 2500;

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.majia_guitar_activity);
        
      
        Intent apkUpdateCheckIntent = new Intent(this, ApkUpdateService.class);
        
        apkUpdateCheckIntent.setAction(ApkUpdateService.CHECK_APK_UPDATE_ACTION);
        startService(apkUpdateCheckIntent);
       
        
        mUIHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				finish();
				startActivity(new Intent(MaJiaGuitarActivity.this, GuitarMusicsActivity.class));
				
			}
		}, DISPLAY_TIME_OUT);
    }

}
