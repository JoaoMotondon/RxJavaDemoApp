package com.motondon.rxjavademoapp.view.generalexamples

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.Pair
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.adapter.SimpleStringAdapter
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.Arrays
import java.util.Random
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_general_server_polling_example.*
import kotlinx.android.synthetic.main.activity_general_server_polling_example.view.*
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * This example is intended to demonstrate how to poll a server to request for some data. For each data received, it will analyze it and
 * check whether it should be considered done or not. Depends on the example, it can poll server until data is done or for a limited number
 * of times.
 *
 * Actually we are not polling any real server, but using an observable to simulate this situation.
 *
 * It was based on the following article: https://medium.com/@v.danylo/server-polling-and-retrying-failed-operations-with-retrofit-and-rxjava-8bcc7e641a5a
 *
 */
class ServerPollingExampleActivity : BaseActivity() {

    private var mSimpleStringAdapter: SimpleStringAdapter? = null
    private var pollingAttempt: Int = 0

    // This is the retry matrix used in a variant of exponential backoff
    private var retryMatrix: List<Pair<Int, Int>> = Arrays.asList(
            Pair(1, 1), // First four attempts, sleep 1 second before retry
            Pair(5, 2), // For attempt 5 to 9, sleep 2 second before retry
            Pair(10, 3), // For attempt 10 to 19, sleep 3 second before retry
            Pair(20, 4), // For attempt 20 to 39, sleep 4 second before retry
            Pair(40, 5), // For attempt 40 to 99, sleep 4 second before retry
            Pair(100, 6)  // For the 100th attempts and next ones, sleep 6 second before retry
    )

    // This is a static list of FakeJob objects. It is used to simulate data polling from the server. Items on this list will be
    // emitted sequentially, making only the first 5 items to actually be emitted. The reason is that once the fifth item is emitted,
    // since it represents a job done, this will make our "client" to stop polling the server. Items from #6 to 10# should never
    // be emitted.
    private val fakeJobList = Arrays.asList(
        FakeJob("1"), // Will be always emitted (unless user clicks over stop mSubscription button)
        FakeJob("2"), // Will be always emitted (unless user clicks over stop mSubscription button)
        FakeJob("3"), // Will be always emitted (unless user clicks over stop mSubscription button)
        FakeJob("4"), // Will be always emitted (unless user clicks over stop mSubscription button)
        FakeJob("5", true), // Should stop emission
        FakeJob("6"), // Should never be emitted
        FakeJob("7"), // Should never be emitted
        FakeJob("8"), // Should never be emitted
        FakeJob("9", true)) // Should never be emitted

    /**
     * Just a simple class that simulates data retrieved from a server containing a parameter that informs whether it should be considered done or not.
     *
     */
    inner class FakeJob  constructor(private val value: String, jobDone: Boolean = false) {
        var isJobDone = false

        init {
            this.isJobDone = jobDone
        }

        override fun toString(): String {
            return value
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_server_polling_example)

        btnStartPollingInterval.setOnClickListener { onStartPollingIntervalButtonClick() }
        btnStartPollingRepeatWhenUntilJobDone.setOnClickListener { onStartPollingRepeatWhenButtonClick() }
        btnStartPollingRepeatWhenForThreeTimes.setOnClickListener { onStartPollingRepeatWhenForThreeTimesButtonClick() }
        btnStartPollingRepeatWhenUsingExponentialBackoff.setOnClickListener { onstartPollingRepeatWhenUsingExponentialBackoffButtonClick() }
        btnStopPolling.setOnClickListener { onStopPollingButtonClick() }

        mListView.layoutManager = LinearLayoutManager(this)
        mSimpleStringAdapter = SimpleStringAdapter(this)
        mListView.adapter = mSimpleStringAdapter

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        mSimpleStringAdapter?.clear()
        pollingAttempt = 0
    }

    private fun onStartPollingIntervalButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartPollingIntervalButtonClick()")
            resetData()
            mSubscription = startPolling_Interval()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartPollingRepeatWhenButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartPollingRepeatWhenButtonClick()")
            resetData()
            mSubscription = startPolling_repeatWhen_untilJobDone()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStartPollingRepeatWhenForThreeTimesButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onStartPollingRepeatWhenForThreeTimesButtonClick()")
            resetData()
            mSubscription = startPolling_repeatWhen_forThreeTimes()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onstartPollingRepeatWhenUsingExponentialBackoffButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onstartPollingRepeatWhenUsingExponentialBackoffButtonClick()")
            resetData()
            mSubscription = startPolling_repeatWhen_exponentialBackoff()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStopPollingButtonClick() {
        Log.v(TAG, "onStopPollingButtonClick()")
        mSubscription?.let {
            it.unsubscribe()
        }
    }

    private fun emitJob(): Observable<FakeJob> {
        Log.v(TAG, "emitJob()")

        return Observable
            .timer(0, TimeUnit.SECONDS)
            .flatMap { _ -> Observable.just(fakeJobList[pollingAttempt++]) }

            .doOnNext { _ ->
                try {
                    Log.v(TAG, "emitItems() - attempt: $pollingAttempt")
                    Thread.sleep(100)

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }
            }
    }

    /**
     * This method will generate a random FakeJob (note it does not use fakeJobList)
     *
     * It has 15% of chance to generate a done job and 85% of chance to generate a not done job.
     *
     * This is used only by startPolling_repeatWhen_exponentialBackoff test.
     *
     * @return
     */
    private fun emitRandomJob(): Observable<FakeJob> {
        Log.v(TAG, "emitRandomJob()")

        return Observable
            .timer(0, TimeUnit.SECONDS)
            .flatMap { _ ->
                val job: FakeJob
                val randomNumber = Random().nextInt(100)
                pollingAttempt++

                // When using random job, we have 15% of change to create a done job...
                job = if (randomNumber <= 15) {
                    FakeJob("Random Job", true)
                } else {
                    // ... and 85% of chance to create a non done job
                    FakeJob("Random Job")
                }

                Observable.just(job)
            }

            .doOnNext { _ -> Log.v(TAG, "emitItems() - attempt: $pollingAttempt") }
    }

    /**
     * This example requests data every 1 second by using interval operator until gets a job that is considered done.
     * Then we stop polling.
     *
     * @return
     */
    private fun startPolling_Interval(): Subscription {

        // Emit an observable each 1 second
        return Observable.interval(0, 1000, TimeUnit.MILLISECONDS)

            // Poll the server
            .flatMap { _ ->
                Log.v(TAG, "flatMap - calling emitData() method...")
                emitJob()
            }

            // Just for log purpose
            .compose(showDebugMessages("flatMap"))

            // Now, for each job, check whether jobDone flag is true. If so, takeUntil will completes making
            // interval operator to also completes.
            .takeUntil { fakeJob ->
                if (fakeJob.isJobDone) {
                    Log.v(TAG, "takeUntil - FakeJob done")
                } else {
                    Log.v(TAG, "takeUntil - FakeJob not finished yet")
                }
                fakeJob.isJobDone
            }

            // Just for log purpose
            .compose(showDebugMessages("takeUntil"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber())
    }

    /**
     * This example is similar to the "startPolling_Interval", but using repeatWhen operator instead to poll the server until it
     * gets a job considered done.
     *
     *
     * @return
     */
    private fun startPolling_repeatWhen_untilJobDone(): Subscription {

        return emitJob()

            // Just for log purpose
            .compose(showDebugMessages("emitData"))

            .repeatWhen { observable ->
                Log.v(TAG, "repeatWhen()")

                // Wait for one second and emit source observable forever. This will be consumed by the takeUntil() observable which will do its
                // job until fakeJob is done.
                observable.flatMap { o ->
                    Log.v(TAG, "flatMap()")
                    Observable.timer(1, TimeUnit.SECONDS)
                }
            }

            // Just for log purpose
            .compose(showDebugMessages("repeatWhen"))

            .takeUntil { fakeJob ->
                if (fakeJob.isJobDone) {
                    Log.v(TAG, "takeUntil - FakeJob done")
                } else {
                    Log.v(TAG, "takeUntil - FakeJob not finished yet")
                }
                fakeJob.isJobDone
            }
            // Just for log purpose
            .compose(showDebugMessages("takeUntil"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber())
    }

    /**
     * This example polls the server only for three times. Since it uses emitJob() method to get a job
     *
     * We known in advanced this example will never get a job that is considered donse, since we are using fakeJobList list that
     * contains a done job only in its fifth item.
     *
     * Of course this is just for demonstration purpose when we need to poll a server a fixed number of times, regardless we get an
     * expected value or not.
     *
     * @return
     */
    private fun startPolling_repeatWhen_forThreeTimes(): Subscription {

        return emitJob()

            // Just for log purpose
            .compose(showDebugMessages("emitData"))

            .repeatWhen { observable ->
                Log.v(TAG, "repeatWhen()")

                observable.zipWith(Observable.range(COUNTER_START, MAX_ATTEMPTS)) { _, integer ->
                    Log.v(TAG, "zipWith()")
                    integer
                }
                    .flatMap { o ->
                        Log.v(TAG, "flatMap()")
                        Observable.timer(1, TimeUnit.SECONDS)
                    }
            }

            // Just for log purpose
            .compose(showDebugMessages("repeatWhen"))

            .takeUntil { fakeJob ->
                if (fakeJob.isJobDone) {
                    Log.v(TAG, "takeUntil - FakeJob done")
                } else {
                    Log.v(TAG, "takeUntil - FakeJob not finished yet")
                }
                fakeJob.isJobDone
            }
            // Just for log purpose
            .compose(showDebugMessages("takeUntil"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber())
    }

    /**
     * This example polls the server until it gets a job done using a variant of an exponential backoff to increase the time it waits before a new
     * server request. Once it gets a job considered done, it resets the exponential backoff counter and re-start pooling the server.
     *
     * emitRandomJob() returns a random job, so we do not know when it will return a job done or not. Actually there are 15% of chance to get a job done
     * and 85% of change to get one that is not done.
     *
     * Note that it will poll the server forever, or until you click on Stop button.
     *
     * @return
     */
    private fun startPolling_repeatWhen_exponentialBackoff(): Subscription {

        // This method will emit one job at a time.
        return emitRandomJob()

            // Just for log purpose
            .compose(showDebugMessages("emitData"))

            .repeatWhen { observable ->
                Log.v(TAG, "repeatWhen()")

                // While we get a job that is not finished yet, we will keep polling server but using an exponential backoff. This is done by the
                // method getSecondsToSleep() which will return the number of seconds to sleep based on the number of attempts.
                observable.concatMap { o ->
                    val numOfSeconds = getSecondsToSleep(pollingAttempt)
                    Log.v(TAG, "flatMap() - Attempt: $pollingAttempt - Waiting for $numOfSeconds second(s) prior next server polling...")

                    // Sleep the number of seconds returned by the getSecondsToSleep() method.
                    Observable.timer(numOfSeconds.toLong(), TimeUnit.SECONDS)
                }
            }

            // Just for log purpose
            .compose(showDebugMessages("repeatWhen"))

            .observeOn(AndroidSchedulers.mainThread())

            .filter { fakeJob ->
                when {
                    fakeJob.isJobDone -> {
                        Log.v(TAG, "filter() - FakeJob done. Reset retryMatrix counter.")

                        // When we get a done job, we will not filtering it, meaning it will be propagate downstream the subscriber.
                        // Also we will reset attemptCount so that we will keep polling server for the next done job but using
                        // retryMatrix from its initial values (i.e.: sleeps for 5 seconds in the first minute, 10 sec next four minutes,
                        // and so on).
                        pollingAttempt = 1

                    }
                    else -> {
                        Log.v(TAG, "filter() - FakeJob not finished yet")

                        // Since we are in a filter() operator, job is not done, no notification is propagated down the chain, and
                        // recycler view is not updated with such information. So, we add it here.
                        mSimpleStringAdapter?.let { it ->
                            it.addString("Attempt: $pollingAttempt - Job not finished. Waiting ${getSecondsToSleep(pollingAttempt)} sec")
                            mListView.scrollToPosition(it.itemCount - 1)
                        }
                    }
                }

                fakeJob.isJobDone
            }

            // Just for log purpose
            .compose(showDebugMessages("filter"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber())
    }

    private fun resultSubscriber(): Subscriber<FakeJob> {
        return object : Subscriber<FakeJob>() {

            override fun onCompleted() {
                Log.v(TAG, "subscribe.onCompleted")
            }

            override fun onError(e: Throwable) {
                Log.v(TAG, "subscribe.doOnError: " + e.message)
            }

            override fun onNext(fakeJob: FakeJob) {

                Log.v(TAG, "subscribe.onNext")

                var msg = "Attempt: $pollingAttempt"
                msg = if (fakeJob.isJobDone) {
                    "$msg - Job done"
                } else {
                    "$msg - Job not finished yet"
                }
                mSimpleStringAdapter?.let {
                    it.addString(msg)
                    mListView.scrollToPosition(it.itemCount - 1)
                }
            }
        }
    }

    /**
     * This method returns the number of seconds an observer will sleeps based on the retry count. It extracts this value
     * from the retryMatrix
     *
     * @param attempt
     * @return
     */
    private fun getSecondsToSleep(attempt: Int): Int {

        var secondsToSleep: Int = 0
        var i = 0
        while (i < retryMatrix.size && retryMatrix[i].first <= attempt) {
            secondsToSleep = retryMatrix[i].second
            i++
        }

        Log.v(TAG, "getSecondsToSleep() - attempt: $attempt - secondsToSleep: $secondsToSleep")

        return secondsToSleep
    }

    companion object {

        private val TAG = ServerPollingExampleActivity::class.java.simpleName

        private const val COUNTER_START = 1
        private const val MAX_ATTEMPTS = 3
    }
}
