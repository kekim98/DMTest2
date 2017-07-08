package com.woongjin.dm.dmlib.locationCheckModule;

/**
 * Created by kekim98 on 2017-05-26.
 */

class LocationInfo {
    String CID="";  // cell id
    String ADDR1=""; //wifi mac addr
    String ADDR2="";
    String ADDR3="";
    String ADDR4="";
    double LAT= 0.0f; //위도
    double LNG= 0.0f; //경도

    public String getCID() {
        return CID;
    }

    public void setCID(String CID) {
        this.CID = CID;
    }

    public String getADDR1() {
        return ADDR1;
    }

    public void setADDR1(String ADDR1) {
        this.ADDR1 = ADDR1;
    }

    public String getADDR2() {
        return ADDR2;
    }

    public void setADDR2(String ADDR2) {
        this.ADDR2 = ADDR2;
    }

    public String getADDR3() {
        return ADDR3;
    }

    public void setADDR3(String ADDR3) {
        this.ADDR3 = ADDR3;
    }

    public String getADDR4() {
        return ADDR4;
    }

    public void setADDR4(String ADDR4) {
        this.ADDR4 = ADDR4;
    }

    public double getLAT() {
        return LAT;
    }

    public void setLAT(double LAT) {
        this.LAT = LAT;
    }

    public double getLNG() {
        return LNG;
    }

    public void setLNG(double LNG) {
        this.LNG = LNG;
    }
}
