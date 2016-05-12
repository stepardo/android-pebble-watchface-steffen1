/**
 * Android Pebble Watchface Steffen 1
 *
 * This file is licensed under the terms of the Gnu General Public license,
 * version 2. The author does not permit military use. See the file LICENSE
 * for details.
 */
package de.cybervisor.steffen.android_pebble_watchface_steffen1;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final UUID APP_UUID = UUID.fromString("d17a33f6-e2ed-41d5-b506-db1f6c15bb68");
    private PebbleKit.PebbleDataReceiver mDataReceiver;

    public final static int PHONE_CMD_BATTERY_LEVEL = 0;
    public final static int PHONE_RET_BATTERY_LEVEL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Construct output String
        StringBuilder builder = new StringBuilder();
        builder.append("Pebble Info\n\n");

        // Is the watch connected?
        boolean isConnected = PebbleKit.isWatchConnected(this);
        builder.append("Watch connected: " + (isConnected ? "true" : "false")).append("\n");

        if (isConnected) {
            // What is the firmware version?
            PebbleKit.FirmwareVersionInfo info = PebbleKit.getWatchFWVersion(this);
            builder.append("Firmware version: ");
            builder.append(info.getMajor()).append(".");
            builder.append(info.getMinor()).append("\n");

            // Is AppMessage supported?
            boolean appMessageSupported = PebbleKit.areAppMessagesSupported(this);
            builder.append("AppMessage supported: " + (appMessageSupported ? "true" : "false"));

            TextView textView = (TextView) findViewById(R.id.text_view);
            textView.setText(builder.toString());

            // Push a notification
            if (false) {
                final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

                final Map data = new HashMap();
                data.put("title", "Test Message");
                data.put("body", "Whoever said nothing was impossible never tried to slam a revolving door.");
                final JSONObject jsonData = new JSONObject(data);
                final String notificationData = new JSONArray().put(jsonData).toString();

                i.putExtra("messageType", "PEBBLE_ALERT");
                i.putExtra("sender", "PebbleKit Android");
                i.putExtra("notificationData", notificationData);
                sendBroadcast(i);
            }
        } else {
            builder.append("Watch is not connected.");
        }

        if(mDataReceiver == null) {
            mDataReceiver = new PebbleKit.PebbleDataReceiver(APP_UUID) {
                @Override
                public void receiveData(Context context, int transactionId, PebbleDictionary dict) {
                    // Message received, over!
                    PebbleKit.sendAckToPebble(context, transactionId);
                    Log.i("receiveData", "Got message from Pebble!");
                    TextView textView = (TextView) findViewById(R.id.text_view);
                    textView.setText("Received something.");
                    // Up received?
                    if (dict.getInteger(0) == PHONE_CMD_BATTERY_LEVEL) {
                        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                        Intent batteryStatus = context.registerReceiver(null, ifilter);
                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                        Log.println(Log.INFO, "nothing", "level " + level + " scale " + scale);

                        PebbleDictionary resultDict = new PebbleDictionary();
                        resultDict.addInt32(PHONE_RET_BATTERY_LEVEL, level);
                        PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID, resultDict);
                    }
                }
            };
            PebbleKit.registerReceivedDataHandler(getApplicationContext(), mDataReceiver);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
