package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.Random
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_operators_timeout_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

class TimeoutExampleActivity : BaseActivity() {
    /**
     * This is a bonus example. If source takes longer than 500ms to emit items, a timeout will be thrown and retryWhen will delay 5 seconds prior to emit. It source throws error for
     * three times, retryWhen will emit an error and the whole chain will finish
     *
     */
    private var attemptCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_timeout_example)

        btnStartTimetoutOperatorTest.setOnClickListener{ onStartTimeoutTestButtonClick() }
        btnStartTimetoutOperatorWithSecondObservableTest.setOnClickListener{ onStartTimeoutWithSecondObservableTestButtonClick() }
        btnStartTimetoutOperatorWithFunctionTest.setOnClickListener{ onStartTimeoutWithFunctionTestButtonClick() }
        btnStartTimetoutOperatorWithFunctionAndSecondObservableTest.setOnClickListener{ onStartTimeoutWithFunctionAndSecondObservableTestButtonClick() }
        btnStartTimetoutOperatorWithFunctionAndSecondFunctionTest.setOnClickListener{ onStartTimeoutWithFunctionAndSecondFunctionTestButtonClick() }
        btnStartTimetoutOperatorWithFunctionAndSecondFunctionAndSecondObservableTest.setOnClickListener{ onStartTimeoutWithFunctionAndSecondFunctionAndSecondObservableTestButtonClick() }
        btnStartBonusTest.setOnClickListener{ onStartBonusTestButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onStartTimeoutTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartTimeoutTestButtonClick()")
            resetData()
            mSubscription = startTimeoutTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartTimeoutWithSecondObservableTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartTimeoutWithSecondObservableTestButtonClick()")
            resetData()
            mSubscription = startTimeoutWithSecondObservableTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartTimeoutWithFunctionTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartTimeoutWithFunctionTestButtonClick()")
            resetData()
            mSubscription = startTimeoutWithFunction()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartTimeoutWithFunctionAndSecondObservableTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartTimeoutWithFunctionAndSecondObservableTestButtonClick()")
            resetData()
            mSubscription = startTimeoutWithFunctionAndSecondObservable()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartTimeoutWithFunctionAndSecondFunctionTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartTimeoutWithFunctionAndSecondFunctionTestButtonClick()")
            resetData()
            mSubscription = startTimeoutWithAFunctionForTheFirstEmittedItemAndAnotherFunctionForTheOtherItems()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartTimeoutWithFunctionAndSecondFunctionAndSecondObservableTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartTimeoutWithFunctionAndSecondFunctionAndSecondObservableTestButtonClick()")
            resetData()
            mSubscription = startTimeoutWithFunctionAndASecondFunctionAndASecondObservable()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }


    private fun onStartBonusTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartBonusTestButtonClick()")
            resetData()
            mSubscription = startBonusTest_TimeoutWithRetry()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun emitItems(numberOfItems: Int): Observable<Int> {
        Log.v(TAG, "emitItems() - numberOfItems: $numberOfItems")

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(0, numberOfItems)

            .doOnNext { number ->
                try {
                    val timeout = Random().nextInt(700 - 100) + 100

                    Log.v(TAG, "Item: $number will be emitted with a delay around: ${timeout}ms")
                    Thread.sleep(timeout.toLong())
                    Log.v(TAG, "emitItems() - Emitting number: $number")

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }
    }

    /**
     * Useful for the tests that uses a second observable in case of a timeout.
     *
     * @return
     */
    private fun emitSecondItems(): Observable<Int> {
        Log.v(TAG, "emitSecondItems()")

        return Observable
            .range(100, 5)
            .doOnNext { number ->
                try {
                    Log.v(TAG, "emitSecondItems() - Emitting number: $number")
                    Thread.sleep(100)

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }
    }

    /**
     * If source takes more than 500ms to emit, an error is emitted.
     *
     * @return
     */
    private fun startTimeoutTest(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitItems(20) }

            .timeout(500, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("timeout"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example will trigger a second observable in case of the first observable takes more than 500ms to emit
     *
     * @return
     */
    private fun startTimeoutWithSecondObservableTest(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitItems(20) }

            .timeout(500, TimeUnit.MILLISECONDS, emitSecondItems())

            // Just for log purpose
            .compose(showDebugMessages("timeout(300ms)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example applies different timeouts based on whether emitted items are even or odd
     *
     * @return
     */
    private fun startTimeoutWithFunction(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitItems(20) }

            .timeout<Long> { number ->
                // For even emitted numbers, a 300ms timeout is applied, otherwise 600ms.
                if (number % 2 == 0) {
                    return@timeout Observable.timer(300, TimeUnit.MILLISECONDS)
                } else {
                    return@timeout Observable.timer(600, TimeUnit.MILLISECONDS)
                }
            }

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example is basically a combination of the two previous examples: startTimeoutWithSecondObservableTest and startTimeoutWithFunction
     *
     * @return
     */
    private fun startTimeoutWithFunctionAndSecondObservable(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitItems(20) }

            .timeout<Long>({ number ->
                // For even emitted numbers, a timeout of 300ms is used, otherwise 600ms. For any item, in case of timeout, a second observable is triggered.
                if (number % 2 == 0) {
                    return@timeout Observable.timer(300, TimeUnit.MILLISECONDS)
                } else {
                    return@timeout Observable.timer(600, TimeUnit.MILLISECONDS)
                }
            }, emitSecondItems())

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function and a second observable)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example applies a function for the first item by defining a timeout only for it. Then, it defines a second function
     * that will be applied to all remaining items.
     *
     * @return
     */
    private fun startTimeoutWithAFunctionForTheFirstEmittedItemAndAnotherFunctionForTheOtherItems(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitItems(20) }

            .timeout<Long, Long>({

                // This function will be applied only for the first emitted item
                Observable.timer(6500, TimeUnit.MILLISECONDS)}, {
                    number ->
                        // For all the other emitted items, for even numbers, a timeout of 300ms is used, otherwise 600ms.
                        if (number % 2 == 0) {
                            return@timeout Observable.timer(300, TimeUnit.MILLISECONDS)
                        } else {
                            return@timeout Observable.timer(600, TimeUnit.MILLISECONDS)
                        }
                    })

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function and a second function)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startTimeoutWithFunctionAndASecondFunctionAndASecondObservable(): Subscription {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitItems(20) }

            .timeout<Long, Long>({

                // This function will be applied only for the first emitted item.
                Observable.timer(500, TimeUnit.MILLISECONDS)}, {
                    number ->
                        // For all the other emitted items, for even numbers, a timeout of 300ms is used, otherwise 600ms.
                        if (number % 2 == 0) {
                            return@timeout Observable.timer(300, TimeUnit.MILLISECONDS)
                        } else {
                            return@timeout Observable.timer(600, TimeUnit.MILLISECONDS)
                        }
                    },

                // In case  of timeout on any item (i.e. the first one that uses an exclusive function or any other), this
                // observable will be used instead of terminating with an error.
                emitSecondItems())

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function, a second function and a second observable)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startBonusTest_TimeoutWithRetry(): Subscription {

        attemptCount = 0

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ -> emitItems(20) }

            .timeout(500, TimeUnit.MILLISECONDS)

            .retryWhen { error -> error
                .flatMap<Any> { _ ->
                    if (++attemptCount >= MAX_ATTEMPTS) {
                        return@flatMap Observable.error<Any>(Throwable("Reached max number of attempts ($MAX_ATTEMPTS). Emitting an error..."))
                    } else {
                        Log.v(TAG, "Wait for 5 seconds prior to retry")
                        return@flatMap Observable.timer(5, TimeUnit.SECONDS) as Observable
                    }
                }
            }

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function, a second function and a second observable)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    companion object {
        private val TAG = TimeoutExampleActivity::class.java.simpleName
        private const val MAX_ATTEMPTS = 3
    }
}
