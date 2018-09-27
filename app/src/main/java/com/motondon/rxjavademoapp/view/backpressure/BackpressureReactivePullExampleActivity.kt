package com.motondon.rxjavademoapp.view.backpressure

import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_backpressure_reactive_pull_example.*
import rx.Observable

import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers


class BackpressureReactivePullExampleActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backpressure_reactive_pull_example)

        btnSubscriberWithRequestMethodCallTest.setOnClickListener { onSubscriberWithRequestMethodCallButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onSubscriberWithRequestMethodCallButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onSubscriberWithRequestMethodCallButtonClick()")
            resetData()
            startSubscriberWithRequestMethodCallTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_LONG).show()
        }
    }

    private fun emitNumbers(numberOfItemsToEmit: Int, timeToSleep: Int): Observable<Int> {
        Log.v(TAG, "emitNumbers()")

        return Observable
                .range(0, numberOfItemsToEmit)
            .doOnNext { number ->
                try {
                    Log.v(TAG, "emitNumbers() - Emitting number: $number")
                    Thread.sleep(timeToSleep.toLong())

                    val w = AndroidSchedulers.mainThread().createWorker()
                    w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }
            }
    }

    /**
     * This example demonstrates how to use Subscriber::request() method. Since our subscriber was initialized by calling
     * Subscriber::request(1), that means one item will be requested at a time. Later, in Subscriber's onNext, only after
     * it processes an item, another one will be requested.
     *
     * @return
     */
    private fun startSubscriberWithRequestMethodCallTest(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)
            .flatMap { _ -> emitNumbers(20, 10) }

            // Since our subscriber will request for item, this is one way on how we can log it.
            .doOnRequest { number -> Log.v(TAG, "Requested $number") }

            // Subscribe our subscriber which will request for items.
            .subscribe(resultSubscriber())
    }

    private fun resultSubscriber(): Subscriber<Int> {
        return object : Subscriber<Int>() {

            override fun onStart() {
                request(1)
            }

            override fun onCompleted() {
                Log.v(TAG, "subscribe.onCompleted")
                val w2 = AndroidSchedulers.mainThread().createWorker()
                w2.schedule { tvResult.text = "${tvResult.text} - onCompleted" }
            }

            override fun onError(e: Throwable) {
                Log.v(TAG, "subscribe.doOnError: $e")
                val w2 = AndroidSchedulers.mainThread().createWorker()
                w2.schedule { tvResult.text = "${tvResult.text} - doOnError$e" }
            }

            override fun onNext(number: Int?) {
                Log.v(TAG, "subscribe.onNext $number")

                try {
                    // Sleep for a while. We could do whatever we want here prior to request a new item. This is totally
                    // up to us
                    Thread.sleep(500)

                    // Now, after "processing" the item, request observable to emit another one
                    request(1)
                } catch (e: InterruptedException) {
                    Log.v(TAG, "subscribe.onNext. We got a InterruptedException!")
                }

                val w2 = AndroidSchedulers.mainThread().createWorker()
                w2.schedule { tvResult.text = tvResult.text.toString() + " " + number }
            }
        }
    }

    companion object {
        private val TAG = BackpressureReactivePullExampleActivity::class.java.simpleName
    }
}
