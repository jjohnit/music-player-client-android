// ClipServerServices.aidl
package com.jjasan2.clipserver;

// Declare any non-default types here with import statements

interface ClipServerServices {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    boolean startService();
    boolean play(int songIndex);
    boolean pause();
    boolean resume();
    boolean stop();
    boolean stopService();
}