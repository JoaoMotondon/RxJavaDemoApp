package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.Arrays
import java.util.Random
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_operators_conditional_operators_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

class ConditionalOperatorsExampleActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_conditional_operators_example)

        btnStartSkipWhileTest.setOnClickListener{ onStartSkipWhileTestButtonClick() }
        btnStartSkipUntilTest.setOnClickListener{ onStartSkipUntilTestButtonClick() }
        btnStartTakeWhileTest.setOnClickListener{ onStartTakeWhileTestButtonClick() }
        btnStartTakeUntilTest.setOnClickListener{ onStartTakeUntilTestButtonClick() }
        btnStartAmbOperatorTest.setOnClickListener{ onStartAmbOperatorButtonClick() }
        btnStartAllOperatorTest.setOnClickListener{ onStartAllOperatorButtonClick() }
        btnStartContainsOperatorTest.setOnClickListener{ onStartContainsOperatorTest() }
        btnStartExistsOperatorTest.setOnClickListener{ onStartExistsOperatorTest() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onStartSkipWhileTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartSkipWhileTestButtonClick()")
            resetData()
            mSubscription = startSkipWhileTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartSkipUntilTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartSkiUntilTestButtonClick()")
            resetData()

            AlertDialog.Builder(this)
                .setTitle("SkipUntil Test")
                .setMessage("Emitted items will be skipped until a fake operation finishes its job (which can take up to 5 seconds)")
                .setPositiveButton("OK") { _, _ -> mSubscription = startSkipUntilTest() }
                .show()

        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartTakeWhileTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartTakeWhileTestButtonClick()")
            resetData()
            mSubscription = startTakeWhileTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartTakeUntilTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartTakeUntilTestButtonClick()")
            resetData()

            AlertDialog.Builder(this)
                .setTitle("TakeUntil Test")
                .setMessage("Emitted items will be emitted downstream only while a fake operation is being executed (which can take up to 5 seconds)")
                .setPositiveButton("OK") { _, _ -> mSubscription = startTakeUntilTest() }
                .show()

        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartAmbOperatorButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartAmbOperatorButtonClick()")
            resetData()
            mSubscription = startAmbOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartAllOperatorButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartAllOperatorButtonClick()")
            resetData()
            mSubscription = startAllOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartContainsOperatorTest() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartContainsOperatorTest()")
            resetData()
            mSubscription = startContainsOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartExistsOperatorTest() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartExistsOperatorTest()")
            resetData()
            mSubscription = startExistsOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Helper method that emits N random numbers
     *
     * @param numberOfItems
     * @return
     */
    private fun emitItems(numberOfItems: Int, randomSleep: Boolean): Observable<Int> {
        Log.v(TAG, "emitItems() - numberOfItems: $numberOfItems")

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(0, numberOfItems)

            // Generate a random number (for each emitted item)
            .map { _ -> Random().nextInt(10) }

            .doOnNext { n ->

                try {
                    var timeToSleep = 400
                    if (randomSleep) {
                        timeToSleep = Random().nextInt(1000 - 200) + 200

                    }
                    Log.v(TAG, "emitItems() - Sleeping $timeToSleep before emit number $n")
                    Thread.sleep(timeToSleep.toLong())
                    Log.v(TAG, "emitItems() - Emitting number: $n")

                } catch (e: InterruptedException) {
                    Log.v(TAG, "emitItems() - Got an InterruptedException!")
                }

                // Now, log it on the GUI in order to inform user about the emitted item
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $n" }
            }
    }

    /**
     * Helper method that emits a list of numbers by using a random delay
     *
     * @param list
     * @return
     */
    private fun emitItems(list: List<Int>): Observable<Int> {
        Log.v(TAG, "emitOddNumbers()")

        val timeToSleep = Random().nextInt(1000 - 200) + 200

        return Observable.zip(
            Observable.from(list),
            Observable.interval(timeToSleep.toLong(), TimeUnit.MILLISECONDS)
        ) { a, _ -> a }
            .doOnNext { n ->
                Log.v(TAG, "emitItems() - Emitting number: $n")

                // Now, log it on the GUI in order to inform user about the emitted item
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $n" }
            }
    }

    private fun doSomeContinuouslyOperation(): Observable<Int> {
        Log.v(TAG, "doSomeContinuouslyOperation()")

        return Observable.interval(100, TimeUnit.MILLISECONDS)
            .map { number -> number.toInt() }
            .doOnNext { _ ->
                try {
                    runOnUiThread { Toast.makeText(applicationContext, "Starting operation", Toast.LENGTH_SHORT).show() }

                    val timeToSleep = Random().nextInt(6 - 3) + 3
                    Log.v(TAG, "doSomeContinuouslyOperation() - Sleeping for $timeToSleep second(s)")
                    Thread.sleep((timeToSleep * 1000).toLong())

                    runOnUiThread { Toast.makeText(applicationContext, "Operation done", Toast.LENGTH_SHORT).show() }

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }
            }.take(1)
    }

    /**
     * This example will emit 30random numbers (between 0 and 10) and skip them until it gets number 7. Once it gets it, it will stop skip them (in other words:
     * start propagate them downstream).
     *
     * @return
     */
    private fun startSkipWhileTest(): Subscription {

        return emitItems(30, false)

            .skipWhile { number ->
                val shouldSkip: Boolean = if (number == MAGIC_NUMBER) {
                    // When we get our magic number (i.e. number seven) we will stop skipping.
                    Log.v(TAG, "skipWhile() - Hey, we got number $MAGIC_NUMBER. Lets stop skipping!")
                    false

                } else {
                    // Skip while random number is different from our MAGIC_NUMBER (number seven)
                    Log.v(TAG, "skipWhile() - Got number: $number. Skipping while we do not get number seven")
                    true
                }

                shouldSkip
            }

            // This should be printed only after skipWhile receives the number seven (our MAGIC_NUMBER).
            .doOnNext { item -> Log.v(TAG, "skipWhile() - OnNext($item)") }

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startSkipUntilTest(): Subscription {

        return emitItems(30, false)

            .skipUntil(doSomeContinuouslyOperation())

            // We will hit here after doSomeContinuouslyOperation method returns, since it will emit an observable making skipUntil stop skipping.

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * Note that this test will be finished (i.e.: subscriber::onCompleted will be called) when we get number SEVEN, since takeWhile will emit onComplete making
     * interval() operator to also finish its job.
     *
     * @return
     */
    private fun startTakeWhileTest(): Subscription {

        return emitItems(30, false)

            .takeWhile { number ->
                val shouldTake: Boolean = if (number == MAGIC_NUMBER) {
                    // When we get our magic number (i.e. number seven) it will stop take items and emit onCompleted instead of onNext.
                    Log.v(TAG, "takeWhile() - Hey, we got number $MAGIC_NUMBER. Our job is done here.")
                    false

                } else {
                    // Take emitted items (i.e.: emit them downstream) while they are different from our MAGIC_NUMBER (number seven)
                    Log.v(TAG, "takeWhile() - Got number: $number. Emit it while we do not get number SEVEN")
                    true
                }

                shouldTake
            }

            // This will be printed while takeWhile gets numbers different from seven
            .doOnNext { number -> Log.v(TAG, "takeWhile() - doOnNext($number)") }

            // When takeWhile receives number seven, it will complete.
            .doOnCompleted { Log.v(TAG, "takeWhile() - doOnCompleted().") }

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startTakeUntilTest(): Subscription {
        return emitItems(30, false)

            .takeUntil(doSomeContinuouslyOperation())

            // We will hit here until doSomeContinuouslyOperation method is being executed. After that, it will emit an observable making takeUntil to complete.

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * According to the docs, amb() operator can have multiple source observables, but will emit all items from ONLY the first of these Observables
     * to emit an item or notification. All the others will be discarded.
     *
     * @return
     */
    private fun startAmbOperatorTest(): Subscription {

        val oddNumbers = Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15)
        val evenNumbers = Arrays.asList(2, 4, 6, 8, 10, 12, 14)

        return Observable.amb(emitItems(oddNumbers), emitItems(evenNumbers))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * If all emitted numbers are even, all() operator will emit true and complete, otherwise it will emit false before completes
     *
     * @return
     */
    private fun startAllOperatorTest(): Subscription {

        return emitItems(3, false)

            .all { number -> number % 2 == 0 }

            // doOnNext argument will contain a true value when all items emitted from the source are even. Otherwise it will be false.
            .doOnNext { number -> Log.v(TAG, "all() - doOnNext($number)") }

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startContainsOperatorTest(): Subscription {

        return emitItems(5, false)

            .contains(3)

            .doOnNext { n ->
                val w = AndroidSchedulers.mainThread().createWorker()

                if (n) {
                    Log.v(TAG, "startContainsOperatorTest() - contains() operator returned true, which means number three was emitted")
                    w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} Wow! Number three was emitted! " }
                } else {
                    Log.v(TAG, "startContainsOperatorTest() - contains() operator returned false, meaning source observable did not emit number three ")
                    w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} Source did not emit number three! " }
                }
            }

            .doOnCompleted { Log.v(TAG, "startContainsOperatorTest() - doOnCompleted()") }

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startExistsOperatorTest(): Subscription {

        return emitItems(10, false)

            .exists { number -> number % 3 == 0 }

            .doOnNext { n ->
                val w = AndroidSchedulers.mainThread().createWorker()

                if (n) {
                    Log.v(TAG, "startExistsOperatorTest() - exists() operator returned true, which means a number multiple of three was emitted")
                    w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} Wow! A number multiple of three was emitted! " }
                } else {
                    Log.v(TAG, "startExistsOperatorTest() - exists() operator returned false, meaning source observable did not emit a number multiple of three ")
                    w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} Source did not emit a number multiple of three! " }
                }
            }

            .doOnCompleted { Log.v(TAG, "startExistsOperatorTest() - doOnCompleted()") }

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    companion object {
        private val TAG = ConditionalOperatorsExampleActivity::class.java.simpleName
        private const val MAGIC_NUMBER = 7
    }
}
