package com.motondon.rxjavademoapp.view.operators;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * This activity allows users to test different values for join operator:
 *   - left Observable emission delay
 *   - right Observable emission delay
 *   - left window duration
 *   - right window duration
 *   - left Observable number of items to emit
 *   - right Observable number of items to emit
 *
 */
public class JoinExampleActivity extends BaseActivity {

    private static final String TAG = JoinExampleActivity.class.getSimpleName();
    private static final Integer NEVER_CLOSE = -1;

    @BindView(R.id.tv_emitted_numbers) TextView tvEmittedNumbers;
    @BindView(R.id.tv_result) TextView tvResult;

    @BindView(R.id.et_left_observable_delay) EditText etLeftObservableDelayBetweenEmission;
    @BindView(R.id.et_right_observable_delay) EditText etRightObservableDelayBetweenEmission;
    @BindView(R.id.et_left_window_duration) EditText etLeftWindowDuration;
    @BindView(R.id.et_right_window_duration) EditText etRightWindowDuration;
    @BindView(R.id.et_left_observable_number_of_items_to_be_emitted) EditText etLeftObservableNumberOfItemsToBeEmitted;
    @BindView(R.id.et_righ_observable_number_of_items_to_be_emitted) EditText etRightObservableNumberOfItemsToBeEmitted;

    private Integer leftDelayBetweenEmission;
    private Integer rightDelayBetweenEmission;
    private Integer leftWindowDuration;
    private Integer rightWindowDuration;
    private Integer leftNumberOfItemsToBeEmitted;
    private Integer rightNumberOfItemsToBeEmitted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators_join_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }

        // Prevent keyboard to be visible when activity resumes.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void resetData() {
        tvEmittedNumbers.setText("");
        tvResult.setText("");
    }

    @OnClick(R.id.btn_join_operator_test)
    public void onSstartJoinOperatorTestButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onSstartJoinOperatorTestButtonClick()");
            resetData();
            readData();
            subscription = startJoinOperatorTest();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_stop_test)
    public void onStopSubscription() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void readData() {
        leftDelayBetweenEmission = Integer.parseInt(etLeftObservableDelayBetweenEmission.getText().toString());
        if (leftDelayBetweenEmission < 0) {
            leftDelayBetweenEmission = 0;
        } else if (leftDelayBetweenEmission > 5000) {
            leftDelayBetweenEmission = 5000;
        }
        Log.v(TAG, "readData() - leftDelayBetweenEmission: " + leftDelayBetweenEmission);

        rightDelayBetweenEmission = Integer.parseInt(etRightObservableDelayBetweenEmission.getText().toString());
        if (rightDelayBetweenEmission < 0) {
            rightDelayBetweenEmission = 0;
        } else if (rightDelayBetweenEmission > 5000) {
            rightDelayBetweenEmission = 5000;
        }
        Log.v(TAG, "readData() - rightDelayBetweenEmission: " + rightDelayBetweenEmission);

        if (etLeftWindowDuration.getText().toString().isEmpty()) {
            leftWindowDuration = NEVER_CLOSE;
        } else {
            leftWindowDuration = Integer.parseInt(etLeftWindowDuration.getText().toString());
            if (leftWindowDuration < 0) {
                leftWindowDuration = 0;
            } else if (leftWindowDuration > 5000) {
                leftWindowDuration = 5000;
            }
        }
        Log.v(TAG, "readData() - leftWindowDuration: " + leftWindowDuration);

        if (etRightWindowDuration.getText().toString().isEmpty()) {
            rightWindowDuration = NEVER_CLOSE;
        } else {
            rightWindowDuration = Integer.parseInt(etRightWindowDuration.getText().toString());
            if (rightWindowDuration < 0) {
                rightWindowDuration = 0;
            } else if (rightWindowDuration > 5000) {
                rightWindowDuration = 5000;
            }
        }
        Log.v(TAG, "readData() - rightWindowDuration: " + rightWindowDuration);

        leftNumberOfItemsToBeEmitted = Integer.parseInt(etLeftObservableNumberOfItemsToBeEmitted.getText().toString());
        if (leftNumberOfItemsToBeEmitted < 1) {
            leftNumberOfItemsToBeEmitted = 1;
        } else if (leftNumberOfItemsToBeEmitted > 40) {
            leftNumberOfItemsToBeEmitted = 40;
        }
        Log.v(TAG, "readData() - leftNumberOfItemsToBeEmitted: " + leftNumberOfItemsToBeEmitted);

        rightNumberOfItemsToBeEmitted = Integer.parseInt(etRightObservableNumberOfItemsToBeEmitted.getText().toString());
        if (rightNumberOfItemsToBeEmitted < 1) {
            rightNumberOfItemsToBeEmitted = 1;
        } else if (rightNumberOfItemsToBeEmitted > 40) {
            rightNumberOfItemsToBeEmitted = 40;
        }
        Log.v(TAG, "readData() - rightNumberOfItemsToBeEmitted: " + rightNumberOfItemsToBeEmitted);
    }

    private Observable<Integer> emitItems(Integer numberOfItemsToBeEmitted, Integer delayBetweenEmission, String caption) {
        return Observable.interval(delayBetweenEmission, TimeUnit.MILLISECONDS)
            .map((number) -> number.intValue())
            .doOnNext((number) -> {
                Log.v(TAG, "emitItems() - " + caption + " Observable. Emitting number: " + number);
                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvEmittedNumbers.setText(tvEmittedNumbers.getText() + " " + number));

            })
            .take(numberOfItemsToBeEmitted)
            .subscribeOn(Schedulers.newThread());
    }


    private Subscription startJoinOperatorTest() {

        Observable<Integer> left = emitItems(leftNumberOfItemsToBeEmitted, leftDelayBetweenEmission, "left");
        Observable<Integer> right = emitItems(rightNumberOfItemsToBeEmitted, rightDelayBetweenEmission, "right");

        return left
            .join(right,
                i -> {
                    if (leftWindowDuration == NEVER_CLOSE) {
                        return Observable.never();
                    } else {
                        return  Observable.timer(leftWindowDuration, TimeUnit.MILLISECONDS).compose(showDebugMessages("leftDuration")).subscribeOn(Schedulers.computation());
                    }
                },
                i -> {
                    if (rightWindowDuration == NEVER_CLOSE) {
                        return Observable.never();
                    } else {
                        return Observable.timer(rightWindowDuration, TimeUnit.MILLISECONDS).compose(showDebugMessages("rightDuration")).subscribeOn(Schedulers.computation());
                    }
                },
                (l, r) -> {
                    Log.v(TAG, "join() - Joining left number: " + l + " with right number: " + r);
                    return Arrays.asList(l.intValue(), r.intValue());
                }
            )
            .compose(applySchedulers())
            .subscribe(resultSubscriber(tvResult));
    }

    /**
     * THIS VERSION IS THE SAME AS THE ABOVE, BUT WITH NO GUI FEEDBACK FOR THE EMISSIONS NOR COMPUTATION SCHEDULER ON THE DURATION SELECTORS.
     *
     * We created this version since the version above, due the GUI updates, we cant sleep (while window's are opened) nor update the GUI on the main
     * thread, otherwise the main screen will freeze. The downside is that due to the different schedulers we are using the emissions might be out
     * of order, making it hard to understand. So, depends on what you want: a GUI feedback or analyse log messages, comment one and uncomment
     * the other.
     *
     **/
    /*private Subscription startJoinOperatorTest() {

        Observable<Long> left = Observable.interval(leftDelayBetweenEmission, TimeUnit.MILLISECONDS).take(leftNumberOfItemsToBeEmitted );
        Observable<Long> right = Observable.interval(rightDelayBetweenEmission, TimeUnit.MILLISECONDS).take(rightNumberOfItemsToBeEmitted);

        return left
            .join(right,
                i -> {
                    Log.v(TAG, "join() - Emitting left number (window is " + leftWindowDuration + "): " + i);
                    if (leftWindowDuration == NEVER_CLOSE) {
                        return Observable.never();
                    } else {
                        return  Observable.timer(leftWindowDuration, TimeUnit.MILLISECONDS).compose(showDebugMessages("leftDuration"));
                    }
                },
                i -> {
                    Log.v(TAG, "join() - Emitting right number (window is " + rightWindowDuration + "): " + i);
                    if (rightWindowDuration == NEVER_CLOSE) {
                        return Observable.never();
                    } else {
                        return Observable.timer(rightWindowDuration, TimeUnit.MILLISECONDS).compose(showDebugMessages("rightDuration"));
                    }
                },
                (l, r) -> {
                    Log.v(TAG, "join() - Joining left number: " + left + " with right number: " + right);
                    return Arrays.asList(l.intValue(), r.intValue());
                }
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<List<Integer>>() {

                @Override
                public void onCompleted() {
                    Log.v(TAG, "subscribe.onCompleted");
                }

                @Override
                public void onError(Throwable e) {
                    Log.v(TAG, "subscribe.doOnError: " + e);
                }

                @Override
                public void onNext(List<Integer> number) {
                    Log.v(TAG, "subscribe.onNext" + " " + number);
                }
            });
    } */
}
