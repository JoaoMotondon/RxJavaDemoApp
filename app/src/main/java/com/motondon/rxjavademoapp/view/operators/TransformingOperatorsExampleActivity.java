package com.motondon.rxjavademoapp.view.operators;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class TransformingOperatorsExampleActivity extends BaseActivity {

    class SpinnerOptionAndMethodName extends Pair {

        public SpinnerOptionAndMethodName(String first, String second) {
            super(first, second);
        }

        @Override
        public String toString() {
            return (String) first;
        }
    }

    private static final String TAG = TransformingOperatorsExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;
    @BindView(R.id.s_test_options) Spinner sTestOptions;

    // Hold the method name related to the test user chosen in the spinner control.
    private String currentTestMethodName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_transforming_operators_example);
        ButterKnife.bind(this);

        // Fill spinner view up with all available test names and their related method names. We will use reflection to call a method based on the user choice
        List<SpinnerOptionAndMethodName> testOptions = new ArrayList<>();
        testOptions.add(new SpinnerOptionAndMethodName("buffer() w/count", "startBufferOperatorTestWithCount"));
        testOptions.add(new SpinnerOptionAndMethodName("buffer() w/timespan", "startBufferOperatorTestWithTimespan"));
        testOptions.add(new SpinnerOptionAndMethodName("buffer() w/count and timespan", "startBufferOperatorTestWithCountAndTimespan"));
        testOptions.add(new SpinnerOptionAndMethodName("buffer() w/timespan and timeshift", "startBufferOperatorTestWithTimespanAndTimeshift"));
        testOptions.add(new SpinnerOptionAndMethodName("buffer() w/boundary", "startBufferOperatorTestWithBoundary"));
        testOptions.add(new SpinnerOptionAndMethodName("buffer() w/selector", "startBufferOperatorTestWithSelector"));
        testOptions.add(new SpinnerOptionAndMethodName("window() w/count and timespan", "startWindowOperatorTestWithCountAndTimespan"));
        testOptions.add(new SpinnerOptionAndMethodName("scan()", "startScanOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("scan() w/seed", "startScanOperatorWithSeedTest"));

        // Do not show this example, since it is entirely based on a great GIST. See comments below
        //testOptions.add(new SpinnerOptionAndMethodName("scan() Fibonacci", "startScanOperatorFibonacciTest"));


        ArrayAdapter<SpinnerOptionAndMethodName> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, testOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sTestOptions.setAdapter(adapter);

        // Set the default method name
        currentTestMethodName = "startFirstOperatorTest";

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }
    }

    @OnItemSelected(R.id.s_test_options)
    public void spinnerTestOptionsItemSelected(Spinner spinner, int position) {
        SpinnerOptionAndMethodName testItem = (SpinnerOptionAndMethodName) spinner.getAdapter().getItem(position);

        currentTestMethodName = (String) testItem.second;

        tvEmittedNumbers.setText("");
        tvResult.setText("");
    }

    @OnClick(R.id.btn_start_test)
    public void onButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onButtonClick()");

            tvEmittedNumbers.setText("");
            tvResult.setText("");

            try {

                // Instantiate an object of type method that returns a method name we will invoke
                Method m = this.getClass().getDeclaredMethod(currentTestMethodName);

                // Now, invoke method user selected
                subscription = (Subscription) m.invoke(this);

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    private Observable<Integer> emitItems(Integer numberOfItems, boolean randomItems, boolean randomDelay) {
        Log.v(TAG, "emitItems() - numberOfItems: " + numberOfItems);

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(0, numberOfItems)

            // Generate a random number (for each emitted item)
            .map((num) -> {
                if (randomItems) {
                    return new Random().nextInt(10);
                } else {
                    return num;
                }
            })

            .doOnNext((number) -> {
                try {
                    if (randomDelay) {
                        // Sleep for sometime between 100 and 300 milliseconds
                        Thread.sleep(new Random().nextInt(300 - 100) + 100);
                    } else {
                        // Sleep for 200ms
                        Thread.sleep(200);
                    }
                    Log.v(TAG, "emitItems() - Emitting number: " + number);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));
            });
    }

    private Observable<Integer> doAContinuouslyOperation(boolean showDoneMessage) {
        Log.v(TAG, "doAContinuouslyOperation()");

        return Observable.interval(100, TimeUnit.MILLISECONDS)
            .map((number) -> number.intValue())
            .doOnNext((number) -> {
                try {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Starting operation", Toast.LENGTH_SHORT).show());

                    Integer timeToSleep = new Random().nextInt(6 - 3) + 3;
                    Log.v(TAG, "doAContinuouslyOperation() - Sleeping for " + timeToSleep + " second(s)");
                    Thread.sleep(timeToSleep * 1000);

                    if (showDoneMessage) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Operation done", Toast.LENGTH_SHORT).show());
                    }

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }
            }).take(1);
    }

    private Observable<Integer> doASecondContinuouslyOperation() {
        Log.v(TAG, "doASecondContinuouslyOperation()");

        return Observable.just(1)
            .doOnNext((number) -> {
                try {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Starting a second operation. Items will be buffered", Toast.LENGTH_SHORT).show());

                    Integer timeToSleep = new Random().nextInt(6 - 3) + 3;
                    Log.v(TAG, "doASecondContinuouslyOperation() - Sleeping for " + timeToSleep + " second(s)");
                    Thread.sleep(timeToSleep * 1000);

                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Second operation done", Toast.LENGTH_SHORT).show());

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }
            });
    }



    /**
     * This example will emit groups of 4 items, no matter of how long delay between emitted items is.
     *
     * @return
     */
    private Subscription startBufferOperatorTestWithCount() {

        return emitItems(18, false, true)

            .buffer(4)

            // Just for log purpose
            .compose(showDebugMessages("buffer(4)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example will emit group of items that is emitted in a window of 700ms. Since emitted items will be delayed randomically, there is
     * no way to know how many items will be grouped together.
     *
     * @return
     */
    private Subscription startBufferOperatorTestWithTimespan() {

        return emitItems(20, false, true)

            .buffer(700, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("buffer(700ms)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     *
     * This example will emit group of four items or N items that is emitted in a window of 700ms, which comes first.
     *
     *
     * @return
     */
    private Subscription startBufferOperatorTestWithCountAndTimespan() {

        // Source will emit sequential numbers, and using a fixed delay of 200ms between each emission
        return emitItems(20, false, false)

            .buffer(700, TimeUnit.MILLISECONDS, 4)

            // Just for log purpose
            .compose(showDebugMessages("buffer(700ms or 4 items [which comes first])"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startBufferOperatorTestWithTimespanAndTimeshift() {

        return emitItems(40, false, false)

            .buffer(600, 3000, TimeUnit.MILLISECONDS)

            // Just for log purpose
            .compose(showDebugMessages("buffer(600ms, 3000ms)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startBufferOperatorTestWithBoundary() {

        return emitItems(40, false, true)

            .buffer(doAContinuouslyOperation(true))

            // Just for log purpose
            .compose(showDebugMessages("buffer(w/boundary)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startBufferOperatorTestWithSelector() {

        return emitItems(70, false, true)

            .buffer(doAContinuouslyOperation(false), (taskDone) -> doASecondContinuouslyOperation())

            // Just for log purpose
            .compose(showDebugMessages("buffer(w/selectors)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startWindowOperatorTestWithCountAndTimespan() {

        return emitItems(30, false, false)

            .window(700, TimeUnit.MILLISECONDS, 4)

            .compose(showDebugMessages("window()"))

            .flatMap(o -> o.toList())

            // Just for log purpose
            .compose(showDebugMessages("window(700ms or 4 items [which comes first])"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     *
     * @return
     */
    private Subscription startScanOperatorTest() {

        return emitItems(20, true, true)

            .scan((accumulator, item) -> {
                if (item % 2 == 0) {
                    return accumulator + item;
                }

                return accumulator;
            })

            // Just for log purpose
            .compose(showDebugMessages("scan"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example uses a variant of scan() operator which accepts a seed value that is applied to the first emitted item.
     *
     * When using a seed with a value of 3, it will be applied to the accumulator (only for the fist item)
     *
     * @return
     */
    private Subscription startScanOperatorWithSeedTest() {

        Integer seed = 3;

        return emitItems(10, false, true)

            .scan(seed, (accumulator, item) -> {
                Log.v(TAG, "scan() - seed: " + seed + " - accumulator: " + accumulator + " - item: " + item);
                return accumulator + item;
            })

            // Just for log purpose
            .compose(showDebugMessages("scan() w/seed"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * Fibonacci sequence using scan() operator. This example was based on the link below:
     *
     * https://gist.github.com/benjchristensen/a962c4bfa8adae286daf
     *
     * It is here just for demonstration purpose. It is not even being called anywhere on this app
     *
     * @return
     */
    private Subscription startScanOperatorFibonacciTest() {

        // This is the seed which will be used in the scan operator. This will be used the first time accumulator function is
        // called.
        Pair<Integer, Integer> seed = new Pair<>(0, 0);

        return emitItems(15, false, true)

            // This example ignores source emission, but instead only processes result of the function
            .scan(seed, (accumulator, source) -> {
                Log.v(TAG, "scan() - source: " + source + " - accumulator: " + accumulator);

                Integer f1 = accumulator.first;
                Integer f2 = accumulator.second;

                // The first time this accumulator function is called, result will contain a pair with zero value for both first
                // and second attributes (this is due the seed parameter we provided in the first scan() parameter. On this case,
                // assign zero and one values to f1 and f2 variables.
                f1 = f1 == 0 ? 0 : f1;
                f2 = f2 == 0 ? 1 : f2;

                // Now sum both values...
                int fn = f1 + f2;

                // ...and re-assign new values to the return attribute.
                accumulator = new Pair<>(f2, fn);

                return accumulator;
            })

            // Just for log purpose
            .compose(showDebugMessages("scan"))

            .filter((number) -> number.first != 0)

            // Just for log purpose
            .compose(showDebugMessages("filter"))

            // Sum both f1 and f2, since we are implementing a fibonacci sequence.
            // This is what will be propagated downstream.
            .map((number) -> number.first + number.second)

            // Just for log purpose
            .compose(showDebugMessages("map"))

            // Also, starts the sequence with these values (this is due the first three numbers of the fibonacci sequence)
            .startWith(0, 1, 1)

            // Just for log purpose
            .compose(showDebugMessages("startWith"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }
}
