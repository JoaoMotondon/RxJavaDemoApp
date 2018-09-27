package com.motondon.rxjavademoapp.view.hotobservables

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.HotObservablesBaseActivity

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_hot_observable_connect_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * This example demonstrates how to use ConnectableObservable::connect() operator.
 *
 */
class HotObservableConnectExampleActivity : HotObservablesBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_observable_connect_example)

        btnConnect.setOnClickListener{ onConnectButtonClick() }
        btnSubscribeFirst.setOnClickListener{ onSubscribeFirstButtonClick() }
        btnSubscribeSecond.setOnClickListener{ onSubscribeSecondButtonClick() }
        btnUnsubscribeFirst.setOnClickListener{ onUnsubscribeFirstButtonClick() }
        btnUnsubscribeSecond.setOnClickListener{ onUnsubscribeSecondButtonClick() }
        btnDisconnect.setOnClickListener{ onDisconnectButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")

        // We will create the ConnectableObservable in the onCreate() method, so it will be available for the two
        // subscribers to subscribe to it, even before call ConnectionObservable::connect().
        connectable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .doOnNext { number ->
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }

            // This will convert the Observable to a ConnectableObservable
            .publish()
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResultFirstSubscription.text = ""
        tvResultSecondSubscription.text = ""
    }

    private fun onConnectButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onConnectButtonClick()")
            resetData()
            mSubscription = connect()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSubscribeFirstButtonClick() {
        if (isFirstSubscriptionUnsubscribed()) {
            Log.v(TAG, "onSubscribeFirstButtonClick()")
            firstSubscription = subscribeFirst()
        } else {
            Toast.makeText(applicationContext, "First subscriber already started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSubscribeSecondButtonClick() {
        if (isSecondSubscriptionUnsubscribed()) {
            Log.v(TAG, "onSubscribeSecondButtonClick()")
            secondSubscription = subscribeSecond()
        } else {
            Toast.makeText(applicationContext, "Second subscriber already started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onUnsubscribeFirstButtonClick() {
        Log.v(TAG, "onUnsubscribeFirstButtonClick()")
        unsubscribeFirst(true)
    }

    private fun onUnsubscribeSecondButtonClick() {
        Log.v(TAG, "onUnsubscribeSecondButtonClick()")
        unsubscribeSecond(true)
    }

    private fun onDisconnectButtonClick() {
        Log.v(TAG, "onDisconnectButtonClick()")

        mSubscription?.let {
            if (!it.isUnsubscribed) {
                unsubscribeFirst(false)
                unsubscribeSecond(false)
                disconnect()
            }
        } ?: Toast.makeText(applicationContext, "Observable not connected", Toast.LENGTH_SHORT).show()
    }

    /**
     * From the docs:
     *
     * "A Connectable Observable resembles an ordinary Observable, except that it does not begin emitting items when
     * it is subscribed to, but only when its connect() method is called."
     *
     * This means that when user clicks on "connect" button, if there is already any subscriber subscribed to it, it will start
     * receiving emitted items, otherwise, emitted items will be discarded.
     *
     * After connecting to the observable, new subscribers will only receive new emitted items.
     *
     * @return
     */
    private fun connect(): Subscription? {
        Log.v(TAG, "connect()")

        // This will instruct the connectable observable to begin emitting items. If there is any subscriber subscribed to it,
        // it will start receiving items.
        return connectable?.connect()
    }

    /**
     * If observable is already connected, when this button is pressed, this subscriber will start receiving items. If there is no
     * connection yet, nothing will happen (until a mSubscription)
     *
     * @return
     */
    private fun subscribeFirst(): Subscription? {
        Log.v(TAG, "subscribeFirst()")

        return connectable?.let {
            it
            .compose(applySchedulers())
            .subscribe(resultSubscriber(tvResultFirstSubscription))
        } ?: null
    }

    /**
     * If observable is already connected, when this button is pressed, this subscriber will start receiving items. If there is no
     * connection yet, nothing will happen (until a mSubscription)
     *
     * @return
     */
    private fun subscribeSecond(): Subscription? {
        Log.v(TAG, "subscribeSecond()")

        return connectable?.let {
            it
                .compose(applySchedulers())
                .subscribe(resultSubscriber(tvResultSecondSubscription))
        } ?: null
    }

    /**
     * By unsubscribing the mSubscription returned by the connect() method, all subscriptions will stop receiving items.
     *
     */
    private fun disconnect() {
        Log.v(TAG, "disconnect()")
        mSubscription?.unsubscribe()
        mSubscription = null
    }

    companion object {
        private val TAG = HotObservableConnectExampleActivity::class.java.simpleName
    }
}
