/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.R;
import com.majia.guitar.ui.CommonTitleBarFragment.Args;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;

/**
 * 
 * @author panxu
 * @since 2014-5-25
 */
public class SettingActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.more_activity);
		
		 if (savedInstanceState == null) {
		        
		        FragmentManager fragmentManager = getSupportFragmentManager();
		        FragmentTransaction ft = fragmentManager.beginTransaction();
		        
		        Args titleBarArgs = Args.buidArgs()
		                                .setTitle(R.string.setting)
		                                .setShowBack(true);
		        
		        CommonTitleBarFragment titleBarFragment = CommonTitleBarFragment.newInstance(titleBarArgs);
		        
		        ft.add(R.id.titleBarContainer, titleBarFragment);
		        ft.commit();
	        }
		 
		 View apkVersionView = findViewById(R.id.set_version_check_view);
		 apkVersionView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				startActivity(new Intent(SettingActivity.this, VersionInfoActivity.class));
			}
		});
		 
	}
}
