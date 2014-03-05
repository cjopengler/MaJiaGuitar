/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.google.gson.JsonSyntaxException;
import com.majia.guitar.util.HttpUtil;

/**
 * 
 * @author panxu
 * @since 2014-2-24
 */
public class RemoteMusicServer {
    
    private static final String VERSION_URL = "http://yogaguitar.duapp.com/version.php";
    private static final String GET_MUSICS_URL = "http://yogaguitar.duapp.com/musics.php";
    
    private RemoteMusicServer() {
        
    }
    
    public static Versions getVersions() {
        Versions versions = null;
        
        HttpUriRequest request = new HttpGet(VERSION_URL);
        try {
            VersionJson versionJson = HttpUtil.executeForJsonObject(request, VersionJson.class);
            versions = new Versions(versionJson);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        
        return versions;
    }
    
    public static List<MusicEntity> getMusics() {
        HttpUriRequest request = new HttpGet(GET_MUSICS_URL);
        List<MusicJson> musicJsons = null;
        try {
            musicJsons = HttpUtil.executeForJsonArray(request);
        } catch (ClientProtocolException e) {
            
        } catch (JsonSyntaxException e) {
            
        } catch (IOException e) {
           
        }
        
        List<MusicEntity> musicEntities = null;
        
        if (musicJsons != null) {
            musicEntities = new ArrayList<MusicEntity>(musicJsons.size());
            
            for (MusicJson musicJson : musicJsons) {
                musicEntities.add(new MusicEntity(musicJson));
            }
        }
        
        return musicEntities;
    }
    
    public static List<MusicJson> getMusicJsons() {
        HttpUriRequest request = new HttpGet(GET_MUSICS_URL);
        List<MusicJson> musicJsons = null;
        try {
            musicJsons = HttpUtil.executeForJsonArray(request);
        } catch (ClientProtocolException e) {
            
        } catch (JsonSyntaxException e) {
            
        } catch (IOException e) {
           
        }
        
        return musicJsons;
    }
    
    
}
