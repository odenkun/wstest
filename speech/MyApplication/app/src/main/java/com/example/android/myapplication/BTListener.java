package com.example.android.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import java.util.List;

public class BTListener implements BluetoothProfile.ServiceListener {
    private BluetoothHeadset mBluetoothHeadset;
    private Callback callback;
    
    public BTListener(Callback callback,Activity activity) {
        this.callback = callback;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.getProfileProxy(activity, this, BluetoothProfile.HEADSET);
    }
    
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (profile != BluetoothProfile.HEADSET) {
            return;
        }
        mBluetoothHeadset = (BluetoothHeadset) proxy;
        List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
        for (BluetoothDevice device : devices) {
            if (mBluetoothHeadset.startVoiceRecognition(device)) {
                callback.onBTConnected();
                break;
            }
        }
        
    }
    
    public void onServiceDisconnected(int profile) {
    }
    
    void stop() {
        callback = null;
        if (mBluetoothHeadset == null) return;
        
        List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
        
        if (devices == null) return;
        
        for (BluetoothDevice device : devices) {
            mBluetoothHeadset.stopVoiceRecognition(device);
        }
    }
    
    interface Callback{
        void onBTConnected();
    }
}
