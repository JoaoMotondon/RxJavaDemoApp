package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.support.v4.util.Pair
import android.support.v7.app.ActionBar
import android.util.Log
import android.view.View
import android.widget.*

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_operators_transforming_operators_example.*

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Random
import java.util.concurrent.TimeUnit

import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

class TransformingOperatorsExampleActivity : BaseActivity() {

    // Hold the method name related to the test user chosen in the spinner control.
    private var currentTestMethodName: String = ""

    internal inner class SpinnerOptionAndMethodName(first: String, second: String) : Pair<Any, Any>(first, second) {
        override fun toString(): String {
            return first as String
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_transforming_operators_example)

        sTestOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinnerTestOptionsItemSelected(parent as Spinner, position)
            }
        }
        //sTestOptions.onItemSelectedListener { (spinner, position) -> spinnerTestOptionsItemSelected(spinner, position)}
        btnStartTest.setOnClickListener { onButtonClick() }
        
        // Fill spinner view up with all available test names and their related method names. We will use reflection to call a method based on the user choice
        val testOptions = ArrayList<SpinnerOptionAndMethodName>()
        testOptions.add(SpinnerOptionAndMethodName("buffer() w/count", "startBufferOperatorTestWithCount"))
        testOptions.add(SpinnerOptionAndMethodName("buffer() w/timespan", "startBufferOperatorTestWithTimespan"))
        testOptions.add(SpinnerOptionAndMethodName("buffer() w/count and timespan", "startBufferOperatorTestWithCountAndTimespan"))
        testOptions.add(SpinnerOptionAndMethodName("buffer() w/timespan and timeshift", "startBufferOperatorTestWithTimespanAndTimeshift"))
        testOptions.add(SpinnerOptionAndMethodName("buffer() w/boundary", "startBufferOperatorTestWithBoundary"))
        testOptions.add(SpinnerOptionAndMethodName("buffer() w/selector", "startBufferOperatorTestWithSelector"))
        testOptions.add(SpinnerOptionAndMethodName("window() w/count and timespan", "startWindowOperatorTestWithCountAndTimespan"))
        testOptions.add(SpinnerOptionAndMethodName("scan()", "startScanOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("scan() w/seed", "startScanOperatorWithSeedTest"))

        // Do not show this example, since it is entirely based on a great GIST. See comments below
        //testOptions.add(new SpinnerOptionAndMethodName("scan() Fibonacci", "startScanOperatorFibonacciTest"));

        val adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, testOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sTestOptions.adapter = adapter

        // Set the default method name
        currentTestMethodName = "startFirstOperatorTest"

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun spinnerTestOptionsItemSelected(spinner: Spinner, position: Int) {
        val testItem = spinner.adapter.getItem(position) as SpinnerOptionAndMethodName

        currentTestMethodName = testItem.second as String

        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onButtonClick()")

            tvEmittedNumbers.text = ""
            tvResult.text = ""

            try {

                // Instantiate an object of type method that returns a method name we will invoke
                val m = this.javaClass.getDeclaredMethod(currentTestMethodName)

                // Now, invoke method user selected
                mSubscription = m.invoke(this) as Subscription

            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun emitItems(numberOfItems: Int, randomItems: Boolean, randomDelay: Boolean): Observable<Int> {
        Log.v(TAG, "emitItems() - numberOfItems: $numberOfItems")

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(0, numberOfItems)

            // Generate a random number (for each emitted item)
            .map<Int> { num ->
                if (randomItems) {
                    // Generate a random number (for each emitted item)
                    return@map Random ().nextInt(10)
                } else {
                    return@map num
                }
            }

            .doOnNext { number ->
                try {
                    if (randomDelay) {
                        // Sleep for sometime between 100 and 300 milliseconds
                        Thread.sleep((Random().nextInt(300 - 100) + 100).toLong())
                    } else {
                        // Sleep for 200ms
                        Thread.sleep(200)
                    }
                    Log.v(TAG, "emitItems() - Emitting number: $number")

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }
    }

    private fun doAContinuouslyOperation(showDoneMessage: Boolean): Observable<Int> {
        Log.v(TAG, "doAContinuouslyOperation()")

        return Observable.interval(100, TimeUnit.MILLISECONDS)
            .map { number -> number.toInt() }
            .doOnNext { number ->
                try {
                    runOnUiThread { Toast.makeText(applicationContext, "Starting operation", Toast.LENGTH_SHORT).show() }

                    val timeToSleep = Random().nextInt(6 - 3) + 3
                    Log.v(TAG, "doAContinuouslyOperation() - Sleeping for $timeToSleep second(s)")
                    Thread.sleep((timeToSleep * 1000).toLong())

                    if (showDoneMessage) {
                        runOnUiThread { Toast.makeText(applicationContext, "Operation done", Toast.LENGTH_SHORT).show() }
                    }

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }
            }.take(1)
    }

    private fun doASecondContinuouslyOperation(): Observable<Int> {
        Log.v(TAG, "doASecondContinuouslyOperation()")

        return Observable.just(1)
            .doOnNext { number ->
                try {
                    runOnUiThread { Toast.makeText(applicationContext, "Starting a second operation. Items will be buffered", Toast.LENGTH_SHORT).show() }

                    val timeToSleep = Random().nextInt(6 - 3) + 3
                    Log.v(TAG, "doASecondContinuouslyOperation() - Sleeping for $timeToSleep second(s)")
                    Thread.sleep((timeToSleep * 1000).toLong())

                    runOnUiThread { Toast.makeText(applicationContext, "Second operation done", Toast.LENGTH_SHORT).show() }

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }
            }
    }


    /**
     * This example will emit groups of 4 items, no matter of how long delay between emitted items is.
     *
     * @return
     */
    private fun startBufferOperatorTestWithCount(): Subscription {

        return emitItems(18, false, true)

            .buffer(4)

            // Just for log purpose
            .compose(showDebugMessages("buffer(4)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example will emit group of items that is emitted in a window of 700ms. Since emitted items will be delayed randomically, there is
     * no way to know how many items will be grouped together.
     *
     * @return
     */
    private fun startBufferOperatorTestWithTimespan(): Subscription {

        return emitItems(20, false, true)

            .buffer(700, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("buffer(700ms)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     *
     * This example will emit group of four items or N items that is emitted in a window of 700ms, which comes first.
     *
     *
     * @return
     */
    private fun startBufferOperatorTestWithCountAndTimespan(): Subscription {

        // Source will emit sequential numbers, and using a fixed delay of 200ms between each emission
        return emitItems(20, false, false)

            .buffer(700, TimeUnit.MILLISECONDS, 4)

            // Just for log purpose
            .compose(showDebugMessages("buffer(700ms or 4 items [which comes first])"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startBufferOperatorTestWithTimespanAndTimeshift(): Subscription {

        return emitItems(40, false, false)

            .buffer(600, 3000, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("buffer(600ms, 3000ms)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startBufferOperatorTestWithBoundary(): Subscription {

        return emitItems(40, false, true)

            .buffer(doAContinuouslyOperation(true))

            // Just for log purpose
            .compose(showDebugMessages("buffer(w/boundary)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startBufferOperatorTestWithSelector(): Subscription {

        return emitItems(70, false, true)

            .buffer(doAContinuouslyOperation(false)) { _ -> doASecondContinuouslyOperation() }

            // Just for log purpose
            .compose(showDebugMessages("buffer(w/selectors)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startWindowOperatorTestWithCountAndTimespan(): Subscription {

        return emitItems(30, false, false)

            .window(700, TimeUnit.MILLISECONDS, 4)

            .compose(showDebugMessages("window()"))

            .flatMap { o -> o.toList() }

            // Just for log purpose
            .compose(showDebugMessages("window(700ms or 4 items [which comes first])"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     *
     * @return
     */
    private fun startScanOperatorTest(): Subscription {

        return emitItems(20, true, true)

            .scan { accumulator, item ->
                if (item % 2 == 0) {
                    return@scan accumulator +item
                }

                accumulator
            }

            // Just for log purpose
            .compose(showDebugMessages("scan"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example uses a variant of scan() operator which accepts a seed value that is applied to the first emitted item.
     *
     * When using a seed with a value of 3, it will be applied to the accumulator (only for the fist item)
     *
     * @return
     */
    private fun startScanOperatorWithSeedTest(): Subscription {

        val seed = 3

        return emitItems(10, false, true)

            .scan(seed) { accumulator, item ->
                Log.v(TAG, "scan() - seed: $seed - accumulator: $accumulator - item: $item")
                accumulator + item
            }

            // Just for log purpose
            .compose(showDebugMessages("scan() w/seed"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    companion object {
        private val TAG = TransformingOperatorsExampleActivity::class.java.simpleName
    }
}
