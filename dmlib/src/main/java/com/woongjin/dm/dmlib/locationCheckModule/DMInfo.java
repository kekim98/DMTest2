package com.woongjin.dm.dmlib.locationCheckModule;

/**
 * Created by kekim98 on 2017-07-08.
 */

public class DMInfo {
    int result = -1; // -1=fail, 0=success
    int result_GPS = -1;
    int result_DM = -1;
    int errCode = 0; // 0=unknown, 1=wifi off, 2=gps off, 3= gps scan fail,
    String dmData = ""; //encryption data
    double latitude = 0.0f; //위도
    double longitude = 0.0f; //경도
    double distance= 0.0f; //거리차

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getDmData() {
        return dmData;
    }

    public void setDmData(String dmData) {
        this.dmData = dmData;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getResult_GPS() {
        return result_GPS;
    }

    public void setResult_GPS(int result_GPS) {
        this.result_GPS = result_GPS;
    }

    public int getResult_DM() {
        return result_DM;
    }

    public void setResult_DM(int result_DM) {
        this.result_DM = result_DM;
    }
}
