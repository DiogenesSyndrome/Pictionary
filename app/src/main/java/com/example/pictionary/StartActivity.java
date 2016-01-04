package com.example.pictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class StartActivity extends Activity {

    public final String TAG ="StartActivity";

    //creating new branch
    Context context = this;
    String username;

    final String USERNAME_INTENT = "username_intent";

    BroadcastReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Open the preference file
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        username = sharedPref.getString("pseudonym",null);
        if (username == null){
            editor.putString("pseudonym","Username: " + android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL);
            editor.commit();
            username = sharedPref.getString("pseudonym",null);
        }


        // Init layout
        setContentView(R.layout.activity_start);


        // Update username textview
        TextView usernameTv = (TextView) findViewById(R.id.username_text_view);
        usernameTv.setText(username);

        usernameTv.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayUsernameDialog();
            }
        });


        // Define broadcast for usernameDialog
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == USERNAME_INTENT) {
                    username = intent.getStringExtra("Username");

                    SharedPreferences sharedPref = context.getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.putString("pseudonym",username);
                    editor.commit();

                    TextView usernameTv = (TextView) findViewById(R.id.username_text_view);
                    usernameTv.setText("Username: " + username);
                }
            }
        };



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        // Test create button
        check_create_button(event);

        // Test join button
        check_join_button(event);

        return true;
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(USERNAME_INTENT));
    }

    private void check_create_button(MotionEvent event){
        ImageView iv = (ImageView) findViewById(R.id.button_create);

        Rect imageRect = new Rect();

        int x = (int) event.getX();
        int y = (int) event.getY();

        iv.getGlobalVisibleRect(imageRect);

        if (imageRect.contains(x,y) && event.getAction()==MotionEvent.ACTION_DOWN){
            iv.setImageDrawable(getDrawable(R.drawable.create_new_game_clicked));
        }
        else if (imageRect.contains(x,y) && event.getAction()==MotionEvent.ACTION_UP && iv.getDrawable().getConstantState().equals(getDrawable(R.drawable.create_new_game_clicked).getConstantState())){
            iv.setImageDrawable(getDrawable(R.drawable.create_new_game));
            Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
            startActivity(intent);
        }
        else if (!(imageRect.contains(x,y)) && event.getAction()==MotionEvent.ACTION_UP){
            iv.setImageDrawable(getDrawable(R.drawable.create_new_game));
        }
    }

    private void check_join_button(MotionEvent event){
        ImageView iv = (ImageView) findViewById(R.id.button_join);

        Rect imageRect = new Rect();

        int x = (int) event.getX();
        int y = (int) event.getY();

        iv.getGlobalVisibleRect(imageRect);

        if (imageRect.contains(x,y) && event.getAction()==MotionEvent.ACTION_DOWN){
            iv.setImageDrawable(getDrawable(R.drawable.join_game_clicked));
        }
        else if (imageRect.contains(x,y) && event.getAction()==MotionEvent.ACTION_UP && iv.getDrawable().getConstantState().equals(getDrawable(R.drawable.join_game_clicked).getConstantState())){
            iv.setImageDrawable(getDrawable(R.drawable.join_game));
            Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
            startActivity(intent);
        }
        else if (!(imageRect.contains(x,y)) && event.getAction()==MotionEvent.ACTION_UP){
            iv.setImageDrawable(getDrawable(R.drawable.join_game));
        }
    }


    private void displayUsernameDialog(){

//        AlertDialog.Builder usernameDialog = new AlertDialog.Builder(this);

//        usernameDialog.showUsernameDialog();

        DialogFragment newFragment = new UsernameDialog();
        newFragment.show(getFragmentManager(), "username");



    }

    public void setUsername(String s){
        username = s;
    }

    public String getUsername(){
        return username;
    }


}

