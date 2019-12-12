package com.example.mmsi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import lecho.lib.hellocharts.view.LineChartView;

public class DisplayActivity extends AppCompatActivity {
    TextView EEGTv;
    TextView GSRTv;
//    TextView EyeTv;
    Button startRecord;
    Button stoptRecord;
    LineChartView lineChartView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);



        startRecord = findViewById(R.id.start_recording);

        lineChartView = findViewById(R.id.chart);
        // Add signal here

        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "connecting to server...", Toast.LENGTH_SHORT).show();
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "connected to server, no data available.", Toast.LENGTH_SHORT).show();
//                                EEGTv.setText("---");
//                                GSRTv.setText(String.format(Locale.getDefault(), "No Data"));
//                                EyeTv.setText(String.format(Locale.getDefault(), "No Data"));


                            }
                        }, 5000);

            }
        });

        //
        stoptRecord = findViewById(R.id.stop_recording);
        stoptRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "recording stopped", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
