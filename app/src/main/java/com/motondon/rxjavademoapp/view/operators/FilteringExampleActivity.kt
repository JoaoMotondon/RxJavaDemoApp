package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.support.v4.util.Pair
import android.support.v7.app.ActionBar
import android.util.Log
import android.view.View
import android.widget.*

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity
import kotlinx.android.synthetic.main.activity_operators_filtering_example.*

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Random

import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers


class FilteringExampleActivity : BaseActivity() {
    
    // Hold the method name related to the test user chosen in the spinner control.
    private var currentTestMethodName: String? = null

    /**
     * This is a helper class that is used to fill the spinner up with pairs of test name and a related method name.
     *
     * When an item is selected from the spinner, we will extract a method name and call it by using reflection.
     *
     * We extended from Pair class (instead of using it directly) since we want a custom toString() method
     *
     */
    internal inner class SpinnerOptionAndMethodName(first: String, second: String) : Pair<Any, Any>(first, second) {
        override fun toString(): String {
            return first as String
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_filtering_example)

        sTestOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinnerTestOptionsItemSelected(parent as Spinner, position)
            }
        }
        //sTestOptions.onItemSelectedListener { (spinner, position) -> spinnerTestOptionsItemSelected(spinner, position)}
        btnStartTest.setOnClickListener{ onButtonClick() }

        // Fill spinner view up with all available test names and their related method names. We will use reflection to call a method based on the user choice
        val testOptions = ArrayList<SpinnerOptionAndMethodName>()
        testOptions.add(SpinnerOptionAndMethodName("first()", "startFirstOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("first() with predicate", "startFirstOperatorWithPredicateFunctionTest"))
        testOptions.add(SpinnerOptionAndMethodName("firstOrDefault(9999)", "startFirstOrDefaultOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("takeFirst()", "startTakeFirstOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("single()", "startSingleOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("singleOrDefault(9999)", "startSingleOrDefaultOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("elementAt(3)", "startElementAtOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("last()", "startLastOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("lastOrDefault()", "startLastOrDefaultOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("take(5)", "startTakeOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("takeLast(5)", "startTakeLastOperatorTest"))
        testOptions.add(SpinnerOptionAndMethodName("filter()", "startFilterOperatorTest"))

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

    /**
     * Helper method that emits N random numbers
     *
     * @param numberOfItems
     * @return
     */
    private fun emitItems(numberOfItems: Int): Observable<Int> {
        Log.v(TAG, "emitItems() - numberOfItems: $numberOfItems")

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(0, numberOfItems)

            // Generate a random number (for each emitted item)
            .map { randomNumber -> Random().nextInt(10) }

            .doOnNext { n ->

                try {
                    Thread.sleep(100)
                    Log.v(TAG, "emitItems() - Emitting number: " + n)

                } catch (e: InterruptedException) {
                    Log.v(TAG, "emitItems() - Got an InterruptedException!")
                }

                // Now, log it on the GUI in order to inform user about the emitted item
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $n" }
            }
    }

    /**
     * As the name suggests, this method will emit items (up to 10) or an empty observable based on an internal criteria
     *
     * @return
     */
    private fun emitItemsOrEmpty(): Observable<Int> {
        Log.v(TAG, "emitItemsOrEmpty()")

        // Generate a random number
        val randomNumber = Random().nextInt(10 - 1) + 1

        // If it is greater than 5, emit an empty observable
        if (randomNumber > 5) {

            // Now, log it on the GUI in order to inform user about the emitted item
            val w = AndroidSchedulers.mainThread().createWorker()
            w.schedule { tvEmittedNumbers.text = "Empty observable" }

            return Observable.empty()
        }

        // Otherwise, if it is less or equals to 5, call emitItems()method which will emit N random numbers
        return emitItems(Random().nextInt(10 - 1) + 1)
    }

    /**
     * In case of emitItemsOrEmpty() helper method emits an item, first() operator will emit it downstream, then terminate the chain. But in
     * case of an empty observable is emitted, since first() operator expects at least one item to be emitted, this example will terminate
     * with the following error: "Sequence contains no elements".
     *
     * @return
     */
    private fun startFirstOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            // Emit only the first item emitted by the source Observable. In case of none item emitted,
            // it terminates with an error
            .first()

            // Just for log purpose
            .compose(showDebugMessages("first"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * This example uses a variation of first() operator, which accepts a predicate function and will complete successfully
     * when that predicate is evaluated as true.
     *
     * @return
     */
    private fun startFirstOperatorWithPredicateFunctionTest(): Subscription {

        // This will emit 5 random numbers from 1 to 10
        return emitItems(5)

            // Emit the first number emitted from the source Observable that is multiple of three
            .first { number -> number % 3 == 0 }

            // Just for log purpose
            .compose(showDebugMessages("first(...)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * If the source Observable emits an empty observable, default value
     * will be emitted. Otherwise, if it emits some value, it will be emitted downstream and terminate the chain.
     *
     * @return
     */
    private fun startFirstOrDefaultOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            // In case of the source Observable finishes before emit any item, DEFAULT_VALUE will be emitted instead
            .firstOrDefault(DEFAULT_VALUE)

            // Just for log purpose
            .compose(showDebugMessages("firstOrDefault($DEFAULT_VALUE)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * From the docs:
     *
     * The takeFirst operator behaves similarly to first, with the exception of how these operators behave when the source Observable
     * emits no items that satisfy the predicate. In such a case, first will throw a NoSuchElementException while takeFirst will return
     * an empty Observable (one that calls onCompleted but never calls onNext).
     *
     * @return
     */
    private fun startTakeFirstOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            // In case of no item is emitted by the source Observable, takeFirst will return an empty observable
            .takeFirst { _ -> true }

            // Just for log purpose
            .compose(showDebugMessages("takeFirst()"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * single() operator expects only one item to be emitted. If more than one item is emitted it will terminate with an error (Sequence
     * contains too many elements).
     *
     * Also, if the source Observable does not emit any  item before completing, it throws a NoSuchElementException
     * and will terminate with error: "Sequence contains no elements".
     *
     * @return
     */
    private fun startSingleOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            // If emitItemsOrEmpty() emits an empty observable or more than one item, single() will terminate with an error,
            // otherwise it will terminate successfully
            .single()

            // Just for log purpose
            .compose(showDebugMessages("single"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     *
     * singleOrDefault() operator is slightly different from the single() operator. In case of no element is emitted,
     * instead of terminate with an  error, it will emit the default value.
     *
     * @return
     */
    private fun startSingleOrDefaultOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            .singleOrDefault(DEFAULT_VALUE)

            // Just for log purpose
            .compose(showDebugMessages("singleOrDefault($DEFAULT_VALUE)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }


    /**
     * elementAt(N) expects at least N items are emitted from the source observable. In case of less items are emitted,
     * IndexOutOfBoundsException is thrown.
     *
     * @return
     */
    private fun startElementAtOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            .elementAt(3)

            // Just for log purpose
            .compose(showDebugMessages("elementAt(3)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * last() operator emits only the last emitted item by the source Observable. In case of an empty observable is emitted, since last() operator
     * expects at least one item to be emitted, it will terminate with an error (NoSuchElementException)
     *
     * @return
     */
    private fun startLastOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            // Will emit the last item emitted by the source Observable
            .last()

            // Just for log purpose
            .compose(showDebugMessages("last"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * Similar to last() operation, but in case of the source Observable fails to emit any item, a default value will be emitted instead of an error.
     *
     * @return
     */
    private fun startLastOrDefaultOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            .lastOrDefault(DEFAULT_VALUE)

            // Just for log purpose
            .compose(showDebugMessages("lastOrDefault($DEFAULT_VALUE)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * take(N) operator returns N items emitted by the source Observable.
     *
     * @return
     */
    private fun startTakeOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            // Even in case of fewer items than N are emitted (or no item is emitted), take(5) will complete after source observable completes
            .take(5)

            // Just for log purpose
            .compose(showDebugMessages("take(5)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * Emits the last N item emitted by the source Observable. In case of no item is emitted, it will completes with no error.
     *
     * @return
     */
    private fun startTakeLastOperatorTest(): Subscription {

        return emitItemsOrEmpty()

            // Will emit only the last 2 items. In case of no item is emitted, it will successfully complete after source observable completes
            .takeLast(5)

            // Just for log purpose
            .compose(showDebugMessages("takeLast(5)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * Emits only those numbers that make predicate function to evaluate as true.
     *
     * @return
     */
    private fun startFilterOperatorTest(): Subscription {

        return emitItems(50)

            // Will emit all the numbers emitted by the source Observable that are multiple of three
            .filter { number -> number % 3 == 0 }

            // Just for log purpose
            .compose(showDebugMessages("filter(...)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    companion object {
        private val TAG = FilteringExampleActivity::class.java.simpleName
        private const val DEFAULT_VALUE = 9999
    }
}
