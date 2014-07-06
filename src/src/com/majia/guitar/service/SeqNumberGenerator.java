/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author panxu
 * @since 2014-3-6
 */
public class SeqNumberGenerator {
    private final AtomicLong mSeqNumber;
    
    public SeqNumberGenerator() {
        this(0);
    }
    
    public SeqNumberGenerator(long startNumber) {
        mSeqNumber = new AtomicLong(startNumber);
    }
    
    public long generate() {
        return mSeqNumber.incrementAndGet();
    }
}
