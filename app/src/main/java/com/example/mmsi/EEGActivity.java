package com.example.mmsi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.pwittchen.neurosky.library.NeuroSky;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class EEGActivity extends AppCompatActivity {
    public static final String SERVER_DATA_MSG = "SERVER_DATA_MSG";

    private static final String LOG_TAG = "NeuroSky";
    private static final String SEPARATOR = ", ";
    private static final String HTTP_SERVER_ADDRESS = "http://10.153.67.63:5000/upload";
    private static final String SERVER_REQUEST_FAIL = "Server request failure...";
    private static final String SERVER_REQUEST_EXCEPTION = "The system threw an exception when receiving the server's response...";
    private static final String SERVER_REQUEST_SUCCESSFUL = "Server response received successfully!";

    private int obs = 1;
    private boolean isMonitoring;
    private String serverResponseMessage;
    private String serverResponseData;
    private ArrayList<String> dataCSVs;
    private ArrayList< HashMap<String, String> > brainWaveData;
    private HashMap<String, String> currentRow;

    private NeuroSky neuroSky;
    private OkHttpClient httpClient;

    private TextView stateTv, attentionTv, meditationTv;
    private Button connectButton, startMonitorButton, stopMonitorButton, disconnectButton, continueToGSRButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eeg);

        neuroSky = createNeuroSky();
        brainWaveData = new ArrayList<>();
        dataCSVs = new ArrayList<>();

        stateTv = findViewById(R.id.state_tv);
        attentionTv = findViewById(R.id.attention_tv);
        meditationTv = findViewById(R.id.tv_meditation);

        connectButton = findViewById(R.id.connect_btn);
        continueToGSRButton = findViewById(R.id.goto_gsr_btn);
        startMonitorButton = findViewById(R.id.start_monitoring_btn);
        stopMonitorButton = findViewById(R.id.stop_monitoring_btn);
        disconnectButton = findViewById(R.id.disconnect_btn);

        // Make sure read external storage is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
            }
        }

        connectButton.setOnClickListener((View v) -> {
            try {
                neuroSky.connect();
            } catch (BluetoothNotEnabledException e) {
                Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, e.getMessage());
            }
        });


        continueToGSRButton.setOnClickListener((View v) -> {
            sendDataToServer("fklsadfklsdfksadjfsadklfjsadfklsad", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    serverResponseMessage = SERVER_REQUEST_FAIL;
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        if (response.isSuccessful()) {
                            serverResponseMessage = SERVER_REQUEST_SUCCESSFUL;
                            serverResponseData = response.body().string();
                        } else {
                            serverResponseMessage = "Server sent status " + response.code() + " " + response.message();
                        }
                    } catch (Exception e) {
                        serverResponseMessage = SERVER_REQUEST_EXCEPTION;
                    } finally {
                        if (serverResponseMessage.isEmpty() == false) {
                            runOnUiThread(() -> {
                                // Send message
                                Toast.makeText(EEGActivity.this, serverResponseMessage, Toast.LENGTH_SHORT).show();

                                // Prepare next activity
                                Intent intent = new Intent(EEGActivity.this, GSRActivity.class);
                                intent.putExtra(SERVER_DATA_MSG, serverResponseData);

                                // Reset server response data
                                serverResponseMessage = "";
                                serverResponseData = "";
                                dataCSVs.clear();

                                // Go to next activity
                                startActivity(intent);
                            });
                        }
                    }
                }
            });

//            for (String data : dataCSVs) {
//                sendDataToServer(data, new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        serverResponseMessage="Server request failed...";
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) {
//                        try {
//                            if (response.isSuccessful()) {
//                                serverResponseMessage = "Data sent successfully!";
//                            } else {
//                                serverResponseMessage = response.body().string();
//                            }
//                        } catch (Exception e) {
//                            serverResponseMessage="Server request threw and exception...";
//                        } finally {
//                            if (serverResponseMessage.isEmpty() == false) {
//                                Toast.makeText(EEGActivity.this, serverResponseMessage, Toast.LENGTH_SHORT).show();
//                            }
//
//                            serverResponseMessage = "";
//                            dataCSVs.clear();
//                            Toast.makeText(EEGActivity.this, "Hi, " + globalApp.getUserName(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }


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

    private Call sendDataToServer(String csvData, Callback callback) {
        try {
            if (httpClient == null) {
                httpClient = new OkHttpClient();
            }

            RequestBody reqBody = new FormBody.Builder()
                    .add("data", csvData)
                    .build();

            Request req = new Request.Builder()
                    .url(HTTP_SERVER_ADDRESS)
                    .post(reqBody)
                    .build();

            Call call = httpClient.newCall(req);
            call.enqueue(callback);
            return call;
        }
        catch (Exception e) {
            Toast.makeText(getBaseContext() ,e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, e.getMessage());
            return null;
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
            dataCSVs.add(dataCsv);

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
