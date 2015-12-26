package com.example.pictionary;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import android.os.Handler;
import android.widget.Toast;

public class DrawActivity extends Activity {

    private static final String TAG = "DrawActivity";
    private float xCoord;
    private float yCoord;
    private BLESingleton mBLE= BLESingleton.getInstance();

    private String mLatestWord ="no guess word for now";
    private TextView wordView;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        //display touchpad coordinates
        final TextView textView = (TextView)findViewById(R.id.coordinatesDraw);
        //textView.setText("you touched :");
        final View touchView=findViewById(R.id.drawDraw);

        wordView= (TextView) findViewById(R.id.word);

        //CustomModel.getInstance().changeState(true);


        //define here a new method of View, which extends/implements the onTouchListener interface
        //OnTouchListener calls back onTouch on each MotionEvent
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                xCoord = event.getX();
                yCoord = event.getY();

                textView.setText("you touched: " + String.valueOf(xCoord)
                        + 'x' + String.valueOf(yCoord));
                //return true to consume Event from buffer so it allows continous callbacks
                //CustomModel.getInstance().changeState(xCoord, yCoord);
                mBLE.x = xCoord;
                return true;
            }
        });

        //mLatestWord=String.valueOf(mBLE.mWord);
        wordView.setText(mLatestWord);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBLE.stopAdvertising();
        mBLE.shutdownServer();
        Toast.makeText(this, "shutdownServer.", Toast.LENGTH_SHORT).show();

    }


}
