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

public class BackpressureReactivePullExampleActivity extends BaseActivity {

    private static final String TAG = BackpressureReactivePullExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backpressure_reactive_pull_example);
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

    @OnClick(R.id.btn_subscriber_with_request_method_call_test)
    public void onSubscriberWithRequestMethodCallButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onSubscriberWithRequestMethodCallButtonClick()");
            resetData();
            startSubscriberWithRequestMethodCallTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    private Observable<Integer> emitNumbers(final Integer numberOfItemsToBeEmitted, final int timeToSleep) {
        Log.v(TAG, "emitNumbers()");

        return Observable
            .range(0, numberOfItemsToBeEmitted)
            .doOnNext((number) -> {
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
     * This example demonstrates how to use Subscriber::request() method. Since our subscriber was initialized by calling
     * Subscriber::request(1), that means one item will be requested at a time. Later, in Subscriber's onNext, only after
     * it processes an item, another one will be requested.
     *
     * @return
     */
    private Subscription startSubscriberWithRequestMethodCallTest() {

        return Observable
            .timer(0, TimeUnit.SECONDS)
            .flatMap((num) -> emitNumbers(20, 10))

            // Since our subscriber will request for item, this is one way on how we can log it.
            .doOnRequest((number) -> Log.v(TAG, "Requested " + number))

            // Subscribe our subscriber which will request for items.
            .subscribe(resultSubscriber());
    }

    @NonNull
    private Subscriber<Integer> resultSubscriber() {
        return new Subscriber<Integer>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onCompleted() {
                Log.v(TAG, "subscribe.onCompleted");
                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() ->  tvResult.setText(tvResult.getText() + " - onCompleted"));
            }

            @Override
            public void onError(final Throwable e) {
                Log.v(TAG, "subscribe.doOnError: " + e);
                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() ->  tvResult.setText(tvResult.getText() + " - doOnError" + e));
            }

            @Override
            public void onNext(final Integer number) {
                Log.v(TAG, "subscribe.onNext" + " " + number);

                try {
                    // Sleep for a while. We could do whatever we want here prior to request a new item. This is totally
                    // up to us
                    Thread.sleep(500);

                    // Now, after "processing" the item, request observable to emit another one
                    request(1);
                } catch (InterruptedException e) {
                    Log.v(TAG, "subscribe.onNext. We got a InterruptedException!");
                }

                final Scheduler.Worker w2 = AndroidSchedulers.mainThread().createWorker();
                w2.schedule(() ->  tvResult.setText(tvResult.getText() + " " + number));
            }
        };
    }
}
