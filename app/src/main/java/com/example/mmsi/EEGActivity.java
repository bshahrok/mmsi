package com.example.mmsi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.pwittchen.neurosky.library.NeuroSky;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;


public class EEGActivity extends AppCompatActivity {
    private static final String LOG_TAG = "NeuroSky";
    private static final String SEPARATOR = ", ";
    private static final String CSV_FILENAME = "LOG";
    private static final String BASE_DIR = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

    private ArrayList< HashMap<String, String> > brainWaveData;
    private HashMap<String, String> currentRow;
    private int obs = 1, fileCounter = 1;
    private boolean isMonitoring;

    private NeuroSky neuroSky;

    private TextView stateTv, attentionTv, meditationTv;
    private Button connectButton, startMonitorButton, stopMonitorButton, disconnectButton, continueToGSRButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eeg);

        neuroSky = createNeuroSky();
        brainWaveData = new ArrayList<>();

        stateTv = findViewById(R.id.state_tv);
        attentionTv = findViewById(R.id.attention_tv);
        meditationTv = findViewById(R.id.tv_meditation);

        connectButton = findViewById(R.id.connect_btn);
        continueToGSRButton = findViewById(R.id.goto_gsr_btn);
        startMonitorButton = findViewById(R.id.start_monitoring_btn);
        stopMonitorButton = findViewById(R.id.stop_monitoring_btn);
        disconnectButton = findViewById(R.id.disconnect_btn);

        connectButton.setOnClickListener((View v) -> {
            try {
                neuroSky.connect();
            } catch (BluetoothNotEnabledException e) {
                Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, e.getMessage());
            }
        });


        continueToGSRButton.setOnClickListener((View v) -> {
            GlobalVarApp globalApp = ((GlobalVarApp)getApplicationContext());
            Intent i = new Intent(EEGActivity.this, GSRActivity.class);
            startActivity(i);
                Toast.makeText(EEGActivity.this, "Hi, " + globalApp.getUserName(), Toast.LENGTH_SHORT).show();
        });

        startMonitorButton.setOnClickListener((View v) -> {
             try {
                neuroSky.startMonitoring();
                isMonitoring = true;
            } catch (BluetoothNotEnabledException e) {
                Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, e.getMessage());
            }
        });

        stopMonitorButton.setOnClickListener((View v) -> {
            try {
                neuroSky.stopMonitoring();
                packageAndSendBrainWaveData();
                isMonitoring = false;
            } catch (BluetoothNotEnabledException e) {
                Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, e.getMessage());
            }
        });

        disconnectButton.setOnClickListener((View v) -> {
            try {
                neuroSky.disconnect();
            } catch (BluetoothNotEnabledException e) {
                Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, e.getMessage());
            }
        });
    }

    public void writeCSVFile(String data) {
        String pathname = CSV_FILENAME + fileCounter++ + ".csv";
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(pathname, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch(Exception e) {
            Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    private void packageAndSendBrainWaveData() {
        if (brainWaveData.size() > 0) {
            String dataCsv = "";
            ArrayList<String> keyList = new ArrayList<>(brainWaveData.get(0).keySet());

            Collections.sort(keyList);

            // Add header
            for (String key : keyList) {
                dataCsv += key + SEPARATOR;
            }

            dataCsv = dataCsv.substring( 0, dataCsv.length() - SEPARATOR.length() ) + "\n";

            // Add data
            for (HashMap<String, String> row : brainWaveData) {
                keyList = new ArrayList<>(row.keySet());

                Collections.sort(keyList);

                for (String key : keyList) {
                    dataCsv += row.get(key) + SEPARATOR;
                }

                dataCsv = dataCsv.substring( 0, dataCsv.length() - SEPARATOR.length() ) + "\n";
            }

            Log.d(LOG_TAG, dataCsv);
            writeCSVFile(dataCsv);

            brainWaveData.clear(); // Reset for next monitoring
            obs = 1;
        }
    }

    @NonNull
    private NeuroSky createNeuroSky() {
        return new NeuroSky(new ExtendedDeviceMessageListener() {
            @Override public void onStateChange(State state) {
                handleStateChange(state);
            }

            @Override public void onSignalChange(Signal signal) {
                handleSignalChange(signal);
            }

            @Override public void onBrainWavesChange(Set<BrainWave> brainWaves) {
                handleBrainWavesChange(brainWaves);
            }
        });
    }

    private void handleStateChange(final State state) {
        if (neuroSky != null && state.equals(State.CONNECTED)) neuroSky.startMonitoring();

        stateTv.setText(state.toString());
        Log.d("stateChange", state.toString());
    }

    private void handleSignalChange(final Signal signal) {
        switch (signal) {
            case ATTENTION:
                attentionTv.setText(getFormattedMessage("attention: %d", signal));
                addValueToCurrentRow("Attention", signal.getValue() + "");
                break;

            case MEDITATION:
                meditationTv.setText(getFormattedMessage("meditation: %d", signal));
                addValueToCurrentRow("Meditation", signal.getValue() + "");
                break;

            case EEG_POWER:
               addValueToCurrentRow("totPwr", signal.getValue() + "");
               break;

            default:
                Log.d(LOG_TAG , getFormattedMessage("other-->: %d", signal));
        }

        Log.d("signalChange", String.format("%s: %d", signal.toString(), signal.getValue()));
    }

    private void addValueToCurrentRow(String name, String value) {
        if (isMonitoring) {
            if (currentRow == null) {
                resetRow();
            }

            currentRow.put(name, value);

            if (currentRow.size() == 15) {
                brainWaveData.add(currentRow);
                currentRow = null;
            }
        }
    }

    private String getFormattedMessage(String messageFormat, Signal signal) {
        return String.format(Locale.getDefault(), messageFormat, signal.getValue());
    }

    private void resetRow() {
        currentRow = new HashMap<>();
        currentRow.put("obs", obs++ + "");
        currentRow.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        currentRow.put("Derived", "NA");
        currentRow.put("class", "1");
    }

    private void handleBrainWavesChange(final Set<BrainWave> brainWaves) {
        for (BrainWave brainWave : brainWaves) {
            Log.d("BrainWave", String.format("%s: %d", brainWave.toString(), brainWave.getValue()));

            String dataType = null;

            switch(brainWave) {
                case DELTA:
                    dataType = "Delta";
                    break;

                case THETA:
                    dataType = "Theta";
                    break;

                case LOW_ALPHA:
                    dataType = "Alpha1";
                    break;

                case HIGH_ALPHA:
                    dataType = "Alpha2";
                    break;

                case LOW_BETA:
                    dataType = "Beta1";
                    break;

                case HIGH_BETA:
                    dataType = "Beta2";
                    break;

                case LOW_GAMMA:
                    dataType = "Gamma1";
                    break;

                case MID_GAMMA:
                    dataType = "Gamma2";
            }

            if (dataType != null) {
                addValueToCurrentRow(dataType, brainWave.getValue() + "");
            }
        }
    }


}
