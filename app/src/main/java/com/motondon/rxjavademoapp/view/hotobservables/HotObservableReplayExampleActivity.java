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
 * This example demonstrates how to use ConnectableObservable::replay() operator.
 *
 * As soon as we call connect(), our observable will start emit items and also collect them (depends on the replay() variation,
 * collected items might vary)
 *
 * Later, as we subscribe our subscribers, it will "replay" all collected items.
 *
 */
public class HotObservableReplayExampleActivity extends HotObservablesBaseActivity {

    private static final String TAG = HotObservableReplayExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result_first_subscription) TextView tvResultFirstSubscription;
    @BindView(R.id.tv_result_second_subscription) TextView tvResultSecondSubscription;

    private boolean replyOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_observable_replay_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }
    }

    private void resetData() {
        tvEmittedNumbers.setText("");
        tvResultFirstSubscription.setText("");
        tvResultSecondSubscription.setText("");
    }

    @OnClick(R.id.btn_replay)
    public void onReplayButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onReplayButtonClick()");
            resetData();
            replay();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_replay_with_buffer_size)
    public void onReplayWithBufferSizeButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onReplayWithBufferSizeButtonClick()");
            resetData();
            replayWithBufferSize();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_replay_with_time)
    public void onReplayWithTimeButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onReplayWithTimeButtonClick()");
            resetData();
            replayWithTime();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_connect)
    public void onConnectButtonClick() {

        if (!replyOk) {
            Toast.makeText(getApplicationContext(), "Please, choose one replay option prior to connect.", Toast.LENGTH_SHORT).show();
            return;
        }

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
        if (!replyOk) {
            Toast.makeText(getApplicationContext(), "Please, choose one replay option prior to subscribe.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firstSubscription == null || (firstSubscription != null && firstSubscription.isUnsubscribed())) {
            Log.v(TAG, "onSubscribeFirstButtonClick()");
            firstSubscription = subscribeFirst();
        } else {
            Toast.makeText(getApplicationContext(), "First subscriber already started", Toast.LENGTH_SHORT).show();
        }
    }


    @OnClick(R.id.btn_subscribe_second)
    public void onSubscribeSecondButtonClick() {
        if (!replyOk) {
            Toast.makeText(getApplicationContext(), "Please, choose one replay option prior to subscribe.", Toast.LENGTH_SHORT).show();
            return;
        }

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
     * When calling replay, observable will start emit and replay() operator will collect them.
     *
     * @return
     */
    private void replay() {
        Log.v(TAG, "replay()");

        // First create our observable. Later, when calling connect, it will start emitting items and collecting them.
        connectable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .doOnNext((number) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            })
            .replay();

        // Used only to prevent user to click on the connect button prior to choose one of the replay() approaches.
        replyOk = true;
    }

    /**
     * When calling replay(bufferSize), observable will start emit and collect items. It will replay at most [bufferSize] items
     * emitted by the observable.
     *
     * @return
     */
    private void replayWithBufferSize() {
        Log.v(TAG, "replayWithBufferSize(5)");

        // First create our observable. Later, when calling connect, it will start emitting items and collecting at most N them.
        connectable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .doOnNext((number) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            })

            // Replay the last 5 emitted items.
            .replay(5);

        // Used only to prevent user to click on the connect button prior to choose one of the replay() approaches.
        replyOk = true;
    }

    /**
     * When calling replay(time), observable will start emit and collect items. Upon subscription, it will replay all items emitted
     * by the observable within a specified time window.
     *
     * @return
     */
    private void replayWithTime() {
        Log.v(TAG, "replayWithTime(6 seconds)");

        // First create our observable. Later, when calling connect, it will start emitting items and collecting them.
        connectable = Observable
            .interval(500, TimeUnit.MILLISECONDS)
            .doOnNext((number) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            })

            // Replay all items emitted by the observable within this time window.
            .replay(6, TimeUnit.SECONDS);

        // Used only to prevent user to click on the connect button prior to choose one of the replay() approaches.
        replyOk = true;
    }

    /**
     * When this button is pressed, observable will start emitting items. If there is already a subscription, it will start receiving
     * emitted items. Otherwise emitted items will be collected and after a subscription, they will be replayed.
     *
     * @return
     */
    private Subscription connect() {
        Log.v(TAG, "connect()");
        return connectable.connect();
    }

    /**
     * If we are already connected to the observable, when this button is pressed, all emitted items will be replayed (depends on
     * which replay variant we are testing, replayed items might vary).
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
     * If we are already connected to the observable, when this button is pressed, all emitted items will be replayed (depends on
     * which replay variant we are testing, replayed items might vary).
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
     * By unsubscribing the subscriber returned by the connect() method, all subscriptions will stop receiving items.
     *
     */
    private void disconnect() {
        Log.v(TAG, "disconnect()");
        subscription.unsubscribe();

        replyOk = false;

    }
}
