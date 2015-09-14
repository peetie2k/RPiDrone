package com.peetie.rpidrone;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Peetie_2 on 29.07.2015.
 */
public class UDPTelemetryReader implements Runnable {

    private  int port;
    private  String ip;
    private  FrSkyTelemetryData telemData;
    private boolean connectionFailure = false;

    UDPTelemetryReader(String ip, int port, FrSkyTelemetryData telemData) {
        this.ip = ip;
        this.port = port;
        this.telemData = telemData;
        //run();
    }

    public void run() {
        // TODO Auto-generated method stub
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(port, InetAddress.getByName(ip));

            byte[] data = new byte[14];
            while (true) {
                DatagramPacket packet = new DatagramPacket(data, data.length);
                serverSocket.receive(packet);
                String dataStr = new String(packet.getData(), 0, packet.getLength());
                telemData.setData(dataStr);
                Log.e("Data",dataStr);
            }

        } catch (SocketException e) {
            connectionFailure = true;
        } catch (IOException e) {
            connectionFailure = true;
        }
    }
}

