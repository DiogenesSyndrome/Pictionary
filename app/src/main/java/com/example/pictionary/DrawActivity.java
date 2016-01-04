package com.example.pictionary;

import android.app.Activity;

import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Handler;
import android.widget.Toast;

public class DrawActivity extends Activity implements BLESingleton.onWordListener {

    private static final String TAG = "DrawActivity";

    static String BUNDLE_PIXEL_COLOR_LIST = "BUNDLE_PIXEL_COLOR_LIST";
    static String BUNDLE_SELECTED_COLOR = "BUNDLE_SELECTED_COLOR";
    public static final int NBR_PIXEL = 20;
    public static final int BITMAP_RESOLUTION = 500;
    public double averagePixelSize = 0.;
    Integer[][] pixel_id_list = new Integer[NBR_PIXEL][NBR_PIXEL];
    Integer[][] pixel_color_list = new Integer[NBR_PIXEL][NBR_PIXEL];
    public static final int WHITE = 1;
    public static final int BLACK = 2;
    public static final int RED = 3;
    public static final int BLUE = 4;

    private SquLayout sl;
    private int sl_coord[] = new int[2];


    //private CanvasView canvasView= new CanvasView(this);
    private CanvasView canvasView;


    private Handler mHandler = new Handler();
    private int mInterval = 500;
    private int chrono_value=0;
    public static final int PIXEL_DRAW = 0;
    int selected_color = 1;


    //--------------------------------------------------------------------------
    private BLESingleton mBLE= BLESingleton.getInstance();
    private TextView wordView;

    public TextView mClientCoord;

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
        //canvasView.setCoordinateListener(this);


        //define here a new method of View, which extends/implements the onTouchListener interface
        //OnTouchListener calls back onTouch on each MotionEvent
        /*
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

        */

        //--------------------------------------------------------------------------------------
        // Init view reference and position
        sl = (SquLayout) findViewById(R.id.main_square);

        selected_color = BLACK;

        if (PIXEL_DRAW==0){

            define_canvas_layout();

            //define_canvas_listener();


        }


        define_select_color();

        // CHRONOMETER
        //mHandler = new Handler();
        startRepeatingTask();
    }

    /*
    @Override
    public void onBackPressed() {
    new AlertDialog.Builder(this)
        .setTitle("Really Exit?")
        .setMessage("Are you sure you want to exit?")
        .setNegativeButton(android.R.string.no, null)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                DrawActivity.super.onBackPressed();
                mBLE.stopAdvertising();
                mBLE.shutdownServer();
                Log.i(TAG, "Activity Stopped by Back button");
            }
        }).create().show();
    }

*/

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Activity on Pause");

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mBLE.stopAdvertising();
        mBLE.shutdownServer();
        Log.i(TAG, "Activity Stopped");
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
                if (Dictionary.checkDictionary(word) == true)
                    Toast.makeText(DrawActivity.this, "PLAYER X WINS", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(DrawActivity.this, "guess attempt", Toast.LENGTH_SHORT).show();
            }
        });

    }


    /*
    @Override
    public void screenTouched(){
        float[] array = canvasView.getTouchEventCoord();
        mBLE.setCoordinates(array[0], array[1]);
        Log.i(TAG, "coordinated changed : " + array[0] + array[1]);
    }
    */


    //TODO: for visibility, put this in a class please
    //-------------------------------------------------------------------------------------------
    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        sl.getLocationOnScreen(sl_coord);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        for (int i=0;i<NBR_PIXEL;i++)
            for (int j=0;j<NBR_PIXEL;j++) {
                savedInstanceState.putInt(BUNDLE_PIXEL_COLOR_LIST + "_" + i + "_" + j, pixel_color_list[i][j]);
                savedInstanceState.putInt(BUNDLE_SELECTED_COLOR,selected_color);
            }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }



    private void define_canvas_layout(){
        canvasView= new CanvasView(this);
        canvasView.setPaintColor(getSelectedColor(selected_color));
        ViewGroup.LayoutParams viewParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        canvasView.setLayoutParams(viewParams);
        sl.addView(canvasView);
    }


    private void define_select_color(){

        // CREATE COLOR CHOICE onClick() ----------------------------------------------------------

        TextView white_panel = (TextView) findViewById(R.id.palette_white);
        TextView black_panel = (TextView) findViewById(R.id.palette_black);
        TextView red_panel = (TextView) findViewById(R.id.palette_red);
        TextView blue_panel = (TextView) findViewById(R.id.palette_blue);

        white_panel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                selected_color=WHITE;
                canvasView.setPaintColor(getSelectedColor(selected_color));
            }
        });
        black_panel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                selected_color=BLACK;
                canvasView.setPaintColor(getSelectedColor(selected_color));
            }
        });
        red_panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_color = RED;
                canvasView.setPaintColor(getSelectedColor(selected_color));
            }
        });
        blue_panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_color = BLUE;
                canvasView.setPaintColor(getSelectedColor(selected_color));
            }
        });



    }

    private int getSelectedColor(int selected_color){

        switch(selected_color){
            case WHITE:
                return ContextCompat.getColor(getApplicationContext(), R.color.white);
            case BLACK:
                return ContextCompat.getColor(getApplicationContext(), R.color.black);
            case RED:
                return ContextCompat.getColor(getApplicationContext(), R.color.red);
            case BLUE:
                return ContextCompat.getColor(getApplicationContext(), R.color.blue);
        }
        return 0;

    }


    private void startRepeatingTask(){
        mStatusChecker.run();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            update_chrono();
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void update_chrono(){

        chrono_value+=1;

        chrono_value = mod(chrono_value,57);

        ImageView iv = (ImageView) findViewById(R.id.chrono);

        switch(chrono_value){
            case 0:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_00));
                break;
            case 1:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_01));
                break;
            case 2:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_02));
                break;
            case 3:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_03));
                break;
            case 4:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_04));
                break;
            case 5:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_05));
                break;
            case 6:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_06));
                break;
            case 7:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_07));
                break;
            case 8:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_08));
                break;
            case 9:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_09));
                break;
            case 10:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_10));
                break;
            case 11:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_11));
                break;
            case 12:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_12));
                break;
            case 13:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_13));
                break;
            case 14:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_14));
                break;
            case 15:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_15));
                break;
            case 16:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_16));
                break;
            case 17:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_17));
                break;
            case 18:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_18));
                break;
            case 19:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_19));
                break;
            case 20:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_20));
                break;
            case 21:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_21));
                break;
            case 22:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_22));
                break;
            case 23:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_23));
                break;
            case 24:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_24));
                break;
            case 25:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_25));
                break;
            case 26:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_26));
                break;
            case 27:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_27));
                break;
            case 28:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_28));
                break;
            case 29:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_29));
                break;
            case 30:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_30));
                break;
            case 31:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_31));
                break;
            case 32:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_32));
                break;
            case 33:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_33));
                break;
            case 34:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_34));
                break;
            case 35:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_35));
                break;
            case 36:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_36));
                break;
            case 37:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_37));
                break;
            case 38:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_38));
                break;
            case 39:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_39));
                break;
            case 40:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_40));
                break;
            case 41:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_41));
                break;
            case 42:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_42));
                break;
            case 43:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_43));
                break;
            case 44:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_44));
                break;
            case 45:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_45));
                break;
            case 46:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_46));
                break;
            case 47:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_47));
                break;
            case 48:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_48));
                break;
            case 49:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_49));
                break;
            case 50:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_50));
                break;
            case 51:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_51));
                break;
            case 52:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_52));
                break;
            case 53:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_53));
                break;
            case 54:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_54));
                break;
            case 55:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_55));
                break;
            case 56:
                iv.setImageDrawable(getDrawable(R.drawable.chrono_56));
                break;
        }

    }

    private int mod(int x, int y)
    {
        int result = x % y;
        if (result < 0)
            result += y;
        return result;
    }

    private int max(int x,int y){
        if (x >= y)
            return x;
        else
            return y;
    }

    private int min(int x,int y){
        if (x <= y)
            return x;
        else
            return y;
    }
}
