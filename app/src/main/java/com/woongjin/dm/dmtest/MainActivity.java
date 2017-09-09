package com.woongjin.dm.dmtest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapRegionDecoder;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.woongjin.dm.dmlib.locationCheckModule.DMInfo;
import com.woongjin.dm.dmlib.locationCheckModule.LocationCheckService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    LocationCheckService mService;
    boolean mBound = false;

    private Button mRegButton;
    private Button mCheckButton;
    private Button mClearButton;
    private TextView mMsgBox;

    private static DMInfo mRegDMInfo ;
    private static String mRegMsg="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bind to LocationCheckService
        Intent intent = new Intent(this, LocationCheckService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);

        mRegButton = (Button)findViewById(R.id.reg_button);
        mRegButton.setOnClickListener(this);
        mCheckButton = (Button)findViewById(R.id.check_button);
        mCheckButton.setOnClickListener(this);
        mClearButton = (Button)findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(this);

        mMsgBox = (TextView)findViewById(R.id.msg_box);
       // mMsgBox.setMovementMethod(new ScrollingMovementMethod()); // enable scrolling


        setButtonsState(false);

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocationCheckService, cast the IBinder and get LocationCheckService instance
            if (!mBound) {
                LocationCheckService.LocalBinder binder = (LocationCheckService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
                setButtonsState(true);

                mService.initService();
                Log.d(TAG, "ServiceConnected-mBound:" + mBound);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            setButtonsState(false);
            Log.d(TAG, "onServiceDisconnected().................................");
        }
    };

    @Override
    public void onClick(View view) {
        if(!mBound){return;}

        if (view == mRegButton) {
            DMInfo dmInfo = mService.regDM();

            mRegDMInfo = dmInfo; // 점검을 위해 저장해 둠

            if(dmInfo.getResult() == LocationCheckService.SUCCESS){
                String result = "등록 성공";
                String dmData = "DM Data :" + dmInfo.getDmData();
                String lat = "위도 :" + String.valueOf(dmInfo.getLatitude());
                String lng = "경도 :" + String.valueOf(dmInfo.getLongitude());

                mRegMsg = result + "\n" + dmData + "\n" + lat + ", " + lng;
                mMsgBox.setText(mRegMsg);
            }else{
                String result = "등록 실패";
                String err="";
                switch (dmInfo.getErrCode()){
                    case LocationCheckService.E_UNKNOWN :
                        err = "프로그램 에러";
                        break;
                    case LocationCheckService.E_GPSOFF:
                        err = "GPS 비활성화";
                        break;
                    case LocationCheckService.E_GPSSCANFAIL:
                        err = "GPS 위치정보 획득 실패";
                        break;
                    case LocationCheckService.E_WIFIOFF:
                        err = "WIFI 비활성화";
                        break;
                    default:
                        Log.e(TAG, "확인되지 않은 에러코드 발생");
                }

                mMsgBox.setText(result + "\n" + err);
            }


        } else if (view == mCheckButton) {
            DMInfo regData = new DMInfo();

            if(mRegDMInfo == null){
                mMsgBox.setText("등록 버튼을 누른 후 검사 버튼을 눌러 주세요");
                return;
            }
            mRegDMInfo.setLatitude(0.0);
            mRegDMInfo.setLongitude(0.0);

            DMInfo dmInfo = mService.verifyDM(mRegDMInfo);

            String result="";
            if(dmInfo.getResult() == LocationCheckService.SUCCESS){
                 result = "점검 성공";
            }else{
                 result = "점검 실패";
            }

            String result_gps="";
            if(dmInfo.getResult_GPS() == LocationCheckService.SUCCESS){
                 result_gps = "GPS 결과 성공";
            }else{
                 result_gps = "GPS 결과 실패";
            }
            String result_dm ="";
            if(dmInfo.getResult_DM() == LocationCheckService.SUCCESS){
                 result_dm = "DM 결과 성공";
            }else{
                 result_dm = "DM 결과 실패";
            }

            String dmData = "DM Data :" + dmInfo.getDmData();
            String lat = "위도 :" + String.valueOf(dmInfo.getLatitude());
            String lng = "경도 :" + String.valueOf(dmInfo.getLongitude());
            String distance = "간격 :" + String.valueOf(dmInfo.getDistance());

            mMsgBox.setText(mRegMsg + "\n\n" + result + ", " + result_gps + ", " + result_dm + "\n" + lat + ", " + lng + ", " + distance +  "\n" + dmData );

        } else if (view == mClearButton) {
         //   mRegDMInfo = null;
            mMsgBox.setText(mRegMsg);
        }
    }

    private void setButtonsState(boolean enable) {
        mRegButton.setEnabled(enable);
        mCheckButton.setEnabled(enable);
        mClearButton.setEnabled(enable);
    }
}
