package com.example.mmsi;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class EEGActivity extends AppCompatActivity {
    private static final String LOG_TAG = "NeuroSky";

    private NeuroSky neuroSky;

    private TextView stateTv, attentionTv; //, mediationTv, blinkTv;
    private Button connectButton, startMonitorButton, stopMonitorButton, disconnectButton, continueToGSRButton;

    private ArrayList< HashMap<String, String> > brainWaveData;

    private static final String SEPARATOR = ",";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eeg);

        neuroSky = createNeuroSky();
        brainWaveData = new ArrayList<>();

        stateTv = findViewById(R.id.state_tv);
        attentionTv = findViewById(R.id.attention_tv);
        // meditationTv = findViewById(R.id.tv_meditation);
        // blinkTv = findViewById(R.id.tv_blink);
        connectButton = findViewById(R.id.connect_btn);
        continueToGSRButton = findViewById(R.id.goto_gsr_btn);
        startMonitorButton = findViewById(R.id.start_monitoring_btn);
        stopMonitorButton = findViewById(R.id.stop_monitoring_btn);
        disconnectButton = findViewById(R.id.disconnect_btn);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    neuroSky.connect();
                } catch (BluetoothNotEnabledException e) {
                    Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });


        continueToGSRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // GlobalVarApp globalApp = ((GlobalVarApp)getApplicationContext());
                Intent i = new Intent(EEGActivity.this, GSRActivity.class);
                startActivity(i);
                // Toast.makeText(EEGActivity.this, "hi"+globalApp.getUserName() , Toast.LENGTH_SHORT).show();
            }
        });

        startMonitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    neuroSky.startMonitoring();
                } catch (BluetoothNotEnabledException e) {
                    Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });

        stopMonitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    neuroSky.stopMonitoring();

                    // Package brainwave data and clear
                    if (brainWaveData.size() > 0) {
                        String dataCsv = "";

                        // Add header
                        for (String key : brainWaveData.get(0).keySet()) {
                            dataCsv += key + SEPARATOR;
                        }
                        dataCsv = dataCsv.substring( 0, dataCsv.length() - SEPARATOR.length() - 1 ) + "\n";

                        // Add data
                        for (HashMap<String, String> row : brainWaveData) {
                            for (String key : row.keySet()) {
                                dataCsv += row.get(key) + SEPARATOR;
                            }
                            dataCsv = dataCsv.substring( 0, dataCsv.length() - SEPARATOR.length() - 1 ) + "\n";
                        }

                        Log.d(LOG_TAG, dataCsv);
                    }

                } catch (BluetoothNotEnabledException e) {
                    Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    neuroSky.disconnect();
                } catch (BluetoothNotEnabledException e) {
                    Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });
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

                HashMap<String, String> dataRow = new HashMap<>();

                dataRow.put("obs", 1 + "");
                dataRow.put("time", 5.9 + "");
                dataRow.put("Delta", 52574 + "");
                dataRow.put("Theta", 21514 + "");
                dataRow.put("Alpha1", 11594 + "");
                dataRow.put("Alpha2", 8277 + "");
                dataRow.put("Beta1", 18599 + "");
                dataRow.put("Beta2", 13913 + "");
                dataRow.put("Gamma1", 9731 + "");
                dataRow.put("Gamma2", 17660 + "");
                dataRow.put("Attention", signal + "");
                dataRow.put("Meditation", "NA");
                dataRow.put("Derived", "NA");
                dataRow.put("totPwr", "153862");
                dataRow.put("clpass", "1*");

                brainWaveData.add(dataRow);

                break;
//            case MEDITATION:
//                meditationTv.setText(getFormattedMessage("meditation: %d", signal));
//                break;
//            case BLINK:
//                blinkTv.setText(getFormattedMessage("blink: %d", signal));
//                break;
            default:
                Log.d(LOG_TAG , getFormattedMessage("other-->: %d", signal));
//            case UNKNOWN:
//                break;
//            case STATE_CHANGE:
//                break;
//            case POOR_SIGNAL:
//                break;
//
//            case SLEEP_STAGE:
//                break;
//            case LOW_BATTERY:
//                break;
//            case RAW_COUNT:
//                break;
//            case RAW_DATA:
//                break;
//            case HEART_RATE:
//                break;
//            case RAW_MULTI:
//                break;
//            case EEG_POWER:
//                break;
        }

        Log.d("signalChange", String.format("%s: %d", signal.toString(), signal.getValue()));
    }

    private String getFormattedMessage(String messageFormat, Signal signal) {
        return String.format(Locale.getDefault(), messageFormat, signal.getValue());
    }

    private void handleBrainWavesChange(final Set<BrainWave> brainWaves) {
        for (BrainWave brainWave : brainWaves) {
            Log.d("BrainWave", String.format("%s: %d", brainWave.toString(), brainWave.getValue()));
        }
    }


}
