package com.motondon.rxjavademoapp.view.generalexamples;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cantrowitz.rxbroadcast.RxBroadcast;
import com.jakewharton.rxbinding.view.RxView;
import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * This example uses RxBroadcast library to demonstrate how to listen for broadcast system messages in a reactive way.
 *
 * It shows two examples: 1) listen for network status and 2) battery monitoring.
 *
 * Toggle button manipulation was taken from http://android--code.blogspot.com.br/2015/08/android-togglebutton-style.html
 *
 * Battery icons taken from http://downloadicons.net/power-icons?page=7
 *
 */
public class BroadcastSystemStatusExampleActivity extends BaseActivity {
    private static final String TAG = BroadcastSystemStatusExampleActivity.class.getSimpleName();

    private static int TYPE_WIFI = 1;
    //private static int TYPE_MOBILE = 2;
    //private static int TYPE_NOT_CONNECTED = 0;

    private boolean mIsConnected;
    private WifiManager wifiManager;

    @BindView(R.id.tv_network_status) TextView tvNetworkStatus;
    @BindView(R.id.btn_enable_disable_wifi) ToggleButton btnEnableDisableWiFi;

    @BindView(R.id.tv_battery_status) TextView tvBatteryStatus;
    @BindView(R.id.img_battery_status) ImageView imgBatteryLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_broadcast_system_status_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }

        wifiManager = (WifiManager)this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Create a listener for the WiFi Enable/Disable button by using RxBinding library
        RxView.clicks(btnEnableDisableWiFi).subscribe(view -> {

            Log.v(TAG, "btnEnableDisableWiFi::onClick() - Current WiFi status is: " + mIsConnected + ". Requesting WiFiManager to change it...");

            // The default toggle control behavior is to change its state when it is clicked, but we are keeping current state by calling
            // ToggleButton::setChecked() method and delegating such change to the network broadcast based on the network status.
            // This might be a poor solution, since we can face some quick flip when clicking over it.
            btnEnableDisableWiFi.setChecked(mIsConnected);

            // Now asks WiFiManager to change WiFi current status
            wifiManager.setWifiEnabled(!mIsConnected);
        });

        // This is the main part of this example. See how easy is to start a broadcast listener when using RxBroadcast. Just create an
        // IntentFilter as we normally do and then call RxBroadcast.fromBroadcast(...) to create an observable. We could also filter
        // messages the same way we do when using RxJava.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        RxBroadcast
            .fromBroadcast(this, filter)
            .subscribe(intent -> handleNetworkConnectivityChanges());

        IntentFilter filter2 = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        RxBroadcast
                .fromBroadcast(this, filter2)
                .subscribe(intent -> handleBatteryChange(intent));
    }

    /**
     * This method handles network connectivity status received by the system broadcast. Basically it checks whether there is a wifi
     * connection available or not and shows a text message accordingly.
     *
     */
    private void handleNetworkConnectivityChanges() {
        Log.v(TAG, "handleNetworkConnectivityChanges() - Begin");

        String status = "Not connected to the Internet";

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mIsConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting()
                && activeNetwork.getType() == TYPE_WIFI;

        if (mIsConnected) {
            if (activeNetwork.getType() == TYPE_WIFI) {
                status = "Detected WIFI is enabled";
                btnEnableDisableWiFi.setChecked(true);
                Log.v(TAG, "handleNetworkConnectivityChanges() - mIsConnected is true");
            }

            /*// We are not using it since we are only interesting in the WiFi network, but let this code here for future references
            if (activeNetwork.getType() == TYPE_MOBILE) {
                status = "Detected mobile data is enabled";
                btnEnableDisableWiFi.setChecked(true);
            }*/

        } else {
            btnEnableDisableWiFi.setChecked(false);
            Log.v(TAG, "handleNetworkConnectivityChanges() - mIsConnected is false");
        }

        Log.d(TAG, status);
        tvNetworkStatus.setText(status);

        Log.v(TAG, "handleNetworkConnectivityChanges() - End");
    }

    /**
     * This method handles battery status changes. It uses some extras from received intent so that it can check whether device is being
     * charged, and if so, whether it is connected via USB or an AC power supply. Then it shows the power status in a text view.
     *
     * We could make this example more interesting by taking some action when the battery is low, such as shows a toast, or send a message,
     * etc. It is up to you!
     *
     * See this link for more details on battery monitoring:
     * https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
     *
     * @param intent
     */
    private void handleBatteryChange(Intent intent) {
        Bundle bundle = intent.getExtras();

        if (null == bundle)
            return;

        boolean isPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
        //String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        //int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        //int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int level = 0;

        boolean usbCharge = plugged == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = plugged == BatteryManager.BATTERY_PLUGGED_AC;

        //boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        //        status == BatteryManager.BATTERY_STATUS_FULL;

        if(isPresent) {
            if (rawlevel >= 0 && scale > 0) {
                level = (rawlevel * 100) / scale;
            }

            if (usbCharge) {
                imgBatteryLevel.setBackgroundResource(R.drawable.battery_charging_usb);
                tvBatteryStatus.setText("USB Charging");

                // Full charge
                if (level == 100) {
                    tvBatteryStatus.setText("USB Charging - Full");
                } else {
                    tvBatteryStatus.setText("USB Charging - Battery Level: " + level + "%");
                }

            } else if (acCharge) {
                imgBatteryLevel.setBackgroundResource(R.drawable.battery_charging_ac);
                // Full charge
                if (level == 100) {
                    tvBatteryStatus.setText("AC Charging - Full");
                } else {
                    tvBatteryStatus.setText("AC Charging - Battery Level: " + level + "%");
                }

            } else { // not charging
                tvBatteryStatus.setText("Battery Level: " + level + "%");

                if (level == 100) {
                    imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_full);
                } else if (level >= 70) {
                    imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_almost_full);
                } else if (level >= 50) {
                    imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_half);
                } else if (level >= 30) {
                    imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_low);
                } else if (level >= 15) {
                    imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_very_low);
                } else {
                    imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_empty);
                }
            }
        } else {
            imgBatteryLevel.setBackgroundResource(0);
            tvBatteryStatus.setText("Could not detected any battery on this device");
        }
    }
}
