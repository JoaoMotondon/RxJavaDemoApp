package com.motondon.rxjavademoapp.view.backpressure

import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.Random
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_backpressure_specific_operators_example.*
import rx.BackpressureOverflow
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.schedulers.Schedulers

/**
 *
 * This example shows some specific RxJava operators that handle backpressure
 *
 */
class BackpressureSpecificOperatorsExampleActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backpressure_specific_operators_example)

        btnOnBackpressureBufferOperatorTest.setOnClickListener{ onBackpressureBufferOperatorButtonClick() }
        btnOnBackpressureBufferOperatorWithAnActionTest.setOnClickListener{ onBackpressureBufferOperatorWithAnActionButtonClick() }
        btnOnBackpressureBufferOperatorWithAnActionAndStrategyTest.setOnClickListener{ onBackpressureBufferOperatorWithAnActionAndStrategyButtonClick() }
        btnOnBackpressureDropOperatorTest.setOnClickListener{ onBackpressureDropOperatorButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")

        resetData()
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""

        tvOverrunNumbers.text = ""
        tvOverrunNumbers.tag = null

        tvOverrunCaption.text = ""
    }
    
    fun onBackpressureBufferOperatorButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onBackpressureBufferOperatorButtonClick()")
            resetData()
            startBackpressureBufferOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_LONG).show()
        }
    }
    
    fun onBackpressureBufferOperatorWithAnActionButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onBackpressureBufferOperatorWithAnActionButtonClick()")
            resetData()
            tvOverrunCaption.text = "Overrun"
            tvOverrunNumbers.tag = 0
            startBackpressureBufferOperatorWithAnActionTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_LONG).show()
        }
    }

    fun onBackpressureBufferOperatorWithAnActionAndStrategyButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onBackpressureBufferOperatorWithAnActionAndStrategyButtonClick()")
            resetData()
            tvOverrunCaption.text = "Number of discarded items"
            tvOverrunNumbers.tag = 0
            startBackpressureBufferOperatorWithAnActionAndStrategyTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_LONG).show()
        }
    }

    fun onBackpressureDropOperatorButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onBackpressureDropOperatorButtonClick()")
            resetData()
            tvOverrunCaption.text = "Dropped items"
            startBackpressureDropOperatorTest()
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
     * onBackpressureBuffer maintains a buffer of all emissions from the source Observable and emits them to downstream Subscribers according
     * to the requests they generate. If the buffer overflows, the observable will fail.
     *
     * @return
     */
    private fun startBackpressureBufferOperatorTest(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitNumbers(100, 50) }

            // We are requesting observable to emit 100 items, but our BackpressureBuffer will be able to buffer only
            // 40 item. Since we are simulating a heavy processing by sleeping for 200ms on each item, when our buffer
            // hits 40 items, it will emit a Overflowed exception
            .onBackpressureBuffer(40)

            // Just for log purpose
            .compose(showDebugMessages("onBackpressureBuffer"))

            .observeOn(Schedulers.newThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(200))
    }

    private fun startBackpressureBufferOperatorWithAnActionTest(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitNumbers(100, 50) }

            // We are requesting observable to emit 100 items, but our BackpressureBuffer will be able to buffer only
            // 40 item. Since we are simulating a heavy processing by sleeping for 200ms on each item, when our buffer
            // hits 40 items, it will emit a Overflowed exception, BUT since we are using a variant which accepts an action
            // we can take some when it happens (actually we are just printing something on the screen).
            .onBackpressureBuffer(40) {
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule {
                    val count = tvOverrunNumbers.tag as Int + 1
                    Log.d(TAG, "Buffer overrun for: $count time(s)")
                    tvOverrunNumbers.tag = count

                    tvOverrunNumbers.text = "${tvOverrunNumbers.text}Ops, buffer overrun!"
                }
            }

            // Just for log purpose
            .compose(showDebugMessages("onBackpressureBuffer"))

            .observeOn(Schedulers.newThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(200))
    }

    private fun startBackpressureBufferOperatorWithAnActionAndStrategyTest(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitNumbers(100, 10) }

            // We are requesting observable to emit 100 items, but our BackpressureBuffer will be able to buffer only
            // 40 item. Since we are simulating a hard processing by sleeping for 200ms on each item, when our buffer
            // hits 40 items, it will follow the overflow strategy. On our case, since we are using ON_OVERFLOW_DROP_OLDEST,
            // it will drop the oldest item whenever it runs into this situation.
            .onBackpressureBuffer(
                40,
                Action0 {
                    val w = AndroidSchedulers.mainThread().createWorker()
                    w.schedule {

                        // Get some info here. They will be used later in onComplete to show the user.
                        val count = tvOverrunNumbers.tag as Int + 1
                        Log.d(TAG, "Buffer overrun for: $count time(s)")
                        tvOverrunNumbers.tag = count
                    }
                },
                BackpressureOverflow.ON_OVERFLOW_DROP_OLDEST
            )

            // Just for log purpose
            .compose(showDebugMessages("onBackpressureBuffer"))

            .observeOn(Schedulers.newThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(200))
    }

    /**
     * From the docs:
     *
     * onBackpressureDrop drops emissions from the source Observable unless there is a pending request from a downstream
     * Subscriber, in which case it will emit enough items to fulfill the request.
     *
     * We added a random sleep timer while processing onNext in order to simulate a heavy processing. This will make onBackpressureDrop
     * operator to drop some items, since there will be some lack of requests from the downstream.
     *
     * @return
     */
    private fun startBackpressureDropOperatorTest(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitNumbers(100, 30) }

            // We are requesting observable to emit 100 items, and also are simulating a hard processing by sleeping
            // sometime between 10 and 100ms for each item. Since we are using onBackpressureDrop, overflowed items will be dropped.
            .onBackpressureDrop { droppedItem ->
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvOverrunNumbers.text = "${tvOverrunNumbers.text} $droppedItem" }
            }

            // Just for log purpose
            .compose(showDebugMessages("onBackpressureDrop"))

            .observeOn(Schedulers.newThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(RANDOM_SLEEP_TIME))
    }

    /**
     * This method is used by the onBackpressureBuffer and onBackpressureDrop tests. Since onNext method
     * sleeps for a while (in order to simulate a processing longer than the emitted items), if we do this in
     * the main thread, GUI it might freeze. So, those notifications are being executed in a new worker thread.
     *
     * But, when we need to update GUI, of course we need to do it in the main thread.
     *
     * @param timeToSleep
     * @return
     */
    private fun resultSubscriber(timeToSleep: Int): Subscriber<Int> {
        return object : Subscriber<Int>() {

            override fun onCompleted() {
                Log.v(TAG, "subscribe.onCompleted")
                // Request GUI to update in the main thread, since this notification is being executed in a worker thread.
                val w2 = AndroidSchedulers.mainThread().createWorker()
                w2.schedule {
                    tvResult.text = "${tvResult.text} - onCompleted"

                    // This is useful only for the startBackpressureBufferOperatorWithAnActionAndStrategyTest test, once it uses ON_OVERFLOW_DROP_OLDEST
                    // strategy, making onBackpressureBuffer() operator to not finish with error when its buffer is full, but discard the oldest item.
                    // Each time it discarded an item, we incremented a counter. Now it is time to show the user how many items were discarded.
                    tvOverrunNumbers.tag?.let {
                        if (it is Int) {
                            tvOverrunNumbers.text = it.toString()
                        }
                    }
                }
            }

            override fun onError(e: Throwable) {
                Log.v(TAG, "subscribe.doOnError: $e")
                // Request GUI to update in the main thread, since this notification is being executed in a worker thread.
                val w2 = AndroidSchedulers.mainThread().createWorker()
                w2.schedule { tvResult.text = "${tvResult.text} - doOnError: $e" }
            }

            override fun onNext(number: Int) {
                Log.v(TAG, "subscribe.onNext $number")

                // Sleep for a while in order to simulate a hard processing
                try {
                    var tts = if (timeToSleep == RANDOM_SLEEP_TIME) Random().nextInt(100 - 10) + 10 else timeToSleep
                    Thread.sleep(tts.toLong())

                } catch (e: InterruptedException) {
                    Log.v(TAG, "subscribe.onNext. We got a InterruptedException!")
                }

                // Request GUI to update in the main thread, since this notification is being executed in a worker thread.
                val w2 = AndroidSchedulers.mainThread().createWorker()
                w2.schedule { tvResult.text = "${tvResult.text} $number" }
            }
        }
    }

    companion object {
        private val TAG = BackpressureSpecificOperatorsExampleActivity::class.java.simpleName
        private const val RANDOM_SLEEP_TIME = -1
    }
}
