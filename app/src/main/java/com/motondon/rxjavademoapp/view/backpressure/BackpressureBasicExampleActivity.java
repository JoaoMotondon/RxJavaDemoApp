package com.motondon.rxjavademoapp.view.backpressure;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 *
 * This activity shows two examples: one that emits items faster than they can be consumed. Quickly it will finish with a MissingBackpressureException.
 * The second one adds the throttleLast() operator to the chain in order to try to alliviate emitted items downstream to try to avoid that exception.
 *
 */
public class BackpressureBasicExampleActivity extends BaseActivity {

    private static final String TAG = BackpressureBasicExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backpressure_basic_example);
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

    @OnClick(R.id.btn_missingbackpressureexception_test)
    public void onMissingBackPressureExceptionButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onMissingBackPressureExceptionButtonClick()");
            resetData();
            subscription = starMissingBackpressureExceptionTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_throttle_operator_test)
    public void onThrottleOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onThrottleOperatorButtonClick()");
            resetData();
            tvEmittedNumbers.setText("Check the logs to see all emitted items");
            startThrottleOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This example will throw a MissingBackPressureException since the Observable is emitting items much faster
     * than they can be consumed.
     *
     * @return
     */
    private Subscription starMissingBackpressureExceptionTest() {

        // Emit one item per millisecond...
        return Observable
            .interval(1, TimeUnit.MILLISECONDS)

            .doOnNext((number) -> {
                Log.v(TAG, "doOnNext() - Emitted number: " + number);
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            })

            .compose(applySchedulers())

            // Sleep for 100ms for each emitted item. This will make we receive a BackpressureMissingException quickly.
            .subscribe(resultSubscriber(100));
    }

    /**
     * By using throttleLast operator (which is pretty much similar to collect) we reduce the chances to get a
     * MissingBackpressureException, since it will only emit the last item emitted in a certain period of time.
     *
     * For this example we are using throttleLast intervalDuration to 100ms, which might be enough to let observer to
     * process all emitted items. If we change intervalDuration to a small value (e.g.: 10ms), although we will still
     * use throttleLast operator, it will not be enough to prevent buffer's capacity to be full. Try both values to see
     * that in action.
     *
     * @return
     */
    private Subscription startThrottleOperatorTest() {

        return Observable
            .interval(1, TimeUnit.MILLISECONDS)

            .doOnNext((number) -> {
                Log.v(TAG, "doOnNext() - Emitted number: " + number);

                // For this example we will not print emitted items on the GUI, since it would freeze it. Check the logs to see all emitted items
                // final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                // w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            })

            // Using throttleLast intervalDuration equals to 100ms, we will probably not end up in an exception, since our subscriber will be able to
            // process all emitted items accordingly. If we change it to 10ms, it will quickly throw a MissingBackpressureException.
            .throttleLast(100, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("throttleLast(100)"))

            // Just adding some boundaries here
            .take(20)

            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(100));
    }

    @NonNull
    private Subscriber<Long> resultSubscriber(final Integer timeToSleep) {
        return new Subscriber<Long>() {

            @Override
            public void onCompleted() {
                Log.v(TAG, "subscribe.onCompleted");
                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() -> tvResult.setText(tvResult.getText() + " - onCompleted"));
            }

            @Override
            public void onError(final Throwable e) {
                Log.v(TAG, "subscribe.doOnError: " + e);
                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() -> tvResult.setText(tvResult.getText() + " - doOnError" + e));
            }

            @Override
            public void onNext(final Long number) {
                Log.v(TAG, "subscribe.onNext" + " " + number);
                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() -> tvResult.setText(tvResult.getText() + " " + number));

                try {
                    Thread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    Log.v(TAG, "subscribe.onNext. We got a InterruptedException!");
                }
            }
        };
    }
}
