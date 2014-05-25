/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data.json;

/**
 * 
 * @author panxu
 * @since 2014-5-25
 */
public class ApkVersionJson {
    public int update;
    public long query_version_code; 
    public Description description;
    
    public static class Description {
        public String version_name;
        public String version_code;
        public String change_log;
        public String internal_path;
        public String external_url;
    }
    
    public static final int UPDATE_TRUE = 1;
    public static final int UPDATE_FALSE = 2;
}
