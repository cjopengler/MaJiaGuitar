package com.majia.guitar;


import java.io.File;
import java.io.FileInputStream;

import com.majia.guitar.service.ApkUpdateService;
import com.majia.guitar.ui.MusicFragment;
import com.majia.guitar.ui.MainTitleBarFragment;
import com.majia.guitar.ui.MainTitleBarFragment.Args;
import com.majia.guitar.util.MusicLog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

public class GuitarMusicsActivity extends FragmentActivity {
    private static final String TAG = "MaJiaGuitarActivity";

   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.guitar_music_activity);
        
        if (savedInstanceState == null) {
        
	        FragmentManager fragmentManager = getSupportFragmentManager();
	        FragmentTransaction ft = fragmentManager.beginTransaction();
	        
	        Args titleBarArgs = Args.buidArgs()
	                                .setTitle(R.string.app_name)
	                                .setShowBack(false);
	        
	        MainTitleBarFragment titleBarFragment = MainTitleBarFragment.newInstance(titleBarArgs);
	        
	        ft.add(R.id.titleBarContainer, titleBarFragment);
	        ft.commit();
        }
        
        
    }

}
