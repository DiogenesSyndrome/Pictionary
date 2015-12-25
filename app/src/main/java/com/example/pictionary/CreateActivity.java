package com.example.pictionary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CreateActivity extends Activity {

    private static final String TAG = "CreateActivity";

    private TextView textView;
    private View touchView;

    private float xCoord;
    private float yCoord;

    //construct with specific context
    private BLESingleton mBLE = BLESingleton.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create);

        ListView list = (ListView)findViewById(R.id.list);
        //setContentView(list);

        if(!mBLE.init()){
            Log.e(TAG, "error in BLE initialization");
            Toast.makeText(this, "Couldn't open BLE!.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        list.setAdapter(mBLE.mConnectedDevicesAdapter);

        //display touchpad coordinates
        textView = (TextView)findViewById(R.id.coordinates);
        //textView.setText("you touched :");
        touchView=findViewById(R.id.draw);

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
