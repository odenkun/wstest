package com.example.android.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

enum MicMode {
    BLUETOOTH,
    BUILT_IN
}

public class MainActivity extends Activity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
                           VoiceRecorder.Callback,
                           BTListener.Callback{
    private static final String TAG = "MainActivity";
    
    private static final int REQUEST_CODE = 1;
    
    private static final String[] PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.RECORD_AUDIO
    };
    
    private VoiceRecorder mVoiceRecorder;
    private VoiceTransmitter mTransmitter;
    private MicMode mMicMode = MicMode.BUILT_IN;
    private BTListener mBTListener ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        String[] notGranted = getNotGrantedPermissions();
        if (notGranted.length > 0) {
            ActivityCompat.requestPermissions(this, notGranted, REQUEST_CODE);
        } else {
            mTransmitter = new VoiceTransmitter();
            switch (mMicMode) {
                case BUILT_IN:
                    startVoiceRecord();
                    break;
                case BLUETOOTH:
                    mBTListener = new BTListener(this, this);
                    break;
                default:
                    Log.e(TAG, "MicMode is not assigned.");
            }
        }
    }
    
    private String[] getNotGrantedPermissions() {
        ArrayList<String> notGranted = new ArrayList<>();
        for (String permission: PERMISSIONS) {
            int permissionState = ContextCompat.checkSelfPermission(this, permission);
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permission);
            }
        }
        return notGranted.toArray(new String[]{});
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                
                } else {
                    // パーミッションが必要な処理
                    
                }
            }
        }
    }
    
    void startVoiceRecord() {
        mVoiceRecorder = new VoiceRecorder(this);
        mVoiceRecorder.start();
    }
    
    @Override
    public void onVoiceStart() {
        mTransmitter.run();
    }
    
    @Override
    public void onVoice(byte[] data, int size) {
        mTransmitter.send(data);
    }
    
    @Override
    public void onVoiceEnd() {
        mTransmitter.close();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
        if (mTransmitter != null) {
            mTransmitter.close();
            mTransmitter = null;
        }
        if (mBTListener != null) {
            mBTListener.stop();
            mBTListener = null;
        }
    }
    
    @Override
    public void onBTConnected() {
        startVoiceRecord();
    }
}