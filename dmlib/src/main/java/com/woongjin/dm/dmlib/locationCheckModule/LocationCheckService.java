package com.woongjin.dm.dmlib.locationCheckModule;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.woongjin.dm.dmlib.locationCheckModule._LocationManager.DELIMITER;
import static com.woongjin.dm.dmlib.locationCheckModule._LocationManager.isGPSEnable;
import static com.woongjin.dm.dmlib.locationCheckModule._LocationManager.isWifiEnable;


public class LocationCheckService extends Service {
    private static final String TAG = LocationCheckService.class.getSimpleName();
    public static final int SUCCESS = 0;
    public static final int FAIL = -1;

    public static final int  E_UNKNOWN=0;
    public static final int  E_WIFIOFF=1;
    public static final int  E_GPSOFF=2;
    public static final int  E_GPSSCANFAIL=3;

    //FIXME : 제품 버전에서는 반드시 DEBUG = false 해야 함
    private static final boolean DEBUG = false;
    private static final String DISTANCE_UNIT = "meter";
    private static final double VALIDE_DISTANCE = 1000.0f; //40meter
    private static int sDistance = 0;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationCheckService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationCheckService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public LocationInfo parseLocation(DMInfo data){
        LocationInfo result = new LocationInfo();
        String cellId="", addr1="", addr2="", addr3="", addr4 ="";
        String lat="", lng="";


        String enc = data.getDmData();
        String dec = getBase64decode(enc);
        Pattern pattern = Pattern.compile("([^-]*)-([^-]*)-([^-]*)-([^-]*)-([^-]*)-([^-]*)-([^-]*)");
        Matcher matcher = pattern.matcher(dec);

        while(matcher.find()) {
            cellId = matcher.group(1);
            addr1 = matcher.group(2);
            addr2 = matcher.group(3);
            addr3 = matcher.group(4);
            addr4 = matcher.group(5);
            lat = matcher.group(6);
            lng = matcher.group(7);
        }

        result.setCID(cellId);
        result.setADDR1(addr1);
        result.setADDR2(addr2);
        result.setADDR3(addr3);
        result.setADDR4(addr4);
        result.setLAT(Double.parseDouble(lat));
        result.setLNG(Double.parseDouble(lng));

        return result;
    }

    private LocationInfo getCurrentLocation(){
        LocationInfo lo = new LocationInfo();
        String tempAddr = _LocationManager.findWifiMacAddr(this);

        Pattern pattern = Pattern.compile("([^-]*)-([^-]*)-([^-]*)-([^-]*)");
        Matcher matcher = pattern.matcher(tempAddr);
        String addr1="", addr2="", addr3="", addr4 ="";
        while(matcher.find()) {
            addr1 = matcher.group(1);
            addr2 = matcher.group(2);
            addr3 = matcher.group(3);
            addr4 = matcher.group(4);
        }
        lo.setADDR1(addr1);
        lo.setADDR2(addr2);
        lo.setADDR3(addr3);
        lo.setADDR4(addr4);

        String cid = _LocationManager.findCellID(this);
        lo.setCID(cid);

        double lat = _LocationManager.getLat();
        lo.setLAT(lat);

        double lng = _LocationManager.getLng();
        lo.setLNG(lng);
        return lo;
    }
    private boolean isValideCELL(LocationInfo lo1, LocationInfo lo2){
        return lo1.getCID().equals(lo2.getCID());
    }

    private boolean isValideWIFI(LocationInfo lo1, LocationInfo lo2){
        if(lo1.getADDR1().isEmpty() || lo2.getADDR1().isEmpty()) return false;

        if(!lo1.getADDR1().isEmpty()
                && (lo1.getADDR1().equals(lo2.getADDR1())
                || lo1.getADDR1().equals(lo2.getADDR2())
                || lo1.getADDR1().equals(lo2.getADDR3())
                || lo1.getADDR1().equals(lo2.getADDR4()))) return true;
        if(!lo1.getADDR2().isEmpty()
                && (lo1.getADDR2().equals(lo2.getADDR1())
                || lo1.getADDR2().equals(lo2.getADDR2())
                || lo1.getADDR2().equals(lo2.getADDR3())
                || lo1.getADDR2().equals(lo2.getADDR4()))) return true;

        if(!lo1.getADDR3().isEmpty()
                && (lo1.getADDR3().equals(lo2.getADDR1())
                || lo1.getADDR3().equals(lo2.getADDR2())
                || lo1.getADDR3().equals(lo2.getADDR3())
                || lo1.getADDR3().equals(lo2.getADDR4()))) return true;

        if(!lo1.getADDR4().isEmpty()
                && (lo1.getADDR4().equals(lo2.getADDR1())
                || lo1.getADDR4().equals(lo2.getADDR2())
                || lo1.getADDR4().equals(lo2.getADDR3())
                || lo1.getADDR4().equals(lo2.getADDR4()))) return true;

        return false;
    }

    private boolean isValideGPS(LocationInfo lo1, LocationInfo lo2){
        double lat1, lat2, lon1, lon2;
        lat1 = lo1.getLAT();
        lat2 = lo2.getLAT();
        lon1 = lo1.getLNG();
        lon2 = lo2.getLNG();

        int distance = (int) _LocationManager.distance(lat1,lon1,lat2,lon2,DISTANCE_UNIT);

        sDistance = distance;

        if (distance <= VALIDE_DISTANCE) return true;

        return false;
    }
    /**
     * Base64 인코딩
     */
    static String getBase64encode(String content){
        return Base64.encodeToString(content.getBytes(), 0);
    }

    /**
     * Base64 디코딩
     */
    static String getBase64decode(String content){
        return new String(Base64.decode(content, 0));
    }

    /**
     * method for clients
     **/

    public DMInfo regDM(){
        DMInfo dmInfo = new DMInfo();

        if(!isGPSEnable(this)){
            dmInfo.setErrCode(E_GPSOFF);
            return dmInfo;
        }

        if(!isWifiEnable(this)){
            dmInfo.setErrCode(E_WIFIOFF);
            return dmInfo;
        }

        LocationInfo lo = getCurrentLocation();

        if(lo.getLAT() == 0 || lo.getLNG() == 0){
            dmInfo.setErrCode(E_GPSSCANFAIL);
            return dmInfo;
        }

        dmInfo.setResult(SUCCESS);
        dmInfo.setLatitude(lo.getLAT());
        dmInfo.setLongitude(lo.getLNG());

        String str = lo.getCID() + DELIMITER
                + lo.getADDR1() + DELIMITER
                + lo.getADDR2() + DELIMITER
                + lo.getADDR3() + DELIMITER
                + lo.getADDR4() + DELIMITER
                + lo.getLAT() + DELIMITER
                + lo.getLNG() + DELIMITER;
        String encData = getBase64encode(str);
        dmInfo.setDmData(encData);

        return dmInfo;
    }

    public DMInfo verifyDM(DMInfo rcvInfo){
        DMInfo dmInfo = new DMInfo();

        if(!isGPSEnable(this)){
            dmInfo.setErrCode(E_GPSOFF);
            dmInfo.setResult_GPS(FAIL);
            return dmInfo;
        }
        if(!isWifiEnable(this)){
            dmInfo.setErrCode(E_WIFIOFF);
            dmInfo.setResult_DM(FAIL);
            return dmInfo;
        }

        if(rcvInfo == null || rcvInfo.getDmData().isEmpty() /*|| rcvInfo.getLatitude() == 0 || rcvInfo.getLongitude() == 0*/) {
            dmInfo.setErrCode(E_UNKNOWN);
            return dmInfo;
        }

        LocationInfo clo = getCurrentLocation();
        LocationInfo olo = parseLocation(rcvInfo);

        if(clo.getLAT() == 0 || clo.getLNG() == 0){
            dmInfo.setErrCode(E_GPSSCANFAIL);
            return dmInfo;
        }

        String str = clo.getCID() + DELIMITER
                + clo.getADDR1() + DELIMITER
                + clo.getADDR2() + DELIMITER
                + clo.getADDR3() + DELIMITER
                + clo.getADDR4() + DELIMITER;
        String encData = getBase64encode(str);
        dmInfo.setDmData(encData);

        dmInfo.setLatitude(clo.getLAT());
        dmInfo.setLongitude(clo.getLNG());

        if(isValideGPS(clo,olo)){
            dmInfo.setResult_GPS(SUCCESS);
        }

        if(isValideCELL(clo, olo) || isValideWIFI(clo, olo)){
            dmInfo.setResult_DM(SUCCESS);
        }

        if(clo.getADDR1().isEmpty()){
            if(isValideGPS(clo, olo) &&  isValideCELL(clo,olo)){
                dmInfo.setResult(SUCCESS);
            }
        }else{
            if(isValideGPS(clo, olo) &&  isValideWIFI(clo,olo)){
                dmInfo.setResult(SUCCESS);
            }
        }
        dmInfo.setDistance(sDistance);

        return dmInfo;
    }

    public DMInfo initService() {
        DMInfo dmInfo = new DMInfo();

        _LocationManager.initLocationManager(this);
        return dmInfo;
    }
}
