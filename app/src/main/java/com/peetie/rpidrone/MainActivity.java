package com.peetie.rpidrone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.peetie.rpidrone.gstreamer.VideoPlaybackFull;
import com.peetie.rpidrone.wifitools.WtIw;

import org.apache.http.conn.HttpHostConnectException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;


public class MainActivity extends ActionBarActivity {

    private static final String portName = "PORT";
    private static final String ipAddressName = "IP_ADDRESS";
    private static final String frequencyName = "FREQUENCY";
    private static final String transferModeName = "MODE";
    private static final String transferModeBroadcast = "WBC";
    private static final String transferModeWLAN = "WLAN";
    private String ipAddress;
    private String port;
    private String frequency;
    private String telemPort = "3001";
    private String transferMode = "WBC";
    private UDPTelemetryReader telemReader;
    private FrSkyTelemetryData telemData;

    private EditText mFrequency;
    private EditText mIP;
    private EditText mPort;
    private RadioGroup mMode;

    public static Enumeration<InetAddress> getWifiInetAddresses(final Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final String macAddress = wifiInfo.getMacAddress();
        final String[] macParts = macAddress.split(":");
        final byte[] macBytes = new byte[macParts.length];
        for (int i = 0; i < macParts.length; i++) {
            macBytes[i] = (byte) Integer.parseInt(macParts[i], 16);
        }
        try {
            final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                final NetworkInterface networkInterface = e.nextElement();
                if (Arrays.equals(networkInterface.getHardwareAddress(), macBytes)) {
                    return networkInterface.getInetAddresses();
                }
            }
        } catch (SocketException e) {
            Log.wtf("WIFIIP", "Unable to NetworkInterface.getNetworkInterfaces()");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends InetAddress> T getWifiInetAddress(final Context context, final Class<T> inetClass) {
        final Enumeration<InetAddress> e = getWifiInetAddresses(context);
        while (e.hasMoreElements()) {
            final InetAddress inetAddress = e.nextElement();
            if (inetAddress.getClass() == inetClass) {
                return (T) inetAddress;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Runtime.getRuntime().exec("su");
        } catch (IOException exc) {

        }

        mIP = (EditText) findViewById(R.id.txtIpAddress);
        mPort = (EditText) findViewById(R.id.txtPort);
        mFrequency = (EditText) findViewById(R.id.txtFrequency);
        mMode = (RadioGroup) findViewById(R.id.rgMode);

        loadPreferences();
        mIP.setText(ipAddress);
        mPort.setText(port);
        mFrequency.setText(frequency);
        if (transferMode.equals(transferModeBroadcast)) {
            mMode.check(R.id.rbBroadcast);
        } else {
            mMode.check(R.id.rbWLAN);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPreferences() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        port = prefs.getString(portName, port);
        ipAddress = prefs.getString(ipAddressName, ipAddress);
        frequency = prefs.getString(frequencyName, frequency);
        transferMode = prefs.getString(transferModeName, transferMode);
        if (transferMode == null) transferMode = "WB";
    }

    private void savePreferences() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(portName, port);
        editor.putString(ipAddressName, ipAddress);
        editor.putString(frequencyName, frequency);
        editor.putString(transferModeName, transferMode);


        editor.commit();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public void setTransferMode(String transferMode) {
        this.transferMode = transferMode;
    }

    public void onButtonSaveClick(View v) {
        this.setPort(mPort.getText().toString());
        this.setIpAddress(mIP.getText().toString());
        this.setFrequency(mFrequency.getText().toString());
        if (mMode.getCheckedRadioButtonId() == R.id.rbBroadcast)
            transferMode = transferModeBroadcast;
        else
            transferMode = transferModeWLAN;

        savePreferences();
    }

    public void onStartStreamClick(View v) {
        if (transferMode.equals(transferModeWLAN))
            startWLANStream();
        else
            startBroadcast();


    }

    public void showToast(String message) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void startBroadcast() {
        showToast("WB selected");
        try {

            Runtime.getRuntime().exec("su");


            Process process = new ProcessBuilder()
                    .command("killall", "ifplugd")
                    .redirectErrorStream(true)
                    .start();

            try {
                InputStream in = process.getInputStream();
                OutputStream out = process.getOutputStream();

                if (in != null) {
                    BufferedReader br = null;
                    StringBuilder sb = new StringBuilder();

                    String line;
                    try {

                        br = new BufferedReader(new InputStreamReader(in));
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    String input = sb.toString();
                }
            } finally {
                process.destroy();
            }

            Runtime.getRuntime().exec("killall ifplugd");
            Runtime.getRuntime().exec("ifconfig wlan0 down");

            WtIw iw = new WtIw();

            String device = new String("wlan0");


            //
            int err = iw.exec(device);
            if (err == 0)
                Log.i("IW-Result", String.valueOf(err));


        } catch (IOException e) {
            Log.wtf("Exec", e.getMessage());
        }


        // Runtime.getRuntime().exec("sudo iw dev wlan0 set monitor otherbss fcsfail");
        //Runtime.getRuntime().exec("ifconfig wlan0 up");
        //Runtime.getRuntime().exec("sudo iwconfig wlan0 channel 13");

    }


    private void startWLANStream() {
        //Inet4Address droidIPV4Address = getWifiInetAddress(this.getApplicationContext(), Inet4Address.class);
        /*if (droidIPV4Address == null) {
            showToast("WiFi not connected");
            return;
        }*/


        String droidIP = "192.168.100.2";// droidIPV4Address.toString().substring(1);

        telemData = FrSkyTelemetryData.getInstance();
        telemReader = new UDPTelemetryReader(droidIP, Integer.parseInt(telemPort), telemData);
        Thread t = new Thread(telemReader);
        t.start();


        StringBuffer sbuffStart = new StringBuffer();
        sbuffStart.append("http://");
        sbuffStart.append(ipAddress);
        sbuffStart.append(":");
        sbuffStart.append(port);
        sbuffStart.append("/startStream");


        RestClient clientStart = new RestClient(sbuffStart.toString());
        clientStart.AddParam("IP", droidIP);
        clientStart.AddParam("Port", "9000");
        clientStart.AddParam("telemPort", "3001");


        StringBuffer sbuffStop = new StringBuffer();
        sbuffStop.append("http://");
        sbuffStop.append(ipAddress);
        sbuffStop.append(":");
        sbuffStop.append(port);
        sbuffStop.append("/stopStream");


        RestClient clientStop = new RestClient(sbuffStop.toString());

        try {
            clientStop.Execute(RestClient.RequestMethod.GET);
            Thread.sleep(1000);
            clientStart.Execute(RestClient.RequestMethod.GET);
        } catch (HttpHostConnectException e) {
            showToast(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String response = clientStart.getResponse();

        if (response == null) {
            response = "Connection failed";
        }

        showToast(response);


        Intent intent = new Intent(MainActivity.this, VideoPlaybackFull.class);
        Bundle b = new Bundle();
        b.putString("ip", this.ipAddress); //Your id
        b.putString("port", this.port); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
        finish();
    }


}
