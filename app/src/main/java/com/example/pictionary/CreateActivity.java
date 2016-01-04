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

        //show list of all remote connected devices
        //TODO: show names instead of horrible MAC addresses
        list.setAdapter(mBLE.mConnectedDevicesAdapter);

        //display touchpad coordinates
        textView = (TextView)findViewById(R.id.coordinates);
        //textView.setText("you touched :");
        touchView=findViewById(R.id.draw);

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

        //automatically restart server if it was killed

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

        //java Garbage collector takes care
        mBLE = null;

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
