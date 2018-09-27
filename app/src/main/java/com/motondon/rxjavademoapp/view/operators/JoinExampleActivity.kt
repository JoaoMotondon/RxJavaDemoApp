package com.motondon.rxjavademoapp.view.operators

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.R.id.*
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.Arrays
import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_operators_join_example.*
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * This activity allows users to test different values for join operator:
 * - left Observable emission delay
 * - right Observable emission delay
 * - left window duration
 * - right window duration
 * - left Observable number of items to emit
 * - right Observable number of items to emit
 *
 */
class JoinExampleActivity : BaseActivity() {

    private var leftDelayBetweenEmission: Int = 0
    private var rightDelayBetweenEmission: Int = 0
    private var leftWindowDuration: Int = 0
    private var rightWindowDuration: Int = 0
    private var leftNumberOfItemsToEmit: Int = 0
    private var rightNumberOfItemsToEmit: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_join_example)

        btnJoinOperatorTest.setOnClickListener{ onSstartJoinOperatorTestButtonClick() }
        btnStopTest.setOnClickListener{ onStopSubscription() }

        supportActionBar?.title = intent.getStringExtra("TITLE")

        // Prevent keyboard to be visible when activity resumes.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    private fun resetData() {
        tvEmittedNumbers.text = ""
        tvResult.text = ""
    }

    private fun onSstartJoinOperatorTestButtonClick() {
        if (isUnsubscribed()) {
            Log.v(TAG, "onSstartJoinOperatorTestButtonClick()")
            resetData()
            readData()
            mSubscription = startJoinOperatorTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onStopSubscription() {
        mSubscription?.unsubscribe()
    }

    private fun readData() {
        leftDelayBetweenEmission = Integer.parseInt(etLeftObservableDelayBetweenEmission.text.toString())
        if (leftDelayBetweenEmission < 0) {
            leftDelayBetweenEmission = 0
        } else if (leftDelayBetweenEmission > 5000) {
            leftDelayBetweenEmission = 5000
        }
        Log.v(TAG, "readData() - leftDelayBetweenEmission: $leftDelayBetweenEmission")

        rightDelayBetweenEmission = Integer.parseInt(etRightObservableDelayBetweenEmission.text.toString())
        if (rightDelayBetweenEmission < 0) {
            rightDelayBetweenEmission = 0
        } else if (rightDelayBetweenEmission > 5000) {
            rightDelayBetweenEmission = 5000
        }
        Log.v(TAG, "readData() - rightDelayBetweenEmission: $rightDelayBetweenEmission")

        if (etLeftWindowDuration.text.toString().isEmpty()) {
            leftWindowDuration = NEVER_CLOSE
        } else {
            leftWindowDuration = Integer.parseInt(etLeftWindowDuration.text.toString())
            if (leftWindowDuration < 0) {
                leftWindowDuration = 0
            } else if (leftWindowDuration > 5000) {
                leftWindowDuration = 5000
            }
        }
        Log.v(TAG, "readData() - leftWindowDuration: $leftWindowDuration")

        if (etRightWindowDuration.text.toString().isEmpty()) {
            rightWindowDuration = NEVER_CLOSE
        } else {
            rightWindowDuration = Integer.parseInt(etRightWindowDuration.text.toString())
            if (rightWindowDuration < 0) {
                rightWindowDuration = 0
            } else if (rightWindowDuration > 5000) {
                rightWindowDuration = 5000
            }
        }
        Log.v(TAG, "readData() - rightWindowDuration: $rightWindowDuration")

        leftNumberOfItemsToEmit = Integer.parseInt(etLeftObservableNumberOfItemsToEmit.text.toString())
        if (leftNumberOfItemsToEmit < 1) {
            leftNumberOfItemsToEmit = 1
        } else if (leftNumberOfItemsToEmit > 40) {
            leftNumberOfItemsToEmit = 40
        }
        Log.v(TAG, "readData() - leftNumberOfItemsToEmit: $leftNumberOfItemsToEmit")

        rightNumberOfItemsToEmit = Integer.parseInt(etRightObservableNumberOfItemsToEmit.text.toString())
        if (rightNumberOfItemsToEmit < 1) {
            rightNumberOfItemsToEmit = 1
        } else if (rightNumberOfItemsToEmit > 40) {
            rightNumberOfItemsToEmit = 40
        }
        Log.v(TAG, "readData() - rightNumberOfItemsToEmit: $rightNumberOfItemsToEmit")
    }

    private fun emitItems(numberOfItemsToBeEmitted: Int, delayBetweenEmission: Int, caption: String): Observable<Int> {
        return Observable.interval(delayBetweenEmission.toLong(), TimeUnit.MILLISECONDS)
                .map { number -> number.toInt() }
                .doOnNext { number ->
                    Log.v(TAG, "emitItems() - $caption Observable. Emitting number: $number")
                    val w = AndroidSchedulers.mainThread().createWorker()
                    w.schedule { tvEmittedNumbers.text = "${tvEmittedNumbers.text} $number" }

                }
                .take(numberOfItemsToBeEmitted)
                .subscribeOn(Schedulers.newThread())
    }


    private fun startJoinOperatorTest(): Subscription {

        val left = emitItems(leftNumberOfItemsToEmit, leftDelayBetweenEmission, "left")
        val right = emitItems(rightNumberOfItemsToEmit, rightDelayBetweenEmission, "right")

        return left
            .join<Int, Long, Long, List<Int>>(right,
                { _ ->
                    if (leftWindowDuration === NEVER_CLOSE) {
                        return@join Observable.never()
                    } else {
                        return@join Observable.timer(leftWindowDuration.toLong(), TimeUnit.MILLISECONDS).compose<Long>(showDebugMessages<Long>("leftDuration")).subscribeOn(Schedulers.computation())
                    }
                },
                { _ ->
                    if (rightWindowDuration === NEVER_CLOSE) {
                        return@join Observable.never<Long>()
                    } else {
                        return@join Observable.timer(rightWindowDuration.toLong(), TimeUnit.MILLISECONDS).compose<Long>(showDebugMessages<Long>("rightDuration")).subscribeOn(Schedulers.computation())
                    }
                },
                { l, r ->
                    Log.v(TAG, "join() - Joining left number: $l with right number: $r")
                    Arrays.asList(l.toInt(), r.toInt())
                }
            )
            .compose(applySchedulers())
            .subscribe(resultSubscriber(tvResult))
    }

    companion object {

        private val TAG = JoinExampleActivity::class.java.simpleName
        private const val NEVER_CLOSE = -1
    }
}
