package com.motondon.rxjavademoapp.view.hotobservables;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.HotObservablesBaseActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * This example demonstrates how to use ConnectableObservable::refCount() operator.
 *
 * refCount keeps a reference to all the subscribers subscribed to it. When we call refCount, observable does not start emitting items, but only when
 * the first subscriber subscribe to it.
 *
 */
public class HotObservableRefCountExampleActivity extends HotObservablesBaseActivity {

    private static final String TAG = HotObservableRefCountExampleActivity.class.getSimpleName();

    // This is the Observable returned by the refCount() method call. Subscribers must use it to subscribe.
    private Observable<Long> observable;

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result_first_subscription) TextView tvResultFirstSubscription;
    @BindView(R.id.tv_result_second_subscription) TextView tvResultSecondSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_observable_refcount_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }

        // Just start our Hot Observable. Note that this will NOT make it to start emitting items
        connectable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .doOnNext((number) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            })

            // This will convert the Observable to a ConnectableObservable
            .publish();
    }

    private void resetData() {
        tvEmittedNumbers.setText("");
        tvResultFirstSubscription.setText("");
        tvResultSecondSubscription.setText("");
    }

    @OnClick(R.id.btn_refcount)
    public void onRefCountButtonClick() {

        if (firstSubscription == null || (firstSubscription != null && firstSubscription.isUnsubscribed()) &&
                (secondSubscription == null ||secondSubscription != null && secondSubscription.isUnsubscribed())) {

            Log.v(TAG, "onRefCountButtonClick()");
            resetData();
            refCount();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_subscribe_first)
    public void onSubscribeFirstButtonClick() {
        if (observable == null) {
            Log.v(TAG, "onSubscribeFirstButtonClick() - Cannot start a subscriber. You must first call refCount().");

            // When using refCount, we must subscribe our subscriber's upon the Observable returned by the refCount, and not on the
            // ConnectableObservable returned by the publish() (as we do when using connect() operator).
            Toast.makeText(getApplicationContext(), "You must first call refCount()", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firstSubscription == null || (firstSubscription != null && firstSubscription.isUnsubscribed())) {

            // Just clean up GUI in order to make things clear.
            if (secondSubscription != null && secondSubscription.isUnsubscribed()) {
                resetData();
            }

            Log.v(TAG, "onSubscribeFirstButtonClick()");
            firstSubscription = subscribeFirst();

        } else {
            Toast.makeText(getApplicationContext(), "First subscriber already started", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_subscribe_second)
    public void onSubscribeSecondButtonClick() {
        if (observable == null) {
            Log.v(TAG, "onSubscribeSecondButtonClick() - Cannot start a subscriber. You must first call refCount().");

            // When using refCount, we must subscribe our subscriber's upon the Observable returned by the refCount, and not on the
            // ConnectableObservable returned by the publish() (as we do when using connect() operator).
            Toast.makeText(getApplicationContext(), "You must first call refCount()", Toast.LENGTH_SHORT).show();
            return;
        }

        if (secondSubscription == null || (secondSubscription != null && secondSubscription.isUnsubscribed())) {

            // Just clean up GUI in order to make things clear.
            if (firstSubscription != null && firstSubscription.isUnsubscribed()) {
                resetData();
            }

            Log.v(TAG, "onSubscribeSecondButtonClick()");
            secondSubscription = subscribeSecond();

        } else {
            Toast.makeText(getApplicationContext(), "Second subscriber already started", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When unsubscribing a subscriber, if there is no more subscriber subscribed to the observable, it will stop emit items.
     *
     */
    @OnClick(R.id.btn_unsubscribe_first)
    public void onUnsubscribeFirstButtonClick() {
        Log.v(TAG, "onUnsubscribeFirstButtonClick()");
        unsubscribeFirst(true);
    }

    /**
     * When unsubscribing a subscriber, if there is no more subscriber subscribed to the observable, it will stop emit items.
     *
     */
    @OnClick(R.id.btn_unsubscribe_second)
    public void onUnsubscribeSecondButtonClick() {
        Log.v(TAG, "onUnsubscribeSecondButtonClick()");
        unsubscribeSecond(true);
    }

    private void refCount() {
        Log.v(TAG, "refCount()");

        // refCount returns Observable<T> that is connected as long as there are subscribers to it.
        observable = connectable.refCount();
    }

    /**
     * If this is the first subscriber to subscribe to the observable, it will make observable to start emitting items.
     *
     * @return
     */
    private Subscription subscribeFirst() {
        Log.v(TAG, "subscribeFirst()");

        return observable
            .compose(applySchedulers())
            .subscribe(resultSubscriber(tvResultFirstSubscription));
    }

    /**
     * If this is the first subscriber to subscribe to the observable, it will make observable to start emitting items.
     *
     * @return
     */
    private Subscription subscribeSecond() {
        Log.v(TAG, "subscribeSecond()");

        return observable
            .compose(applySchedulers())
            .subscribe(resultSubscriber(tvResultSecondSubscription));
    }
}
