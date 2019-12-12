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

import java.util.Locale;
import java.util.Set;


public class EEGActivity extends AppCompatActivity {
    private final static String LOG_TAG = "NeuroSky";
    private NeuroSky neuroSky;
    TextView stateTv;
    TextView attentionTv;
    TextView meditationTv;
    TextView blinkTv;

    Button connectButton;
    Button startMonButton;
    Button stopMonButton;
    Button disconnectButton;
    Button continueToGSRButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eeg);

        stateTv = findViewById(R.id.state_tv);
        attentionTv = findViewById(R.id.attention_tv);
//        meditationTv = findViewById(R.id.tv_meditation);
//        blinkTv = findViewById(R.id.tv_blink);

        connectButton = findViewById(R.id.connect_btn);
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


        continueToGSRButton = findViewById(R.id.goto_gsr_btn);
        continueToGSRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                GlobalVarApp globalApp = ((GlobalVarApp)getApplicationContext());
                // go to the GSR activity
                Intent i = new Intent(EEGActivity.this, GSRActivity.class);
                startActivity(i);
//                Toast.makeText(EEGActivity.this, "hi"+globalApp.getUserName() , Toast.LENGTH_SHORT).show();
            }
        });

        //
        startMonButton = findViewById(R.id.start_monitoring_btn);
        startMonButton.setOnClickListener(new View.OnClickListener() {
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
        //
        stopMonButton = findViewById(R.id.stop_monitoring_btn);
        stopMonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    neuroSky.stopMonitoring();
                } catch (BluetoothNotEnabledException e) {
                    Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });
        //
        disconnectButton = findViewById(R.id.disconnect_btn);
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
        //
        neuroSky = createNeuroSky();
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
