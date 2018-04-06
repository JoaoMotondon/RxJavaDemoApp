package com.motondon.rxjavademoapp.view.operators;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.observables.MathObservable;

public class AggregateOperatorsExampleActivity extends BaseActivity {

    private static final String TAG = AggregateOperatorsExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_aggregate_operators_example);
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

    @OnClick(R.id.btn_start_sum_operator_test)
    public void onStartSumOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartSumOperatorButtonClick()");
            resetData();
            subscription = startSumOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_max_operator_test)
    public void onStartMaxOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartMaxOperatorButtonClick()");
            resetData();
            subscription = startMaxOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_collect_with_empty_statefactory_test)
    public void onStartCollectTestWithEmptyStateFactoryButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartCollectTestWithEmptyStateFactoryButtonClick()");
            resetData();
            subscription = startCollectTestWithEmptyStateFactory();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_collect_test_with_statefactory_with_values)
    public void onStartCollectTestWithStateFactoryWithValuesButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartCollectTestWithStateFactoryWithValuesButtonClick()");
            resetData();
            subscription = startCollectTestWithStateFactoryWithValues();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_reduce_test)
    public void onStartReduceTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartReduceTestButtonClick()");
            resetData();
            subscription = startReduceOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_reduce_with_global_seed_test)
    public void onStartReduceOperatorTestWithGlobalSeedButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartReduceOperatorTestWithGlobalSeedButtonClick()");
            resetData();
            startReduceOperatorTestWithGlobalSeed();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_reduce_with_null_seed_test)
    public void onStartReduceOperatorTestWithNullSeedButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartReduceOperatorTestWithNullSeedButtonClick()");
            resetData();
            startReduceOperatorTestWithNullSeed();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    private Observable<Integer> emitItems(Integer numberOfItems, boolean randomItems) {
        Log.v(TAG, "emitItems() - numberOfItems: " + numberOfItems);

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(1, numberOfItems)

            .map((item) -> randomItems ? (new Random().nextInt(20 - 0) + 0) : item )

            .doOnNext((number) -> {
                try {
                    Log.v(TAG, "emitItems() - Emitting number: " + number);

                    // Sleep for sometime between 100 and 300 milliseconds
                    Thread.sleep(new Random().nextInt(300 - 100) + 100);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            })
            .doOnCompleted(() -> Log.v(TAG, "onCompleted"));
    }


    private Subscription startSumOperatorTest() {

        return MathObservable.sumInteger(emitItems(20, true))

            // Just for log purpose
            .compose(showDebugMessages("sumInteger"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startMaxOperatorTest() {

        return MathObservable.max(emitItems(20, true))

            // Just for log purpose
            .compose(showDebugMessages("max"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }


    /**
     * This example adds emitted items to the stateFactory (i.e.: an array of Integer). In the end, it acts like the toList() operator.
     *
     * @return
     */
    private Subscription startCollectTestWithEmptyStateFactory() {

        Func0< ArrayList<Integer>> stateFactory = () -> new ArrayList<>();

        return emitItems(5, false)

            .collect(stateFactory, (list, item) -> list.add(item))

            // Just for log purpose
            .compose(showDebugMessages("collect"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * The only difference from the previous example is that on this one, we initialize stateFactory with some values.
     *
     * @return
     */
    private Subscription startCollectTestWithStateFactoryWithValues() {

        Func0< ArrayList<Integer>> stateFactory = () -> {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(44);
            list.add(55);
            return list;
        };

        return emitItems(5, false)

            .collect(stateFactory, (list, item) -> list.add(item))

            // Just for log purpose
            .compose(showDebugMessages("collect"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example will return the max emitted value by using reduce() operator.
     *
     * @return
     */
    private Subscription startReduceOperatorTest() {

        return emitItems(15, true)

            // Reduce operator  will only emit onNext when source observable terminates.
            .reduce((accumulator, item) -> {
                Integer max = item > accumulator ? item : accumulator;
                Log.v(TAG, "reduce() - item: " + item + " - current accumulator: " + accumulator + " - new accumulator (max): " + max);
                return max;
            })

            // Just for log purpose
            .compose(showDebugMessages("reduce"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example is intended to demonstrate reduce operator usage with a seed that is shared between all subscription.
     * This will impact in the items and result might not be what we expect.
     *
     * See link below (item #8) for a very good explanation about it:
     *
     * http://akarnokd.blogspot.hu/2015/05/pitfalls-of-operator-implementations_14.html
     *
     * Also see this link:
     *
     * https://stackoverflow.com/questions/30633799/can-rxjava-reduce-be-unsafe-when-parallelized
     *
     */
    private void startReduceOperatorTestWithGlobalSeed() {

        // Emit only tree numbers
        Observable<List<Integer>> observable = emitItems(3, false)

            // Note this example will share [new ArrayList<Integer>()] between all evaluations of the chain. So, the result might not be what we expect.
            // The next example shows how to fix it. Thanks to Dávid Karnok (http://akarnokd.blogspot.hu/2015/05/pitfalls-of-operator-implementations_14.html)
            .reduce(new ArrayList<Integer>(), (accumulator, item) -> {
                Log.v(TAG, "reduce() - item: " + item + " - accumulator: " + accumulator);
                accumulator.add(item);
                return accumulator;
            })

            // Just for log purpose
            .compose(showDebugMessages("reduce"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers());


        // Now, subscribe it for the first time...
        Log.v(TAG, "Subscribe for the fist time...");
        observable.subscribe(resultSubscriber(tvResult));

        // ... and for the second time...
        Log.v(TAG, "Subscribe for the second time...");
        observable.subscribe(resultSubscriber(tvResult));

        // ... and for the third time
        Log.v(TAG, "Subscribe for the third time...");
        observable.subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example is intended to demonstrate reduce operator with a seed that is NOT shared between all evaluation
     * of the chain.
     *
     * See link below (item #8) for a very good explanation about it:
     *
     * http://akarnokd.blogspot.hu/2015/05/pitfalls-of-operator-implementations_14.html
     *
     */
    private void startReduceOperatorTestWithNullSeed() {

        // Emit only tree items
        Observable<List<Integer>> observable = emitItems(3, false)

            // Reduce operator  will only emit onNext when source observable terminates.
            .reduce((ArrayList<Integer>)null, (accumulator, item) -> {
                if (accumulator == null) {
                    Log.v(TAG, "reduce() - accumulator is NULL. Instantiate it. This appears to be the first time this method is called.");
                    accumulator = new ArrayList<>();
                }

                Log.v(TAG, "reduce() - item: " + item + " - accumulator: " + accumulator);
                accumulator.add(item);
                return accumulator;
            })

            // Just for log purpose
            .compose(showDebugMessages("reduce"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers());


        // Now, subscribe it for the first time...
        Log.v(TAG, "Subscribe for the fist time...");
        observable.subscribe(resultSubscriber(tvResult));

        // ... and for the second time...
        Log.v(TAG, "Subscribe for the second time...");
        observable.subscribe(resultSubscriber(tvResult));

        // ... and for the third time
        Log.v(TAG, "Subscribe for the third time...");
        observable.subscribe(resultSubscriber(tvResult));
    }
}
