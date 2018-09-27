package com.motondon.rxjavademoapp.view.hotobservables

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.HotObservablesBaseActivity

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_hot_observable_refcount_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * This example demonstrates how to use ConnectableObservable::refCount() operator.
 *
 * refCount keeps a reference to all the subscribers subscribed to it. When we call refCount, observable does not start emitting items, but only when
 * the first subscriber subscribe to it.
 *
 */
class HotObservableRefCountExampleActivity : HotObservablesBaseActivity() {

    // This is the Observable returned by the refCount() method call. Subscribers must use it to subscribe.
    private var observable: Observable<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_observable_refcount_example)

        btnRefCount.setOnClickListener{ onRefCountButtonClick() }
        btnSubscribeFirst.setOnClickListener{ onSubscribeFirstButtonClick() }
        btnSubscribeSecond.setOnClickListener{ onSubscribeSecondButtonClick() }
        btnUnsubscribeFirst.setOnClickListener{ onUnsubscribeFirstButtonClick() }
        btnUnsubscribeSecond.setOnClickListener{ onUnsubscribeSecondButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")

        // Just start our Hot Observable. Note that this will NOT make it to start emitting items
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

    private fun onRefCountButtonClick() {

        if (isFirstSubscriptionUnsubscribed() && (isSecondSubscriptionUnsubscribed())) {

            Log.v(TAG, "onRefCountButtonClick()")
            resetData()
            refCount()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSubscribeFirstButtonClick() {
        if (observable == null) {
            Log.v(TAG, "onSubscribeFirstButtonClick() - Cannot start a subscriber. You must first call refCount().")

            // When using refCount, we must subscribe our subscriber's upon the Observable returned by the refCount, and not on the
            // ConnectableObservable returned by the publish() (as we do when using connect() operator).
            Toast.makeText(applicationContext, "You must first call refCount()", Toast.LENGTH_SHORT).show()
            return
        }

        if (isFirstSubscriptionUnsubscribed()) {

            // Just clean up GUI in order to make things clear.
            secondSubscription?.let {
                if (it.isUnsubscribed) resetData()
            }

            Log.v(TAG, "onSubscribeFirstButtonClick()")
            firstSubscription = subscribeFirst()

        } else {
            Toast.makeText(applicationContext, "First subscriber already started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSubscribeSecondButtonClick() {
        if (observable == null) {
            Log.v(TAG, "onSubscribeSecondButtonClick() - Cannot start a subscriber. You must first call refCount().")

            // When using refCount, we must subscribe our subscriber's upon the Observable returned by the refCount, and not on the
            // ConnectableObservable returned by the publish() (as we do when using connect() operator).
            Toast.makeText(applicationContext, "You must first call refCount()", Toast.LENGTH_SHORT).show()
            return
        }

        if (isSecondSubscriptionUnsubscribed()) {

            // Just clean up GUI in order to make things clear.
            firstSubscription?.let {
                if (it.isUnsubscribed) resetData()
            }

            Log.v(TAG, "onSubscribeSecondButtonClick()")
            secondSubscription = subscribeSecond()

        } else {
            Toast.makeText(applicationContext, "Second subscriber already started", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * When unsubscribing a subscriber, if there is no more subscriber subscribed to the observable, it will stop emit items.
     *
     */
    private fun onUnsubscribeFirstButtonClick() {
        Log.v(TAG, "onUnsubscribeFirstButtonClick()")
        unsubscribeFirst(true)
    }

    /**
     * When unsubscribing a subscriber, if there is no more subscriber subscribed to the observable, it will stop emit items.
     *
     */
    private fun onUnsubscribeSecondButtonClick() {
        Log.v(TAG, "onUnsubscribeSecondButtonClick()")
        unsubscribeSecond(true)
    }

    private fun refCount() {
        Log.v(TAG, "refCount()")

        // refCount returns Observable<T> that is connected as long as there are subscribers to it.
        observable = connectable?.refCount()
    }

    /**
     * If this is the first subscriber to subscribe to the observable, it will make observable to start emitting items.
     *
     * @return
     */
    private fun subscribeFirst(): Subscription? {
        Log.v(TAG, "subscribeFirst()")

        return observable?.let {
            it
                .compose(applySchedulers())
                .subscribe(resultSubscriber(tvResultFirstSubscription))
        } ?: null
    }

    /**
     * If this is the first subscriber to subscribe to the observable, it will make observable to start emitting items.
     *
     * @return
     */
    private fun subscribeSecond(): Subscription? {
        Log.v(TAG, "subscribeSecond()")

        return observable?.let {
            it
                .compose(applySchedulers())
                .subscribe(resultSubscriber(tvResultSecondSubscription))
        } ?: null
    }

    companion object {
        private val TAG = HotObservableRefCountExampleActivity::class.java.simpleName
    }
}
