package com.example.pictionary;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class JoinActivity extends Activity {

    private static final String TAG = "ClientActivity";

    //TODO: define variable somewheere as  as GUESSING = 1 , DRAWING  = 0
    public static int status =1;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private SparseArray<BluetoothDevice> mDevices;

    private BluetoothGatt mConnectedGatt;

    private Handler mHandler = new Handler();

    /* Client UI elements */
    private TextView mLatestValueX;
    private TextView mLatestValueY;

    private TextView textView;
    private EditText answerBox;
    private View touchView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mLatestValueX = (TextView) findViewById(R.id.x_value);
        mLatestValueY = (TextView) findViewById(R.id.y_value);

        textView=(TextView)findViewById(R.id.draw_coordinates);
        touchView=findViewById(R.id.touch_button);

        /*
         * Bluetooth in Android 4.3+ is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mDevices = new SparseArray<BluetoothDevice>();

        answerBox = (EditText) findViewById(R.id.edit_answer);

        setButtonSend();

        //TODO: change status thing
        if(status==1){
            touchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float x = event.getX();
                    float y = event.getY();


                    textView.setText("you touched: " + String.valueOf(x)
                            + 'x' + String.valueOf(y));
                    //return true to consume Event from buffer so it allows continous callbacks

                    if (mConnectedGatt != null) {
                        float [] array = {x,y};

                        Log.i(TAG, "array is : " +array[0] +array[1] );
                        BluetoothGattCharacteristic xyCharacteristic = mConnectedGatt
                                .getService(DeviceProfile.SERVICE_UUID)
                                .getCharacteristic(DeviceProfile.CHARACTERISTIC_XY_UUID);

                            xyCharacteristic.setValue(DeviceProfile.bytesFromArray(array));

                            mConnectedGatt.writeCharacteristic(xyCharacteristic);

                    }

                    //Log.i(TAG, "Just send x coord: " + x+ "Just send y coord: " + y);

                    return true;
                }
            });

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
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
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG, "Activity on Pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Activity stopped");

        /*
        //Stop any active scans
        stopScan();
        //Disconnect from any active connection
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }
        */
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //Stop any active scans
        stopScan();
        //Disconnect from any active connection
        if (mConnectedGatt != null) {
            mConnectedGatt.disconnect();
            mConnectedGatt = null;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_join, menu);
        //Add any device elements we've discovered to the overflow menu
        for (int i=0; i < mDevices.size(); i++) {
            BluetoothDevice device = mDevices.valueAt(i);
            menu.add(0, mDevices.keyAt(i), 0, device.getName());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                mDevices.clear();
                startScan();
                return true;
            default:
                //Obtain the discovered device to connect with
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to " + device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events
                 */
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Begin a scan for new servers that advertise our
     * matching service.
     */
    private void startScan() {
        //Scan for devices advertising our custom service
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(DeviceProfile.SERVICE_UUID))
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(scanFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();
        mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, mScanCallback);
    }

    /*
     * Terminate any active scans
     */
    private void stopScan() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
    }

    /*
     * Callback handles results from new devices that appear
     * during a scan. Batch results appear when scan delay
     * filters are enabled.
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult");
            //Prints name of devices
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults: "+results.size()+" results");

            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "LE Scan Failed: " + errorCode);
        }

        private void processResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.i(TAG, "New LE Device: " + device.getName() + " @ " + result.getRssi());
            //Add it to the collection
            mDevices.put(device.hashCode(), device);
            //Update the overflow menu
            invalidateOptionsMenu();

            stopScan();
        }
    };

    /*
     * Callback handles GATT client events, such as results from
     * reading or writing a characteristic value on the server.
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange "
                    +DeviceProfile.getStatusDescription(status)+" "
                    +DeviceProfile.getStateDescription(newState));

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered:");

            for (BluetoothGattService service : gatt.getServices()) {
                Log.d(TAG, "Service: "+service.getUuid());

                if (DeviceProfile.SERVICE_UUID.equals(service.getUuid())) {
                    List<BluetoothGattCharacteristic> characteristicList =
                            gatt.getService(DeviceProfile.SERVICE_UUID).getCharacteristics();
                    BluetoothGattCharacteristic characteristic;

                    /*
                    for (BluetoothGattCharacteristic charact : characteristicList){
                        Log.d(TAG, "charact " + charact.getUuid());
                    }
                    */

                    characteristic = service.getCharacteristic(DeviceProfile.CHARACTERISTIC_COORD_X_UUID);
                    gatt.readCharacteristic(characteristic);
                    Log.i(TAG, "successfully initialized x Coord characteristic");
                    //Register for further updates as notifications
                    if(gatt.setCharacteristicNotification(characteristic, true))
                        Log.i(TAG, "successfully set notifications for x coord");

                    characteristic = service.getCharacteristic(DeviceProfile.CHARACTERISTIC_COORD_Y_UUID);
                    gatt.readCharacteristic(characteristic);
                    Log.i(TAG, "successfully initialized y Coord characteristic");
                    //Register for further updates as notifications
                    if(gatt.setCharacteristicNotification(characteristic, true));
                        Log.i(TAG, "successfully set notifications for y coord");
                    }

                }

        }

        //Callback reporting the result of a readCharacterstic operation
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            final int charValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);

            //switch/ case

            if (DeviceProfile.CHARACTERISTIC_COORD_X_UUID.equals(characteristic.getUuid())) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLatestValueX.setText(String.valueOf(charValue));
                    }
                });
            }

            if (DeviceProfile.CHARACTERISTIC_COORD_Y_UUID.equals(characteristic.getUuid())) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLatestValueY.setText(String.valueOf(charValue));
                    }
                });

                //Register for further updates as notifications
                //if(gatt.setCharacteristicNotification(characteristic, true))
                   // Log.i(TAG, "successfully set notifications for y coord");
            }


            if (DeviceProfile.CHARACTERISTIC_WORD_UUID.equals(characteristic.getUuid())) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //updateDateText((long)charValue * 1000);
                        //updateDateText((long)charValue * 1000);
                    }
                });
            }

        }

        //this callback is triggered by a notification from the remote device
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final int charValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);

            //TODO: change charac only when necessary

            if (DeviceProfile.CHARACTERISTIC_COORD_X_UUID.equals(characteristic.getUuid())) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLatestValueX.setText(String.valueOf(charValue));
                    }
                });
            }
            else if (DeviceProfile.CHARACTERISTIC_COORD_Y_UUID.equals(characteristic.getUuid())) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLatestValueY.setText(String.valueOf(charValue));
                    }
                });
            }
        }
    };

    public void setButtonSend(){
        final Button buttonStart = (Button) findViewById(R.id.button_send);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mConnectedGatt != null) {
                    String myWord = answerBox.getText().toString();

                    byte[] value = DeviceProfile.bytesFromString(myWord);

                    BluetoothGattCharacteristic wordCharacteristic = mConnectedGatt
                            .getService(DeviceProfile.SERVICE_UUID)
                            .getCharacteristic(DeviceProfile.CHARACTERISTIC_WORD_UUID);
                    wordCharacteristic.setValue(value);

                    mConnectedGatt.writeCharacteristic(wordCharacteristic);
                }

            }
        });
    }


}
