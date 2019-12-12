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

        eegChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eegSelected = ((CheckBox) v).isChecked(); // Check which checkbox was clicked
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

    }

    public static class BrainWaveData {
        public int
                obs, delta, theta, alpha1,
                alpha2, beta1, beta2, gamma1,
                gamma2, totPwr;
        public double time;
        public String attention, meditation, derived, clpass;

        private static final String SEPARATOR = ", ";

        @Override
        public String toString() {
            String res = "";

            String[] components = {
                Integer.toString(obs),
                Integer.toString(delta),
                Integer.toString(theta),
                Integer.toString(alpha1),
                Integer.toString(alpha2),
                Integer.toString(beta1),
                Integer.toString(beta2),
                Integer.toString(gamma1),
                Integer.toString(gamma2),
                Integer.toString(totPwr),

                Double.toString(time),

                attention,
                meditation,
                derived,
                clpass
            };

            for (String comp : components) {
                res += comp + SEPARATOR;
            }

            res = res.substring(0, res.length() - SEPARATOR.length() - 1);

            return res;
        }
    }
}
