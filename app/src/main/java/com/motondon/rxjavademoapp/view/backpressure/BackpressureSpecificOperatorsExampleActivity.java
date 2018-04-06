package com.motondon.rxjavademoapp.view.backpressure;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import rx.BackpressureOverflow;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 
 * This example shows some specific RxJava operators that handle backpressure
 *
 */
public class BackpressureSpecificOperatorsExampleActivity extends BaseActivity {

    private static final String TAG = BackpressureSpecificOperatorsExampleActivity.class.getSimpleName();
    private static final int RANDOM_SLEEP_TIME = -1;

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_overrun_numbers) TextView tvOverrunNumbers;
    @BindView(R.id.tv_overrun_caption) TextView tvOverrunCaption;
    @BindView(R.id.tv_result) TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backpressure_specific_operators_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }

        resetData();
    }

    private void resetData() {
        tvEmittedNumbers.setText("");
        tvResult.setText("");

        tvOverrunNumbers.setText("");
        tvOverrunNumbers.setTag(null);

        tvOverrunCaption.setText("");
    }

    @OnClick(R.id.btn_onbackpressurebuffer_operator_test)
    public void onBackpressureBufferOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onBackpressureBufferOperatorButtonClick()");
            resetData();
            startBackpressureBufferOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_onbackpressurebuffer_operator_with_an_action_test)
    public void onBackpressureBufferOperatorWithAnActionButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onBackpressureBufferOperatorWithAnActionButtonClick()");
            resetData();
            tvOverrunCaption.setText("Overrun");
            tvOverrunNumbers.setTag(0);
            startBackpressureBufferOperatorWithAnActionTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_onbackpressurebuffer_operator_with_an_action_and_a_strategy_test)
    public void onBackpressureBufferOperatorWithAnActionAndStrategyButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onBackpressureBufferOperatorWithAnActionAndStrategyButtonClick()");
            resetData();
            tvOverrunCaption.setText("Number of discarded items");
            tvOverrunNumbers.setTag(0);
            startBackpressureBufferOperatorWithAnActionAndStrategyTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_onbackpressuredrop_operator_test)
    public void onBackpressureDropOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onBackpressureDropOperatorButtonClick()");
            resetData();
            tvOverrunCaption.setText("Dropped items");
            startBackpressureDropOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    private Observable<Integer> emitNumbers(final Integer numberOfItemsToBeEmitted, final int timeToSleep) {
        Log.v(TAG, "emitNumbers()");

        return Observable
            .range(0, numberOfItemsToBeEmitted)
            .doOnNext((number) ->  {
                try {
                    Log.v(TAG, "emitNumbers() - Emitting number: " + number);
                    Thread.sleep(timeToSleep);

                    final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                    w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }
            });
    }

    /**
     * onBackpressureBuffer maintains a buffer of all emissions from the source Observable and emits them to downstream Subscribers according
     * to the requests they generate. If the buffer overflows, the observable will fail.
     *
     * @return
     */
    private Subscription startBackpressureBufferOperatorTest() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((num) -> emitNumbers(100, 50))

            // We are requesting observable to emit 100 items, but our BackpressureBuffer will be able to buffer only
            // 40 item. Since we are simulating a heavy processing by sleeping for 200ms on each item, when our buffer
            // hits 40 items, it will emit a Overflowed exception
            .onBackpressureBuffer(40)

            // Just for log purpose
            .compose(showDebugMessages("onBackpressureBuffer"))

            .observeOn(Schedulers.newThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(200));
    }

    private Subscription startBackpressureBufferOperatorWithAnActionTest() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((num) -> emitNumbers(100, 50))

            // We are requesting observable to emit 100 items, but our BackpressureBuffer will be able to buffer only
            // 40 item. Since we are simulating a heavy processing by sleeping for 200ms on each item, when our buffer
            // hits 40 items, it will emit a Overflowed exception, BUT since we are using a variant which accepts an action
            // we can take some when it happens (actually we are just printing something on the screen).
            .onBackpressureBuffer(40, () -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> {
                    final Integer count = (Integer) tvOverrunNumbers.getTag() + 1;
                    Log.d(TAG, "Buffer overrun for: " + count + " time(s)");
                    tvOverrunNumbers.setTag(count);

                    tvOverrunNumbers.setText(tvOverrunNumbers.getText() + "Ops, buffer overrun!");
                });
            })

            // Just for log purpose
            .compose(showDebugMessages("onBackpressureBuffer"))

            .observeOn(Schedulers.newThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(200));
    }

    private Subscription startBackpressureBufferOperatorWithAnActionAndStrategyTest() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((num) -> emitNumbers(100, 10))

            // We are requesting observable to emit 100 items, but our BackpressureBuffer will be able to buffer only
            // 40 item. Since we are simulating a hard processing by sleeping for 200ms on each item, when our buffer
            // hits 40 items, it will follow the overflow strategy. On our case, since we are using ON_OVERFLOW_DROP_OLDEST,
            // it will drop the oldest item whenever it runs into this situation.
            .onBackpressureBuffer(
                40, () -> {
                    final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                    w.schedule(() -> {

                        // Get some info here. They will be used later in onComplete to show the user.
                        final Integer count = (Integer) tvOverrunNumbers.getTag() + 1;
                        Log.d(TAG, "Buffer overrun for: " + count + " time(s)");
                        tvOverrunNumbers.setTag(count);
                    });
                },
                BackpressureOverflow.ON_OVERFLOW_DROP_OLDEST)

            // Just for log purpose
            .compose(showDebugMessages("onBackpressureBuffer"))

            .observeOn(Schedulers.newThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(200));
    }

    /**
     * From the docs:
     *
     * onBackpressureDrop drops emissions from the source Observable unless there is a pending request from a downstream
     * Subscriber, in which case it will emit enough items to fulfill the request.
     *
     * We added a random sleep timer while processing onNext in order to simulate a heavy processing. This will make onBackpressureDrop
     * operator to drop some items, since there will be some lack of requests from the downstream.
     *
     * @return
     */
    private Subscription startBackpressureDropOperatorTest() {

        return Observable
            .timer(0, TimeUnit.SECONDS)

            .flatMap((num) -> emitNumbers(100, 30))

            // We are requesting observable to emit 100 items, and also are simulating a hard processing by sleeping
            // sometime between 10 and 100ms for each item. Since we are using onBackpressureDrop, overflowed items will be dropped.
            .onBackpressureDrop((droppedItem) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvOverrunNumbers.setText(tvOverrunNumbers.getText() + " " + droppedItem));
            })

            // Just for log purpose
            .compose(showDebugMessages("onBackpressureDrop"))

            .observeOn(Schedulers.newThread())

            // Finally subscribe it.
            .subscribe(resultSubscriber(RANDOM_SLEEP_TIME));
    }

    /**
     * This method is used by the onBackpressureBuffer and onBackpressureDrop tests. Since onNext method
     * sleeps for a while (in order to simulate a processing longer than the emitted items), if we do this in
     * the main thread, GUI it might freeze. So, those notifications are being executed in a new worker thread.
     *
     * But, when we need to update GUI, of course we need to do it in the main thread.
     *
     * @param timeToSleep
     * @return
     */
    @NonNull
    private Subscriber<Integer> resultSubscriber(final Integer timeToSleep) {
        return new Subscriber<Integer>() {

            @Override
            public void onCompleted() {
                Log.v(TAG, "subscribe.onCompleted");
                // Request GUI to update in the main thread, since this notification is being executed in a worker thread.
                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() ->  {
                    tvResult.setText(tvResult.getText() + " - onCompleted");

                    // This is useful only for the startBackpressureBufferOperatorWithAnActionAndStrategyTest test, once it uses ON_OVERFLOW_DROP_OLDEST
                    // strategy, making onBackpressureBuffer() operator to not finish with error when its buffer is full, but discard the oldest item.
                    // Each time it discarded an item, we incremented a counter. Now it is time to show the user how many items were discarded.
                    if (tvOverrunNumbers.getTag() != null && tvOverrunNumbers.getTag() instanceof Integer) {
                        tvOverrunNumbers.setText(tvOverrunNumbers.getTag().toString());
                    }
                });
            }

            @Override
            public void onError(final Throwable e) {
                Log.v(TAG, "subscribe.doOnError: " + e);
                // Request GUI to update in the main thread, since this notification is being executed in a worker thread.
                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() -> tvResult.setText(tvResult.getText() + " - doOnError: " + e));
            }

            @Override
            public void onNext(final Integer number) {
                Log.v(TAG, "subscribe.onNext" + " " + number);

                // Sleep for a while in order to simulate a hard processing
                try {
                    Thread.sleep(timeToSleep == RANDOM_SLEEP_TIME ? (new Random().nextInt(100 - 10) + 10) : timeToSleep);
                } catch (InterruptedException e) {
                    Log.v(TAG, "subscribe.onNext. We got a InterruptedException!");
                }

                // Request GUI to update in the main thread, since this notification is being executed in a worker thread.
                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() ->  tvResult.setText(tvResult.getText() + " " + number));
            }
        };
    }
}
