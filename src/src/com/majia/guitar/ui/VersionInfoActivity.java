package com.majia.guitar.ui;

import com.majia.guitar.R;
import com.majia.guitar.ui.CommonTitleBarFragment.Args;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class VersionInfoActivity extends FragmentActivity {
    public static final String NEW_APP_VERSION_ACTION = "com.majia.guitar.ui.VersionInfoActivity.NEW_APP_VERSION_ACTION";
	private ImageView mBackImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.version_info_activity);
		
		mBackImageView = (ImageView) findViewById(R.id.backImageView);
		
		mBackImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		 
	}
}
