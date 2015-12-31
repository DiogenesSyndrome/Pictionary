package com.example.pictionary;

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
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Lucian on 12/15/2015.
 */

/**
 * DEV COMMENTS :
 * Long debates on whether the class should extend ContextWrapper (to have access to a class' context)
 * or should describe a interface to be implemented in each Activity.
 * This is a singleton (unique static instantiation) and not a service, bc BLE is not needed in the
 * background and it is easier to implement : )
 *
 */
public class BLESingleton extends ContextWrapper{

    private static final String TAG = "BLESingleton";
    private static BLESingleton mInstance = null;

    //TODO: CHANGE THIS URGENTLY
    public float x;
    public float y;
    public float xCoord;
    public String mWord;


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;

    private ArrayList<BluetoothDevice> mConnectedDevices;
    protected ArrayAdapter<BluetoothDevice> mConnectedDevicesAdapter;


    private BLESingleton(Context base) {
        super(base);
    }

    public static BLESingleton getInstance(Context base) {
        // Context can be instantiated only once
        if(mInstance==null){
            mInstance = new BLESingleton(base);
        }
        return mInstance;
    }

    public static BLESingleton getInstance(){
        if(mInstance==null)
            Log.i(TAG,"trying to getInstance from non-constructed class");
        return mInstance;
    }

    public boolean init(){
        mConnectedDevices = new ArrayList<BluetoothDevice>();

        // the Adapter will transform an array of objects (devices) in  Views with correspoding
        // item layout and a toString() conversion
        mConnectedDevicesAdapter = new ArrayAdapter<BluetoothDevice>(this,
                android.R.layout.simple_list_item_1, mConnectedDevices);

        /*
         * Bluetooth in Android 4.3+ is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return false;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            return false;
        }

        /*
         * Check for advertising support. Not all devices are enabled to advertise
         * Bluetooth LE data.
         */
        if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Toast.makeText(this, "No Advertising Support.", Toast.LENGTH_SHORT).show();
            return false;
        }

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);

        initServer();
        startAdvertising();

        Toast.makeText(this, "BLE Advertising!.", Toast.LENGTH_SHORT).show();
        return true;
    }

    /*
   * Create the GATT server instance, attaching all services and
   * characteristics that should be exposed
   */

    public void initServer() {
        BluetoothGattService service =new BluetoothGattService(DeviceProfile.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic elapsedCharacteristic =
                new BluetoothGattCharacteristic(DeviceProfile.CHARACTERISTIC_COORD_X_UUID,
                        //Read-only characteristic, supports notifications
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattCharacteristic offsetCharacteristic =
                new BluetoothGattCharacteristic(DeviceProfile.CHARACTERISTIC_WORD_UUID,
                        //Read+write permissions
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(elapsedCharacteristic);
        service.addCharacteristic(offsetCharacteristic);

        mGattServer.addService(service);
    }

    /*
     * Callback handles all incoming requests from GATT clients.
     * From connections to read/write requests.
     */
    public BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback()     {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);

            Log.i(TAG, "onConnectionStateChange "
                    +DeviceProfile.getStatusDescription(status)+" "
                    +DeviceProfile.getStateDescription(newState));


            if (newState == BluetoothProfile.STATE_CONNECTED) {
                postDeviceChange(device, true);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                postDeviceChange(device, false);
            }
        }

        //callback called when remote client requests to read a characteristic
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device,
                                                int requestId,
                                                int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            //Log.i(TAG, "onCharacteristicReadRequest " + characteristic.getUuid().toString());

            if (DeviceProfile.CHARACTERISTIC_COORD_X_UUID.equals(characteristic.getUuid())) {
                mGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        DeviceProfile.bytesFromInt(xCoord));
            }

            if (DeviceProfile.CHARACTERISTIC_WORD_UUID.equals(characteristic.getUuid())) {
                mGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        DeviceProfile.bytesFromString(mWord));
            }


            /*
             * Unless the characteristic supports WRITE_NO_RESPONSE,
             * always send a response back for any request.
             */
            mGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    null);
        }

        //callback triggered by a call to characateristic.write() method from the client
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite,
                                                 boolean responseNeeded,
                                                 int offset,
                                                 byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            //Log.i(TAG, "onCharacteristicWriteRequest "+characteristic.getUuid().toString());

            if (DeviceProfile.CHARACTERISTIC_WORD_UUID.equals(characteristic.getUuid())) {
                //int stringInBytes = DeviceProfile.unsignedIntFromBytes(value);
                mWord = DeviceProfile.stringFromBytes(value);
                //setStoredValue(newOffset);

                //resend data to confirm correct reception
                if (responseNeeded) {
                    mGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            value);
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BLESingleton.this, "Word Sent: "+ mWord, Toast.LENGTH_SHORT).show();
                        if(Dictionary.checkDictionary(mWord)==true)
                            Toast.makeText(BLESingleton.this, "COrrect", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(BLESingleton.this, "FALSE", Toast.LENGTH_SHORT).show();
                    }
                });

                notifyConnectedDevices();
            }
        }
    };


    /*
    * Terminate the server and any running callbacks
    */
    public void shutdownServer() {
        mHandler.removeCallbacks(mNotifyRunnable);

        if (mGattServer == null) return;

        mGattServer.close();
    }

    //separate thread for notifications
    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            notifyConnectedDevices();
            mHandler.postDelayed(this, 50);
        }
    };

    /*
    * Initialize the advertiser
    */
    public void startAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(DeviceProfile.SERVICE_UUID))
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }

    /*
    * Terminate the advertiser
    */
    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        Log.i(TAG, "Peripheral Advertise Stopped.");

    }

    /*
    * Callback handles events from the framework describing
    * if we were successful in starting the advertisement requests.
    */
    public AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "Peripheral Advertise Started.");
            //postStatusMessage("GATT Server Ready");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "Peripheral Advertise Failed: "+errorCode);
            //postStatusMessage("GATT Server Error "+errorCode);
        }
    };

    public Handler mHandler = new Handler();
    /*
    private void postStatusMessage(final String message) {
        mBLE.mHandler.post(new Runnable() {
            @Override
            public void run() {
                setTitle(message);
            }
        });
    }
    */


    private void postDeviceChange(final BluetoothDevice device, final boolean toAdd) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //This will add the item to our list and update the adapter at the same time.
                if (toAdd) {
                    mConnectedDevicesAdapter.add(device);
                } else {
                    mConnectedDevicesAdapter.remove(device);
                }

                //Trigger our periodic notification once devices are connected
                mHandler.removeCallbacks(mNotifyRunnable);
                if (!mConnectedDevices.isEmpty()) {
                    mHandler.post(mNotifyRunnable);
                }
            }
        });
    }

    /* Storage and access to local characteristic data */
    //notify remote device such that callbacks can be called
    //this will run on a separate thread
    public void notifyConnectedDevices() {
        for (BluetoothDevice device : mConnectedDevices) {
            BluetoothGattCharacteristic readCharacteristic = mGattServer.getService(DeviceProfile.SERVICE_UUID)
                    .getCharacteristic(DeviceProfile.CHARACTERISTIC_COORD_X_UUID);

            readCharacteristic.setValue(DeviceProfile.bytesFromInt(x));

            mGattServer.notifyCharacteristicChanged(device, readCharacteristic, false);
        }
    }

    private Object mLock = new Object();

    private int mTimeOffset;

    private byte[] getStoredValue() {
        synchronized (mLock) {
            return DeviceProfile.getShiftedTimeValue(mTimeOffset);
        }
    }

    private void setStoredValue(int newOffset) {
        synchronized (mLock) {
            mTimeOffset = newOffset;
        }
    }





}
