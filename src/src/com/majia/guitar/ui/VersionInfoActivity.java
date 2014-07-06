package com.majia.guitar.ui;

import com.majia.guitar.R;
import com.majia.guitar.ui.CommonTitleBarFragment.Args;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class VersionInfoActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.version_info_activity);
		
		 if (savedInstanceState == null) {
		        
		        FragmentManager fragmentManager = getSupportFragmentManager();
		        FragmentTransaction ft = fragmentManager.beginTransaction();
		        
		        Args titleBarArgs = Args.buidArgs()
		                                .setTitle(R.string.yoga_guitar)
		                                .setShowBack(true);
		        
		        CommonTitleBarFragment titleBarFragment = CommonTitleBarFragment.newInstance(titleBarArgs);
		        
		        ft.add(R.id.titleBarContainer, titleBarFragment);
		        ft.commit();
	        }
		 
		 
	}
}
