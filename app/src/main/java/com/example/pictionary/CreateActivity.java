package com.example.pictionary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.HashSet;

public class CreateActivity extends Activity {

    private static final String TAG = "CreateActivity";

    private TextView textView;
    private View touchView;
    //construct with specific context
    private BLESingleton mBLE = BLESingleton.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create);

        ListView list = (ListView)findViewById(R.id.list);

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
        /*
        touchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //mBLE.xCoord= event.getX();
                    //mBLE.yCoord= event.getY();
                    mBLE.setCoordinates(event.getX(),event.getY());
                    //textView.setText("you touched: " + String.valueOf(xCoord)
                      //      + 'x' + String.valueOf(yCoord));
                    //return true to consume Event from buffer so it allows continous callbacks

                    return true;
                }
        });
        */

        //start game
        setButtonStartGame();
        setButtonExit();

        //create database
        int size = Dictionary.addDictionary();
        Log.i(TAG, "Dictionary loaded with " + size + " words");

    }


    @Override
    protected void onResume() {
        super.onResume();

        //reconnect to previous ?
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mBLE.stopAdvertising();
       // mBLE.shutdownServer();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mBLE.stopAdvertising();
        mBLE.shutdownServer();
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

    public void setButtonExit(){
        Button buttonStart = (Button) findViewById(R.id.button_exit);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //call onDestroy
                finish();
            }
        });
    }




}
