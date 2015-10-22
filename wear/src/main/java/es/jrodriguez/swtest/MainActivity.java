package es.jrodriguez.swtest;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private ToggleButton mToggleButton;

    private Integer computedHeartRate = null;

    Sensor mHeartRateSensor;
    SensorManager mSensorManager;

    private static final String CAPABILITY_SHOW_HEART_RATE_DATA = "receive_heart_rate_data";
    private static final String CAPABILITY_SHOW_HEART_RATE_STATUS = "receive_heart_rate_status";

    public static final String MESSAGE_PATH_SHOW_HEART_RATE_DATA = "/receive_heart_rate_data";
    public static final String MESSAGE_PATH_SHOW_HEART_RATE_STATUS = "/receive_heart_rate_status";

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mToggleButton = (ToggleButton) findViewById(R.id.toggle);

        mToggleButton.setOnClickListener(toggleHeratRate);

        setupHostConnection();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));

            updateText();
        } else {
            mContainerView.setBackgroundColor(Color.parseColor("#673ab7"));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));

            updateText();
        }
    }

    private void updateText() {
        if (computedHeartRate != null)
            mTextView.setText(getResources().getString(R.string.ppm, computedHeartRate));
        else
            mTextView.setText("");
    }

    public View.OnClickListener toggleHeratRate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mToggleButton.isChecked())
                activateSensor();
            else
                deactivateSensor();
        }
    };

    private void activateSensor() {
        if (mSensorManager != null) {
            if (mHeartRateSensor != null) {
                mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                computedHeartRate = 0;
                updateText();
                sendHeartRateData(0);
            }
            sendHeartRateStatus(true);
        }
    }

    private void deactivateSensor() {
        if (mSensorManager != null) {
            if (mHeartRateSensor != null)
                mSensorManager.unregisterListener(this);
            else {
                computedHeartRate = null;
                updateText();
            }
            sendHeartRateStatus(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Register the listener
        activateSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Unregister the listener
        deactivateSensor();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Update your data. This check is very raw. You should improve it when the sensor is unable to calculate the heart rate
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            if ((int) event.values[0] > 0) {
                computedHeartRate = Math.round(event.values[0]);
                updateText();
                sendHeartRateData(computedHeartRate.intValue());
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    private void setupHostConnection() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();

                Wearable.CapabilityApi.getCapability(mGoogleApiClient,
                        CAPABILITY_SHOW_HEART_RATE_DATA,
                        CapabilityApi.FILTER_REACHABLE).setResultCallback(new ResultCallbacks<CapabilityApi.GetCapabilityResult>() {
                    @Override
                    public void onSuccess(CapabilityApi.GetCapabilityResult getCapabilityResult) {
                        updateDataCapability(getCapabilityResult.getCapability());
                    }

                    @Override
                    public void onFailure(Status status) {
                        updateDataCapability(null);
                    }
                });

        CapabilityApi.CapabilityListener dataCapabilityListener =
                new CapabilityApi.CapabilityListener() {
                    @Override
                    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                        updateDataCapability(capabilityInfo);
                    }
                };

        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                dataCapabilityListener,
                CAPABILITY_SHOW_HEART_RATE_DATA);


        Wearable.CapabilityApi.getCapability(mGoogleApiClient,
                CAPABILITY_SHOW_HEART_RATE_STATUS,
                CapabilityApi.FILTER_REACHABLE).setResultCallback(new ResultCallbacks<CapabilityApi.GetCapabilityResult>() {
            @Override
            public void onSuccess(CapabilityApi.GetCapabilityResult getCapabilityResult) {
                updateStateCapability(getCapabilityResult.getCapability());
            }

            @Override
            public void onFailure(Status status) {
                updateStateCapability(null);
            }
        });

        CapabilityApi.CapabilityListener stateCapabilityListener =
                new CapabilityApi.CapabilityListener() {
                    @Override
                    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                        updateStateCapability(capabilityInfo);
                    }
                };

        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                stateCapabilityListener,
                CAPABILITY_SHOW_HEART_RATE_STATUS);
    }

    private String hostNodeId = null;

    private CapabilityInfo stateCapabilityInfo = null;
    private CapabilityInfo dataCapabilityInfo = null;

    private void updateStateCapability(CapabilityInfo capabilityInfo) {
        stateCapabilityInfo = capabilityInfo;

        updateHost();
    }

    private void updateDataCapability(CapabilityInfo capabilityInfo) {
        dataCapabilityInfo = capabilityInfo;

        updateHost();
    }

    private void updateHost() {
        if ((stateCapabilityInfo != null) && (dataCapabilityInfo != null)) {
            hostNodeId = pickBestNodeId(stateCapabilityInfo.getNodes(), dataCapabilityInfo.getNodes());
        }
    }

    private String pickBestNodeId(Set<Node> stateCapableNodes, Set<Node> dataCapableNodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node stateCapableNode : stateCapableNodes) {
            if (stateCapableNode.isNearby()) {
                for (Node dataCapableNode : dataCapableNodes) {
                    if ((dataCapableNode.isNearby()) && (dataCapableNode.getId().equals(stateCapableNode.getId())))
                        return dataCapableNode.getId();
                }
            } else if (bestNodeId == null) {
                for (Node dataCapableNode : dataCapableNodes) {
                    if (dataCapableNode.getId().equals(stateCapableNode.getId()))
                        bestNodeId = dataCapableNode.getId();
                }
            }
        }
        return bestNodeId;
    }


    private void sendHeartRateData(int data) {
        if (hostNodeId != null) {
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(data);
            Wearable.MessageApi.sendMessage(mGoogleApiClient, hostNodeId,
                    MESSAGE_PATH_SHOW_HEART_RATE_DATA, b.array());
        }
    }

    private void sendHeartRateStatus(boolean data) {
        if (hostNodeId != null) {
            ByteBuffer b = ByteBuffer.allocate(1);
            b.put((byte) ((data)?1:0));
            Wearable.MessageApi.sendMessage(mGoogleApiClient, hostNodeId,
                    MESSAGE_PATH_SHOW_HEART_RATE_STATUS, b.array());
        }
    }

}
