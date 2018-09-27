package com.motondon.rxjavademoapp.view.generalexamples

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log

import com.cantrowitz.rxbroadcast.RxBroadcast
import com.jakewharton.rxbinding.view.RxView
import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import kotlinx.android.synthetic.main.activity_general_broadcast_system_status_example.*

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
class BroadcastSystemStatusExampleActivity : BaseActivity() {
    //private static int TYPE_MOBILE = 2;
    //private static int TYPE_NOT_CONNECTED = 0;

    private var mIsConnected: Boolean = false
    private var wifiManager: WifiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_broadcast_system_status_example)

        supportActionBar?.title = intent.getStringExtra("TITLE")

        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Create a listener for the WiFi Enable/Disable button by using RxBinding library
        RxView.clicks(btnEnableDisableWiFi).subscribe { _ ->

            Log.v(TAG, "btnEnableDisableWiFi::onClick() - Current WiFi status is: $mIsConnected. Requesting WiFiManager to change it...")

            // The default toggle control behavior is to change its state when it is clicked, but we are keeping current state by calling
            // ToggleButton::setChecked() method and delegating such change to the network broadcast based on the network status.
            // This might be a poor solution, since we can face some quick flip when clicking over it.
            btnEnableDisableWiFi.isChecked = mIsConnected

            // Now asks WiFiManager to change WiFi current status
            wifiManager?.isWifiEnabled = !mIsConnected
        }

        // This is the main part of this example. See how easy is to start a broadcast listener when using RxBroadcast. Just create an
        // IntentFilter as we normally do and then call RxBroadcast.fromBroadcast(...) to create an observable. We could also filter
        // messages the same way we do when using RxJava.
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        RxBroadcast
                .fromBroadcast(this, filter)
                .subscribe { _ -> handleNetworkConnectivityChanges() }

        val filter2 = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        RxBroadcast
                .fromBroadcast(this, filter2)
                .subscribe { intent -> handleBatteryChange(intent) }
    }

    /**
     * This method handles network connectivity status received by the system broadcast. Basically it checks whether there is a wifi
     * connection available or not and shows a text message accordingly.
     *
     */
    private fun handleNetworkConnectivityChanges() {
        Log.v(TAG, "handleNetworkConnectivityChanges() - Begin")

        var status = "Not connected to the Internet"

        val cm = applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        mIsConnected = (activeNetwork != null
                && activeNetwork.isConnectedOrConnecting
                && activeNetwork.type == TYPE_WIFI)

        if (mIsConnected) {
            if (activeNetwork.type == TYPE_WIFI) {
                status = "Detected WIFI is enabled"
                btnEnableDisableWiFi.isChecked = true
                Log.v(TAG, "handleNetworkConnectivityChanges() - mIsConnected is true")
            }

            /*// We are not using it since we are only interesting in the WiFi network, but let this code here for future references
            if (activeNetwork.getType() == TYPE_MOBILE) {
                status = "Detected mobile data is enabled";
                btnEnableDisableWiFi.setChecked(true);
            }*/

        } else {
            btnEnableDisableWiFi.isChecked = false
            Log.v(TAG, "handleNetworkConnectivityChanges() - mIsConnected is false")
        }

        Log.d(TAG, status)
        tvNetworkStatus.text = status

        Log.v(TAG, "handleNetworkConnectivityChanges() - End")
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
    private fun handleBatteryChange(intent: Intent) {
        val bundle = intent.extras ?: return

        val isPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        var level = 0

        val usbCharge = plugged == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge = plugged == BatteryManager.BATTERY_PLUGGED_AC

        if (isPresent) {
            if (rawlevel >= 0 && scale > 0) {
                level = rawlevel * 100 / scale
            }

            if (usbCharge) {
                imgBatteryLevel.setBackgroundResource(R.drawable.battery_charging_usb)
                tvBatteryStatus.text = "USB Charging"

                // Full charge
                if (level == 100) {
                    tvBatteryStatus.text = "USB Charging - Full"
                } else {
                    tvBatteryStatus.text = "USB Charging - Battery Level: $level%"
                }

            } else if (acCharge) {
                imgBatteryLevel.setBackgroundResource(R.drawable.battery_charging_ac)
                // Full charge
                if (level == 100) {
                    tvBatteryStatus.text = "AC Charging - Full"
                } else {
                    tvBatteryStatus.text = "AC Charging - Battery Level: $level%"
                }

            } else { // not charging
                tvBatteryStatus.text = "Battery Level: $level%"

                when {
                    level == 100 -> imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_full)
                    level >= 70 -> imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_almost_full)
                    level >= 50 -> imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_half)
                    level >= 30 -> imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_low)
                    level >= 15 -> imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_very_low)
                    else -> imgBatteryLevel.setBackgroundResource(R.drawable.battery_status_empty)
                }
            }
        } else {
            imgBatteryLevel.setBackgroundResource(0)
            tvBatteryStatus.text = "Could not detected any battery on this device"
        }
    }

    companion object {
        private val TAG = BroadcastSystemStatusExampleActivity::class.java.simpleName
        private const val TYPE_WIFI = 1
    }
}
