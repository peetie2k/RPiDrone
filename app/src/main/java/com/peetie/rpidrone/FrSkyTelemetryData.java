package com.peetie.rpidrone;



import java.util.HashMap;

/**
 * Created by Peetie_2 on 29.07.2015.
 */
public class FrSkyTelemetryData {

    public static final int ID_GPS_ALTIDUTE_BP = 0x01;
    public static final int ID_GPS_ALTIDUTE_AP = 0x09;
    public static final int ID_TEMPRATURE1 = 0x02;
    public static final int ID_RPM = 0x03;
    public static final int ID_FUEL_LEVEL = 0x04;
    public static final int ID_TEMPRATURE2 = 0x05;
    public static final int ID_VOLT = 0x06;
    public static final int ID_ALTITUDE_BP = 0x10;
    public static final int ID_ALTITUDE_AP = 0x21;
    public static final int ID_GPS_SPEED_BP = 0x11;
    public static final int ID_GPS_SPEED_AP = 0x19;
    public static final int ID_LONGITUDE_BP = 0x12;
    public static final int ID_LONGITUDE_AP = 0x1A;
    public static final int ID_E_W = 0x22;
    public static final int ID_LATITUDE_BP = 0x13;
    public static final int ID_LATITUDE_AP = 0x1B;
    public static final int ID_N_S = 0x23;
    public static final int ID_COURSE_BP = 0x14;
    public static final int ID_COURSE_AP = 0x1C;
    public static final int ID_DATE_MONTH = 0x15;
    public static final int ID_YEAR = 0x16;
    public static final int ID_HOUR_MINUTE = 0x17;
    public static final int ID_SECOND = 0x18;
    public static final int ID_ACC_X = 0x24;
    public static final int ID_ACC_Y = 0x25;
    public static final int ID_ACC_Z = 0x26;
    public static final int ID_VOLTAGE_AMP = 0x39;
    public static final int ID_VOLTAGE_AMP_BP = 0x3A;
    public static final int ID_VOLTAGE_AMP_AP = 0x3B;
    public static final int ID_CURRENT = 0x28;
    public static final int ID_GYRO_X = 0x40;
    public static final int ID_GYRO_Y = 0x41;
    public static final int ID_GYRO_Z = 0x42;
    public static final int ID_VERT_SPEED = 0x30;

    private HashMap<Integer, Integer> telemtryDataMap;
    private static FrSkyTelemetryData _instance;

    public static FrSkyTelemetryData getInstance(){
        if( _instance == null)
            _instance = new FrSkyTelemetryData();
        return _instance;
    }

    private FrSkyTelemetryData() {
        telemtryDataMap = new HashMap<Integer, Integer>();
        telemtryDataMap.put(ID_GPS_ALTIDUTE_BP, 0);
        telemtryDataMap.put(ID_GPS_ALTIDUTE_AP, 0);
        telemtryDataMap.put(ID_TEMPRATURE1, 0);
        telemtryDataMap.put(ID_RPM, 0);
        telemtryDataMap.put(ID_FUEL_LEVEL, 0);
        telemtryDataMap.put(ID_TEMPRATURE2, 0);
        telemtryDataMap.put(ID_VOLT, 0);
        telemtryDataMap.put(ID_ALTITUDE_BP, 0);
        telemtryDataMap.put(ID_ALTITUDE_AP, 0);
        telemtryDataMap.put(ID_GPS_SPEED_BP, 0);
        telemtryDataMap.put(ID_GPS_SPEED_AP, 0);
        telemtryDataMap.put(ID_LONGITUDE_BP, 0);
        telemtryDataMap.put(ID_LONGITUDE_AP, 0);
        telemtryDataMap.put(ID_E_W, 0);
        telemtryDataMap.put(ID_LATITUDE_BP, 0);
        telemtryDataMap.put(ID_LATITUDE_AP, 0);
        telemtryDataMap.put(ID_N_S, 0);
        telemtryDataMap.put(ID_COURSE_BP, 0);
        telemtryDataMap.put(ID_COURSE_AP, 0);
        telemtryDataMap.put(ID_DATE_MONTH, 0);
        telemtryDataMap.put(ID_YEAR, 0);
        telemtryDataMap.put(ID_HOUR_MINUTE, 0);
        telemtryDataMap.put(ID_SECOND, 0);
        telemtryDataMap.put(ID_ACC_X, 0);
        telemtryDataMap.put(ID_ACC_Y, 0);
        telemtryDataMap.put(ID_ACC_Z, 0);
        telemtryDataMap.put(ID_VOLTAGE_AMP, 0);
        telemtryDataMap.put(ID_VOLTAGE_AMP_BP, 0);
        telemtryDataMap.put(ID_VOLTAGE_AMP_AP, 0);
        telemtryDataMap.put(ID_CURRENT, 0);
        telemtryDataMap.put(ID_GYRO_X, 0);
        telemtryDataMap.put(ID_GYRO_Y, 0);
        telemtryDataMap.put(ID_GYRO_Z, 0);
        telemtryDataMap.put(ID_VERT_SPEED, 0);
    }

    public void setData(String dataString) {
        String[] splitted = dataString.split(":");
        String idStr = splitted[0];
        String valueStr = splitted[1];

        idStr = idStr.replace("0x","");
        valueStr = valueStr.replace("0x","");

        Integer id = Integer.parseInt(idStr, 16);
        Integer value = Integer.parseInt(valueStr);
        telemtryDataMap.put(id,value);
    }

    public int getData(int key){
        return telemtryDataMap.get(key);
    }


}


