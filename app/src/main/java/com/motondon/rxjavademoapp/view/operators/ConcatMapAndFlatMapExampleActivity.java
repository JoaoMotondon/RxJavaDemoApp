package com.motondon.rxjavademoapp.view.operators;

import android.os.Bundle;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Examples on this activity are based on the following article:
 *
 * http://fernandocejas.com/2015/01/11/rxjava-observable-tranformation-concatmap-vs-flatmap/
 *
 * Please, visit it in order to get more details about it.
 *
 */
public class ConcatMapAndFlatMapExampleActivity extends BaseActivity {
    private static final String TAG = ConcatMapAndFlatMapExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_original_emitted_items) TextView originalEmittedItems;
    @BindView(R.id.tv_flat_map_result) TextView flatMapResult;
    @BindView(R.id.tv_concat_map_result) TextView concatMapResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_concatmap_flatmap_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }
    }

    @OnClick(R.id.btn_flatmap_test)
    public void onFlatMapTestButtonClick() {

        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onFlatMapTestButtonClick");
            originalEmittedItems.setText("");
            flatMapResult.setText("");
            subscription = flatMapTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_concatmap_test)
    public void onConcatMapTestButtonClick() {

        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onConcatMapTestButtonClick");
            originalEmittedItems.setText("");
            concatMapResult.setText("");
            subscription = concatMapTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    private Observable<Integer> emitData() {

        return Observable
            .range(1, 10)
            .doOnNext((number) -> {
                try {
                    Log.v(TAG, "emitData() - Emitting number: " + number);
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> originalEmittedItems.setText(originalEmittedItems.getText() + " " + number));
            });
    }

    /**
     * This is a very simple test just to demonstrate how flatMap operator works.
     *
     * Basically (and according to the documentation), FlatMap merges the emissions of these Observables, so that they may interleave.
     *
     * @return
     */
    private Subscription flatMapTest() {

        return emitData()

            .flatMap((data) ->
                Observable

                    .just(data)

                    .compose(showDebugMessages("just"))

                    // Just adding a delay here, so that we can better see elements being emitted in the GUI
                    .delay(200, TimeUnit.MILLISECONDS)

                    .compose(showDebugMessages("delay"))
            )

            // Just for log purpose
            .compose(showDebugMessages("flatMap"))

            .map((data) ->  data.toString())

            // Just for log purpose
            .compose(showDebugMessages("map"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(flatMapResult));
    }

    /**
     * This example is similar to the flatMapTest, but as the name implies, it uses concatMap operator instead.
     *
     * Note that concatMap() uses concat operator so that it cares about the order of the emitted elements.
     *
     * @return
     */
    private Subscription concatMapTest() {

        return emitData()

            .concatMap((data) -> {
                // Here we added some log messages allowing us to analyse the concatMap() operator behavior.
                // We can see in the log messages that concatMap emits its items as they are received (after applies its function)
                return Observable

                    .just(data)

                    .compose(showDebugMessages("just"))

                    // Just adding a delay here, so that we can better see elements being emitted in the GUI
                    .delay(200, TimeUnit.MILLISECONDS)

                    .compose(showDebugMessages("delay"));
            })

            // Just for log purpose
            .compose(showDebugMessages("concatMap"))

            .map((data) -> data.toString())

            // Just for log purpose
            .compose(showDebugMessages("map"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(concatMapResult));
    }
}
