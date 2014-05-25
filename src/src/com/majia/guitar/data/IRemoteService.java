/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.JsonSyntaxException;
import com.majia.guitar.data.json.ApkVersionJson;

/**
 * 
 * @author panxu
 * @since 2014-5-25
 */
public interface IRemoteService {
    ApkVersionJson queryApkVersion(int versionCode) throws JsonSyntaxException, ClientProtocolException, IOException;
}
