package com.example.mmsi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.esense.esenselib.ESenseConfig;
import io.esense.esenselib.ESenseConnectionListener;
import io.esense.esenselib.ESenseEvent;
import io.esense.esenselib.ESenseEventListener;
import io.esense.esenselib.ESenseManager;
import io.esense.esenselib.ESenseSensorListener;

public class GSRActivity extends AppCompatActivity implements ESenseSensorListener, ESenseConnectionListener, ESenseEventListener {
    private static final String TAG = "eSense";
    Button continueGSRBtn;
    Button connectGSRButton;
    private ESenseManager manager;
    private String sensorName = "eSense-0615";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsr);

        continueGSRBtn = findViewById(R.id.continue_gsr_btn);
        continueGSRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to the eeg activity
                Intent i = new Intent(GSRActivity.this, DisplayActivity.class);
                startActivity(i);
            }
        });
        connectGSRButton = findViewById(R.id.connect_gsr_btn);
        connectGSRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Sensor not connected!", Toast.LENGTH_LONG).show();
            }
        });
        manager = createESenseManager();
    }

    private ESenseManager createESenseManager () {
        manager = new ESenseManager(sensorName, getBaseContext());
        Log.d(TAG, "manager created!" + manager.getDeviceName());
        return manager;
    }
    @Override
    protected void onStart() {
        super.onStart();

        // hooke th buttons with the listeners
        findViewById(R.id.continue_gsr_btn).setOnClickListener(handleClick);
        findViewById(R.id.disconnect_gsr_btn).setOnClickListener(handleClick);
        findViewById(R.id.register_listen_gsr_btn ).setOnClickListener(handleClick);
        findViewById(R.id.unregister_listen_gsr_btn).setOnClickListener(handleClick);
    }


    private View.OnClickListener handleClick = new View.OnClickListener() {
//        listen for clicks
//        ESSL mESSL = null;
        @Override
        public void onClick(View v) {
            Button btn = (Button)v;
            switch(btn.getId()){
                case R.id.connect_gsr_btn:
                    manager = createESenseManager();
                    manager.connect(10000);
                    break;
            }
        }
    };
    /**
     * Called when the device with the specified name has been found during a scan
     *
     * @param manager device manager
     */

    @Override
    public void onDeviceFound (ESenseManager manager) {
        Toast.makeText(getBaseContext(), TAG + "Found", Toast.LENGTH_LONG);
        Log.d(TAG, "Motion Sensor Found");
    }

    /**
     * Called when the device with the specified name has not been found during a scan
     *
     * @param manager device manager
     */
    @Override
    public void onDeviceNotFound(ESenseManager manager) {
        Log.d(TAG, "Motion Sensor not Found");
    }

    /**
     * Called when the connection has been successfully made
     *
     * @param manager device manager
     */
    @Override
    public void onConnected(ESenseManager manager) {
        Log.d(TAG, "Motion Sensor Connected");
    }

    /**
     * Called when the device has been disconnected
     *
     * @param manager device manager
     */
    @Override
    public void onDisconnected(ESenseManager manager) {
        Log.d(TAG, "Motion Sensor disconnected");
    }

    /**
     * Called when the information on battery voltage has been received
     *
     * @param voltage battery voltage in Volts
     */
    @Override
    public void onBatteryRead(double voltage) {

    }

    /**
     * Called when the button event has changed
     *
     * @param pressed true if the button is pressed, false if it is released
     */
    @Override
    public void onButtonEventChanged(boolean pressed) {

    }

    /**
     * Called when the information on advertisement and connection interval has been received
     *
     * @param minAdvertisementInterval minimum advertisement interval (unit: milliseconds)
     * @param maxAdvertisementInterval maximum advertisement interval (unit: milliseconds)
     * @param minConnectionInterval    minimum connection interval (unit: milliseconds)
     * @param maxConnectionInterval    maximum connection interval (unit: milliseconds)
     */
    @Override
    public void onAdvertisementAndConnectionIntervalRead(int minAdvertisementInterval, int maxAdvertisementInterval, int minConnectionInterval, int maxConnectionInterval) {

    }

    /**
     * Called when the information on the device name has been received
     *
     * @param deviceName name of the device
     */
    @Override
    public void onDeviceNameRead(String deviceName) {

    }

    /**
     * Called when the information on sensor configuration has been received
     *
     * @param config current sensor configuration
     */
    @Override
    public void onSensorConfigRead(ESenseConfig config) {

    }

    /**
     * Called when the information on accelerometer offset has been received
     *
     * @param offsetX x-axis factory offset
     * @param offsetY y-axis factory offset
     * @param offsetZ z-axis factory offset
     */
    @Override
    public void onAccelerometerOffsetRead(int offsetX, int offsetY, int offsetZ) {

    }

    /**
     * Called when there is new sensor data available
     *
     * @param evt object containing the sensor samples received
     */
    @Override
    public void onSensorChanged(ESenseEvent evt) {

    }
}
