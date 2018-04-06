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
 * This example demonstrates how to use ConnectableObservable::cache() operator.
 * 
 * It ensures that all observers see the same sequence of emitted items, even if they subscribe after the Observable has begun emitting items.
 * 
 */
public class HotObservableCacheExampleActivity extends HotObservablesBaseActivity {

    private static final String TAG = HotObservableCacheExampleActivity.class.getSimpleName();

    private Observable<Long> observable;

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result_first_subscription) TextView tvResultFirstSubscription;
    @BindView(R.id.tv_result_second_subscription) TextView tvResultSecondSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_observable_cache_example);
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

    @OnClick(R.id.btn_cache)
    public void onCacheButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onCacheButtonClick()");
            resetData();
            cache();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_subscribe_first)
    public void onSubscribeFirstButtonClick() {

        if (observable == null) {
            Log.v(TAG, "onSubscribeFirstButtonClick() - Cannot start a subscriber. You must first call cache().");

            Toast.makeText(getApplicationContext(), "You must first call cache()", Toast.LENGTH_SHORT).show();
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

        if (observable == null) {
            Log.v(TAG, "onSubscribeFirstButtonClick() - Cannot start a subscriber. You must first call cache().");

            Toast.makeText(getApplicationContext(), "You must first call cache()", Toast.LENGTH_SHORT).show();
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

    /**
     * When calling cache, that will NOT make observable to start emit items. It will only start emitting items when a first
     * subscriber subscribes to it. Then, it will receive all cached items.
     *
     * Just using take(30) in order to prevent it to emit forever.
     *
     * @return
     */
    private void cache() {
        Log.v(TAG, "cache()");

        // cache returns Observable<T> that is connected as long as there are subscribers to it.
        observable = Observable
            .interval(750, TimeUnit.MILLISECONDS)
            .doOnNext((number) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            })

            // Prevent our observable to emit forever
            .take(30)

            .cache();
    }

    /**
     * If this is the first subscription, it will make observable to start emitting items. But, if there is
     * already another subscription, it means that observable has already started emitting items and collecting them.
     * So, after we subscribe to it, it will first receive all collected items.
     *
     * @return
     */
    private Subscription subscribeFirst() {
        Log.v(TAG, "subscribeFirst()");

        return observable
            .compose(applySchedulers())
            .subscribe(HotObservableCacheExampleActivity.this.resultSubscriber(tvResultFirstSubscription));
    }

    /**
     * If this is the first subscription, it will make observable to start emitting items. But, if there is
     * already another subscription, it means that observable has already started emitting items and collecting them.
     * So, after we subscribe to it, it will first receive all collected items.
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
