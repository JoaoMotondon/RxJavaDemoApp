package com.motondon.rxjavademoapp.view.operators

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.R.id.*
import com.motondon.rxjavademoapp.view.base.BaseActivity

import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.main.activity_operators_concatmap_flatmap_example.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * Examples on this activity are based on the following article:
 *
 * http://fernandocejas.com/2015/01/11/rxjava-observable-tranformation-concatmap-vs-flatmap/
 *
 * Please, visit it in order to get more details about it.
 *
 */
class ConcatMapAndFlatMapExampleActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operators_concatmap_flatmap_example)

        btnFlatMapTest.setOnClickListener { onFlatMapTestButtonClick() }
        btnConcatMapTest.setOnClickListener { onConcatMapTestButtonClick() }

        supportActionBar?.title = intent.getStringExtra("TITLE")
    }

    private fun onFlatMapTestButtonClick() {

        if (isUnsubscribed()) {
            Log.v(TAG, "onFlatMapTestButtonClick")
            tvOriginalEmittedItems.text = ""
            tvFlatMapResult.text = ""
            mSubscription = flatMapTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onConcatMapTestButtonClick() {

        if (isUnsubscribed()) {
            Log.v(TAG, "onConcatMapTestButtonClick")
            tvOriginalEmittedItems.text = ""
            tvConcatMapResult.text = ""
            mSubscription = concatMapTest()
        } else {
            Toast.makeText(applicationContext, "Test is already running", Toast.LENGTH_SHORT).show()
        }
    }

    private fun emitData(): Observable<Int> {

        return Observable
            .range(1, 50)
            .doOnNext { number ->
                try {
                    Log.v(TAG, "emitData() - Emitting number: $number")
                    Thread.sleep(20)

                } catch (e: InterruptedException) {
                    Log.v(TAG, "Got an InterruptedException!")
                }

                val w = AndroidSchedulers.mainThread().createWorker()
                w.schedule { tvOriginalEmittedItems.text = "${tvOriginalEmittedItems.text} $number" }
            }
    }

    /**
     * This is a very simple test just to demonstrate how flatMap operator works.
     *
     * Basically (and according to the documentation), FlatMap merges the emissions of these Observables, so that they may interleave.
     *
     * @return
     */
    private fun flatMapTest(): Subscription {

        return emitData()

            .flatMap { data ->
                Observable

                    .just(data)

                    .compose(showDebugMessages("just"))

                    // Just adding a delay here, so that we can better see elements being emitted in the GUI
                    .delay(200, TimeUnit.MILLISECONDS)

                    .compose(showDebugMessages("delay"))
            }

            // Just for log purpose
            .compose(showDebugMessages("flatMap"))

            .map { data -> "$data" }

            // Just for log purpose
            .compose(showDebugMessages("map"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvFlatMapResult))
    }

    /**
     * This example is similar to the flatMapTest, but as the name implies, it uses concatMap operator instead.
     *
     * Note that concatMap() uses concat operator so that it cares about the order of the emitted elements.
     *
     * @return
     */
    private fun concatMapTest(): Subscription {

        return emitData()

            .concatMap { data ->
                // Here we added some log messages allowing us to analyse the concatMap() operator behavior.
                // We can see in the log messages that concatMap emits its items as they are received (after applies its function)
                Observable

                    .just(data)

                    .compose(showDebugMessages("just"))

                    // Just adding a delay here, so that we can better see elements being emitted in the GUI
                    .delay(200, TimeUnit.MILLISECONDS)

                    .compose(showDebugMessages("delay"))
            }

            // Just for log purpose
            .compose(showDebugMessages("concatMap"))

            .map { data -> data.toString() }

            // Just for log purpose
            .compose(showDebugMessages("map"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvConcatMapResult))
    }

    companion object {
        private val TAG = ConcatMapAndFlatMapExampleActivity::class.java.simpleName
    }
}
