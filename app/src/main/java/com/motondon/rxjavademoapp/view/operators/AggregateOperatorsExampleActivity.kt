package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.R.id.*
import com.motondon.rxjavademoapp.view.base.BaseActivity

import kotlinx.android.synthetic.main.activity_operators_aggregate_operators_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func0
import rx.observables.MathObservable
import java.util.*

class AggregateOperatorsExampleActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_aggregate_operators_example)

        btnStartSumOperatorTest.setOnClickListener { onStartSumOperatorButtonClick() }
        btnStartMaxOperatorTest.setOnClickListener { onStartMaxOperatorButtonClick() }
        btnStartCollectWithEmptyStateFactoryTest.setOnClickListener { onStartCollectTestWithEmptyStateFactoryButtonClick() }
        btnStartCollectTestWithEmptyStateFactoryWithValues.setOnClickListener { onStartCollectTestWithStateFactoryWithValuesButtonClick() }
        btnStartReduceTest.setOnClickListener { onStartReduceTestButtonClick() }
        btnStartReduceWithGlobalSeedTest.setOnClickListener { onStartReduceOperatorTestWithGlobalSeedButtonClick() }
        btnStartReduceWithNullSeedTest.setOnClickListener { onStartReduceOperatorTestWithNullSeedButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onStartSumOperatorButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartSumOperatorButtonClick()")
            resetData()
            mSubscription = startSumOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartMaxOperatorButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartMaxOperatorButtonClick()")
            resetData()
            mSubscription = startMaxOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartCollectTestWithEmptyStateFactoryButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartCollectTestWithEmptyStateFactoryButtonClick()")
            resetData()
            mSubscription = startCollectTestWithEmptyStateFactory()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartCollectTestWithStateFactoryWithValuesButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartCollectTestWithStateFactoryWithValuesButtonClick()")
            resetData()
            mSubscription = startCollectTestWithStateFactoryWithValues()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartReduceTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartReduceTestButtonClick()")
            resetData()
            mSubscription = startReduceOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartReduceOperatorTestWithGlobalSeedButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartReduceOperatorTestWithGlobalSeedButtonClick()")
            resetData()
            startReduceOperatorTestWithGlobalSeed()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartReduceOperatorTestWithNullSeedButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartReduceOperatorTestWithNullSeedButtonClick()")
            resetData()
            startReduceOperatorTestWithNullSeed()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun emitItems(numberOfItems: Int, randomItems: Boolean): Observable<Int> {
        Log.v(TAG, "emitItems() - numberOfItems: $numberOfItems")

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(1, numberOfItems)

            .map { item -> if (randomItems) Random().nextInt(20 - 0) + 0 else item }

            .doOnNext { number ->
                try {
                    Log.v(TAG, "emitItems() - Emitting number: $number")

                    // Sleep for sometime between 100 and 300 milliseconds
                    Thread.sleep((Random().nextInt(300 - 100) + 100).toLong())

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }
            }
            .doOnCompleted { Log.v(TAG, "onCompleted") }
    }


    private fun startSumOperatorTest(): Subscription {

        return MathObservable.sumInteger(emitItems(20, true))

            // Just for log purpose
            .compose(showDebugMessages("sumInteger"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    private fun startMaxOperatorTest(): Subscription {

        return MathObservable.max(emitItems(20, true))

            // Just for log purpose
            .compose(showDebugMessages("max"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }


    /**
     * This example adds emitted items to the stateFactory (i.e.: an array of Integer). In the end, it acts like the toList() operator.
     *
     * @return
     */
    private fun startCollectTestWithEmptyStateFactory(): Subscription {

        val stateFactory = { ArrayList<Int>() }

        return emitItems(5, false)

            .collect(stateFactory) { list, item -> list.add(item) }

            // Just for log purpose
            .compose(showDebugMessages("collect"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * The only difference from the previous example is that on this one, we initialize stateFactory with some values.
     *
     * @return
     */
    private fun startCollectTestWithStateFactoryWithValues(): Subscription {

        val stateFactory = {
            val list = ArrayList<Int>()
            list.add(44)
            list.add(55)
            list
        }

        return emitItems(5, false)

            .collect(stateFactory) { list, item -> list.add(item) }

            // Just for log purpose
            .compose(showDebugMessages("collect"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example will return the max emitted value by using reduce() operator.
     *
     * @return
     */
    private fun startReduceOperatorTest(): Subscription {

        return emitItems(15, true)

            // Reduce operator  will only emit onNext when source observable terminates.
            .reduce { accumulator, item ->
                val max = if (item > accumulator) item else accumulator
                Log.v(TAG, "reduce() - item: $item - current accumulator: $accumulator - new accumulator (max): $max")
                max
            }

            // Just for log purpose
            .compose(showDebugMessages("reduce"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example is intended to demonstrate reduce operator usage with a seed that is shared between all mSubscription.
     * This will impact in the items and result might not be what we expect.
     *
     * See link below (item #8) for a very good explanation about it:
     *
     * http://akarnokd.blogspot.hu/2015/05/pitfalls-of-operator-implementations_14.html
     *
     * Also see this link:
     *
     * https://stackoverflow.com/questions/30633799/can-rxjava-reduce-be-unsafe-when-parallelized
     *
     */
    private fun startReduceOperatorTestWithGlobalSeed() {

        // Emit only tree numbers
        val observable = emitItems(3, false)

            // Note this example will share [new ArrayList<Integer>()] between all evaluations of the chain. So, the result might not be what we expect.
            // The next example shows how to fix it. Thanks to Dávid Karnok (http://akarnokd.blogspot.hu/2015/05/pitfalls-of-operator-implementations_14.html)
            .reduce(ArrayList<Int>()) { accumulator, item ->
                Log.v(TAG, "reduce() - item: $item - accumulator: $accumulator")
                accumulator.add(item)
                accumulator
            }

            // Just for log purpose
            .compose(showDebugMessages("reduce"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose<List<Int>>(applySchedulers())


        // Now, subscribe it for the first time...
        Log.v(TAG, "Subscribe for the fist time...")
        observable.subscribe(resultSubscriber(tvResult))

        // ... and for the second time...
        Log.v(TAG, "Subscribe for the second time...")
        observable.subscribe(resultSubscriber(tvResult))

        // ... and for the third time
        Log.v(TAG, "Subscribe for the third time...")
        observable.subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example is intended to demonstrate reduce operator with a seed that is NOT shared between all evaluation
     * of the chain.
     *
     * See link below (item #8) for a very good explanation about it:
     *
     * http://akarnokd.blogspot.hu/2015/05/pitfalls-of-operator-implementations_14.html
     *
     */
    private fun startReduceOperatorTestWithNullSeed() {

        // Emit only tree items
        val observable = emitItems(3, false)

            // Reduce operator  will only emit onNext when source observable terminates.
            .reduce<ArrayList<Int>>(null) { accumulator, item: Int ->
                if (accumulator == null) {
                    Log.v(TAG, "reduce() - accumulator is NULL. Instantiate it. This appears to be the first time this method is called.")
                    // Sorry... could not make it work in Kotlin! Error: "Val cannot be reassigned" when trying to assign a value to the accumulator.
                    // Think I need to investigate it a bit more... when I get this done, I will update this code.
                    // ****** The way it is, when running this example we will get a NPE when trying to add an item to the accumulator *******
                    // accumulator = ArrayList()
                }

                Log.v(TAG, "reduce() - item: $item - accumulator: $accumulator")
                accumulator.add(item)
                return@reduce accumulator
            }

            // Just for log purpose
            .compose(showDebugMessages("reduce"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose<List<Int>>(applySchedulers())


        // Now, subscribe it for the first time...
        Log.v(TAG, "Subscribe for the fist time...")
        observable.subscribe(resultSubscriber(tvResult))

        // ... and for the second time...
        Log.v(TAG, "Subscribe for the second time...")
        observable.subscribe(resultSubscriber(tvResult))

        // ... and for the third time
        Log.v(TAG, "Subscribe for the third time...")
        observable.subscribe(resultSubscriber(tvResult))
    }

    companion object {
        private val TAG = AggregateOperatorsExampleActivity::class.java.simpleName
    }
}
