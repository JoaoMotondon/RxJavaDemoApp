package com.motondon.rxjavademoapp.view.generalexamples

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_general_server_polling_after_data_processing_example.*
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.schedulers.Schedulers

/**
 * Examples on this activity were based on this GitHub issue (in the Ben Christensen's answers):
 * - https://github.com/ReactiveX/RxJava/issues/448
 *
 *
 * This example differs from the ServerPollingExampleActivity in a way that each time it poll a server, it simulates a local data
 * processing (actually it just sleeps a random time) and only then it polls the server again, no matter how long it takes to
 * process the data locally.
 *
 * Note that none of the examples in this activity implements any condition to stop polling (e.g.: takeUntil). So it will emit forever
 * unless stop button is clicked.
 *
 */
class ServerPollingAfterDataProcessingExampleActivity : BaseActivity() {

    private var pollingAttempt: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_server_polling_after_data_processing_example)

        btnProcessDataRecursively.setOnClickListener { onProcessDataRecursivelyButtonClick() }
        btnProcessDataManualRecursionWithScheduler.setOnClickListener { onProcessDataManualRecursionWithSchedulerButtonClick() }
        btnProcessDataManualRecursionRepeatWhen.setOnClickListener { onProcessDataManualRecursionRepeatWhenButtonClick() }
        btnStopJobProcessing.setOnClickListener { onStopJobProcessingButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun resetData() {
        pollingAttempt = 0
        tvServerPollingCount.text = ""
        tvLocalDataProcessingCount.text = ""
    }

    private fun onProcessDataRecursivelyButtonClick() {
        Log.v(TAG, "onProcessDataRecursivelyButtonClick()")
        if (isUnsubscribed()) {
            resetData()
            mSubscription = processDataRecursively()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onProcessDataManualRecursionWithSchedulerButtonClick() {
        Log.v(TAG, "onProcessDataManualRecursionWithSchedulerButtonClick()")
        if (isUnsubscribed()) {
            resetData()
            mSubscription = processData_manualRecursion_with_scheduler()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onProcessDataManualRecursionRepeatWhenButtonClick() {
        Log.v(TAG, "onProcessDataManualRecursionRepeatWhenButtonClick()")
        if (isUnsubscribed()) {
            resetData()
            mSubscription = processData_manualRecursion_with_repeatWhen()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStopJobProcessingButtonClick() {
        Log.v(TAG, "onStopJobProcessingButtonClick()")
        mSubscription?.let {
            it.unsubscribe()
        }
    }

    private fun emitData(): Observable<Int> {
        Log.v(TAG, "emitData()")

        return Observable

            .just(++pollingAttempt)

            // This is just to not emit forever and flood our GUI
            .take(10)

            .doOnNext { number ->
                try {
                    Log.v(TAG, "emitItems() - Emitting number: $number")
                    Thread.sleep(100)

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvServerPollingCount.text = "${tvServerPollingCount.text} $number" }
            }
    }


    /**
     * This is a very simple example. It gets a data from the server and, after process it locally, it finishes its job, so
     * Subscriber::onCompleted() is called. From there, it calls this method again (recursively).
     *
     * This will fetch server data forever. We could, of course, add a break condition in order to stop it (e.g.: takeUntil,
     * take(N), etc)
     *
     * @return
     */
    private fun processDataRecursively(): Subscription {

        // Fetch some data from a server (do not forget this is just a simulation. No data is actually being requested to any server).
        return emitData()

            .map { data ->
                Log.v(TAG, "flatMap()")

                // Now, after get some data from a server, let's process it. Note this processing takes about 2 seconds to finishes, and
                // only after that, it will terminate.
                heavyDataProcessing(data)

                data
            }

            .compose(applySchedulers())

            // Finally subscribe it. Note this observer call processDataRecursively() method when it is done. This is the idea for this
            // example: when data is finished processed, poll the server again.
            .subscribe(object : Observer<Int> {

                override fun onCompleted() {
                    Log.v(TAG, "subscribe.onCompleted - Calling processDataRecursively() method in order to poll the server again")
                    // Once we get here, it means data was processed locally accordingly. So, call processDataRecursively()
                    // method recursively in order to poll server again.
                    mSubscription = processDataRecursively()
                }

                override fun onError(e: Throwable) {
                    Log.v(TAG, "subscribe.doOnError: ${e.message}")
                }

                override fun onNext(data: Int?) {
                    Log.v(TAG, "subscribe.onNext: $data")
                    tvLocalDataProcessingCount.text = "${tvLocalDataProcessingCount.text} $data"
                }
            })
    }

    /**
     * This method will also poll the server only after finish processing previous data locally, but it uses manual recursion instead.
     *
     * @return
     */
    private fun processData_manualRecursion_with_scheduler(): Subscription {

        return Observable.just("")

            .map { _ ->
                Observable
                    .create(Observable.OnSubscribe<Int> { o ->
                        val w = Schedulers.newThread().createWorker()
                        o.add(w)

                        // Create an action that will be scheduled to the Scheduler.Worker
                        val schedule = object : Action0 {
                            override fun call() {

                                // When this action is scheduled, it will poll the server and emit it by calling Subscribe.onNext(), but we need to emit the data itself and not the observable returned
                                // by the emitData() method. So, in order to get the data, we consume it synchronously (by using toBlocking() and forEach() operators together) and then
                                // we call Subscriber::onNext() with the data we want to emit
                                emitData()
                                    .toBlocking()
                                    .forEach { data ->
                                        // Here we emit data received from the server. It will be consumed by the forEach() operator. Inside that method, data will be processed synchronously, and only after
                                        // that, it we will re-schedule a new server polling. This is exactly what we want to achieve on this example.
                                        o.onNext(data)
                                    }

                                // When we get here, it means data was already processed. So re-schedule this action in order to poll the server again.
                                w.schedule(this, 10, TimeUnit.MILLISECONDS)
                            }
                        }

                        // Schedule our action which will poll the server and emit its data to be processed synchronously, and after the data processing, it will re-schedule itself
                        // entering in an infinite loop.
                        w.schedule(schedule)
                    })


                    // by using toBlocking() operator, we ensure data will be first processed locally and only then we will re-schedule another server polling.
                    .toBlocking()

                    // this operator will consume data from the server that was emitted by the Subscriber::onNext(). It will process the data synchronously (since we are already in a
                    // computation thread) and only when it returns a re-schedule will be done.
                    .forEach { data2 ->
                        Log.v(TAG, "forEach() - data: $data2")
                        heavyDataProcessing(data2)

                        // We get here when data is processed accordingly. So, let's update the GUI.
                        val w = AndroidSchedulers.mainThread().createWorker()
                        w.schedule { tvLocalDataProcessingCount.text = "${tvLocalDataProcessingCount.text} $data2" }

                        Log.v(TAG, "forEach() - Leaving...")
                    }

                0
            }

            // Just for log purpose
            .compose(showDebugMessages("map"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // For the test perspective we could avoid subscribing our observable, since all the important job is being done inside the forEach operator. But
            // we will subscribe it in order to be able to stop it when we want (we have a stop button in the GUI)
            .subscribe(resultSubscriber())

    }

    /**
     * This example also uses manual recursion, but using another approach.
     *
     * @return
     */
    private fun processData_manualRecursion_with_repeatWhen(): Subscription {

        return Observable.just("")

            .subscribeOn(Schedulers.newThread())

            .map { _ ->
                Log.v(TAG, "map()")

                Observable
                    .timer(0, TimeUnit.SECONDS)
                    .flatMap { _ ->
                        Log.v(TAG, "timer() - Calling emitData()...")
                        emitData()
                    }

                    .repeatWhen { observable ->
                        Log.v(TAG, "repeatWhen()")
                        observable.delay(10, TimeUnit.MILLISECONDS)
                    }

                    .subscribeOn(Schedulers.computation())

                    .toBlocking()

                    .forEach { data ->
                        Log.v(TAG, "forEach() - data: $data")
                        heavyDataProcessing(data)

                        // We get here when data is processed accordingly. So, let's update the GUI.
                        val w = AndroidSchedulers.mainThread().createWorker()
                        w.schedule { tvLocalDataProcessingCount.text = "${tvLocalDataProcessingCount.text} $data" }
                    }

                ""
            }

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // For the test perspective we could avoid subscribe our observable, since all the important job is being done inside the forEach operator. But
            // we will subscribe it in order to be able to stop it when we want (we have a stop button in the GUI)
            .subscribe(resultSubscriber())
    }

    /**
     * This method simulates an intensive data processing.
     *
     * Actually it will only sleep for a while. This is a block operation, so be sure to call it always in a thread other than the mainThread,
     * otherwise you will may end up an ANR (Application Not Responding) error message.
     *
     * @param data
     */
    private fun heavyDataProcessing(data: Int?) {

        Log.v(TAG, "heavyDataProcessing() - Starting processing of data: $data")

        try {
            Thread.sleep(2000)
            Log.v(TAG, "heavyDataProcessing() - Data: $data was processed.")

        } catch (e: InterruptedException) {
            // If we cancel mSubscription while job is being processed, we will hit here.
            Log.e(TAG, "heavyDataProcessing() - Data: $data was not processed due an interruption.")
        }

    }

    private fun <Integer> resultSubscriber(): Subscriber<Integer> {
        return object : Subscriber<Integer>() {

            override fun onCompleted() {
                Log.v(TAG, "subscribe.onCompleted")
            }

            override fun onError(e: Throwable) {
                Log.v(TAG, "subscribe.doOnError: ${e.message}")
            }

            override fun onNext(number: Integer) {
                Log.v(TAG, "subscribe.onNext")
            }
        }
    }

    companion object {
        private val TAG = ServerPollingAfterDataProcessingExampleActivity::class.java.simpleName
    }
}
