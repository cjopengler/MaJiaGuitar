/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

/**
 * 
 * @author panxu
 * @since 2014-3-26
 */
public abstract class AbstractRequestListener<T> implements IRequestListener<T>{
    private final long mSeqNumber;
    private static final SeqNumberGenerator SEQ_NUMBER_GENERATOR = new SeqNumberGenerator();
    
    public AbstractRequestListener() {
        mSeqNumber = SEQ_NUMBER_GENERATOR.generate();
    }
    
    public long getSeqNumber() {
        return mSeqNumber;
    }
}
