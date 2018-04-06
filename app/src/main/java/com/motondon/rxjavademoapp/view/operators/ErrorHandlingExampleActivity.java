package com.motondon.rxjavademoapp.view.operators;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
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

/**
 * Examples on this activity are based on the following articles:
 *
 * http://blog.danlew.net/2015/12/08/error-handling-in-rxjava/
 * http://reactivex.io/RxJava/javadoc/rx/Observable.html#onErrorReturn(rx.functions.Func1)
 *
 * Please, visit them in order to get into details.
 *
 */
public class ErrorHandlingExampleActivity extends BaseActivity {

    class MyException extends Exception {
        public MyException(String detailMessage) {
            super(detailMessage);
        }
    }

    private static final String TAG = ErrorHandlingExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_emitted_values) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_error_handling_example);
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

    @OnClick(R.id.btn_start_unchecked_exception_test)
    public void onStartUncheckedExceptionTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartUncheckedExceptionTestButtonClick()");
            resetData();
            subscription = startUncheckedExceptionTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_checked_exception_test)
    public void onStartCheckedExceptionTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartCheckedExceptionTestButtonClick()");
            resetData();
            subscription = startCheckedExceptionTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_onerrorreturn_operator_test)
    public void onStartOnErrorReturnOperatorTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartOnErrorReturnOperatorTestButtonClick()");
            resetData();
            subscription = startOnErrorReturnOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_onerrorresumenext_operator_test)
    public void onStartOnErrorResumeNextOperatorTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartOnErrorResumeNextOperatorTestButtonClick()");
            resetData();
            subscription = startOnErrorResumeNextOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_onexceptionresumenext_operator_test)
    public void onStartOnExceptionResumeNextOperatorTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartOnExceptionResumeNextOperatorTestButtonClick()");
            resetData();
            subscription = startOnExceptionResumeNextOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    private Observable<Integer> emitRandomNumber() {
        Log.v(TAG, "emitRandomNumber()");

        return Observable.create((s) -> {
            final int randomNumber = new Random().nextInt(10);

            final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
            w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + randomNumber));

            s.onNext(randomNumber);
            s.onCompleted();
        });
    }

    private Observable<String> emitSecondObservable() {
        Log.v(TAG, "emitSecondObservable()");

        final List<String> list = Arrays.asList("@", "#", "$", "%", "&");

        return Observable

            .from(list)

            .doOnNext((n) -> {
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + n));
            });
    }

    private String throwAnException() throws MyException {
        Log.v(TAG, "throwAnException() - Throwing MyException()...");

        throw new MyException("This is an example of a checked exception");
    }

    /**
     * Unchecked exceptions will be caught by the subscriber.onError. We can see it in the logs.
     *
     * We could of course wrap our code to handle it, but this example is intended to demonstrate how unchecked
     * exceptions are handled by the sequence.
     *
     * @return
     */
    private Subscription startUncheckedExceptionTest() {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap((tick) -> emitRandomNumber())

            .map((randomNumber) -> {
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    Log.v(TAG, "map() - We will throw a RuntimeException...");
                    throw new RuntimeException("Hey, this is a forced runtime exception...");
                } else {
                    Log.v(TAG, "Returning random number: " + randomNumber);
                    return "" + randomNumber;
                }
            })

            // Just for log purpose
            .compose(showDebugMessages("map"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * Since this example catches a checked exception, we can do whatever we want. Here we will emit an error, which will terminate the
     * sequence with an error
     *
     * @return
     */
    private Subscription startCheckedExceptionTest() {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap((tick) -> emitRandomNumber())

            .flatMap((randomNumber) -> {
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    try {
                        // This is just to simulate a method call which can throw a checked exception. Since checked exception must be explicit caught (in a try/catch
                        // block or throwing it), we catch it here and emit an error.
                        Log.v(TAG, "Throwing an exception. Random number: " + randomNumber);
                        return Observable.just(throwAnException());

                    } catch (Throwable e) {
                        Log.v(TAG, "Catching the exception and emitting an error...");
                        // Here we catch our exception and emit an error. We could of course emit whatever value we wanted, but this is only to demonstrate
                        // this error will terminate the sequence and handled by the observer.onError
                        return Observable.error(e);
                    }
                } else {
                    // If number is less than seven, just re-emit it
                    Log.v(TAG, "Just re-emitting random number: " + randomNumber);
                    return Observable.just("" + randomNumber);
                }
            })

            // Just for log purpose
            .compose(showDebugMessages("flatMap"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * From the docs:
     * 
     * By default, when an Observable encounters an error that prevents it from emitting the expected item to its Observer, 
     * the Observable invokes its Observer's onError method, and then quits without invoking any more of its Observer's methods. 
     * The onErrorReturn method changes this behavior. If you pass a function (resumeFunction) to an Observable's onErrorReturn
     * method, if the original Observable encounters an error, instead of invoking its Observer's onError method, it will 
     * instead emit the return value of resumeFunction.
     * 
     * From the Dan Lew blog (mentioned on the top of this activity):
     * 
     * "... when using these [error handling] operators, the upstream Observables are still going to shut down! They've already 
     * seen a terminal event (onError); all that onError[Return|ResumeNext] does is replace the onError notification with a 
     * different sequence downstream." ... "You might expect the interval to continue emitting after the map throws an exception,
     * but it doesn't! Only the downstream subscribers avoid onError."
     * 
     * @return
     */
    private Subscription startOnErrorReturnOperatorTest() {

    	return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap((tick) -> emitRandomNumber())

            .map((randomNumber) -> {
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    Log.v(TAG, "Forcing an exception. Random number: " + randomNumber);
                    return String.valueOf(4 / 0);
                } else {
                    Log.v(TAG, "Returning random number: " + randomNumber);
                    return "" + randomNumber;
                }
            })

            // When source observable throws an exception, it will be caught here and we will emit an string instead of the error. This is what onErrorReturn operator is for.
            .onErrorReturn((error) -> " [This is a fallback string which will be emitted instead of the error emitted from the source observable (we forced an exception when source observable emitted a random number greater than 7.]")
            
            // Just for log purpose
            .compose(showDebugMessages("onErrorReturn"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * From the docs:
     * 
     * By default, when an Observable encounters an error that prevents it from emitting the expected item to its Observer, 
     * the Observable invokes its Observer's onError method, and then quits without invoking any more of its Observer's methods. 
     * The onErrorResumeNext method changes this behavior. If you pass another Observable (resumeSequence) to an Observable's 
     * onErrorResumeNext method, if the original Observable encounters an error, instead of invoking its Observer's onError 
     * method, it will instead relinquish control to resumeSequence which will invoke the Observer's onNext method if it is 
     * able to do so. In such a case, because no Observable necessarily invokes onError, the Observer may never know that an 
     * error happened.
     * 
     * @return
     */
    private Subscription startOnErrorResumeNextOperatorTest() {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap((tick) -> emitRandomNumber())

            .map((randomNumber) -> {
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    Log.v(TAG, "Forcing an exception. Random number: " + randomNumber);
                    return String.valueOf(4 / 0);
                } else {
                    Log.v(TAG, "Returning random number: " + randomNumber);
                    return "" + randomNumber;
                }
            })

            .onErrorResumeNext((throwable) -> {
                Log.v(TAG, "onErrorResumeNext() - Found an error: " + throwable.getMessage() + " - Emitting a second Observable instead...");
                return emitSecondObservable();
            })
            
            // Just for log purpose
            .compose(showDebugMessages("onErrorResumeNext"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }
    
    /**
     * From the docs:
     * 
     * This differs from onErrorResumeNext(rx.functions.Func1<? super java.lang.Throwable, ? extends rx.Observable<? extends T>>) in 
     * that this one does not handle Throwable or Error but lets those continue through.
     * 
     * @return
     */
    private Subscription startOnExceptionResumeNextOperatorTest() {

        return Observable.interval(1, TimeUnit.SECONDS)

            .flatMap((tick) -> emitRandomNumber())

            .map((randomNumber) -> {
                // Only forces an exception when random number is greater than seven.
                if (randomNumber > 7) {
                    Log.v(TAG, "Forcing an exception. Random number: " + randomNumber);
                    return String.valueOf(4 / 0);
                } else {
                    Log.v(TAG, "Returning random number: " + randomNumber);
                    return "" + randomNumber;
                }
            })

            .onExceptionResumeNext(emitSecondObservable())
            
            // Just for log purpose
            .compose(showDebugMessages("onExceptionResumeNext"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber(tvResult));
    }
}
