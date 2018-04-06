package com.motondon.rxjavademoapp.view.backpressure;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * This example was based on the following article:
 * https://github.com/Froussios/Intro-To-RxJava/blob/master/Part%204%20-%20Concurrency/4.%20Backpressure.md
 *
 */
public class BackpressureManualRequestExampleActivity extends BaseActivity {

    private static final String TAG = BackpressureManualRequestExampleActivity.class.getSimpleName();

    private ControlledPullSubscriber<Integer> puller;

    @BindView(R.id.tv_emitted_items) TextView tvEmittedItems;
    @BindView(R.id.tv_number_of_requested_items) TextView tvNumberOfRequestedItems;
    @BindView(R.id.tv_result) TextView tvResult;
    @BindView(R.id.et_number_of_items_to_emit) EditText etNumberOfItemsToEmit;
    @BindView(R.id.et_number_of_items_to_request) EditText etNumberOfItemsToRequest;

    private Integer numberOfItemsToEmit;
    private Integer numberOfItemsToRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backpressure_manual_request_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void resetData() {
        tvNumberOfRequestedItems.setText("");
        tvEmittedItems.setText("");
        tvResult.setText("");
    }

    @OnClick(R.id.btn_init_test)
    public void onInitTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onInitTestButtonClick()");

            resetData();
            readNumberOfItemsToEmit();

            initTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_request_items)
    public void onRequestItemsButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onRequestItemsButtonClick()");

            readNumberOfItemsToRequest();
            requestItems();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_LONG).show();
        }
    }

    private void readNumberOfItemsToEmit() {
        numberOfItemsToEmit = Integer.parseInt(etNumberOfItemsToEmit.getText().toString());
        if (numberOfItemsToEmit < 0) {
            numberOfItemsToEmit = 0;
            etNumberOfItemsToEmit.setText(numberOfItemsToEmit.toString());
        } else if (numberOfItemsToEmit > 100) {
            numberOfItemsToEmit = 100;
            etNumberOfItemsToEmit.setText(numberOfItemsToEmit.toString());
        }
    }

    private void readNumberOfItemsToRequest() {
        numberOfItemsToRequest = Integer.parseInt(etNumberOfItemsToRequest.getText().toString());
        if (numberOfItemsToRequest < 0) {
            numberOfItemsToRequest = 0;
            etNumberOfItemsToRequest.setText(numberOfItemsToRequest.toString());
        } else if (numberOfItemsToRequest > 10) {
            numberOfItemsToRequest = 10;
            etNumberOfItemsToRequest.setText(numberOfItemsToRequest.toString());
        }
    }

    private Observable<Integer> emitNumbers() {
        Log.v(TAG, "emitNumbers() - numberOfItemsToEmit: " + numberOfItemsToEmit);

        return Observable
            .range(0, numberOfItemsToEmit)
            .doOnNext((number) -> {
                try {
                    Log.v(TAG, "emitNumbers() - Emitting number: " + number);
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedItems.setText(tvEmittedItems.getText() + " " + number));
            });
    }

    /**
     * When the test is initialized, we need first to instantiate our Subscriber. Then we subscribe an observable to it.
     *
     * But note that, since our Subscriber (i.e.: puller) was initialized by calling Subscriber::request(0) in the
     * onStart method, no item will be emitted until a call to the Subscriber::request(n) is made with a value greater
     * than zero. This is what will happen when user clicks on the "Request Items" button.
     *
     */
    private void initTest() {

        puller = new ControlledPullSubscriber<>(
            () ->  {
                Log.v(TAG, "ControlledPullSubscriber.onCompleted");

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvResult.setText(tvResult.getText() + " - onCompleted"));
            },
            (throwable) ->  {
                Log.v(TAG, "ControlledPullSubscriber.doOnError: " + throwable.getMessage());

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvResult.setText(tvResult.getText() + " - doOnError"));
            },
            (integer) ->  {
                Log.v(TAG, "ControlledPullSubscriber.onNext");

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvResult.setText(tvResult.getText() + " " + integer));
            }
        );

        emitNumbers()

            .subscribeOn(Schedulers.computation())

            // This side effect method will allow us to see each time items are requested to the observable.
            .doOnRequest((item) -> Log.v(TAG, "Requested: " + item))

            // Subscribe our subscriber which will request for items.
            .subscribe(puller);
    }

    /**
     * When user clicks on the "Request Items" button, we just call our subscriber's requestMore method, which will
     * call Subscriber::request(n) method. This will make the subscriber to request the observable to emit more N items.
     *
     * This way we have total control over how many/when items are emitted.
     *
     */
    private void requestItems() {
        puller.requestMore(numberOfItemsToRequest);
    }

    /**
     * This is our Subscriber which starts requesting zero items to the observable (when it is subscriber).  It also
     * exposes a requestMore(n) method which is called whenever user wants to tells observable to emit items.
     *
     * @param <T>
     */
    public class ControlledPullSubscriber<T> extends Subscriber<T> {

        private final Action1<T> onNextAction;
        private final Action1<Throwable> onErrorAction;
        private final Action0 onCompletedAction;

        public ControlledPullSubscriber(
                Action0 onCompletedAction,
                Action1<Throwable> onErrorAction,
                Action1<T> onNextAction) {

            this.onCompletedAction = onCompletedAction;
            this.onErrorAction = onErrorAction;
            this.onNextAction = onNextAction;
        }

        @Override
        public void onStart() {
            // Since we request no items at the beginning, that means we are telling the Observable to not emit any item unless we request it
            // by calling request() method. This is what requestMore() method does. By doing this, we will have complete control over it, since
            // we can call requestMore() at any time when we are able to process data.
            request(0);
        }

        @Override
        public void onCompleted() {
            onCompletedAction.call();
        }

        @Override
        public void onError(Throwable e) {
            onErrorAction.call(e);
        }

        @Override
        public void onNext(T t) {
            onNextAction.call(t);
        }

        public void requestMore(int n) {
            request(n);
        }
    }
}
