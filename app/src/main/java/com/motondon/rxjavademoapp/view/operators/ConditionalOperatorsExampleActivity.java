package com.motondon.rxjavademoapp.view.operators;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class ConditionalOperatorsExampleActivity extends BaseActivity {

    private static final String TAG = ConditionalOperatorsExampleActivity.class.getSimpleName();

    private static final int MAGIC_NUMBER = 7;

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_conditional_operators_example);
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

    @OnClick(R.id.btn_start_skipwhile_test)
    public void onStartSkipWhileTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartSkipWhileTestButtonClick()");
            resetData();
            subscription = startSkipWhileTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_skipuntil_test)
    public void onStartSkipUntilTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartSkiUntilTestButtonClick()");
            resetData();

            new AlertDialog.Builder(this)
                    .setTitle("SkipUntil Test")
                    .setMessage("Emitted items will be skipped until a fake operation finishes its job (which can take up to 5 seconds)")
                    .setPositiveButton("OK", (dialog, which) -> subscription = startSkipUntilTest())
                    .show();

        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_takewhile_test)
    public void onStartTakeWhileTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartTakeWhileTestButtonClick()");
            resetData();
            subscription = startTakeWhileTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_takeuntil_test)
    public void onStartTakeUntilTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartTakeUntilTestButtonClick()");
            resetData();

            new AlertDialog.Builder(this)
                    .setTitle("TakeUntil Test")
                    .setMessage("Emitted items will be emitted downstream only while a fake operation is being executed (which can take up to 5 seconds)")
                    .setPositiveButton("OK", (dialog, which) -> subscription = startTakeUntilTest())
                    .show();

        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_amb_operator_test)
    public void onStartAmbOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartAmbOperatorButtonClick()");
            resetData();
            subscription = startAmbOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_all_operator_test)
    public void onStartAllOperatorButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartAllOperatorButtonClick()");
            resetData();
            subscription = startAllOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_contains_operator_test)
    public void onStartContainsOperatorTest() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartContainsOperatorTest()");
            resetData();
            subscription = startContainsOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_exists_operator_test)
    public void onStartExistsOperatorTest() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartExistsOperatorTest()");
            resetData();
            subscription = startExistsOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method that emits N random numbers
     *
     * @param numberOfItems
     * @return
     */
    private Observable<Integer> emitItems(Integer numberOfItems, boolean randomSleep) {
        Log.v(TAG, "emitItems() - numberOfItems: " + numberOfItems);

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(0, numberOfItems)

            // Generate a random number (for each emitted item)
            .map((randomNumber) -> new Random().nextInt(10))

            .doOnNext((n) -> {

                try {
                    int timeToSleep = 400;
                    if (randomSleep) {
                        timeToSleep = new Random().nextInt(1000 - 200) + 200;

                    }
                    Log.v(TAG, "emitItems() - Sleeping " + timeToSleep + " before emit number " + n);
                    Thread.sleep(timeToSleep);
                    Log.v(TAG, "emitItems() - Emitting number: " + n);

                } catch (InterruptedException e) {
                    Log.v(TAG, "emitItems() - Got an InterruptedException!");
                }

                // Now, log it on the GUI in order to inform user about the emitted item
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + n));
            });
    }

    /**
     * Helper method that emits a list of numbers by using a random delay
     *
     * @param list
     * @return
     */
    private Observable<Integer> emitItems(List<Integer> list) {
        Log.v(TAG, "emitOddNumbers()");

        int timeToSleep = new Random().nextInt(1000 - 200) + 200;

        return Observable.zip(
            Observable.from(list),
            Observable.interval(timeToSleep, TimeUnit.MILLISECONDS),
            (a, b) -> a )
            .doOnNext((n) -> {
                Log.v(TAG, "emitItems() - Emitting number: " + n);

                // Now, log it on the GUI in order to inform user about the emitted item
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + n));
            });
    }

    private Observable<Integer> doSomeContinuouslyOperation() {
        Log.v(TAG, "doSomeContinuouslyOperation()");

        return Observable.interval(100, TimeUnit.MILLISECONDS)
            .map((number) -> number.intValue())
            .doOnNext((number) -> {
                try {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Starting operation", Toast.LENGTH_SHORT).show());

                    Integer timeToSleep = new Random().nextInt(6 - 3) + 3;
                    Log.v(TAG, "doSomeContinuouslyOperation() - Sleeping for " + timeToSleep + " second(s)");
                    Thread.sleep(timeToSleep * 1000);

                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Operation done", Toast.LENGTH_SHORT).show());

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }
            }).take(1);
    }

    /**
     * This example will emit 30random numbers (between 0 and 10) and skip them until it gets number 7. Once it gets it, it will stop skip them (in other words:
     * start propagate them downstream).
     *
     * @return
     */
    private Subscription startSkipWhileTest() {

        return emitItems(30, false)

            .skipWhile((number) -> {
                boolean shouldSkip;

                if (number == MAGIC_NUMBER) {
                    // When we get our magic number (i.e. number seven) we will stop skipping.
                    Log.v(TAG, "skipWhile() - Hey, we got number " + MAGIC_NUMBER + ". Lets stop skipping!");
                    shouldSkip = false;

                } else {
                    // Skip while random number is different from our MAGIC_NUMBER (number seven)
                    Log.v(TAG, "skipWhile() - Got number: " + number + ". Skipping while we do not get number seven");
                    shouldSkip = true;
                }

                return shouldSkip;
            })

            // This should be printed only after skipWhile receives the number seven (our MAGIC_NUMBER).
            .doOnNext((item) -> Log.v(TAG, "skipWhile() - OnNext(" + item + ")"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startSkipUntilTest() {

        return emitItems(30, false)

            .skipUntil((doSomeContinuouslyOperation()))

            // We will hit here after doSomeContinuouslyOperation method returns, since it will emit an observable making skipUntil stop skipping.

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * Note that this test will be finished (i.e.: subscriber::onCompleted will be called) when we get number SEVEN, since takeWhile will emit onComplete making
     * interval() operator to also finish its job.
     *
     * @return
     */
    private Subscription startTakeWhileTest() {

        return emitItems(30, false)

            .takeWhile((number) -> {
                boolean shouldTake;

                if (number == MAGIC_NUMBER) {
                    // When we get our magic number (i.e. number seven) it will stop take items and emit onCompleted instead of onNext.
                    Log.v(TAG, "takeWhile() - Hey, we got number " + MAGIC_NUMBER + ". Our job is done here.");
                    shouldTake = false;

                } else {
                    // Take emitted items (i.e.: emit them downstream) while they are different from our MAGIC_NUMBER (number seven)
                    Log.v(TAG, "takeWhile() - Got number: " + number + ". Emit it while we do not get number SEVEN");
                    shouldTake = true;
                }

                return shouldTake;
            })

            // This will be printed while takeWhile gets numbers different from seven
            .doOnNext((number) -> Log.v(TAG, "takeWhile() - doOnNext(" + number + ")"))

            // When takeWhile receives number seven, it will complete.
            .doOnCompleted(() -> Log.v(TAG, "takeWhile() - doOnCompleted()."))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startTakeUntilTest() {
        return emitItems(30, false)

            .takeUntil((doSomeContinuouslyOperation()))

            // We will hit here until doSomeContinuouslyOperation method is being executed. After that, it will emit an observable making takeUntil to complete.

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * According to the docs, amb() operator can have multiple source observables, but will emit all items from ONLY the first of these Observables
     * to emit an item or notification. All the others will be discarded.
     *
     * @return
     */
    private Subscription startAmbOperatorTest() {

        final List<Integer> oddNumbers = Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15);
        final List<Integer> evenNumbers = Arrays.asList(2, 4, 6, 8, 10, 12, 14);

        return Observable.amb(emitItems(oddNumbers), emitItems(evenNumbers))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * If all emitted numbers are even, all() operator will emit true and complete, otherwise it will emit false before completes
     *
     * @return
     */
    private Subscription startAllOperatorTest() {

        return emitItems(3, false)

            .all(number -> number % 2 == 0)

            // doOnNext argument will contain a true value when all items emitted from the source are even. Otherwise it will be false.
            .doOnNext((number) -> Log.v(TAG, "all() - doOnNext(" + number + ")"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startContainsOperatorTest() {

        return emitItems(5, false)

            .contains(3)

            .doOnNext((n) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();

                if (n) {
                    Log.v(TAG, "startContainsOperatorTest() - contains() operator returned true, which means number three was emitted");
                    w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " Wow! Number three was emitted! "));
                } else {
                    Log.v(TAG, "startContainsOperatorTest() - contains() operator returned false, meaning source observable did not emit number three ");
                    w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " Source did not emit number three! "));
                }
            })

            .doOnCompleted(() -> Log.v(TAG, "startContainsOperatorTest() - doOnCompleted()"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    private Subscription startExistsOperatorTest() {

        return emitItems(10, false)

            .exists((number) -> number % 3 == 0)

            .doOnNext((n) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();

                if (n) {
                    Log.v(TAG, "startExistsOperatorTest() - exists() operator returned true, which means a number multiple of three was emitted");
                    w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " Wow! A number multiple of three was emitted! "));
                } else {
                    Log.v(TAG, "startExistsOperatorTest() - exists() operator returned false, meaning source observable did not emit a number multiple of three ");
                    w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " Source did not emit a number multiple of three! "));
                }
            })

            .doOnCompleted(() -> Log.v(TAG, "startExistsOperatorTest() - doOnCompleted()"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }
}
