/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;


import com.majia.guitar.R;
import com.majia.guitar.service.DownloadService;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * 
 * @author panxu
 * @since 2013-12-22
 */
public class UpdateFragment extends Fragment {
    public static UpdateFragment newInstance() {
        return new UpdateFragment();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View updateView = inflater.inflate(R.layout.update_fragment, container, false);
        
        Button apkUpdateButton = (Button) updateView.findViewById(R.id.apkUpdatebutton);
        apkUpdateButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent downloadServiceIntent = new Intent(getActivity(), DownloadService.class);
                downloadServiceIntent.setAction(DownloadService.DOWNLOAD_ACTION);
                getActivity().startService(downloadServiceIntent);
            }
        });
        
        
        return updateView;
    }
}
