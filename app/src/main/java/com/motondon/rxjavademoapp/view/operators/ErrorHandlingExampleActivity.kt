package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.Arrays
import java.util.Random
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_operators_error_handling_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * Examples on this activity are based on the following articles:
 *
 * http://blog.danlew.net/2015/12/08/error-handling-in-rxjava/
 * http://reactivex.io/RxJava/javadoc/rx/Observable.html#onErrorReturn(rx.functions.Func1)
 *
 * Please, visit them in order to get into details.
 *
 */
class ErrorHandlingExampleActivity : BaseActivity() {
    
    internal inner class MyException(detailMessage: String) : Exception(detailMessage)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_error_handling_example)

        btnStartUncheckedExceptionTest.setOnClickListener{ onStartUncheckedExceptionTestButtonClick() }
        btnStartCheckedExceptionTest.setOnClickListener{ onStartCheckedExceptionTestButtonClick() }
        btnStartOnErrorReturnOperatorTest.setOnClickListener{ onStartOnErrorReturnOperatorTestButtonClick() }
        btnStartOnErrorResumeNextOperatorTest.setOnClickListener{ onStartOnErrorResumeNextOperatorTestButtonClick() }
        btnStartOnExceptionResumeNextOperatorTest.setOnClickListener{ onStartOnExceptionResumeNextOperatorTestButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onStartUncheckedExceptionTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartUncheckedExceptionTestButtonClick()")
            resetData()
            mSubscription = startUncheckedExceptionTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartCheckedExceptionTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartCheckedExceptionTestButtonClick()")
            resetData()
            mSubscription = startCheckedExceptionTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartOnErrorReturnOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartOnErrorReturnOperatorTestButtonClick()")
            resetData()
            mSubscription = startOnErrorReturnOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartOnErrorResumeNextOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartOnErrorResumeNextOperatorTestButtonClick()")
            resetData()
            mSubscription = startOnErrorResumeNextOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartOnExceptionResumeNextOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartOnExceptionResumeNextOperatorTestButtonClick()")
            resetData()
            mSubscription = startOnExceptionResumeNextOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun emitRandomNumber(): Observable<Int> {
        Log.v(TAG, "emitRandomNumber()")

        return Observable.create { s ->
            val randomNumber = Random().nextInt(10)

            val w = AndroidSchedulers.mainThread().createWorker()
            w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $randomNumber" }

            s.onNext(randomNumber)
            s.onCompleted()
        }
    }

    private fun emitSecondObservable(): Observable<String> {
        Log.v(TAG, "emitSecondObservable()")

        val list = Arrays.asList("@", "#", "$", "%", "&")

        return Observable

            .from(list)

            .doOnNext { n ->
                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $n" }
            }
    }

    @Throws(MyException::class)
    private fun throwAnException(): String {
        Log.v(TAG, "throwAnException() - Throwing MyException()...")

        throw MyException("This is an example of a checked exception")
    }

    /**
     * Unchecked exceptions will be caught by the subscriber.onError. We can see it in the logs.
     *
     * We could of course wrap our code to handle it, but this example is intended to demonstrate how unchecked
     * exceptions are handled by the sequence.
     *
     * @return
     */
    private fun startUncheckedExceptionTest(): Subscription {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap { _ -> emitRandomNumber() }

            .map<String> { randomNumber ->
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    Log.v(TAG, "map() - We will throw a RuntimeException...")
                    throw RuntimeException("Hey, this is a forced runtime exception...")
                } else {
                    Log.v(TAG, "Returning random number: $randomNumber")
                    return@map ""+randomNumber
                }
            }

            // Just for log purpose
            .compose(showDebugMessages("map"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * Since this example catches a checked exception, we can do whatever we want. Here we will emit an error, which will terminate the
     * sequence with an error
     *
     * @return
     */
    private fun startCheckedExceptionTest(): Subscription {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap { _ -> emitRandomNumber() }

            .flatMap<String> { randomNumber ->
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    try {
                        // This is just to simulate a method call which can throw a checked exception. Since checked exception must be explicit caught (in a try/catch
                        // block or throwing it), we catch it here and emit an error.
                        Log.v(TAG, "Throwing an exception. Random number: $randomNumber")
                        return@flatMap Observable.just<String>(throwAnException())

                    } catch (e: Throwable) {
                        Log.v(TAG, "Catching the exception and emitting an error...")
                        // Here we catch our exception and emit an error. We could of course emit whatever value we wanted, but this is only to demonstrate
                        // this error will terminate the sequence and handled by the observer.onError
                        return@flatMap Observable.error<String>(e)
                    }

                } else {
                    // If number is less than seven, just re-emit it
                    Log.v(TAG, "Just re-emitting random number: $randomNumber")
                    return@flatMap Observable.just<String>("" + randomNumber)
                }
            }

            // Just for log purpose
            .compose(showDebugMessages("flatMap"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * From the docs:
     *
     * By default, when an Observable encounters an error that prevents it from emitting the expected item to its Observer,
     * the Observable invokes its Observer's onError method, and then quits without invoking any more of its Observer's methods.
     * The onErrorReturn method changes this behavior. If you pass a function (resumeFunction) to an Observable's onErrorReturn
     * method, if the original Observable encounters an error, instead of invoking its Observer's onError method, it will
     * instead emit the return value of resumeFunction.
     *
     * From the Dan Lew blog (mentioned on the top of this activity):
     *
     * "... when using these [error handling] operators, the upstream Observables are still going to shut down! They've already
     * seen a terminal event (onError); all that onError[Return|ResumeNext] does is replace the onError notification with a
     * different sequence downstream." ... "You might expect the interval to continue emitting after the map throws an exception,
     * but it doesn't! Only the downstream subscribers avoid onError."
     *
     * @return
     */
    private fun startOnErrorReturnOperatorTest(): Subscription {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap { _ -> emitRandomNumber() }

            .map<String> { randomNumber ->
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    Log.v(TAG, "Forcing an exception. Random number: $randomNumber")
                    return@map(4 / 0).toString()
                } else {
                    Log.v(TAG, """Returning random number: $randomNumber""")
                    return@map ""+randomNumber
                }
            }

            // When source observable throws an exception, it will be caught here and we will emit an string instead of the error. This is what onErrorReturn operator is for.
            .onErrorReturn { _ -> " [This is a fallback string which will be emitted instead of the error emitted from the source observable (we forced an exception when source observable emitted a random number greater than 7.]" }

            // Just for log purpose
            .compose(showDebugMessages("onErrorReturn"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * From the docs:
     *
     * By default, when an Observable encounters an error that prevents it from emitting the expected item to its Observer,
     * the Observable invokes its Observer's onError method, and then quits without invoking any more of its Observer's methods.
     * The onErrorResumeNext method changes this behavior. If you pass another Observable (resumeSequence) to an Observable's
     * onErrorResumeNext method, if the original Observable encounters an error, instead of invoking its Observer's onError
     * method, it will instead relinquish control to resumeSequence which will invoke the Observer's onNext method if it is
     * able to do so. In such a case, because no Observable necessarily invokes onError, the Observer may never know that an
     * error happened.
     *
     * @return
     */
    private fun startOnErrorResumeNextOperatorTest(): Subscription {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap { _ -> emitRandomNumber() }

            .map<String> { randomNumber ->
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    Log.v(TAG, "Forcing an exception. Random number: $randomNumber")
                    return@map(4 / 0).toString()
                } else {
                    Log.v(TAG, "Returning random number: $randomNumber")
                    return@map ""+randomNumber
                }
            }

            .onErrorResumeNext { throwable ->
                Log.v(TAG, "onErrorResumeNext() - Found an error: ${throwable.message} - Emitting a second Observable instead...")
                emitSecondObservable()
            }

            // Just for log purpose
            .compose(showDebugMessages("onErrorResumeNext"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    /**
     * From the docs:
     *
     * This differs from onErrorResumeNext(rx.functions.Func1>) in
     * that this one does not handle Throwable or Error but lets those continue through.
     *
     * @return
     */
    private fun startOnExceptionResumeNextOperatorTest(): Subscription {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap { _ -> emitRandomNumber() }

            .map<String> { randomNumber ->
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    Log.v(TAG, "Forcing an exception. Random number: $randomNumber")
                    return@map(4 / 0).toString()
                } else {
                    Log.v(TAG, "Returning random number: $randomNumber")
                    return@map ""+randomNumber
                }
            }

            .onExceptionResumeNext(emitSecondObservable())

            // Just for log purpose
            .compose(showDebugMessages("onExceptionResumeNext"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult))
    }

    companion object {
        private val TAG = ErrorHandlingExampleActivity::class.java.simpleName
    }
}
