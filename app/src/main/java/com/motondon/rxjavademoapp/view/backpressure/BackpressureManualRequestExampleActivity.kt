package com.motondon.rxjavademoapp.view.backpressure

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import com.motondon.rxjavademoapp.R.id.tvResult
import kotlinx.android.synthetic.main.activity_backpressure_manual_request_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.functions.Action1
import rx.schedulers.Schedulers

/**
 * This example was based on the following article:
 * https://github.com/Froussios/Intro-To-RxJava/blob/master/Part%204%20-%20Concurrency/4.%20Backpressure.md
 *
 */
class BackpressureManualRequestExampleActivity : BaseActivity() {

    private lateinit var puller: ControlledPullSubscriber<Int>

    private var numberOfItemsToEmit = -1
    private var numberOfItemsToRequest = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backpressure_manual_request_example)

        btnInitTest.setOnClickListener { onInitTestButtonClick() }
        btnRequestItems.setOnClickListener { onRequestItemsButtonClick()}

        supportActionBar?.title = intent.getStringExtra("TITLE")

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun resetData() {
        tvNumberOfRequestedItems.text = ""
        tvEmittedItems.text = ""
        tvResult.text = ""
    }

    private fun onInitTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onInitTestButtonClick()")

            resetData()
            readNumberOfItemsToEmit()

            initTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun onRequestItemsButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onRequestItemsButtonClick()")

            readNumberOfItemsToRequest()
            requestItems()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_LONG).show()
        }
    }

    private fun readNumberOfItemsToEmit() {
        numberOfItemsToEmit = Integer.parseInt(etNumberOfItemsToEmit.text.toString())
        if (numberOfItemsToEmit < 0) {
            numberOfItemsToEmit = 0
            etNumberOfItemsToEmit.setText(numberOfItemsToEmit.toString())
        } else if (numberOfItemsToEmit > 100) {
            numberOfItemsToEmit = 100
            etNumberOfItemsToEmit.setText(numberOfItemsToEmit.toString())
        }
    }

    private fun readNumberOfItemsToRequest() {
        numberOfItemsToRequest = Integer.parseInt(etNumberOfItemsToRequest.text.toString())
        if (numberOfItemsToRequest < 0) {
            numberOfItemsToRequest = 0
            etNumberOfItemsToRequest.setText(numberOfItemsToRequest.toString())
        } else if (numberOfItemsToRequest > 10) {
            numberOfItemsToRequest = 10
            etNumberOfItemsToRequest.setText(numberOfItemsToRequest.toString())
        }
    }

    private fun emitNumbers(): Observable<Int> {
        Log.v(TAG, "emitNumbers() - numberOfItemsToEmit: $numberOfItemsToEmit")

        return Observable
            .range(0, numberOfItemsToEmit)
            .doOnNext { number ->
                try {
                    Log.v(TAG, "emitNumbers() - Emitting number: $number")
                    Thread.sleep(100)

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedItems.text = "${tvEmittedItems.text} $number" }
            }
    }

    /**
     * When the test is initialized, we need first to instantiate our Subscriber. Then we subscribe an observable to it.
     *
     * But note that, since our Subscriber (i.e.: puller) was initialized by calling Subscriber::request(0) in the
     * onStart method, no item will be emitted until a call to the Subscriber::request(n) is made with a value greater
     * than zero. This is what will happen when user clicks on the "Request Items" button.
     *
     */
    private fun initTest() {

        puller = ControlledPullSubscriber(
            Action0 {
                Log.v(TAG, "ControlledPullSubscriber.onCompleted")

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvResult.text = "${tvResult.text} - onCompleted" }
            },
            Action1 { throwable ->
                Log.v(TAG, "ControlledPullSubscriber.doOnError: ${throwable.message}")

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvResult.text = "${tvResult.text} - doOnError" }
            },
            Action1 { integer ->
                Log.v(TAG, "ControlledPullSubscriber.onNext")

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvResult.text = "${tvResult.text} $integer" }
            }
        )

        emitNumbers()

            .subscribeOn(Schedulers.computation())

            // This side effect method will allow us to see each time items are requested to the observable.
            .doOnRequest { item -> Log.v(TAG, "Requested: $item") }

            // Subscribe our subscriber which will request for items.
            .subscribe(puller)
    }

    /**
     * When user clicks on the "Request Items" button, we just call our subscriber's requestMore method, which will
     * call Subscriber::request(n) method. This will make the subscriber to request the observable to emit more N items.
     *
     * This way we have total control over how many/when items are emitted.
     *
     */
    private fun requestItems() {
        puller.requestMore(numberOfItemsToRequest)
    }

    /**
     * This is our Subscriber which starts requesting zero items to the observable (when it is subscriber).  It also
     * exposes a requestMore(n) method which is called whenever user wants to tells observable to emit items.
     *
     * @param <T>
    </T> */
    inner class ControlledPullSubscriber<T>(
            private val onCompletedAction: Action0,
            private val onErrorAction: Action1<Throwable>,
            private val onNextAction: Action1<T>) : Subscriber<T>() {

        override fun onStart() {
            // Since we request no items at the beginning, that means we are telling the Observable to not emit any item unless we request it
            // by calling request() method. This is what requestMore() method does. By doing this, we will have complete control over it, since
            // we can call requestMore() at any time when we are able to process data.
            request(0)
        }

        override fun onCompleted() {
            onCompletedAction.call()
        }

        override fun onError(e: Throwable) {
            onErrorAction.call(e)
        }

        override fun onNext(t: T) {
            onNextAction.call(t)
        }

        fun requestMore(n: Int) {
            request(n.toLong())
        }
    }

    companion object {
        private val TAG = BackpressureManualRequestExampleActivity::class.java.simpleName
    }
}
