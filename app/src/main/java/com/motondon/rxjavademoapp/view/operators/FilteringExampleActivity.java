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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;


public class FilteringExampleActivity extends BaseActivity {

    /**
     * This is a helper class that is used to fill the spinner up with pairs of test name and a related method name.
     *
     * When an item is selected from the spinner, we will extract a method name and call it by using reflection.
     *
     * We extended from Pair class (instead of using it directly) since we want a custom toString() method
     *
     */
    class SpinnerOptionAndMethodName extends Pair {

        public SpinnerOptionAndMethodName(String first, String second) {
            super(first, second);
        }

        @Override
        public String toString() {
            return (String) first;
        }
    }

    private static final String TAG = FilteringExampleActivity.class.getSimpleName();
    private static final Integer DEFAULT_VALUE = 9999;

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;
    @BindView(R.id.s_test_options) Spinner sTestOptions;

    // Hold the method name related to the test user chosen in the spinner control.
    private String currentTestMethodName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_filtering_example);
        ButterKnife.bind(this);

        // Fill spinner view up with all available test names and their related method names. We will use reflection to call a method based on the user choice
        List<SpinnerOptionAndMethodName> testOptions = new ArrayList<>();
        testOptions.add(new SpinnerOptionAndMethodName("first()", "startFirstOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("first() with predicate", "startFirstOperatorWithPredicateFunctionTest"));
        testOptions.add(new SpinnerOptionAndMethodName("firstOrDefault(9999)", "startFirstOrDefaultOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("takeFirst()", "startTakeFirstOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("single()", "startSingleOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("singleOrDefault(9999)", "startSingleOrDefaultOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("elementAt(3)", "startElementAtOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("last()", "startLastOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("lastOrDefault()", "startLastOrDefaultOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("take(5)", "startTakeOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("takeLast(5)", "startTakeLastOperatorTest"));
        testOptions.add(new SpinnerOptionAndMethodName("filter()", "startFilterOperatorTest"));

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

    /**
     * Helper method that emits N random numbers
     *
     * @param numberOfItems
     * @return
     */
    private Observable<Integer> emitItems(Integer numberOfItems) {
        Log.v(TAG, "emitItems() - numberOfItems: " + numberOfItems);

        return Observable

            // Emit N items based on the "numberOfItems" parameter
            .range(0, numberOfItems)

            // Generate a random number (for each emitted item)
            .map((randomNumber) -> new Random().nextInt(10))

            .doOnNext((n) -> {

                try {
                    Thread.sleep(100);
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
     * As the name suggests, this method will emit items (up to 10) or an empty observable based on an internal criteria
     *
     * @return
     */
    private Observable<Integer> emitItemsOrEmpty() {
        Log.v(TAG, "emitItemsOrEmpty()");

        // Generate a random number
        Integer randomNumber = new Random().nextInt(10 - 1) + 1;

        // If it is greater than 5, emit an empty observable
        if (randomNumber > 5) {

            // Now, log it on the GUI in order to inform user about the emitted item
            final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
            w.schedule(() -> tvEmittedNumbers.setText("Empty observable"));

            return Observable.empty();
        }

        // Otherwise, if it is less or equals to 5, call emitItems()method which will emit N random numbers
        return emitItems(new Random().nextInt(10 - 1) + 1);
    }

    /**
     * In case of emitItemsOrEmpty() helper method emits an item, first() operator will emit it downstream, then terminate the chain. But in
     * case of an empty observable is emitted, since first() operator expects at least one item to be emitted, this example will terminate
     * with the following error: "Sequence contains no elements".
     *
     * @return
     */
    private Subscription startFirstOperatorTest() {

        return emitItemsOrEmpty()

            // Emit only the first item emitted by the source Observable. In case of none item emitted,
            // it terminates with an error
            .first()

            // Just for log purpose
            .compose(showDebugMessages("first"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * This example uses a variation of first() operator, which accepts a predicate function and will complete successfully
     * when that predicate is evaluated as true.
     *
     * @return
     */
    private Subscription startFirstOperatorWithPredicateFunctionTest() {

        // This will emit 5 random numbers from 1 to 10
        return emitItems(5)

            // Emit the first number emitted from the source Observable that is multiple of three
            .first((number) -> number % 3 == 0)

            // Just for log purpose
            .compose(showDebugMessages("first(...)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * If the source Observable emits an empty observable, default value
     * will be emitted. Otherwise, if it emits some value, it will be emitted downstream and terminate the chain.
     *
     * @return
     */
    private Subscription startFirstOrDefaultOperatorTest() {

        return emitItemsOrEmpty()

            // In case of the source Observable finishes before emit any item, DEFAULT_VALUE will be emitted instead
            .firstOrDefault(DEFAULT_VALUE)

            // Just for log purpose
            .compose(showDebugMessages("firstOrDefault(" + DEFAULT_VALUE + ")"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * From the docs:
     *
     * The takeFirst operator behaves similarly to first, with the exception of how these operators behave when the source Observable
     * emits no items that satisfy the predicate. In such a case, first will throw a NoSuchElementException while takeFirst will return
     * an empty Observable (one that calls onCompleted but never calls onNext).
     *
     * @return
     */
    private Subscription startTakeFirstOperatorTest() {

        return emitItemsOrEmpty()

            // In case of no item is emitted by the source Observable, takeFirst will return an empty observable
            .takeFirst((number) -> true)

            // Just for log purpose
            .compose(showDebugMessages("takeFirst()"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * single() operator expects only one item to be emitted. If more than one item is emitted it will terminate with an error (Sequence
     * contains too many elements).
     *
     * Also, if the source Observable does not emit any  item before completing, it throws a NoSuchElementException
     * and will terminate with error: "Sequence contains no elements".
     *
     * @return
     */
    private Subscription startSingleOperatorTest() {

        return emitItemsOrEmpty()

            // If emitItemsOrEmpty() emits an empty observable or more than one item, single() will terminate with an error,
            // otherwise it will terminate successfully
            .single()

            // Just for log purpose
            .compose(showDebugMessages("single"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     *
     * singleOrDefault() operator is slightly different from the single() operator. In case of no element is emitted,
     * instead of terminate with an  error, it will emit the default value.
     *
     * @return
     */
    private Subscription startSingleOrDefaultOperatorTest() {

        return emitItemsOrEmpty()

            .singleOrDefault(DEFAULT_VALUE)

            // Just for log purpose
            .compose(showDebugMessages("singleOrDefault(" + DEFAULT_VALUE + ")"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }


    /**
     * elementAt(N) expects at least N items are emitted from the source observable. In case of less items are emitted,
     * IndexOutOfBoundsException is thrown.
     *
     * @return
     */
    private Subscription startElementAtOperatorTest() {

        return emitItemsOrEmpty()

            .elementAt(3)

            // Just for log purpose
            .compose(showDebugMessages("elementAt(3)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * last() operator emits only the last emitted item by the source Observable. In case of an empty observable is emitted, since last() operator
     * expects at least one item to be emitted, it will terminate with an error (NoSuchElementException)
     *
     * @return
     */
    private Subscription startLastOperatorTest() {

        return emitItemsOrEmpty()

            // Will emit the last item emitted by the source Observable
            .last()

            // Just for log purpose
            .compose(showDebugMessages("last"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * Similar to last() operation, but in case of the source Observable fails to emit any item, a default value will be emitted instead of an error.
     *
     * @return
     */
    private Subscription startLastOrDefaultOperatorTest() {

        return emitItemsOrEmpty()

            .lastOrDefault(DEFAULT_VALUE)

            // Just for log purpose
            .compose(showDebugMessages("lastOrDefault(" + DEFAULT_VALUE + ")"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * take(N) operator returns N items emitted by the source Observable.
     *
     * @return
     */
    private Subscription startTakeOperatorTest() {

        return emitItemsOrEmpty()

            // Even in case of fewer items than N are emitted (or no item is emitted), take(5) will complete after source observable completes
            .take(5)

            // Just for log purpose
            .compose(showDebugMessages("take(5)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * Emits the last N item emitted by the source Observable. In case of no item is emitted, it will completes with no error.
     *
     * @return
     */
    private Subscription startTakeLastOperatorTest() {

        return emitItemsOrEmpty()

            // Will emit only the last 2 items. In case of no item is emitted, it will successfully complete after source observable completes
            .takeLast(5)

            // Just for log purpose
            .compose(showDebugMessages("takeLast(5)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * Emits only those numbers that make predicate function to evaluate as true.
     *
     * @return
     */
    private Subscription startFilterOperatorTest() {

        return emitItems(50)

            // Will emit all the numbers emitted by the source Observable that are multiple of three
            .filter((number) -> number % 3 == 0)

            // Just for log purpose
            .compose(showDebugMessages("filter(...)"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }
}
