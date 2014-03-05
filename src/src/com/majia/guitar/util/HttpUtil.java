/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.util;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Http Json传输
 * @author panxu
 * @since 2014-2-2
 */
public final class HttpUtil {
    /**TAG*/
    private final static String TAG = "HttpUtil";
    
    /**http 连接超时*/
    private final static int CONNECTION_TIME_OUT =  10*1000;
    
    /**Socket超时*/
    private final static int SOCKET_TIME_OUT = 10*1000;
    
    /**UA*/
    private final static String BASIC_UA = "Mozilla/5.0 (Linux; U; Android; en-us; MI 2S Build/JRO03L) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    
    /**构造函数私有化 防止被实例化*/
    private HttpUtil() {
        
    }
    
    /**
     * 提交http请求 用户自定义处理结果
     * @param request http请求
     * @param responseHandler 回调处理函数
     * @return 用户自定义结果
     * @throws ClientProtocolException 异常
     * @throws IOException 异常
     */
    public static final <T> T execute(HttpUriRequest request, ResponseHandler<T> responseHandler) throws ClientProtocolException, IOException {
        
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIME_OUT);
        HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIME_OUT);
        

        HttpClient client = new DefaultHttpClient(httpParams);
        
        request.addHeader(
                "User-Agent",
                BASIC_UA);
       
        
        T result = client.execute(request, responseHandler);
        
        return result;
    }
    
    /**
     * http请求并解析出json object 数据结构
     * @param request http请求
     * @param classType 解析后的class类型
     * @return 解析的object数据结构
     * @throws ClientProtocolException 异常
     * @throws IOException 异常
     */
    public static final <T> T executeForJsonObject(HttpUriRequest request, Class<T> classType) throws ClientProtocolException, IOException, JsonSyntaxException {
        return execute(request, new JsonObjectResonponsHandler<T>(classType));
    }
    
    /**
     * http请求并解析出json arry 数据结构
     * @param request http请求
     * @return 解析的数组结构
     * @throws ClientProtocolException 异常
     * @throws IOException 异常
     */
    public static final <T> List<T> executeForJsonArray(HttpUriRequest request) throws ClientProtocolException, IOException, JsonSyntaxException {
        return execute(request, new JsonArrayResonponsHandler<T>());

    }
    
    private static class JsonObjectResonponsHandler<T> implements ResponseHandler<T> {

        /**即将转换的json class类型*/
        private final Class<T> mClassType;
        
        /**
         * 构造函数
         * @param classType json class类型
         */
        public JsonObjectResonponsHandler(Class<T> classType) {
            mClassType = classType;
        }
        
        @Override
        public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException, JsonSyntaxException {
            T result = null;
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                
                String entity = EntityUtils.toString(response.getEntity(), "UTF-8");
            
                Gson gson = new Gson();
                
                result = gson.fromJson(entity, mClassType);
                
            } else {
                MusicLog.e(TAG, "JsonResonponsHandler: get status error " + statusCode);
            }
            return result;
        }
        
    }
    
    private static final class JsonArrayResonponsHandler<T> implements ResponseHandler<List<T>> {

        @Override
        public List<T> handleResponse(HttpResponse response) throws ClientProtocolException, IOException, JsonSyntaxException {
            List<T> result = null;
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                
                String entity = EntityUtils.toString(response.getEntity(), "UTF-8");
            
                Gson gson = new Gson();
                
                TypeToken<List<T>> typeToken = new TypeToken<List<T>>(){};
                result = gson.fromJson(entity, typeToken.getType());
                
                
            } else {
                MusicLog.e(TAG, "JsonResonponsHandler: get status error " + statusCode);
            }
            return result;
        }
        
    }
}
