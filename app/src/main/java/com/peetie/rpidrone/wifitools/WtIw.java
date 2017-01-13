package com.peetie.rpidrone.wifitools;

/**
 * Created by Peetie_2 on 29.05.2016.
 */
public class WtIw {

    static {
        System.loadLibrary("nl");
        System.loadLibrary("iw");
    }

    public native static int exec(String wlanInterface);


}
