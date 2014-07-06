/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

/**
 * 
 * @author panxu
 * @since 2014-3-6
 */
public interface IRequestListener<T> {
    void onResponse(RequestResult result, T content);
}
