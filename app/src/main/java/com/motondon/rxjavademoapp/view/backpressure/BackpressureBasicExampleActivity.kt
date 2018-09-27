package com.motondon.rxjavademoapp.view.backpressure

import android.os.Bundle
import android.util.Log

import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_backpressure_basic_example.*
import rx.Observable

import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * This activity shows two examples: one that emits items faster than they can be consumed. Quickly it will finish with a MissingBackpressureException.
 * The second one adds the throttleLast() operator to the chain in order to try to alliviate emitted items downstream to try to avoid that exception.
 *
 */
class BackpressureBasicExampleActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backpressure_basic_example)

        btnMissingBackpressureExceptionTest.setOnClickListener { onMissingBackPressureExceptionButtonClick() }
        btnThrottleOperatorTest.setOnClickListener { onThrottleOperatorButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onMissingBackPressureExceptionButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onMissingBackPressureExceptionButtonClick()")
            resetData()
            mSubscription = starMissingBackpressureExceptionTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_LONG).show()
        }
    }

    private fun onThrottleOperatorButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onThrottleOperatorButtonClick()")
            resetData()
            tvEmittedNumbers.text = "Check the logs to see all emitted items"
            startThrottleOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * This example will throw a MissingBackPressureException since the Observable is emitting items much faster
     * than they can be consumed.
     *
     * @return
     */
    private fun starMissingBackpressureExceptionTest(): Subscription {

        // Emit one item per millisecond...
        return Observable
            .interval(1, TimeUnit.MILLISECONDS)

            .doOnNext { number ->
                Log.v(TAG, "oOnNext() - Emitted number: $number")
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }

            .compose(applySchedulers())

            // Sleep for 100ms for each emitted item. This will make we receive a BackpressureMissingException quickly.
            .subscribe(resultSubscriber(100))
    }

    /**
     * By using throttleLast operator (which is pretty much similar to collect) we reduce the chances to get a
     * MissingBackpressureException, since it will only emit the last item emitted in a certain period of time.
     *
     * For this example we are using throttleLast intervalDuration to 100ms, which might be enough to let observer to
     * process all emitted items. If we change intervalDuration to a small value (e.g.: 10ms), although we will still
     * use throttleLast operator, it will not be enough to prevent buffer's capacity to be full. Try both values to see
     * that in action.
     *
     * @return
     */
    private fun startThrottleOperatorTest(): Subscription {

        return Observable
            .interval(1, TimeUnit.MILLISECONDS)

            .doOnNext { number ->
                Log.v(TAG, "doOnNext() - Emitted number: $number")

                // For this example we will not print emitted items on the GUI, since it would freeze it. Check the logs to see all emitted items
                // final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                // w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            }

            // Using throttleLast intervalDuration equals to 100ms, we will probably not end up in an exception, since our subscriber will be able to
            // process all emitted items accordingly. If we change it to 10ms, it will quickly throw a MissingBackpressureException.
            .throttleLast(100, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("throttleLast(100)"))

            // Just adding some boundaries here
            .take(20)

            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(100))
    }

    private fun resultSubscriber(timeToSleep: Int): Subscriber<Long> {
        return object : Subscriber<Long>() {

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

            override fun onNext(number: Long?) {
                Log.v(TAG, "subscribe.onNext $number")
                val w2 = AndroidSchedulers.mainThread().createWorker()
                w2.schedule { tvResult.text = "${tvResult.text} $number" }

                try {
                    Thread.sleep(timeToSleep.toLong())
                } catch (e: InterruptedException) {
                    Log.v(TAG, "subscribe.onNext. We got a InterruptedException!")
                }
            }
        }
    }

    companion object {
        private val TAG = BackpressureBasicExampleActivity::class.java.simpleName
    }
}
