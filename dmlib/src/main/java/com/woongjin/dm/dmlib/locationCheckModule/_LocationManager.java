package com.woongjin.dm.dmlib.locationCheckModule;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;


class _LocationManager {
    private static final String TAG = _LocationManager.class.getSimpleName();
    public static final String DELIMITER ="-";
    private static final int DELAY_COUNT=500;
    private static final int MIN_WIFI_RSSI = -85;
    private static final boolean DEBUG = true;

    private static WifiManager wm = null;

    private static LocationManager locationManager = null;
    private static Location currentBestLocation= null;
    private static double  lat=0; // 위도
    private static double lng=0; // 경도

    public static double getLat() {
        return lat;

   //     return currentBestLocation==null?0.0:currentBestLocation.getLatitude();
    }

    public static double getLng() {
        return lng;
     //   return currentBestLocation==null?0.0:currentBestLocation.getLongitude();
    }

    public static String findCellID(Context ctx)  {
        String ret = "";
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();

        int cellID = location.getCid();
        //int lac = location.getLac();

        ret = String.valueOf(cellID);
        Log.d(TAG, "cellID:" + ret);

        return ret;
    }

    public static String findWifiMacAddr(Context ctx) {
        String ret = "";
        String addr1="",addr2="", addr3="", addr4="";

        if(wm == null) return ret;

        wm.startScan();
        // Don't delete for loop
        for(int i=0; i< DELAY_COUNT; i++){
            System.out.print(String.valueOf(i));
        }
        List<ScanResult> scanResults = wm.getScanResults();

        //sorting desc order of RSSI
        Collections.sort(scanResults, new Comparator<ScanResult>(){
            public int compare(ScanResult obj1, ScanResult obj2)
            {
                return (obj1.level > obj2.level) ? -1: (obj1.level > obj2.level) ? 1:0 ;
            }
        });

        int size = scanResults.size();
        Log.d(TAG, "scanResults.size()=" + size);

        for (int i = 0; i < size; i++) {
            ScanResult result = scanResults.get(i);
            Log.d(TAG, "Wifi(" + i + "):" + result.level);

            if(i==0 && result.level > MIN_WIFI_RSSI) {addr1 = result.BSSID;}
            else if(i==1 && result.level > MIN_WIFI_RSSI){addr2 = result.BSSID;}
            else if(i==2 && result.level > MIN_WIFI_RSSI){addr3 = result.BSSID;}
            else if(i==3 && result.level > MIN_WIFI_RSSI){addr4 = result.BSSID;}
            else{break;}
        }

        ret = addr1 + DELIMITER + addr2 + DELIMITER + addr3 + DELIMITER + addr4;
        Log.d(TAG, "MAC:" + ret);

        return ret;
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one LocationInfo reading is better than the current LocationInfo fix
     * @param location  The new LocationInfo that you want to evaluate
     * @param currentBestLocation  The current LocationInfo fix, to which you want to compare the new one
     */
     private static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static  boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    static boolean isGPSEnable(Context ctx){
        locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    static boolean isWifiEnable(Context ctx){
        wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        return wm.isWifiEnabled();
    }

    static int initLocationManager(Context ctx){
       if(!isGPSEnable(ctx)){
           return LocationCheckService.E_GPSOFF;
       }

       if(!isWifiEnable(ctx)){
           return LocationCheckService.E_WIFIOFF;
       }

        // if WIFIEnabled
        if (wm.isWifiEnabled() == false) {
            wm.setWifiEnabled(true);
            for(int i=0; i< DELAY_COUNT; i++){
                System.out.print(String.valueOf(i));
            }
        }

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged.provider: " + provider + " ,status:"+status);
            }
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled.provider: " + provider + " enabled");
            }
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled.provider: " + provider + " disabled");
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        return 0;
    }

    static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit.equals("kilometer")) {
            dist = dist * 1.609344;
        } else if(unit.equals("meter")){
            dist = dist * 1609.344;
        }

        return (dist);
    }
    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
