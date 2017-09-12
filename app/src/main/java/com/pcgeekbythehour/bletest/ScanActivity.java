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

import java.util.Arrays;

public class ScanActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 1500l;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private int mNearestStrength ;
    private byte[] mNearestRecord;

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] advertisement) {
            final byte[] iBeacon_prefix = {0x02, 0x01, 0x06, 0x1a, -1, 0x4c,0x00, 0x02, 0x15};
            final byte[] beaconatorID = {'B', 'E', 'A', 'C', 'O', 'N', 'A', 'T', 'O', 'R'};

            if(Arrays.equals(Arrays.copyOfRange(advertisement,0,9),iBeacon_prefix) &&
                    Arrays.equals(Arrays.copyOfRange(advertisement,9,19),beaconatorID) &&
                            (mNearestStrength == 0 || mNearestStrength < rssi)){
                mNearestStrength = rssi;
                mNearestRecord = advertisement;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mScanning = false;
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();

        if(savedInstanceState != null /*&& savedInstanceState.containsKey("mScanning")*/){
            mNearestStrength = savedInstanceState.getInt("mNearestStrength");
            mNearestRecord = savedInstanceState.getByteArray("mNearestRecord");
        }else{
            mNearestStrength = 0;
            mNearestRecord = new byte[]{0};
        }

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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
                if(! mBluetoothAdapter.startLeScan(mScanCallback)){
                    ((TextView)findViewById(R.id.nearest_advertisement)).setText(R.string.ble_scan_error);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
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
