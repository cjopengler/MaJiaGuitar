/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.util;

/**
 * 
 * @author panxu
 * @since 2014-6-5
 */
public class Assert {

    /**
     * 参考 assertOnly(String message)
     */
    public static void assertOnly() {
        assertOnly("");
    }

    /**
     * 仅仅是assert
     * 
     * @param message
     *            assert时候的message
     */
    public static void assertOnly(String message) {
        throw new RuntimeException(message);
    }

    /**
     * 如果表达式为true,不会被assert掉；如果表达式为false,就会被assert掉
     * 
     * @param condition
     *            表达式的结果
     */
    public static void assertTrue(boolean condition) {
        assertTrue(condition, "");
    }

    /**
     * 如果表达式为true,不会被assert掉；如果表达式为false,就会被assert掉
     * 
     * @param condition
     *            表达式的结果
     * @param message
     *            输出的消息
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    /**
     * 参考 assertNotNull(Object object, String message)
     */
    public static void assertNotNull(Object object) {
        assertNotNull(object, "");
    }

    /**
     * 如果不是null，不会被assert掉；如果为null，会被assert掉
     * 
     * @param object
     *            object
     * @param message
     *            null的消息
     */
    public static void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new RuntimeException(message);
        }
    }
}
