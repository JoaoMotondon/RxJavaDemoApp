package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_operators_combining_observables_example.*
import rx.Observable

import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit


class CombiningObservablesExampleActivity : BaseActivity() {

    private val oddNumbers = ArrayList(Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15))
    private val evenNumbers = ArrayList(Arrays.asList(2, 4, 6, 8, 10, 12, 14))
    private val someNumbers = ArrayList(Arrays.asList(100, 200, 300, 400, 500, 600, 700))
    private val someMoreNumbers = ArrayList(Arrays.asList(1000, 2000, 3000, 4000, 5000, 6000, 7000))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_combining_observables_example)

        btnMergeOperatorUsingListOfObservablesTest.setOnClickListener{ onStartMergeOperatorUsingListOfObservablesTestButtonClick() }
        btnMergeOperatorUsingMultiplesObservablesTest.setOnClickListener{ onStartMergeOperatorUsingMultiplesObservablesTestButtonClick() }
        btnMergeOperatorUsingMultiplesObservablesAndEmitAnErrorTest.setOnClickListener{ onStartMergeOperatorUsingMultiplesObservablesAndEmitAnErrorTestButtonClick() }
        btnMergeDelayErrorOperatorTest.setOnClickListener{ onStartMergeDelayErrorOperatorTestButtonClick() }
        btnMergeWithOperatorTest.setOnClickListener{ onStartMergeWithOperatorTestButtonClick() }
        btnZipOperatorTest.setOnClickListener{ onStartZipOperatorTestButtonClick() }
        btnZipWithOperatorTest.setOnClickListener{ onStartZipWithOperatorTestButtonClick() }
        btnCombineLatestOperatorTest.setOnClickListener{ onStartCombineLatestOperatorTestButtonClick() }
        btnSwitchOnNextOperatorTest.setOnClickListener{ onStartSwitchOnNextOperatorTestButtonClick() }
        btnSwitchOnNextOperatorTest2.setOnClickListener{ onStartSwitchOnNextOperatorTest2ButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onStartMergeOperatorUsingListOfObservablesTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartMergeOperatorUsingListOfObservablesTestButtonClick()")
            resetData()
            mSubscription = startMergeOperatorUsingListOfObservablesTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartMergeOperatorUsingMultiplesObservablesTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartMergeOperatorUsingMultiplesObservablesTestButtonClick()")
            resetData()
            mSubscription = startMergeOperatorUsingMultiplesObservablesTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartMergeOperatorUsingMultiplesObservablesAndEmitAnErrorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartMergeOperatorUsingMultiplesObservablesAndEmitAnErrorTestButtonClick()")
            resetData()
            mSubscription = startMergeOperatorUsingMultiplesObservablesAndEmitsAnErrorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartMergeDelayErrorOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartMergeDelayErrorOperatorTestButtonClick()")
            resetData()
            mSubscription = startMergeDelayErrorOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartMergeWithOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartMergeWithOperatorTestButtonClick()")
            resetData()
            mSubscription = startMergeWithOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartZipOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartZipOperatorTestButtonClick()")
            resetData()
            mSubscription = startZipOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartZipWithOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartZipWithOperatorTestButtonClick()")
            resetData()
            startZipWithOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartCombineLatestOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartCombineLatestOperatorTestButtonClick()")
            resetData()
            startCombineLatestOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartSwitchOnNextOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartSwitchOnNextOperatorTestButtonClick()")
            resetData()
            startSwitchOnNextOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartSwitchOnNextOperatorTest2ButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartSwitchOnNextOperatorTest2ButtonClick()")
            resetData()
            startSwitchOnNextOperatorTest2()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun emitNumbers(list: List<Int>, timeToSleep: Int): Observable<Int> {
        Log.v(TAG, "emitNumbers() - timeToSleep: $timeToSleep")

        return Observable

            // Use timer will make this observable emits items in a separate thread.
            .timer(0, TimeUnit.SECONDS)

            .flatMap { _ ->
                Observable
                    .from(list)
                    .doOnNext { number ->
                        try {
                            Thread.sleep(timeToSleep.toLong())
                            Log.v(TAG, "emitNumbers() - Emitting number: $number")

                        } catch (e: InterruptedException) {
                            Log.v(TAG, "Got an InterruptedException!")
                        }

                        // Now, log it on the GUI in order to inform user about the emitted item
                        val w = AndroidSchedulers.mainThread().createWorker()
                        w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }

                    }
            }
    }

    /**
     * Helper method that emits an error after 1 second
     *
     * @return
     */
    private fun emitError(): Observable<Any> {
        Log.v(TAG, "emitError()")

        return Observable
            .just(0)

            // Using Schedulers.newThread() makes this observable emits items in a separate thread.
            .observeOn(Schedulers.newThread())

            .flatMap { _ -> return@flatMap Observable.error<Any>(Throwable()) }

            .doOnError { _ ->
                try {
                    Log.v(TAG, "emitError() - Sleeping 1 second before emit an error")
                    Thread.sleep(1000)
                    Log.v(TAG, "emitError() - *** Emitting an error ***")

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }

                // Now, log it on the GUI in order to inform user about the emitted item
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} error" }
            }
    }

    /**
     * This example will combine the output of two observables by using merge() operator.
     *
     * For the downstream operator, it will look like items were emitted by a single observable.
     *
     * @return
     */
    private fun startMergeOperatorUsingListOfObservablesTest(): Subscription {

        val list = Arrays.asList(emitNumbers(oddNumbers, 250), emitNumbers(evenNumbers, 150))
        return Observable

            // This demonstrate we can pass a list of observables instead of multiples observables (up to nine) to the merge operator
            .merge(list)

            // Just for log purpose
            .compose(showDebugMessages("merge"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This is another merge variant. It accepts multiple observables (up to nine).
     *
     * @return
     */
    private fun startMergeOperatorUsingMultiplesObservablesTest(): Subscription {

        return Observable

            // This demonstrate we can pass  multiple observables (up to nine) to the merge operator
            .merge(emitNumbers(oddNumbers, 250), emitNumbers(evenNumbers, 150), emitNumbers(someNumbers, 700), emitNumbers(someMoreNumbers, 70))

            // Just for log purpose
            .compose(showDebugMessages("merge"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example shows what happen when any individual observable passed into merge terminates with an error. This makes the whole chain
     * to terminate immediately with an error.
     *
     * @return
     */
    private fun startMergeOperatorUsingMultiplesObservablesAndEmitsAnErrorTest(): Subscription {

        return Observable

            // This demonstrate we can pass  multiple observables (up to nine) to the merge operator
            .merge<Any>(emitNumbers(oddNumbers, 250), emitNumbers(evenNumbers, 150), emitError())

            // Just for log purpose
            .compose(showDebugMessages<Any>("merge"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * Quite similar to the previous example, but in case of error, it won't terminate
     * immediately, but process all emitted items from the sources, and only then it will terminate with an error
     *
     * @return
     */
    private fun startMergeDelayErrorOperatorTest(): Subscription {

        return Observable

            // Even when emitError() emits an error, mergeDelayError will keep processing source emissions and only then it will emit an error
            .mergeDelayError<Any>(emitNumbers(oddNumbers, 250), emitNumbers(evenNumbers, 150), emitError())

            // Just for log purpose
            .compose(showDebugMessages<Any>("mergeDelayError"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example is pretty similar to the merge operator. But this way we can merge sequences one by one in a chain.
     *
     * @return
     */
    private fun startMergeWithOperatorTest(): Subscription {

        return emitNumbers(oddNumbers, 250)

            // merge with this sequence...
            .mergeWith(emitNumbers(evenNumbers, 150))

            // ... and this one...
            .mergeWith(emitNumbers(someNumbers, 700))

            // ... and this one
            .mergeWith(emitNumbers(someMoreNumbers, 70))

            // Just for log purpose
            .compose(showDebugMessages("mergeWith"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * The zip operator combines emitted values from observables (from two up to nine) based on their index.
     *
     * This example will combine two observables (one that emits even numbers and another emitting odd numbers) by emitting a list of two numbers
     * Ex: [1,2], [3,4], [5,6], etc.
     *
     * Note: The zip sequence will terminate when any of the source sequences terminates. Further values from the other sequences will be ignored.
     *
     * @return
     */
    private fun startZipOperatorTest(): Subscription {

        return Observable

            .zip(emitNumbers(oddNumbers, 100), emitNumbers(evenNumbers, 150)) { odd, even -> Arrays.asList(odd, even) }

            // Just for log purpose
            .compose(showDebugMessages("zip"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * There is also a zip variation called zipWith. It allows us to combine the result of zip operators in chain
     *
     * @return
     */
    private fun startZipWithOperatorTest(): Subscription {

        return emitNumbers(Arrays.asList(1, 2, 3, 4, 5, 6, 7), 100)

            .zipWith(emitNumbers(Arrays.asList(1, 2, 3, 4, 5, 6, 7), 150)) { i1, i2 -> i1 + i2 }

            .zipWith(emitNumbers(Arrays.asList(1, 2, 3, 4, 5, 6, 7), 550)) { sumOf_i1_plus_i2, i3 -> sumOf_i1_plus_i2 + i3 }

            // Just for log purpose
            .compose(showDebugMessages("zipWith"))

            .observeOn(AndroidSchedulers.mainThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * From the docs:
     *
     * "When any of the source Observables emits an item, CombineLatest combines the most recently emitted items from each of the other
     * source Observables, using a function you provide, and emits the return value from that function."
     *
     * That being said, this is what this example will output:
     *
     * 1        -> this is emitted at time 1000ms by the first observable
     * 2        -> this is emitted at time 1500ms by the second observable
     * [1,2]    -> This is what will be combined, based on the provided function, since these numbers were the last item emitted by each observable
     * 3        -> this is emitted at time 2000ms by the first observable
     * [3,2]    -> This is what will be combined, (number 3 just emitted by the first observable and number 2 that is the latest item emitted by the second observable)
     * 4        -> this is emitted at time 3000ms by the second observable
     * [3,4]    -> This is what will be combined, (number 3 emitted by the first observable and number 4 that was just emitted by the second observable)
     * 5        -> this is emitted at time 3000ms by the first observable (note this was emitted at the same time as the latest item emitted by the second observable
     * [5,4]    -> This is what will be combined
     *
     * @return
     */
    private fun startCombineLatestOperatorTest(): Subscription {

        return Observable

            .combineLatest(
                    emitNumbers(oddNumbers, 1000), emitNumbers(evenNumbers, 1500)) { odd, even -> Arrays.asList(odd, even) }

            // Just for log purpose
            .compose(showDebugMessages("combineLatest"))

            .observeOn(AndroidSchedulers.mainThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * The switchOnNext operator takes an observable that emits observable. When the outer observable emits a new item, the inner
     * observable will be terminated and started again.
     *
     * So, our outer observable will emit numbers every 600ms. Then, for each item it emits, our inner observable will emit items
     * each 180ms (giving it a chance to emit 3 items). When the outer observable emits a new item, the inner observable will be
     * discarded and a new one will be used to emit items.
     *
     * So the expected result is a sequence of numbers 0, 1 and 2 (that were emitted from the inner Observables)
     *
     * @return
     */
    private fun startSwitchOnNextOperatorTest(): Subscription {

        return Observable

            // This is the outer observable. Items emitted here will be used to control the inner observable. Whenever it emits
            // an item, the inner observable will stop its emission and a new one will be created.
            .switchOnNext(Observable.interval(600, TimeUnit.MILLISECONDS)

                // We are using doOnNext here in order to be able to update our GUI.
                .doOnNext { number ->
                    Log.v(TAG, "outer observable - Emitting number: $number")
                    // Now, log it on the GUI in order to inform user about the emitted item
                    val w = AndroidSchedulers.mainThread().createWorker()
                    w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
                }
                .map { _ ->
                    // This is the inner observable. It will emit items every 180ms. When the outer observable emits a new item
                    // (which is supposed to happen after 600ms) this one will be discarded and a new one will be taken in place.
                    // Since outer observable will emit items each 600ms, inner observable will have a chance to emit 3 items and
                    // then be discarded.
                    Observable.interval(180, TimeUnit.MILLISECONDS)

                        // We are using doOnNext here in order to be able to update our GUI.
                        .doOnNext { number ->
                            Log.v(TAG, "inner observable - Emitting number: $number")
                            val w = AndroidSchedulers.mainThread().createWorker()
                            w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
                        }
                }
            )

            // Just adding some boundaries
            .take(15)

            .map { aLong -> aLong.toInt() }

            // Just for log purpose
            .compose(showDebugMessages("switchOnNext"))

            .observeOn(AndroidSchedulers.mainThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startSwitchOnNextOperatorTest2() {

        Observable
            .switchOnNext(Observable.timer(0, TimeUnit.MILLISECONDS)
                .map { _ -> emitNumbers(oddNumbers, 180) }

                .concatWith(Observable.timer(1000, TimeUnit.MILLISECONDS)
                        .map { _ -> emitNumbers(evenNumbers, 500) })
            )

            // Just for log purpose
            .compose(showDebugMessages("switchOnNext"))

            .observeOn(AndroidSchedulers.mainThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    companion object {
        private val TAG = CombiningObservablesExampleActivity::class.java.simpleName
    }
}
