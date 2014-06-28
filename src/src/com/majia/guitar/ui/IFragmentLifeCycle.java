/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.ui;

import android.support.v4.app.Fragment;

/**
 * 
 * @author panxu
 * @since 2014-6-27
 */
public interface IFragmentLifeCycle {
    void onCreate(Fragment fragment);
    void onDestroy(Fragment fragment);
}
