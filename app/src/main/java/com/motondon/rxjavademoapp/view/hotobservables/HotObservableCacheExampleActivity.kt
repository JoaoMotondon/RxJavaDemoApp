package com.motondon.rxjavademoapp.view.hotobservables

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.HotObservablesBaseActivity

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_hot_observable_cache_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * This example demonstrates how to use ConnectableObservable::cache() operator.
 *
 * It ensures that all observers see the same sequence of emitted items, even if they subscribe after the Observable has begun emitting items.
 *
 */
class HotObservableCacheExampleActivity : HotObservablesBaseActivity() {

    private var observable: Observable<Long>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_observable_cache_example)

        btnCache.setOnClickListener{ onCacheButtonClick() }
        btnSubscribeFirst.setOnClickListener{ onSubscribeFirstButtonClick() }
        btnSubscribeSecond.setOnClickListener{ onSubscribeSecondButtonClick() }
        btnUnsubscribeFirst.setOnClickListener{ onUnsubscribeFirstButtonClick() }
        btnUnsubscribeSecond.setOnClickListener{ onUnsubscribeSecondButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResultFirstSubscription.text = ""
        tvResultSecondSubscription.text = ""
    }

    private fun onCacheButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onCacheButtonClick()")
            resetData()
            cache()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onSubscribeFirstButtonClick() {

        if (observable == null) {
            Log.v(TAG, "onSubscribeFirstButtonClick() - Cannot start a subscriber. You must first call cache().")

            Toast.makeText(applicationContext, "You must first call cache()", Toast.LENGTH_SHORT).show()
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

        if (observable == null) {
            Log.v(TAG, "onSubscribeFirstButtonClick() - Cannot start a subscriber. You must first call cache().")

            Toast.makeText(applicationContext, "You must first call cache()", Toast.LENGTH_SHORT).show()
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

    /**
     * When calling cache, that will NOT make observable to start emit items. It will only start emitting items when a first
     * subscriber subscribes to it. Then, it will receive all cached items.
     *
     * Just using take(30) in order to prevent it to emit forever.
     *
     * @return
     */
    private fun cache() {
        Log.v(TAG, "cache()")

        // cache returns Observable<T> that is connected as long as there are subscribers to it.
        observable = Observable
            .interval(750, TimeUnit.MILLISECONDS)
            .doOnNext { number ->
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }

            // Prevent our observable to emit forever
            .take(30)

            .cache()
    }

    /**
     * If this is the first mSubscription, it will make observable to start emitting items. But, if there is
     * already another mSubscription, it means that observable has already started emitting items and collecting them.
     * So, after we subscribe to it, it will first receive all collected items.
     *
     * @return
     */
    private fun subscribeFirst(): Subscription? {
        Log.v(TAG, "subscribeFirst()")

        return observable?.let {
            it
                .compose(applySchedulers())
                .subscribe(this@HotObservableCacheExampleActivity.resultSubscriber(tvResultFirstSubscription))
        } ?: null
    }

    /**
     * If this is the first mSubscription, it will make observable to start emitting items. But, if there is
     * already another mSubscription, it means that observable has already started emitting items and collecting them.
     * So, after we subscribe to it, it will first receive all collected items.
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
        private val TAG = HotObservableCacheExampleActivity::class.java.simpleName
    }
}
