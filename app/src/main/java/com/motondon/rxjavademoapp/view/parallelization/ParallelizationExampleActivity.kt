package com.motondon.rxjavademoapp.view.parallelization

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.util.Pair
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.*

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.adapter.SimpleStringAdapter
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Random
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import kotlinx.android.synthetic.main.activity_parallelization_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Most examples in this activity were based on the following articles:
 * - http://tomstechnicalblog.blogspot.com.br/2015/11/rxjava-achieving-parallelization.html
 * - http://tomstechnicalblog.blogspot.com.br/2016/02/rxjava-maximizing-parallelization.html
 *
 * This is a good SO question that might be useful when dealing with schedulers:
 * - http://stackoverflow.com/questions/31276164/rxjava-schedulers-use-cases
 *
 * Please, visit them in order to get into details.
 *
 */
class ParallelizationExampleActivity : BaseActivity() {

    private lateinit var mSimpleStringAdapter: SimpleStringAdapter

    // Holds the method name related to the test user chosen in the spinner. It will be used via reflection to call the method instance.
    private var currentTestMethodName: String = ""

    private var defaultTextViewResultColor: ColorStateList? = null

    /**
     * This is a helper class that is used to fill the spinner up with pairs of test name and a related method name.
     *
     */
    internal inner class SpinnerOptionAndMethodName(first: String, second: String) : Pair<Any, Any>(first, second) {

        override fun toString(): String {
            return first as String
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parallelization_example)

        // Fill spinner view up with all available test names and related method names. We will use reflection to call a method based on the user choice
        val testOptions = ArrayList<SpinnerOptionAndMethodName>()
        testOptions.add(SpinnerOptionAndMethodName("Another Single Thread", "startAnotherSingleThreadTest"))
        testOptions.add(SpinnerOptionAndMethodName("Schedulers.newThread", "startSchedulersNewThreadTest"))
        testOptions.add(SpinnerOptionAndMethodName("Too many threads", "startTooManyThreadsTest"))
        testOptions.add(SpinnerOptionAndMethodName("Schedulers.computation", "startSchedulersComputationTest"))
        testOptions.add(SpinnerOptionAndMethodName("newThread with maxConcurrency", "startNewThreadWithMaxConcurrencyTest"))
        testOptions.add(SpinnerOptionAndMethodName("IO Thread with maxConcurrency", "startIoThreadWithMaxConcurrencyTest"))
        testOptions.add(SpinnerOptionAndMethodName("Get number of available processors", "startAvailableProcessorsTest"))
        testOptions.add(SpinnerOptionAndMethodName("Round-Robin (w/groupBy)", "startRoundRobinWithGroupByOperatorTest"))

        val adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, testOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sTestOptions.adapter = adapter

        // Set the default method name (the first option).
        currentTestMethodName = "startAnotherSingleThreadTest"

        supportActionBar?.title = intent.getStringExtra("TITLE")

        sTestOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinnerTestOptionsItemSelected(parent as Spinner, position)
            }
        }
        //sTestOptions.onItemSelectedListener { (spinner, position) -> spinnerTestOptionsItemSelected(spinner, position)}
        
        btnStartTest.setOnClickListener { view -> onButtonClick() }

        listResult.layoutManager = LinearLayoutManager(this)
        mSimpleStringAdapter = SimpleStringAdapter(this)
        listResult.adapter = mSimpleStringAdapter

        defaultTextViewResultColor = tvStatus.textColors
    }
    
    fun spinnerTestOptionsItemSelected(spinner: Spinner, position: Int) {
        val testItem = spinner.adapter.getItem(position) as SpinnerOptionAndMethodName

        currentTestMethodName = testItem.second as String
    }

    private fun onButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onTakeExampleButtonClick()")

            tvStatus.setTextColor(defaultTextViewResultColor)
            tvStatus.text = "Running test..."
            mSimpleStringAdapter.clear()

            try {

                val m = this.javaClass.getDeclaredMethod(currentTestMethodName)
                mSubscription = m.invoke(this) as Subscription

            } catch (e: NoSuchMethodException) {
                Log.e(TAG, "onTakeExampleButtonClick() - NoSuchMethodException: ${e.message}")
            } catch (e: InvocationTargetException) {
                Log.e(TAG, "onTakeExampleButtonClick() - InvocationTargetException: ${e.message}")
            } catch (e: IllegalAccessException) {
                Log.d(TAG, "onTakeExampleButtonClick() - IllegalAccessException: ${e.message}")
            }

        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * This example demonstrates how to ask our observable to be executed in another thread than that one subscribe method was
     * called. To do so, we just call subscriberOn() and use a scheduler on it (this example uses Schedulers.IO). We can also
     * create our own Scheduler implementation, but this is out of scope of this example.
     *
     * Also we are using observerOn() operator to instruct our observable to emit notifications to its observable using Android
     * mainThread.
     *
     * xxx
     *
     * When using subscribeOn, we decide on what Scheduler the Observable.create (or any static method which implicitly calls
     * it) is executed.
     *
     * observeOn controls, on the other hand, the other side of the pipeline, instructing Observable to perform its emission
     * and notifications on a specific scheduler.
     *
     * Important to note that when calling subscriberOn, no matter at what point in the chain of operators it is called, it
     * designates which thread the Observable will begin operating on.
     *
     * Note this will not make emissions to happen in parallel, but all of them will happen in another thread, but synchronously
     * on that thread.
     *
     *
     * From the docs:
     *
     * "By default, an Observable and the chain of operators that you apply to it will do its work, and will notify its observers,
     * on the same thread on which its Subscribe method is called. The SubscribeOn operator changes this behavior by specifying a
     * different Scheduler on which the Observable should operate. The ObserveOn operator specifies a different Scheduler that
     * the Observable will use to send notifications to its observers."
     *
     * xxx
     *
     * One last note: Schedulers.newThread is not recommended to use for intensive computational work (exactly what we are doing in
     * our heavyDataProcessing method) since computational work is expected to use CUP intensively and Schedulers.newThread theoretically
     * can create unlimited threads, which might decrease the performance. But, since this example is only intended to demonstrate
     * how to run an observable in another thread than the mainThread, we will use Schedulers.newThread to show each emission will be
     * processed in a different thread.
     *
     * @return
     */
    private fun startAnotherSingleThreadTest(): Subscription {

        return Observable.range(0, 10)
            .map<Int> { number -> heavyDataProcessing(number, 100, 500) }
            .compose(this.showDebugMessages("map"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber())
    }

    /**
     * As pointed out by Thomas Nield in the links above, by just using subscribeOn we are not achieving parallelization,
     * but just instructing our Observable to be executed in another single thread (this was demonstrated in the
     * startAnotherSingleThreadTest example.
     *
     * Now, we will make real parallelization. To do so, we can use flatMap() operator and call subscribeOn() for each new
     * observable it returns.
     *
     * Depends on how many threads we are creating, this might not be a good approach, since newThread will (in theory)
     * create unlimited threads an will throw an exception if it cannot allocate resources to create a new thread.
     *
     * The result for this method is that each emission will be executed in a different thread in parallel (ok, not every
     * emission is in parallel, since it depends on the number of cores. On the device I used to test it, I had two cores
     * and so, we have some (little) real concurrence).
     *
     * We will see later how to limit the number of threads based on the number of available processors.
     *
     * @return
     */
    private fun startSchedulersNewThreadTest(): Subscription {

        return Observable.range(0, 10)
            .flatMap { number ->
                Observable
                    .just(number)
                    .subscribeOn(Schedulers.newThread())
                    .map<Int> { n -> heavyDataProcessing(n, 1000, 5000) }
            }
            .compose(this.showDebugMessages("flatMap"))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber())
    }

    /**
     * This example is similar to the previous one (startSchedulersNewThreadTest), but instead of create only
     * 10 threads, it will create 10000! This is to demonstrate that Schedulers.newThread will try create new
     * threads for each tasks until it can allocate resources for it. Once it cannot create a new thread, it will
     * throw an exception.
     *
     * @return
     */
    private fun startTooManyThreadsTest(): Subscription {

        return Observable.range(0, 10000)
            .flatMap { number ->
                Observable
                    .just(number)
                    .subscribeOn(Schedulers.newThread())
                    .map<Int> { n -> heavyDataProcessing(n, 100, 500) }
            }
            .compose(this.showDebugMessages("flatMap"))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber())
    }

    /**
     * This example demonstrates how to run parallel computation threads.
     *
     * Due the computation scheduler nature, even if we request a huge number of tasks to run in parallel, it will
     * limit the number of parallel threads based on the the number of processors.
     *
     * Ex.: In a dual core system, we will have up to 2 simultaneously threads.
     *
     * @return
     */
    private fun startSchedulersComputationTest(): Subscription {

        return Observable.range(0, 10)
            .flatMap { number ->
                Observable
                    .just(number)
                    // Schedulers.computation() will limit the number of simultaneously threads based on the number
                    // of available processors.
                    .subscribeOn(Schedulers.computation())
                    .map<Int> { n -> heavyDataProcessing(n, 3000, 5000) }
            }
            .compose(this.showDebugMessages("flatMap"))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber())
    }

    /**
     * This test will use a flatMap maxConcurrent parameter  to MAX_CONCURRENT (which we set its value to 20) in order to
     * limit up to 20 simultaneously threads. This is a good approach to use with Schedulers.newThread and Schedulers.IO,
     * since both schedulers in theory could create unlimited number of threads.
     *
     * We can see it in the result GUI RecycleBin list, which will print "blocks" of 20 items each 8-9 seconds. In the end
     * we can see that, although only 20 thread were executed simultaneously, all of the 60 tasks used in the example were
     * executed using different threads (due the Scheduleer.newThread which always creates a new thread for each task).
     *
     * @return
     */
    private fun startNewThreadWithMaxConcurrencyTest(): Subscription {

        return Observable.range(0, 60)
            .flatMap({ number ->
                Observable
                    .just(number)
                    .subscribeOn(Schedulers.newThread())
                    .map<Int> { n -> heavyDataProcessing(n, 4000, 6000) }
            },
                // Even when requesting tasks to be executed using a new thread, system will limit the number of simultaneously
                // treads up to 20, due this flatmap maxConcurrent parameter.
                MAX_CONCURRENT
            )
            .compose(this.showDebugMessages("flatMap"))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber())
    }

    /**
     * This test will also use MAX_CONCURRENT (which is set to value of 20) in order to limit up to 20 simultaneously
     * threads, but it differs from the previous one (startNewThreadWithMaxConcurrencyTest) in a way it uses
     * Schedulers.IO which uses internally a thread pool (that grows as needed and in our case it will grow up to 20
     * threads). So, whenever a thread finishes its job, that thread goes back to the thread pool in order to be reused
     * when needed.
     *
     * We can see it in the result GUI RecycleBin list, which will print "blocks" of 20 items each 8-9 seconds. In the end
     * we can see that only 20 threads were created to process all of the 60 tasks (due the Schedulers.IO that keeps a
     * thread pool internally).
     *
     * @return
     */
    private fun startIoThreadWithMaxConcurrencyTest(): Subscription {

        return Observable.range(0, 60)
            .flatMap({ number ->
                Observable
                    .just(number)
                    .subscribeOn(Schedulers.io())
                    .map<Int> { n -> heavyDataProcessing(n, 3000, 4000) }
            },
                MAX_CONCURRENT
            )
            .compose(this.showDebugMessages("flatMap"))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber())
    }

    /**
     * This example will get the number of available processors and create an executor based on it. Then,
     * it will create a Scheduler which will use that executor. Later that scheduler will be use in the
     * subscribeOn() operator.
     *
     * By creating a scheduler from an executor, it gives us more control over how many threads we want
     * to run in parallel. In our case, we will use number of processors + 1 which seems to be a good
     * approach to not to decrease the system performance.
     *
     * When comparing this test with startSchedulersComputationTest(), we can note this one runs faster.
     * I do not know how Schedulers.computation works internally, but it seems it is not so optimized as
     * we expect. Maybe there is a plausible reason for that. In the Thomas Nield's maximizin
     * parallelization post, there is a comment from @Kaloyan Roussev which he gives a reasonable try
     * about it. Anyway, it needs further investigation.
     *
     * @return
     */
    private fun startAvailableProcessorsTest(): Subscription {

        // Get the number of available processors on the device and create a scheduler based on it.
        val numberOfThreads = Runtime.getRuntime().availableProcessors() + 1
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val scheduler = Schedulers.from(executor)

        return Observable.range(0, 40)
            .flatMap { number ->
                Observable
                    .just(number)
                    // Since we are using a scheduler from an executor, we can control how many threads
                    // will really run in parallel.
                    .subscribeOn(scheduler)
                    .map<Int> { n -> heavyDataProcessing(n, 3000, 5000) }
            }
            .finallyDo { executor.shutdown() }
            .compose(this.showDebugMessages("flatMap"))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber())
    }

    /**
     * This is a very interesting example. I found it first in the Thomas Nield article in the question
     * section in a question by David Karnok (http://tomstechnicalblog.blogspot.com.br/2015/11/rxjava-achieving-parallelization.html)
     * and then later Thomas created a post about it (http://tomstechnicalblog.blogspot.com.br/2016/02/rxjava-maximizing-parallelization.html).
     *
     * Depends on what kind of processing we are doing in a thread, creating Observable multiple times can be very expensive (imagine a
     * situation where we have 100000 items to be processed!). So it is possible to break up the emissions into batches by using groupBy
     * operator and flatmap the groups. This example breaks up emissions in [number of available cores + 1] GroupObservables, and each
     * one will run in a different thread.
     *
     * I strongly recommend you to take a look in the links mentioned above. They are very very useful!
     *
     * @return
     */
    private fun startRoundRobinWithGroupByOperatorTest(): Subscription {

        val n = AtomicInteger(0)

        // Create a scheduler based on the number of available cores + 1.
        val numberOfThreads = Runtime.getRuntime().availableProcessors() + 1
        val executor = Executors.newFixedThreadPool(numberOfThreads)
        val scheduler = Schedulers.from(executor)

        return Observable.range(0, 40)
            .groupBy { number ->
                // Just get AtomicInteger "n" variable value here for the log purpose, since after getAndIncrement() method call returns, its value is
                // incremented, but we want to log it prior incrementation.
                val i = n.getAndIncrement()
                val modulus = i % numberOfThreads
                Log.v(TAG, "groupBy - Data: $number - n: $n - n.getAndIncrement() % $numberOfThreads: $modulus")

                // Since we are incrementing this AtomicInteger for every emitted item, they will be grouped in batches
                // of numbers multiple of numberOfThreads + 1.
                modulus
            }

            // groupBy operator will group emissions into observables based on the result of its function. So, for each group will
            // be created (or reused if it is already created) an observable on which items will be emitted on. Then, all we have to do is
            // to flatmap it and use GroupedObservable to map the emitted items and call our heavy processing method.
            .flatMap { num ->
                num.observeOn(scheduler)
                // The groupBy operator requires the returned GroupedObservable to be subscribed, otherwise it won't request more data.
                // So we need to work upon the groupObservable in order to be able to get all items emitted on each group.
                .map { nn -> heavyDataProcessing(nn, 400, 500)}

            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber())
    }

    /**
     * This is a helper method which just sleeps for a while in order to simulate a heavy data processing.
     *
     * @param number
     * @param minSleepTime
     * @param maxSleepTime
     * @return
     */
    private fun heavyDataProcessing(number: Int, minSleepTime: Int, maxSleepTime: Int): Int {
        try {

            val timeToSleep = Random().nextInt(maxSleepTime - minSleepTime) + minSleepTime
            val msg = "Processing data $number on thread: ${Thread.currentThread().name} - Sleeping for ${timeToSleep}ms..."
            Log.v(TAG, msg)

            val w = AndroidSchedulers.mainThread().createWorker()
            w.schedule {
                mSimpleStringAdapter.addString(msg)
                listResult.scrollToPosition(mSimpleStringAdapter.itemCount - 1)
            }

            Thread.sleep(timeToSleep.toLong())

            return number

        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    private fun resultSubscriber(): Subscriber<Int> {
        return object : Subscriber<Int>() {

            override fun onCompleted() {
                Log.v(TAG, "subscribe.onCompleted")
                tvStatus.text = "Test executed successfully."
                tvStatus.setTextColor(Color.parseColor("#99cc00"))
            }

            override fun onError(e: Throwable) {
                Log.v(TAG, "subscribe.doOnError: " + e.message)
                tvStatus.setTextColor(Color.parseColor("#d50000"))
                tvStatus.text = "Test finished with errors: ${e.message}"
            }

            override fun onNext(number: Int?) {
                Log.v(TAG, "subscribe.onNext $number")
            }
        }
    }

    companion object {
        private val TAG = ParallelizationExampleActivity::class.java.simpleName
        private val MAX_CONCURRENT = 20
    }
}
