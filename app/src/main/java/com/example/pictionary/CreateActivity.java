package com.example.pictionary;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CreateActivity extends Activity {

    private static final String TAG = "CreateActivity";

    private float xCoord;
    private float yCoord;

    private BLESingleton mBLE = BLESingleton.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create);

        ListView list = (ListView)findViewById(R.id.list);
        //setContentView(list);
        mBLE.init();


        // the Adapter will transform an array of objects (devices) in  Views with correspoding
        // item layout and a toString() conversion
        mBLE.mConnectedDevicesAdapter = new ArrayAdapter<BluetoothDevice>(this,
                android.R.layout.simple_list_item_1, mBLE.mConnectedDevices);

        list.setAdapter(mBLE.mConnectedDevicesAdapter);

        /*
         * Bluetooth in Android 4.3+ is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        mBLE.mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBLE.mBluetoothAdapter = mBLE.mBluetoothManager.getAdapter();

        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBLE.mBluetoothAdapter == null || !mBLE.mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*
         * Check for advertising support. Not all devices are enabled to advertise
         * Bluetooth LE data.
         */
        if (!mBLE.mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Toast.makeText(this, "No Advertising Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBLE.mBluetoothLeAdvertiser = mBLE.mBluetoothAdapter.getBluetoothLeAdvertiser();
        mBLE.mGattServer = mBLE.mBluetoothManager.openGattServer(this, mBLE.mGattServerCallback);

        mBLE.initServer();
        mBLE.startAdvertising();


        //display touchpad coordinates
        final TextView textView = (TextView)findViewById(R.id.coordinates);
        //textView.setText("you touched :");
        final View touchView=findViewById(R.id.draw);


        //define here a new method of View, which extends/implements the onTouchListener interface
        //OnTouchListener calls back onTouch on each MotionEvent
        touchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    xCoord= event.getX();
                    yCoord= event.getY();

                    textView.setText("you touched: " + String.valueOf(xCoord)
                            + 'x' + String.valueOf(yCoord));
                    //return true to consume Event from buffer so it allows continous callbacks

                    mBLE.x = xCoord;
                    mBLE.y = yCoord;
                    return true;
                }
        });

        //start game
        setButtonStartGame();

        //CustomModel.getInstance().setListener(this);

        boolean modelState = CustomModel.getInstance().getState();
        Log.d(TAG, "Current state: " + String.valueOf(modelState));

    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        //mBLE.stopAdvertising();
       // mBLE.shutdownServer();
    }


    public void setButtonStartGame(){
        Button buttonStart = (Button) findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DrawActivity.class);
                startActivity(intent);
            }
        });
    }

    /*
    @Override
    public void stateChanged() {

        boolean modelState = CustomModel.getInstance().getState();
        Log.d(TAG, "MainActivity says: Model state changed: " +
                String.valueOf(modelState));

        xCoord=CustomModel.getInstance().getValueX();
        yCoord=CustomModel.getInstance().getValueY();

        mBLE.x = xCoord;
        mBLE.y = yCoord;
        mBLE.notifyConnectedDevices();
    }*/


}
