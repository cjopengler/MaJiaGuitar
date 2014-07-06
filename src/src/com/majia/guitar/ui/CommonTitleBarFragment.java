/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.MaJiaGuitarApplication;
import com.majia.guitar.R;
import com.majia.guitar.data.ApkVersion;
import com.majia.guitar.data.IUpdateApkVersion.UpdateListener;
import com.majia.guitar.data.UpdateApkVersion;

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
public class CommonTitleBarFragment extends Fragment {
    private static final String TITLE_ARG = "title";
    private static final String SHOW_BACK_ARG = "show_back";
    
    private String mTitle;
    private boolean mIsShowBack;
    
    private ImageView mBackImageView;
    private TextView mTitleTextView;
    
    
    
    
    
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
    
    public static CommonTitleBarFragment newInstance(Args args) {
        CommonTitleBarFragment titleFragment = new CommonTitleBarFragment();
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
        View titleView = inflater.inflate(R.layout.common_title_bar_fragment, container, false);
        
        mTitleTextView = (TextView) titleView.findViewById(R.id.titleTextView);
        mTitleTextView.setText(mTitle);
        
        mBackImageView = (ImageView) titleView.findViewById(R.id.backImageView);
        
        mBackImageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        
        
        if (mIsShowBack) {
            mBackImageView.setVisibility(View.VISIBLE);
        } else {
            mBackImageView.setVisibility(View.GONE);
        }
        

        	
        
        return titleView;
    }
    


}
