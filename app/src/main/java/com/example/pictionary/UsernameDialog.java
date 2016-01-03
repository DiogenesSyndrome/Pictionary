package com.example.pictionary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/**
 * Created by Philippe on 31.12.2015.
 *
 * Defines the dialog used to change the username in the startActivity
 *
 */
public class UsernameDialog extends DialogFragment {

    Button cancelButton;
    Button confirmButton;
    EditText editText;

    String name;

    Context activityContext;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        activityContext = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.username_layout, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView);

        setListener(this, dialogView);

        return builder.create();

    }

    private void setListener(final UsernameDialog dialog,View view){

        cancelButton = (Button) view.findViewById(R.id.username_cancel);
        confirmButton = (Button) view.findViewById(R.id.username_confirm);
        editText = (EditText) view.findViewById(R.id.username_edit_text);

        cancelButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirmButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("username_intent");
                intent.putExtra("Username", editText.getText().toString());
                activityContext.sendBroadcast(intent);
                dialog.dismiss();
            }
        });


    }

}