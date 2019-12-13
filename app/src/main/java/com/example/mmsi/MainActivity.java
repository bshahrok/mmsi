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

    private CheckBox eegChk;
    private TextView userNameTV;
    private Button continueBtn;
    private boolean eegSelected;

    private GlobalVarApp globalApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameTV = findViewById(R.id.username_tv);
        eegChk = findViewById(R.id.eeg_chk);
        continueBtn = findViewById(R.id.continue_btn);

        eegChk.setOnClickListener((View v) -> {
            eegSelected = ((CheckBox) v).isChecked(); // Check which checkbox was clicked
        });

        continueBtn.setOnClickListener((View v) -> {
            if(eegSelected) {
                globalApp = ((GlobalVarApp)getApplicationContext());
                CharSequence name = userNameTV.getText();
                Intent i = new Intent(MainActivity.this, EEGActivity.class);

                globalApp.setUserName(name);
                startActivity(i);
                Toast.makeText(getBaseContext(), "Let's go, " + name + "!" , Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getBaseContext(), "You have to choose at least one of the options" , Toast.LENGTH_SHORT).show();
            }
        });

    }
}
