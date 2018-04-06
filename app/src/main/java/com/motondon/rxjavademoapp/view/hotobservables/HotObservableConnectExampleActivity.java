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
 * This example demonstrates how to use ConnectableObservable::connect() operator.
 *
 */
public class HotObservableConnectExampleActivity extends HotObservablesBaseActivity {

    private static final String TAG = HotObservableConnectExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result_first_subscription) TextView tvResultFirstSubscription;
    @BindView(R.id.tv_result_second_subscription) TextView tvResultSecondSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_observable_connect_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }

        // We will create the ConnectableObservable in the onCreate() method, so it will be available for the two
        // subscribers to subscribe to it, even before call ConnectionObservable::connect().
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

    @OnClick(R.id.btn_connect)
    public void onConnectButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onConnectButtonClick()");
            resetData();
            subscription = connect();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_subscribe_first)
    public void onSubscribeFirstButtonClick() {
        if (firstSubscription == null || (firstSubscription != null && firstSubscription.isUnsubscribed())) {
            Log.v(TAG, "onSubscribeFirstButtonClick()");
            firstSubscription = subscribeFirst();
        } else {
            Toast.makeText(getApplicationContext(), "First subscriber already started", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_subscribe_second)
    public void onSubscribeSecondButtonClick() {
        if (secondSubscription == null || (secondSubscription != null && secondSubscription.isUnsubscribed())) {
            Log.v(TAG, "onSubscribeSecondButtonClick()");
            secondSubscription = subscribeSecond();
        } else {
            Toast.makeText(getApplicationContext(), "Second subscriber already started", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_unsubscribe_first)
    public void onUnsubscribeFirstButtonClick() {
        Log.v(TAG, "onUnsubscribeFirstButtonClick()");
        unsubscribeFirst(true);
    }

    @OnClick(R.id.btn_unsubscribe_second)
    public void onUnsubscribeSecondButtonClick() {
        Log.v(TAG, "onUnsubscribeSecondButtonClick()");
        unsubscribeSecond(true);
    }

    @OnClick(R.id.btn_disconnect)
    public void onDisconnectButtonClick() {
        Log.v(TAG, "onDisconnectButtonClick()");

        if (subscription != null && !subscription.isUnsubscribed()) {
            unsubscribeFirst(false);
            unsubscribeSecond(false);
            disconnect();
        } else {
            Toast.makeText(getApplicationContext(), "Observable not connected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * From the docs:
     *
     * "A Connectable Observable resembles an ordinary Observable, except that it does not begin emitting items when
     * it is subscribed to, but only when its connect() method is called."
     *
     * This means that when user clicks on "connect" button, if there is already any subscriber subscribed to it, it will start
     * receiving emitted items, otherwise, emitted items will be discarded.
     *
     * After connecting to the observable, new subscribers will only receive new emitted items.
     *
     * @return
     */
    private Subscription connect() {
        Log.v(TAG, "connect()");

        // This will instruct the connectable observable to begin emitting items. If there is any subscriber subscribed to it,
        // it will start receiving items.
        return connectable.connect();
    }

    /**
     * If observable is already connected, when this button is pressed, this subscriber will start receiving items. If there is no
     * connection yet, nothing will happen (until a subscription)
     *
     * @return
     */
    private Subscription subscribeFirst() {
        Log.v(TAG, "subscribeFirst()");

        return connectable
            .compose(applySchedulers())
            .subscribe(resultSubscriber(tvResultFirstSubscription));
    }

    /**
     * If observable is already connected, when this button is pressed, this subscriber will start receiving items. If there is no
     * connection yet, nothing will happen (until a subscription)
     *
     * @return
     */
    private Subscription subscribeSecond() {
        Log.v(TAG, "subscribeSecond()");

        return connectable
            .compose(applySchedulers())
            .subscribe(resultSubscriber(tvResultSecondSubscription));
    }

    /**
     * By unsubscribing the subscription returned by the connect() method, all subscriptions will stop receiving items.
     *
     */
    private void disconnect() {
        Log.v(TAG, "disconnect()");
        subscription.unsubscribe();
    }
}
