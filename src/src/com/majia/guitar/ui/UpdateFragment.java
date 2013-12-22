/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import com.majia.guitar.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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
        return updateView;
    }
}
