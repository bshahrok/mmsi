/*
* To Do: Add control to show pages based on the options
* */
package com.example.mmsi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    CheckBox eegChk;
    TextView userNameTV;
    Button continueBtn;
    boolean eegSelected;

    GlobalVarApp globalApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameTV = findViewById(R.id.username_tv);

        eegChk =findViewById(R.id.eeg_chk);
        eegChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                // Check which checkbox was clicked
                eegSelected =checked;
            }
        });

        continueBtn = findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eegSelected) {
                    // go to the eeg activity
                    CharSequence name = userNameTV.getText();
                    globalApp = ((GlobalVarApp)getApplicationContext());
                    globalApp.setUserName(name);
                    Intent i = new Intent(MainActivity.this, EEGActivity.class);
                    startActivity(i);
                    Toast.makeText(getBaseContext(), "let's go " + name , Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getBaseContext(), "You have to choose at least one of the options" , Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
