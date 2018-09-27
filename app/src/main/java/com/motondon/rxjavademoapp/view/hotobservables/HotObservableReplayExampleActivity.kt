package com.motondon.rxjavademoapp.view.hotobservables

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.HotObservablesBaseActivity

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_hot_observable_replay_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * This example demonstrates how to use ConnectableObservable::replay() operator.
 *
 * As soon as we call connect(), our observable will start emit items and also collect them (depends on the replay() variation,
 * collected items might vary)
 *
 * Later, as we subscribe our subscribers, it will "replay" all collected items.
 *
 */
class HotObservableReplayExampleActivity : HotObservablesBaseActivity() {

    private var replyOk = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_observable_replay_example)


        btnReplay.setOnClickListener{ onReplayButtonClick() }
        btnReplayWithBufferSize.setOnClickListener{ onReplayWithBufferSizeButtonClick() }
        btnReplayWithTime.setOnClickListener{ onReplayWithTimeButtonClick() }

        btnConnect.setOnClickListener{ onConnectButtonClick() }
        btnSubscribeFirst.setOnClickListener{ onSubscribeFirstButtonClick() }
        btnSubscribeSecond.setOnClickListener{ onSubscribeSecondButtonClick() }
        btnUnsubscribeFirst.setOnClickListener{ onUnsubscribeFirstButtonClick() }
        btnUnsubscribeSecond.setOnClickListener{ onUnsubscribeSecondButtonClick() }
        btnDisconnect.setOnClickListener{ onDisconnectButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResultFirstSubscription.text = ""
        tvResultSecondSubscription.text = ""
    }

    private fun onReplayButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onReplayButtonClick()")
            resetData()
            replay()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onReplayWithBufferSizeButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onReplayWithBufferSizeButtonClick()")
            resetData()
            replayWithBufferSize()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onReplayWithTimeButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onReplayWithTimeButtonClick()")
            resetData()
            replayWithTime()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onConnectButtonClick() {

        if (!replyOk) {
            Toast.makeText(applicationContext, "Please, choose one replay option prior to connect.", Toast.LENGTH_SHORT).show()
            return
        }

        if (isUnsubscribed()) {
            Log.v(TAG, "onConnectButtonClick()")
            resetData()
            mSubscription = connect()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSubscribeFirstButtonClick() {
        if (!replyOk) {
            Toast.makeText(applicationContext, "Please, choose one replay option prior to subscribe.", Toast.LENGTH_SHORT).show()
            return
        }

        if (isFirstSubscriptionUnsubscribed()) {
            Log.v(TAG, "onSubscribeFirstButtonClick()")
            firstSubscription = subscribeFirst()
        } else {
            Toast.makeText(applicationContext, "First subscriber already started", Toast.LENGTH_SHORT).show()
        }
    }


    private fun onSubscribeSecondButtonClick() {
        if (!replyOk) {
            Toast.makeText(applicationContext, "Please, choose one replay option prior to subscribe.", Toast.LENGTH_SHORT).show()
            return
        }

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
     * When calling replay, observable will start emit and replay() operator will collect them.
     *
     * @return
     */
    private fun replay() {
        Log.v(TAG, "replay()")

        // First create our observable. Later, when calling connect, it will start emitting items and collecting them.
        connectable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .doOnNext { number ->
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }
            .replay()

        // Used only to prevent user to click on the connect button prior to choose one of the replay() approaches.
        replyOk = true
    }

    /**
     * When calling replay(bufferSize), observable will start emit and collect items. It will replay at most [bufferSize] items
     * emitted by the observable.
     *
     * @return
     */
    private fun replayWithBufferSize() {
        Log.v(TAG, "replayWithBufferSize(5)")

        // First create our observable. Later, when calling connect, it will start emitting items and collecting at most N them.
        connectable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .doOnNext { number ->
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }

            // Replay the last 5 emitted items.
            .replay(5)

        // Used only to prevent user to click on the connect button prior to choose one of the replay() approaches.
        replyOk = true
    }

    /**
     * When calling replay(time), observable will start emit and collect items. Upon mSubscription, it will replay all items emitted
     * by the observable within a specified time window.
     *
     * @return
     */
    private fun replayWithTime() {
        Log.v(TAG, "replayWithTime(6 seconds)")

        // First create our observable. Later, when calling connect, it will start emitting items and collecting them.
        connectable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .doOnNext { number ->
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }

            // Replay all items emitted by the observable within this time window.
            .replay(6, TimeUnit.SECONDS)

        // Used only to prevent user to click on the connect button prior to choose one of the replay() approaches.
        replyOk = true
    }

    /**
     * When this button is pressed, observable will start emitting items. If there is already a mSubscription, it will start receiving
     * emitted items. Otherwise emitted items will be collected and after a mSubscription, they will be replayed.
     *
     * @return
     */
    private fun connect(): Subscription? {
        Log.v(TAG, "connect()")
        return connectable?.connect()
    }

    /**
     * If we are already connected to the observable, when this button is pressed, all emitted items will be replayed (depends on
     * which replay variant we are testing, replayed items might vary).
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
     * If we are already connected to the observable, when this button is pressed, all emitted items will be replayed (depends on
     * which replay variant we are testing, replayed items might vary).
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
     * By unsubscribing the subscriber returned by the connect() method, all subscriptions will stop receiving items.
     *
     */
    private fun disconnect() {
        Log.v(TAG, "disconnect()")
        mSubscription?.unsubscribe()

        replyOk = false

    }

    companion object {
        private val TAG = HotObservableReplayExampleActivity::class.java.simpleName
    }
}
