package com.motondon.rxjavademoapp.view.generalexamples;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Examples on this activity were based on this GitHub issue (in the Ben Christensen's answers):
 *   - https://github.com/ReactiveX/RxJava/issues/448
 *
 *
 * This example differs from the ServerPollingExampleActivity in a way that each time it poll a server, it simulates a local data
 * processing (actually it just sleeps a random time) and only then it polls the server again, no matter how long it takes to
 * process the data locally.
 *
 * Note that none of the examples in this activity implements any condition to stop polling (e.g.: takeUntil). So it will emit forever
 * unless stop button is clicked.
 *
 */
public class ServerPollingAfterDataProcessingExampleActivity extends BaseActivity {
    private static final String TAG = ServerPollingAfterDataProcessingExampleActivity.class.getSimpleName();

    @BindView(R.id.tv_server_polling_count) TextView tvServerPollingCount;
    @BindView(R.id.tv_local_data_processed) TextView tvLocalDataProcessingCount;

    private int pollingAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_server_polling_after_data_processing_example);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }
    }

    private void resetData() {
        pollingAttempt = 0;
        tvServerPollingCount.setText("");
        tvLocalDataProcessingCount.setText("");
    }

    @OnClick(R.id.btn_process_data_recursively)
    public void onProcessDataRecursivelyButtonClick() {
        Log.v(TAG, "onProcessDataRecursivelyButtonClick()");
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            resetData();
            subscription = processDataRecursively();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_process_data_manual_recursion_with_scheduler)
    public void onProcessDataManualRecursionWithSchedulerButtonClick() {
        Log.v(TAG, "onProcessDataManualRecursionWithSchedulerButtonClick()");
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            resetData();
            subscription = processData_manualRecursion_with_scheduler();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_process_data_manual_recursion_with_repeat_when)
    public void onProcessDataManualRecursionRepeatWhenButtonClick() {
        Log.v(TAG, "onProcessDataManualRecursionRepeatWhenButtonClick()");
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            resetData();
            subscription = processData_manualRecursion_with_repeatWhen();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_stop_data_processing)
    public void onStopJobProcessingButtonClick() {
        Log.v(TAG, "onStopJobProcessingButtonClick()");
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private Observable<Integer> emitData() {
        Log.v(TAG, "emitData()");

        return Observable

            .just( ++pollingAttempt)

            // This is just to not emit forever and flood our GUI
            .take(10)

            .doOnNext((number) -> {
                try {
                    Log.v(TAG, "emitItems() - Emitting number: " + number);
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }

                final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                w.schedule(() -> tvServerPollingCount.setText(tvServerPollingCount.getText() + " " + number));
            });
    }


    /**
     * This is a very simple example. It gets a data from the server and, after process it locally, it finishes its job, so
     * Subscriber::onCompleted() is called. From there, it calls this method again (recursively).
     * 
     * This will fetch server data forever. We could, of course, add a break condition in order to stop it (e.g.: takeUntil,
     * take(N), etc)
     * 
     * @return
     */
    private Subscription processDataRecursively() {

        // Fetch some data from a server (do not forget this is just a simulation. No data is actually being requested to any server).
        return emitData()

            .map((data) -> {
                Log.v(TAG, "flatMap()");

                // Now, after get some data from a server, let's process it. Note this processing takes about 2 seconds to finishes, and
                // only after that, it will terminate.
                heavyDataProcessing(data);

                return data;
            })

            .compose(applySchedulers())

            // Finally subscribe it. Note this observer call processDataRecursively() method when it is done. This is the idea for this 
            // example: when data is finished processed, poll the server again.
            .subscribe(new Observer<Integer>() {

                @Override
                public void onCompleted() {
                    Log.v(TAG, "subscribe.onCompleted - Calling processDataRecursively() method in order to poll the server again");
                    // Once we get here, it means data was processed locally accordingly. So, call processDataRecursively()
                    // method recursively in order to poll server again.
                    subscription = processDataRecursively();
                }

                @Override
                public void onError(Throwable e) {
                    Log.v(TAG, "subscribe.doOnError: " + e.getMessage());
                }

                @Override
                public void onNext(Integer data) {
                    Log.v(TAG, "subscribe.onNext: " + data);
                    tvLocalDataProcessingCount.setText(tvLocalDataProcessingCount.getText() + " " + data);
                }
            });
    }

    /**
     * This method will also poll the server only after finish processing previous data locally, but it uses manual recursion instead.
     * 
     * @return
     */
    private Subscription processData_manualRecursion_with_scheduler() {

        return Observable.just("")

            .map((data) -> {
                Observable
                    .create((Observable.OnSubscribe<Integer>) o -> {
                        final Scheduler.Worker w = Schedulers.newThread().createWorker();
                        o.add(w);

                        // Create an action that will be scheduled to the Scheduler.Worker
                        Action0 schedule = new Action0() {
                            @Override
                            public void call() {

                                // When this action is scheduled, it will poll the server and emit it by calling Subscribe.onNext(), but we need to emit the data itself and not the observable returned
                                // by the emitData() method. So, in order to get the data, we consume it synchronously (by using toBlocking() and forEach() operators together) and then
                                // we call Subscriber::onNext() with the data we want to emit
                                emitData()
                                    .toBlocking()
                                    .forEach((data) -> {
                                        // Here we emit data received from the server. It will be consumed by the forEach() operator. Inside that method, data will be processed synchronously, and only after
                                        // that, it we will re-schedule a new server polling. This is exactly what we want to achieve on this example.
                                        o.onNext(data);
                                    });

                                // When we get here, it means data was already processed. So re-schedule this action in order to poll the server again.
                                w.schedule(this, 10, TimeUnit.MILLISECONDS);
                            }
                        };

                        // Schedule our action which will poll the server and emit its data to be processed synchronously, and after the data processing, it will re-schedule itself
                        // entering in an infinite loop.
                        w.schedule(schedule);
                    })


                        // by using toBlocking() operator, we ensure data will be first processed locally and only then we will re-schedule another server polling.
                    .toBlocking()

                    // this operator will consume data from the server that was emitted by the Subscriber::onNext(). It will process the data synchronously (since we are already in a
                    // computation thread) and only when it returns a re-schedule will be done.
                    .forEach((data2) -> {
                        Log.v(TAG, "forEach() - data: " + data2);
                        heavyDataProcessing(data2);

                        // We get here when data is processed accordingly. So, let's update the GUI.
                        final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                        w.schedule(() -> tvLocalDataProcessingCount.setText(tvLocalDataProcessingCount.getText() + " " + data2));

                        Log.v(TAG, "forEach() - Leaving...");
                    });

            return 0;
        })

        // Just for log purpose
        .compose(showDebugMessages("map"))

        // Now, apply on which thread observable will run and also on which one it will be observed.
        .compose(applySchedulers())

        // For the test perspective we could avoid subscribing our observable, since all the important job is being done inside the forEach operator. But
        // we will subscribe it in order to be able to stop it when we want (we have a stop button in the GUI)
        .subscribe(resultSubscriber());

    }

    /**
     * This example also uses manual recursion, but using another approach. 
     * 
     * @return
     */
    private Subscription processData_manualRecursion_with_repeatWhen() {

        return Observable.just("")

            .subscribeOn(Schedulers.newThread())

            .map((string) -> {
                Log.v(TAG, "map()");

                Observable
                    .timer(0, TimeUnit.SECONDS)
                    .flatMap((aLong) -> {
                        Log.v(TAG, "timer() - Calling emitData()...");
                        return emitData();
                })

                .repeatWhen((observable) -> {
                    Log.v(TAG, "repeatWhen()");
                    return observable.delay(10, TimeUnit.MILLISECONDS);
                })

                .subscribeOn(Schedulers.computation())

                .toBlocking()

                .forEach((data) -> {
                    Log.v(TAG, "forEach() - data: " + data);
                    heavyDataProcessing(data);

                    // We get here when data is processed accordingly. So, let's update the GUI.
                    final Scheduler.Worker w = AndroidSchedulers.mainThread().createWorker();
                    w.schedule(() -> tvLocalDataProcessingCount.setText(tvLocalDataProcessingCount.getText() + " " + data));
                });

                return "";
            })

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // For the test perspective we could avoid subscribe our observable, since all the important job is being done inside the forEach operator. But
            // we will subscribe it in order to be able to stop it when we want (we have a stop button in the GUI)
            .subscribe(resultSubscriber());
    }

    /**
     * This method simulates an intensive data processing.
     *
     * Actually it will only sleep for a while. This is a block operation, so be sure to call it always in a thread other than the mainThread,
     * otherwise you will may end up an ANR (Application Not Responding) error message.
     *
     * @param data
     */
    private void heavyDataProcessing(Integer data) {

        Log.v(TAG, "heavyDataProcessing() - Starting processing of data: " + data);

        try {
            Thread.sleep(2000);
            Log.v(TAG, "heavyDataProcessing() - Data: " + data + " was processed.");

        } catch (InterruptedException e) {
            // If we cancel subscription while job is being processed, we will hit here.
            Log.e(TAG, "heavyDataProcessing() - Data: " + data + " was not processed due an interruption.");
        }
    }

    @NonNull
    protected  <Integer> Subscriber<Integer> resultSubscriber() {
        return new Subscriber<Integer>() {

            @Override
            public void onCompleted() {
                Log.v(TAG, "subscribe.onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.v(TAG, "subscribe.doOnError: " + e.getMessage());
            }

            @Override
            public void onNext(Integer number) {
                Log.v(TAG, "subscribe.onNext");
            }
        };
    }
}
