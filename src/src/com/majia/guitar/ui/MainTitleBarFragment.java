/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.ApkVersion;
import com.majia.guitar.data.IUpdateApkVersion.UpdateListener;
import com.majia.guitar.data.UpdateApkVersion;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author panxu
 * @since 2013-12-15
 */
public class MainTitleBarFragment extends Fragment implements UpdateListener {
    private static final String TITLE_ARG = "title";
    private static final String SHOW_BACK_ARG = "show_back";
    private static final String SHOW_MORE_ARG = "show_more";
    
    private String mTitle;
    private boolean mIsShowBack;
    
    private ImageView mBackImageView;
    private TextView mTitleTextView;
    private View mMoreLayout;
    private ImageView mMoreImageView;
    private ImageView mUpdateIndicatorImageView;
    
    
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    
    
    
    
    public static class Args {
        
        private Bundle mBundle;
        
        public static Args buidArgs() {
            return new Args();
        }
        
        private Args() {
            mBundle = new Bundle();
        }
        
        public Args setTitle(String title) {
            mBundle.putString(TITLE_ARG, title);
            return this;
        }
        
        public Args setTitle(int resId) {
            String title = MaJiaGuitarApplication.getInstance().getString(resId);
            return setTitle(title);
        }
        
        public Args setShowBack(boolean isShow) {
            mBundle.putBoolean(SHOW_BACK_ARG, isShow);
            return this;
        }
        
        
        private Bundle getArgs() {
            return mBundle;
        }
        
        
    }
    
    public static MainTitleBarFragment newInstance(Args args) {
        MainTitleBarFragment titleFragment = new MainTitleBarFragment();
        titleFragment.setArguments(args.getArgs());
        
        return titleFragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle args = getArguments();
        
        mTitle = args.getString(TITLE_ARG);
        
        mIsShowBack = args.getBoolean(SHOW_BACK_ARG, false);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View titleView = inflater.inflate(R.layout.title_bar_fragment, container, false);
        
        mTitleTextView = (TextView) titleView.findViewById(R.id.titleTextView);
        mTitleTextView.setText(mTitle);
        
        mBackImageView = (ImageView) titleView.findViewById(R.id.backImageView);
        
        mBackImageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        
        mMoreLayout = titleView.findViewById(R.id.moreLayout);
        mMoreImageView = (ImageView) titleView.findViewById(R.id.moreImageView);
        
        mMoreImageView.setOnClickListener(new MoreOnClickListener());
        
        mUpdateIndicatorImageView = (ImageView) titleView.findViewById(R.id.updateIndicatorImageView);
        
        if (mIsShowBack) {
            mBackImageView.setVisibility(View.VISIBLE);
        } else {
            mBackImageView.setVisibility(View.GONE);
        }
        

        	
    	 ApkVersion apkVersion = UpdateApkVersion.getInstance().getApkVersion();
         if (apkVersion.versionCode > MaJiaGuitarApplication.getInstance().getVersionCode()) {
        	 mUpdateIndicatorImageView.setVisibility(View.VISIBLE);
         } else {
        	 mUpdateIndicatorImageView.setVisibility(View.GONE);
         }
        
        
        return titleView;
    }
    


	@Override
	public void onUpdate(ApkVersion apkVersion) {
		mUIHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if (mMoreLayout.getVisibility() == View.VISIBLE) {
					mUpdateIndicatorImageView.setVisibility(View.VISIBLE);
				}
			}
		});
	}
	
	private class MoreOnClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			mUpdateIndicatorImageView.setVisibility(View.GONE);
			
			startActivity(new Intent(getActivity(), MoreActivity.class));
		}
		
	}
}
