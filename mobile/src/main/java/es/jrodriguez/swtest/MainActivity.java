package es.jrodriguez.swtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements MessageApi.MessageListener {

    public static final String MESSAGE_PATH_SHOW_HEART_RATE_DATA = "/receive_heart_rate_data";
    public static final String MESSAGE_PATH_SHOW_HEART_RATE_STATUS = "/receive_heart_rate_status";

    TextView mHeartRate;
    Switch mSwitch;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeartRate = (TextView)findViewById(R.id.text);
        mSwitch = (Switch)findViewById(R.id.switch1);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();

        Wearable.MessageApi.addListener(mGoogleApiClient,this);
    }

    private void updateText(int heartRate) {
        mHeartRate.setText(getResources().getString(R.string.ppm,heartRate));
    }

    private void updateStatus(boolean active) {
        mSwitch.setChecked(active);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_PATH_SHOW_HEART_RATE_DATA)) {
            ByteBuffer b = ByteBuffer.allocate(4);
            b.put(messageEvent.getData());
            int data = b.getInt();
            updateText(data);
        } else if (messageEvent.getPath().equals(MESSAGE_PATH_SHOW_HEART_RATE_STATUS)) {
            ByteBuffer b = ByteBuffer.allocate(1);
            b.put(messageEvent.getData());
            updateStatus((b.get() == 1));
        }
    }
}
