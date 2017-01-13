package com.peetie.rpidrone.wifitools;

import android.content.Context;

/**
 * Created by Peetie_2 on 29.05.2016.
 */
public class iwconfig {

    static {
        System.loadLibrary("libpcap");
        System.loadLibrary("wb_receiver");
    }

    private native static boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native static void nativeBefiReceiver(int param_port,int param_data_packets_per_block, int param_fec_packets_per_block, int param_block_buffers, int param_packet_length, String[] wlan_devices, int num_wlan_devices );
    private static native void nativeInit(Context context) throws Exception;

}
