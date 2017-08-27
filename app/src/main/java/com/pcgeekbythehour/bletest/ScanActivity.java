package com.pcgeekbythehour.bletest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ScanActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000l;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private int mNearestStrength ;
    private byte[] mNearestRecord;

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] advertisement) {
            if(mNearestStrength == 0 || mNearestStrength < rssi){
                mNearestStrength = rssi;
                mNearestRecord = advertisement;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        if(savedInstanceState != null /*&& savedInstanceState.containsKey("mScanning")*/){
            mScanning = savedInstanceState.getBoolean("mScanning");
            mNearestStrength = savedInstanceState.getInt("mNearestStrength");
            mNearestRecord = savedInstanceState.getByteArray("mNearestRecord");
            mHandler = (Handler)savedInstanceState.getParcelable("mHandler");
        }else{
            mScanning = false;
            mNearestStrength = 0;
            mNearestRecord = new byte[]{0};
            mHandler = new Handler();
        }

        updateScanningStatus();
        updateScanResult();

        findViewById(R.id.update_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mScanCallback);
                        updateScanningStatus();
                        updateScanResult();
                    }
                },SCAN_PERIOD);

                mScanning = true;
                updateScanningStatus();
                mNearestStrength = 0;
                mNearestRecord = new byte[]{0};
                mBluetoothAdapter.startLeScan(mScanCallback);
            }
        });

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean("mScanning",mScanning);
        savedInstanceState.putInt("mNearestStrength",mNearestStrength);
        savedInstanceState.putByteArray("mNearestRecord",mNearestRecord);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateScanningStatus(){
        if(mScanning){
            ((TextView)findViewById(R.id.scanning_status)).setText(R.string.status_scanning);
        }else{
            ((TextView)findViewById(R.id.scanning_status)).setText(R.string.status_not_scanning);
        }
    }

    private void updateScanResult(){
        if(mNearestStrength == 0){
            ((TextView)findViewById(R.id.nearest_advertisement)).setText("None");
        }else{
            StringBuilder builder = new StringBuilder();
            for(byte hh : mNearestRecord){
                builder.append(String.format("%02x",hh));
            }
            ((TextView)findViewById(R.id.nearest_advertisement)).setText(builder.toString());
        }
    }
}
