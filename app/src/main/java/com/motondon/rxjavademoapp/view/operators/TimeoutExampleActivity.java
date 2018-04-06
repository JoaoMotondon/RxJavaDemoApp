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

public class TimeoutExampleActivity extends BaseActivity {

    private static final String TAG = TimeoutExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_timeout_example);
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

    @OnClick(R.id.btn_start_timeout_operator_test)
    public void onStartTimeoutTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartTimeoutTestButtonClick()");
            resetData();
            subscription = startTimeoutTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_timeout_operator_with_second_observable_test)
    public void onStartTimeoutWithSecondObservableTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartTimeoutWithSecondObservableTestButtonClick()");
            resetData();
            subscription = startTimeoutWithSecondObservableTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_timeout_operator_with_a_function_test)
    public void onStartTimeoutWithFunctionTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartTimeoutWithFunctionTestButtonClick()");
            resetData();
            subscription = startTimeoutWithFunction();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_timeout_operator_with_a_function_and_a_second_observable_test)
    public void onStartTimeoutWithFunctionAndSecondObservableTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartTimeoutWithFunctionAndSecondObservableTestButtonClick()");
            resetData();
            subscription = startTimeoutWithFunctionAndSecondObservable();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_timeout_operator_with_a_function_and_a_second_function_test)
    public void onStartTimeoutWithFunctionAndSecondFunctionTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartTimeoutWithFunctionAndSecondFunctionTestButtonClick()");
            resetData();
            subscription = startTimeoutWithAFunctionForTheFirstEmittedItemAndAnotherFunctionForTheOtherItems();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_timeout_operator_with_a_function_a_second_function_and_a_second_observable_test)
    public void onStartTimeoutWithFunctionAndSecondFunctionAndSecondObservableTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartTimeoutWithFunctionAndSecondFunctionAndSecondObservableTestButtonClick()");
            resetData();
            subscription = startTimeoutWithFunctionAndASecondFunctionAndASecondObservable();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }


    @OnClick(R.id.btn_start_bonus_test)
    public void onStartBonusTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartBonusTestButtonClick()");
            resetData();
            subscription = startBonusTest_TimeoutWithRetry();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    private Observable<Integer> emitItems(Integer numberOfItems) {
        Log.v(TAG, "emitItems() - numberOfItems: " + numberOfItems);

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(0, numberOfItems)

            .doOnNext((number) -> {
                try {
                    int timeout = new Random().nextInt(700 - 100) + 100;

                    Log.v(TAG, "Item: " + number + " will be emitted with a delay around: " + timeout + "ms");
                    Thread.sleep(timeout);
                    Log.v(TAG, "emitItems() - Emitting number: " + number);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            });
    }

    /**
     * Useful for the tests that uses a second observable in case of a timeout.
     *
     * @return
     */
    private Observable<Integer> emitSecondItems() {
        Log.v(TAG, "emitSecondItems()");

        return Observable
            .range(100, 5)
            .doOnNext((number) -> {
                try {
                    Log.v(TAG, "emitSecondItems() - Emitting number: " + number);
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            });
    }

    /**
     * If source takes more than 500ms to emit, an error is emitted.
     *
     * @return
     */
    private Subscription startTimeoutTest() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((i) -> emitItems(20))

            .timeout(500, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("timeout"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example will trigger a second observable in case of the first observable takes more than 500ms to emit
     *
     * @return
     */
    private Subscription startTimeoutWithSecondObservableTest() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((i) -> emitItems(20))

            .timeout(500, TimeUnit.MILLISECONDS, emitSecondItems())

            // Just for log purpose
            .compose(showDebugMessages("timeout(300ms)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example applies different timeouts based on whether emitted items are even or odd
     *
     * @return
     */
    private Subscription startTimeoutWithFunction() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((i) -> emitItems(20))

            .timeout((number) -> {
                // For even emitted numbers, a 300ms timeout is applied, otherwise 600ms.
                if (number % 2 == 0) {
                    return Observable.timer(300, TimeUnit.MILLISECONDS);
                } else {
                    return Observable.timer(600, TimeUnit.MILLISECONDS);
                }
            })

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example is basically a combination of the two previous examples: startTimeoutWithSecondObservableTest and startTimeoutWithFunction
     *
     * @return
     */
    private Subscription startTimeoutWithFunctionAndSecondObservable() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((i) -> emitItems(20))

            .timeout((number) -> {
                // For even emitted numbers, a timeout of 300ms is used, otherwise 600ms. For any item, in case of timeout, a second observable is triggered.
                if (number % 2 == 0) {
                    return Observable.timer(300, TimeUnit.MILLISECONDS);
                } else {
                    return Observable.timer(600, TimeUnit.MILLISECONDS);
                }
            }, emitSecondItems())

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function and a second observable)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example applies a function for the first item by defining a timeout only for it. Then, it defines a second function
     * that will be applied to all remaining items.
     *
     * @return
     */
    private Subscription startTimeoutWithAFunctionForTheFirstEmittedItemAndAnotherFunctionForTheOtherItems() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((i) -> emitItems(20))

            .timeout(() ->

                // This function will be applied only for the first emitted item
                Observable.timer(6500, TimeUnit.MILLISECONDS),
                    (number) -> {
                        // For all the other emitted items, for even numbers, a timeout of 300ms is used, otherwise 600ms.
                        if (number % 2 == 0) {
                            return Observable.timer(300, TimeUnit.MILLISECONDS);
                        } else {
                            return Observable.timer(600, TimeUnit.MILLISECONDS);
                        }
                    })

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function and a second function)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startTimeoutWithFunctionAndASecondFunctionAndASecondObservable() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((i) -> emitItems(20))

            .timeout(() ->

                // This function will be applied only for the first emitted item.
                Observable.timer(500, TimeUnit.MILLISECONDS),
                    (number) -> {
                        // For all the other emitted items, for even numbers, a timeout of 300ms is used, otherwise 600ms.
                        if (number % 2 == 0) {
                            return Observable.timer(300, TimeUnit.MILLISECONDS);
                        } else {
                            return Observable.timer(600, TimeUnit.MILLISECONDS);
                        }
                    },

                // In case  of timeout on any item (i.e. the first one that uses an exclusive function or any other), this
                // observable will be used instead of terminating with an error.
                emitSecondItems())

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function, a second function and a second observable)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This is a bonus example. If source takes longer than 500ms to emit items, a timeout will be thrown and retryWhen will delay 5 seconds prior to emit. It source throws error for
     * three times, retryWhen will emit an error and the whole chain will finish
     *
     */
    private int attemptCount= 0;
    private static final int MAX_ATTEMPTS = 3;
    private Subscription startBonusTest_TimeoutWithRetry() {

        attemptCount= 0;

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((i) -> emitItems(20))

            .timeout(500, TimeUnit.MILLISECONDS)

            .retryWhen(error -> error
                .flatMap(throwable -> {
                    if (++attemptCount >= MAX_ATTEMPTS) {
                        return (Observable<?>) Observable.error(new Throwable("Reached max number of attempts (" + MAX_ATTEMPTS + "). Emitting an error..."));
                    } else {
                        Log.v(TAG, "Wait for 5 seconds prior to retry");
                        return (Observable<?>) Observable.timer(5, TimeUnit.SECONDS);
                    }
                })
            )

            // Just for log purpose
            .compose(showDebugMessages("timeout(w/function, a second function and a second observable)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }
}
