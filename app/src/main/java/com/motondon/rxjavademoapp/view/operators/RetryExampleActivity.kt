package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.support.v4.util.Pair
import android.util.Log
import android.widget.Toast
import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.R.id.*

import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.Arrays
import java.util.Random
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_operators_retry_and_retrywhen_example.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * Examples on this activity are based on the following article:
 *
 * http://blog.danlew.net/2016/01/25/rxjavas-repeatwhen-and-retrywhen-explained/
 *
 * Please, visit it in order to get more details about it.
 *
 */
class RetryExampleActivity : BaseActivity() {

    private var attemptCount = 0

    private var retryMatrix: List<Pair<Int, Int>> = Arrays.asList(
        Pair(1, 1), // First four attempts, sleep 1 second before retry
        Pair(5, 2), // For attempt 5 to 9, sleep 2 second before retry
        Pair(10, 3), // For attempt 10 to 19, sleep 3 second before retry
        Pair(20, 4), // For attempt 20 to 39, sleep 4 second before retry
        Pair(40, 5), // For attempt 40 to 99, sleep 4 second before retry
        Pair(100, 6)  // For the 100th attempts and next ones, sleep 6 second before retry
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_retry_and_retrywhen_example)

        btnRetryForever.setOnClickListener{ onRetryForeverButtonClick() }
        btnRetryWhenForever.setOnClickListener{ onRetryWhen_ForeverButtonClick() }
        btnRetryWhenNoFlatMap.setOnClickListener{ onRetryWhen_noFlatMapButtonClick() }
        btnRetryWhenThreeTimes.setOnClickListener{ onRetryWhen_threeTimesButtonClick() }
        btnRetryWhenThreeTimesWithZipWith.setOnClickListener{ onRetryWhen_ThreeTimesWithZipWithButtonClick() }
        btnRetryWhenWithExponentialBackoff.setOnClickListener{ onRetryWhen_WithExponentialBackoffButtonClick() }
        btnStopSubscription.setOnClickListener{ onStopSubscription() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        attemptCount = 0
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onRetryForeverButtonClick() {

        if (isUnsubscribed()) {
            resetData()
            mSubscription = retry_forever()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRetryWhen_ForeverButtonClick() {

        if (isUnsubscribed()) {
            resetData()
            mSubscription = retryWhen_forever()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRetryWhen_noFlatMapButtonClick() {

        if (isUnsubscribed()) {
            resetData()
            mSubscription = retryWhen_no_flatMap()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRetryWhen_threeTimesButtonClick() {

        if (isUnsubscribed()) {
            resetData()
            mSubscription = retryWhen_threeTimes()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRetryWhen_ThreeTimesWithZipWithButtonClick() {

        if (isUnsubscribed()) {
            resetData()
            mSubscription = retryWhen_threeTimes_with_zipWith()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onRetryWhen_WithExponentialBackoffButtonClick() {

        if (isUnsubscribed()) {
            resetData()
            mSubscription = retryWhen_withExponentialBackoff()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStopSubscription() {
        mSubscription?.unsubscribe()
    }

    /**
     * This method uses a random value in order to decide whether to emit either an observable or an error.
     *
     * Currently there is 20% of chance to emit and error and 80% of chance to emit an observable.
     *
     *
     * @return
     */
    private fun emitItemsAndThenError(): Observable<*> {
        Log.v(TAG, "emitItemsAndThenError()")

        return Observable
            .zip(
                Observable.range(0, 100),
                Observable.interval(100, TimeUnit.MILLISECONDS))
                { a, _ -> a }
            .flatMap<Int> { n ->
                val random = Random().nextInt(10 - 1) + 1

                if (random > 2) {
                    Log.v(TAG, "emitItemsAndThenError() - Emitting number: $n")
                    updateEmittedItemView("$n")
                    return@flatMap Observable.just(n)
                } else {
                    try {
                        Thread.sleep(300)
                    } catch (e: InterruptedException) {
                        Log.v(TAG, "emitItemsAndThenError() - Got an InterruptedException!")
                    }
                    return@flatMap Observable.error<Int>(Throwable("Emitting an error"))
                }
            }
    }

    private fun retry_forever(): Subscription {

        // Remember that emitItemsAndThenError() can emit an item or an error. When it emits an error, retry will act.
        return emitItemsAndThenError()
            .compose(this.showDebugMessages("emitItemsAndThenError"))
            .retry()
            .compose(this.showDebugMessages("retry"))
            .compose(applySchedulers())
            .subscribe(
                { value -> tvResult.text = "${tvResult.text} $value" },
                { error -> tvResult.text = "${tvResult.text} - doOnError: $error" },
                { tvResult.text = "${tvResult.text} - onCompleted" }
            )
    }

    private fun retryWhen_forever(): Subscription {

        return emitItemsAndThenError()
            .compose(this.showDebugMessages("emitItemsAndThenError"))
            .retryWhen { error -> error
                // Right after retryWhen(...) mSubscription, it will stop here waiting for the source to emit an error.
                .flatMap { _ ->
                    // Since we are always emitting source observable (the Throwable object) after timer() expires (in two seconds), this method will act
                    // exactly like the retry() operator.
                    Log.v(TAG, "retryWhen_forever::flatMap() - Emitting throwable. This will make retryWhen to re-subscribe source observable. Attempt: ${++attemptCount}")
                    Observable
                        .timer(2, TimeUnit.SECONDS)
                        .compose<Long>(this@RetryExampleActivity.showDebugMessages("retryWhen.timer"))
                }
            }
            .compose(this.showDebugMessages("retryWhen"))
            .compose(applySchedulers())
            .subscribe(
                { value -> tvResult.text = "${tvResult.text} $value" },
                { error -> tvResult.text = "${tvResult.text} - doOnError: $error" },
                { tvResult.text = "${tvResult.text} - onCompleted" }
            )
    }

    /**
     * This example demonstrates how retryWhen SHOULD NOT be used. Since it does not react over the error emitted by the source, it terminates as soon as timer
     * operator finishes its job (sleep for two seconds).
     *
     * See next three examples which uses flatMap in order to see how to use it in the right way.
     *
     * @return
     */
    private fun retryWhen_no_flatMap(): Subscription {

        return emitItemsAndThenError()
            .compose(this.showDebugMessages("emitItemsAndThenError"))

            .retryWhen { error ->
                Log.v(TAG, "retryForever2::retryWhen()")

                // Right after the mSubscription, it will get here, but since we are not reacting over the error emitted by
                // the source, retryWhen() will terminate as soon as timer() operator returns (after 2 seconds) making source
                // observable stops emitting and breaking the entire chain.
                //
                // This is actually not what we expect to see in a real application, but we added it here just to demonstrate how retryWhen actually behaves.
                return@retryWhen Observable.timer(2, TimeUnit.SECONDS)

                        // Just for log purpose
                        .compose(showDebugMessages<Long>("retryWhen.timer"))
            }
                .compose(this.showDebugMessages("retryWhen"))
            .compose(applySchedulers())
            .subscribe(
                { value -> tvResult.text = "${tvResult.text} $value" },
                { error -> tvResult.text = "${tvResult.text} - doOnError: $error" },
                { tvResult.text = "${tvResult.text} - onCompleted" }
            )
    }

    /**
     * Note on this example we must explicitly cast flatmap return type to the (Observable) type in the function applied to the emitted items.
     * This is because inside that function we can return multiple types of Observables (an Observable<Integer> until we reach MAX_ATTEMPTS or an Observable<error>
     * after we reach MAX_ATTEMPTS value).
     *
     * This is needed since the compiler resolves lambda expressions types by using the first statement that uses it. In our case, since it will first find
     * the line: "return  Observable.error(...)", it will assume it for the returned type (i.e.: Observable<Throwable> type). Later, when analysing
     * the "else" clause, it will find another return type for the Observable.timer(...) method, which means a value of type Observable<Long>. At this time it does
     * not know what to and will throw an error. So, this is why we need to cast the return line to the Observable type.
     *
     * See these links for details about lambda inference:
     * - http://stackoverflow.com/questions/31227149/why-is-this-type-inference-not-working-with-this-lambda-expression-scenario
     * - http://stackoverflow.com/questions/27508223/when-returning-a-list-of-custom-objects-rxjava-highlights-an-error-but-compiles
     *
     * ********
     *
     * Note that we want to retry at most for MAX_ATTEMPTS times (i.e. 3 times) only for subsequent errors, so, as soon as source emits a valid data, we reset
     * our counter. Then, next time source emits an error, we will start counting errors from zero.
     *
     * @return
    </Long></Throwable></error></Integer> */
    private fun retryWhen_threeTimes(): Subscription {

        return emitItemsAndThenError()
            .compose(this.showDebugMessages("emitItemsAndThenError"))
            .retryWhen { error ->
                error
                    .flatMap<Any> { _ ->
                        // When the number of retries hits MAX_ATTEMPTS (3 times) it will emit an error, otherwise it will delay the error for 1 second
                        // and emit it (this is actually what timer() operator does)
                        //
                        // According to the documentation:
                        //   - when retryWhen() emits an error or completes, it stops resubscribing the source observable.
                        //   - when retryWhen() emits any value (here value does not matter, but only the emitted type) it will resubscribe the
                        //     source observable.
                        //
                        // So, this is how we control re-subscriptions and stops it according to our needs.
                        if (++attemptCount >= MAX_ATTEMPTS) {
                            Log.v(TAG, "retryWhen_threeTimes::retryWhen() - Reached max number of attempts ($MAX_ATTEMPTS). Emitting an error...")
                            return@flatMap  Observable.error<Any>(Throwable("Reached max number of attempts ($MAX_ATTEMPTS). Emitting an error...")) as Observable
                        } else {
                            Log.v(TAG, "retryWhen_threeTimes::retryWhen() - Emitting an empty Observable. Attempt: $attemptCount")
                            return@flatMap Observable.timer(1, TimeUnit.SECONDS) as Observable
                        }
                    }
            }

            // Whenever we get here, it means source emitted an observable, so we need to reset attemptCount attribute.
            // We just want to retry at most for MAX_ATTEMPTS times (i.e. 3 times) for subsequent errors.
            // So, as soon as source emits a valid data, we reset our counter.
            .doOnNext { _ ->
                Log.v(TAG, "retryWhen_threeTimes::doOnNext() - Reset attemptCount.")
                attemptCount = 0
            }

            .compose(this.showDebugMessages("retryWhen"))
            .compose(applySchedulers())
            .subscribe(
                { value -> tvResult.text = "${tvResult.text} $value" },
                { err -> tvResult.text = "${tvResult.text} - doOnError: $err" },
                { tvResult.text = "${tvResult.text} - onCompleted" }
            )
    }

    /**
     * This example also flatmap's the error received from the retryWhen, but instead of use a class scope attribute to control the number of attempts, it uses
     * zipWith() operator combined to range(). Then, it flatmap's the emitted items and sleep for a while prior the re-mSubscription.
     *
     * After range emits all its items, the chain is terminated.
     *
     * Note that we want to retry at most for MAX_ATTEMPTS times (i.e. 3 times) only for subsequent errors, so, as soon as source emits a valid data, we reset
     * our counter. Then, next time source emits an error, we will start counting errors from zero.
     *
     * @return
     */
    private fun retryWhen_threeTimes_with_zipWith(): Subscription {

        return emitItemsAndThenError()
            .compose(this.showDebugMessages("emitItemsAndThenError"))
            .retryWhen { error ->
                Log.v(TAG, "retryWhen_threeTimes_with_zipWith::retryWhen()")

                // The previous example (retryWhen_threeTimes) controls the number of retries by changing a class scope attribute (attemptCount) every time
                // it resubscribes, but RxJava provides an operator that can do it: zipWith(). Basically it emits the source value (in this
                // case the Throwable) combined with the first argument (in this case the values returned from the range() observable which will start from 1
                // and emit for two (MAX_ATTEMPTS) -1) times.  This value is used in the next operator (flatMap) to sleep for 2 seconds prior to resubscribe
                // source operator.
                error
                    .zipWith(Observable.range(COUNTER_START, MAX_ATTEMPTS - 1)) { _, attempt ->
                        Log.v(TAG, "retryWhen_threeTimes_with_zipWith::zipWith() - Attempt $attempt")
                        attempt
                    }
                    .flatMap { _ -> Observable.timer(2, TimeUnit.SECONDS) }
            }

            // Whenever we get here, it means source emitted an observable, so we need to reset attemptCount attribute.
            // We just want to retry at most for MAX_ATTEMPTS times (i.e. 3 times) for subsequent errors.
            // So, as soon as source emits a valid data, we reset our counter.
            .doOnNext { _ ->
                Log.v(TAG, "retryWhen_threeTimes_with_zipWith::doOnNext() - Reset attemptCount.")
                attemptCount = 0
            }

            .compose(this.showDebugMessages("retryWhen"))
            .compose(applySchedulers())
            .subscribe(
                { value -> tvResult.text = "${tvResult.text} $value" },
                { error -> tvResult.text = tvResult.text.toString() + " - doOnError: " + error },
                { tvResult.text = "${tvResult.text} - onCompleted" }
            )
    }

    /**
     * This example uses an exponential backoff to delay re-subscriptions as the error occurs. This is useful when we do not want to bother the server with
     * retries, but gives it more time prior to retry. See retryMatrix construction (in onCreate() method) for timespan details.
     *
     * @return
     */
    private fun retryWhen_withExponentialBackoff(): Subscription {

        return emitItemsAndThenError()
            .compose(this.showDebugMessages("emitItemsAndThenError"))
            .retryWhen { error ->
                error
                    .flatMap { _ ->
                        Observable
                            .timer(getSecondsToSleep(++attemptCount).toLong(), TimeUnit.SECONDS)
                    }
            }
            .compose(this.showDebugMessages("retryWhen"))

            // Whenever we get here, it means source emitted an observable, so we need to reset attemptCount attribute.
            // We just want to retry at most for MAX_ATTEMPTS times (i.e. 3 times) for subsequent errors.
            // So, as soon as source emits a valid data, we reset our counter.
            .doOnNext { _ ->
                Log.v(TAG, "retryWhen_withExponentialBackoff::doOnNext() - Reset attemptCount.")
                attemptCount = 0
            }

            .compose(applySchedulers())
            .subscribe(
                { value -> tvResult.text = "${tvResult.text} $value" },
                { error -> tvResult.text = "${tvResult.text} - doOnError: $error" },
                { tvResult.text = "${tvResult.text} - onCompleted" }
            )
    }

    private fun getSecondsToSleep(attempt: Int): Int {
        var secondsToSleep = 0
        var i = 0
        while (i < retryMatrix.size && retryMatrix[i].first <= attempt) {
            secondsToSleep = retryMatrix[i].second
            i++
        }

        Log.v(TAG, "getSecondsToSleep() - attempt: $attempt - secondsToSleep: $secondsToSleep")
        return secondsToSleep
    }

    /**
     * Created this helper method only to allow our lambda expressions to become even shorter.
     *
     * @param// text
     */
    private fun updateEmittedItemView(text: String) {
        val w = AndroidSchedulers.mainThread().createWorker()
        w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $text" }
    }

    companion object {
        private val TAG = RetryExampleActivity::class.java.simpleName

        private const val COUNTER_START = 1
        private const val MAX_ATTEMPTS = 3
    }
}
