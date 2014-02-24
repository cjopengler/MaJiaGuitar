/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.majia.guitar.util.HttpUtil;

/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class RemoteMusicServer {
    
    private static final String VERSION_URL = "http://yogaguitar.duapp.com/version.php";
    
    private RemoteMusicServer() {
        
    }
    
    public static Versions getVersions() {
        Versions versions = null;
        
        HttpUriRequest request = new HttpGet(VERSION_URL);
        try {
            VersionJson versionJson = HttpUtil.executeForJsonObject(request, VersionJson.class);
            versions = new Versions(versionJson);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return versions;
    }
}
