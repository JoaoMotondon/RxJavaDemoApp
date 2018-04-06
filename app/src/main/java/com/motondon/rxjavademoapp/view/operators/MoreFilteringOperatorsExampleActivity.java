package com.motondon.rxjavademoapp.view.operators;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MoreFilteringOperatorsExampleActivity extends BaseActivity {

    private static final String TAG = MoreFilteringOperatorsExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_more_filtering_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }
    }

    private void resetData() {
        tvEmittedNumbers.setText("");
        tvResult.setText("");
    }

    @OnClick(R.id.btn_sample_operator_example)
    public void onSampleOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onSampleOperatorButtonClick()");
            resetData();
            subscription = startSampleOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "A test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_sample_operator_with_observable_example)
    public void onSampleOperatorWithObservableButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onSampleOperatorWithObservableButtonClick()");
            resetData();
            subscription = startSampleOperatorWithObservableTest();
        } else {
            Toast.makeText(getApplicationContext(), "A test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_throttle_last_example)
    public void onThrottleLastOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onThrottleLastOperatorButtonClick()");
            resetData();
            subscription = startThrottleLastOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "A test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_throttle_first_test)
    public void onThrottleFirstOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onThrottleFirstOperatorButtonClick()");
            resetData();
            subscription = startThrottleFirstOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "A test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_debounce_operator_test)
    public void onDebounceOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onDebounceOperatorButtonClick()");
            resetData();
            subscription = startDebounceOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "A test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_debounce_operator_with_func_test)
    public void onDebounceOperatorWithFuncButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onDebounceOperatorWithFuncButtonClick()");
            resetData();
            subscription = startDebounceOperatorWithFuncTest();
        } else {
            Toast.makeText(getApplicationContext(), "A test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Emit [numberOfItems] items either by a fixed interval (when fixedTimeToSleep is greater than zero) or by
     * using a random delay (randomSleepTime)
     *
     * @param numberOfItems
     * @param fixedTimeToSleep
     * @param randomSleepTime
     * @return
     */
    private Observable<Integer> emitItems(Integer numberOfItems, Integer fixedTimeToSleep, boolean randomSleepTime) {
        Log.v(TAG, "emitItems() - numberOfItems: " + numberOfItems);

        return Observable
            .range(0, numberOfItems)
            .doOnNext((number) -> {
                try {
                    Integer timeToSleep = 500;
                    if (fixedTimeToSleep > 0) {
                        timeToSleep = fixedTimeToSleep;
                    }

                    if (randomSleepTime) {
                        timeToSleep = new Random().nextInt(500 - 100) + 100;
                    }

                    Log.v(TAG, "emitItems() - Emitting number: " + number + " and sleeping for " + timeToSleep + "ms");
                    Thread.sleep(timeToSleep);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }

                // Now, log it on the GUI in order to inform user about the emitted item
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            });
    }

    /**
     * Sleep somewhere between one and five seconds just to simulate a (fake) operator
     *
     * @return
     */
    private Observable<Integer> doAFakeOperation() {
        Log.v(TAG, "doAFakeOperation()");

        return Observable.interval(100, TimeUnit.MILLISECONDS)
            .map((number) -> number.intValue())
            .doOnNext((number) -> {
                try {
                    Integer timeToSleep = new Random().nextInt(5 - 1) + 1;
                    Log.v(TAG, "doAFakeOperation() - Sleeping for " + timeToSleep + " second(s)");
                    Thread.sleep(timeToSleep * 1000);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }
            }).take(1);
    }

    /**
     * Source observable will emit 20 items, each one with 500ms of delay, but since we are using sample() operator with a
     * 4 seconds of period, only the last item emitted during each period will be emitted downstream.
     *
     * @return
     */
    private Subscription startSampleOperatorTest() {

        return emitItems(20, 500, false)

            .sample(4, TimeUnit.SECONDS)

            // Just for log purpose
            .compose(showDebugMessages("sample(4s)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * Source observable will emit some items, each one with 500ms of delay. Whenever doAFakeOperation() method terminates (somewhere between 1 and 5 seconds),
     * the most recent item emitted by the emitItems() will be emitted downstream by the sample operator.
     *
     * @return
     */
    private Subscription startSampleOperatorWithObservableTest() {

        return emitItems(50, 500, false)

            .sample(doAFakeOperation())

            // Just for log purpose
            .compose(showDebugMessages("sample(Observable)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * Source observable will emit some items, each one with 500ms of delay. By using throttleLast() operator with a
     * 4 seconds of period, only the last item emitted on each period will be emitted.
     *
     * This is similar to the sample() operator
     *
     * @return
     */
    private Subscription startThrottleLastOperatorTest() {

        return emitItems(20, 500, false)

            .throttleLast(4, TimeUnit.SECONDS)

            // Just for log purpose
            .compose(showDebugMessages("throttleLast(4s)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }


    /**
     * Source observable will emit some items, each one with 500ms of delay. By using throttleFirst() operator with a
     * 4 seconds of windowDuration, only the first item emitted during each interval will be emitted.
     *
     * @return
     */
    private Subscription startThrottleFirstOperatorTest() {

        return emitItems(20, 500, false)

            .throttleFirst(4, TimeUnit.SECONDS)

            // Just for log purpose
            .compose(showDebugMessages("throttleFirst(4s)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * In this example, debounce uses a timeout equals to 350ms. So, it will only emit an item downstream when source Observable
     * does not emit any item within 350ms. Noticed for this example, emitItems(...) method will emit items by using random interval
     * between 100 and 500ms
     *
     * @return
     */
    private Subscription startDebounceOperatorTest() {

        // Each emission will sleep between 100 and 500 milliseconds
        return Observable
            .timer(0, TimeUnit.SECONDS)
            .flatMap((aLong) -> emitItems(40, 0, true))

            // Debounce will only emit when source observable does not emit items for 350ms
            .debounce(350, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("debounce(350ms)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example will emit items each 1100ms. Debounce selector will sleep between 1 and 5 seconds. If it sleeps for 2 or more seconds,
     * source Observable will emit another item while the current one is still being processed, making it to be discarded. On the other hand, if
     * selector sleeps for 1 second, that means it will finish before the next emission, so it will not be dropped, but emitted downstream.
     *
     * @return
     */
    private Subscription startDebounceOperatorWithFuncTest() {

        return Observable
            .timer(0, TimeUnit.SECONDS)
            .flatMap((aLong) -> emitItems(20, 1100, false))

            .debounce((item) -> {
                Integer timeToSleep = new Random().nextInt(5 - 1) + 1;
                Log.v(TAG, "startDebounceOperatorWithFuncTest() - Sleeping for " + timeToSleep + " second(s) while processing item " + item);
                return Observable.just(item).delay(timeToSleep, TimeUnit.SECONDS);
            })

            // Just for log purpose
            .compose(showDebugMessages("debounce(Func1)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }
}