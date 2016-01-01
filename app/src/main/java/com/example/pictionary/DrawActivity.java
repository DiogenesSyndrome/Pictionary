package com.example.pictionary;

import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import android.os.Handler;
import android.widget.Toast;

public class DrawActivity extends Activity implements BLESingleton.onWordListener {

    private static final String TAG = "DrawActivity";

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

        // do not forget to reference THIS for listener initialization
        mBLE.setWordListener(this);

        //define here a new method of View, which extends/implements the onTouchListener interface
        //OnTouchListener calls back onTouch on each MotionEvent
        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                textView.setText("you touched: " + String.valueOf(x)
                        + 'x' + String.valueOf(y));
                //return true to consume Event from buffer so it allows continous callbacks
                //CustomModel.getInstance().changeState(xCoord, yCoord);
                mBLE.setCoordinates(x, y);

                return true;
            }
        });

        //mLatestWord=String.valueOf(mBLE.mWord);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBLE.stopAdvertising();
        mBLE.shutdownServer();
        Toast.makeText(this, "shutdownServer.", Toast.LENGTH_SHORT).show();

    }

    //interface implementation
    @Override
    public void wordReceived(){

        final String word = mBLE.getWord();
        Log.i(TAG, "word received : " + word);

        //cannot manipulate anything directly in the UI without being in onCreate, so use Runnable
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wordView.setText(word);
                if (Dictionary.checkDictionary(word)==true)
                    Toast.makeText(DrawActivity.this, "PLAYER X WINS", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(DrawActivity.this, "guess attempt", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
