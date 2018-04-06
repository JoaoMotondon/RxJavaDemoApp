package com.motondon.rxjavademoapp.view.generalexamples;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.adapter.SimpleStringAdapter;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * This example is intended to demonstrate how to poll a server to request for some data. For each data received, it will analyze it and
 * check whether it should be considered done or not. Depends on the example, it can poll server until data is done or for a limited number
 * of times.
 *
 * Actually we are not polling any real server, but using an observable to simulate this situation.
 *
 * It was based on the following article: https://medium.com/@v.danylo/server-polling-and-retrying-failed-operations-with-retrofit-and-rxjava-8bcc7e641a5a
 *
 */
public class ServerPollingExampleActivity extends BaseActivity {

    /**
     * Just a simple class that simulates data retrieved from a server containing a parameter that informs whether it should be considered done or not.
     *
     */
    public class FakeJob {
        private String value;
        private boolean jobDone = false;

        public FakeJob(String value) {
            this(value, false);
        }

        public FakeJob(String value, boolean jobDone) {
            this.value = value;
            this.jobDone = jobDone;
        }

        public boolean isJobDone() {
            return jobDone;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final String TAG = ServerPollingExampleActivity.class.getSimpleName();

    private static final int COUNTER_START = 1;
    private static final int MAX_ATTEMPTS = 3;

    @BindView(R.id.my_list) RecyclerView mListView;

    private SimpleStringAdapter mSimpleStringAdapter;
    private Integer pollingAttempt;
    
    private List<Pair<Integer,Integer>> retryMatrix;

    // This is a static list of FakeJob objects. It is used to simulate data polling from the server. Items on this list will be
    // emitted sequentially, making only the first 5 items to actually be emitted. The reason is that once the fifth item is emitted,
    // since it represents a job done, this will make our "client" to stop polling the server. Items from #6 to 10# should never
    // be emitted.
    private List<FakeJob> fakeJobList = Arrays.asList(
            new FakeJob("1"),        // Will be always emitted (unless user clicks over stop subscription button)
            new FakeJob("2"),        // Will be always emitted (unless user clicks over stop subscription button)
            new FakeJob("3"),        // Will be always emitted (unless user clicks over stop subscription button)
            new FakeJob("4"),        // Will be always emitted (unless user clicks over stop subscription button)
            new FakeJob("5", true),  // Should stop emission
            new FakeJob("6"),        // Should never be emitted
            new FakeJob("7"),        // Should never be emitted
            new FakeJob("8"),        // Should never be emitted
            new FakeJob("9", true)); // Should never be emitted

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_server_polling_example);
        ButterKnife.bind(this);

        mListView.setLayoutManager(new LinearLayoutManager(this));
        mSimpleStringAdapter = new SimpleStringAdapter(this);
        mListView.setAdapter(mSimpleStringAdapter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }

        // This is the retry matrix used in a variant of exponential backoff
        retryMatrix = Arrays.asList(
            new Pair<>(  1,   1), // First four attempts, sleep 1 second before retry
            new Pair<>(  5,   2), // For attempt 5 to 9, sleep 2 second before retry
            new Pair<>( 10,   3), // For attempt 10 to 19, sleep 3 second before retry
            new Pair<>( 20,   4), // For attempt 20 to 39, sleep 4 second before retry
            new Pair<>( 40,   5), // For attempt 40 to 99, sleep 4 second before retry
            new Pair<>(100,   6)  // For the 100th attempts and next ones, sleep 6 second before retry
        );
    }

    private void resetData() {
        mSimpleStringAdapter.clear();
        pollingAttempt = 0;
    }

    @OnClick(R.id.btn_start_polling_interval)
    public void onStartPollingIntervalButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartPollingIntervalButtonClick()");
            resetData();
            subscription = startPolling_Interval();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_polling_repeat_when_until_job_done)
    public void onStartPollingRepeatWhenButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartPollingRepeatWhenButtonClick()");
            resetData();
            subscription = startPolling_repeatWhen_untilJobDone();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_polling_repeat_when_for_three_times)
    public void onStartPollingRepeatWhenForThreeTimesButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onStartPollingRepeatWhenForThreeTimesButtonClick()");
            resetData();
            subscription = startPolling_repeatWhen_forThreeTimes();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_start_polling_repeat_when_using_exponential_backoff)
    public void onstartPollingRepeatWhenUsingExponentialBackoffButtonClick() {
        if (subscription == null || (subscription != null && subscription.isUnsubscribed())) {
            Log.v(TAG, "onstartPollingRepeatWhenUsingExponentialBackoffButtonClick()");
            resetData();
            subscription = startPolling_repeatWhen_exponentialBackoff();
        } else {
            Toast.makeText(getApplicationContext(), "Test is already running", Toast.LENGTH_SHORT).show();
        }
    }
    
    @OnClick(R.id.btn_stop_polling)
    public void onStopPollingButtonClick() {
        Log.v(TAG, "onStopPollingButtonClick()");
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private Observable<FakeJob> emitJob() {
        Log.v(TAG, "emitJob()");
        
        return Observable
            .timer(0, TimeUnit.SECONDS)
            .flatMap((aLong) -> Observable.just(fakeJobList.get(pollingAttempt++)))

            .doOnNext((number) -> {
                try {
                    Log.v(TAG, "emitItems() - attempt: " + pollingAttempt);
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Log.v(TAG, "Got an InterruptedException!");
                }
            });
    }

    /**
     * This method will generate a random FakeJob (note it does not use fakeJobList)
     *
     * It has 15% of chance to generate a done job and 85% of chance to generate a not done job.
     * 
     * This is used only by startPolling_repeatWhen_exponentialBackoff test.
     *  
     * @return
     */
    private Observable<FakeJob> emitRandomJob() {
        Log.v(TAG, "emitRandomJob()");

        return Observable
            .timer(0, TimeUnit.SECONDS)
            .flatMap((aLong) -> {
                FakeJob job;
                final Integer randomNumber = new Random().nextInt(100);
                pollingAttempt++;

                // When using random job, we have 15% of change to create a done job...
                if (randomNumber <= 15) {
                    job = new FakeJob("Random Job", true);
                } else {
                    // ... and 85% of chance to create a non done job
                    job = new FakeJob("Random Job");
                }

                return Observable.just(job);
            })

            .doOnNext((number) ->Log.v(TAG, "emitItems() - attempt: " + pollingAttempt));
    }
    
    /**
     * This example requests data every 1 second by using interval operator until gets a job that is considered done.
     * Then we stop polling.
     * 
     * @return
     */
    private Subscription startPolling_Interval() {

        // Emit an observable each 1 second
        return Observable.interval(0, 1000, TimeUnit.MILLISECONDS)

            // Poll the server
            .flatMap((aLong) -> {
                Log.v(TAG, "flatMap - calling emitData() method...");
                return emitJob();
            })

            // Just for log purpose
            .compose(showDebugMessages("flatMap"))

            // Now, for each job, check whether jobDone flag is true. If so, takeUntil will completes making
            // interval operator to also completes.
            .takeUntil((fakeJob) -> {
                if (fakeJob.isJobDone()) {
                    Log.v(TAG, "takeUntil - FakeJob done");
                } else {
                    Log.v(TAG, "takeUntil - FakeJob not finished yet");
                }
                return fakeJob.isJobDone();
            })

            // Just for log purpose
            .compose(showDebugMessages("takeUntil"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber());
    }

    /**
     * This example is similar to the "startPolling_Interval", but using repeatWhen operator instead to poll the server until it
     * gets a job considered done.
     * 
     * 
     * @return
     */
    private Subscription startPolling_repeatWhen_untilJobDone() {

        return emitJob()

            // Just for log purpose
            .compose(showDebugMessages("emitData"))

            .repeatWhen((observable) -> {
                Log.v(TAG, "repeatWhen()");

                // Wait for one second and emit source observable forever. This will be consumed by the takeUntil() observable which will do its
                // job until fakeJob is done.
                return observable.flatMap((o) -> {
                    Log.v(TAG, "flatMap()");
                    return Observable.timer(1, TimeUnit.SECONDS);
                });
            })

            // Just for log purpose
            .compose(showDebugMessages("repeatWhen"))

            .takeUntil((fakeJob) -> {
                if (fakeJob.isJobDone()) {
                    Log.v(TAG, "takeUntil - FakeJob done");
                } else {
                    Log.v(TAG, "takeUntil - FakeJob not finished yet");
                }
                return fakeJob.isJobDone();
            })
            // Just for log purpose
            .compose(showDebugMessages("takeUntil"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber());
    }

    /**
     * This example polls the server only for three times. Since it uses emitJob() method to get a job
     *
     * We known in advanced this example will never get a job that is considered donse, since we are using fakeJobList list that
     * contains a done job only in its fifth item.
     *
     * Of course this is just for demonstration purpose when we need to poll a server a fixed number of times, regardless we get an
     * expected value or not.
     *
     * @return
     */
    private Subscription startPolling_repeatWhen_forThreeTimes() {

        return emitJob()

            // Just for log purpose
            .compose(showDebugMessages("emitData"))

            .repeatWhen((observable) -> {
                Log.v(TAG, "repeatWhen()");

                return observable.zipWith(Observable.range(COUNTER_START, MAX_ATTEMPTS), (aVoid, integer) -> {
                    Log.v(TAG, "zipWith()");
                    return integer;
                })
                .flatMap(o -> {
                    Log.v(TAG, "flatMap()");
                    return Observable.timer(1, TimeUnit.SECONDS);
                });
            })

            // Just for log purpose
            .compose(showDebugMessages("repeatWhen"))

            .takeUntil(fakeJob ->  {
                if (fakeJob.isJobDone()) {
                    Log.v(TAG, "takeUntil - FakeJob done");
                } else {
                    Log.v(TAG, "takeUntil - FakeJob not finished yet");
                }
                return fakeJob.isJobDone();
            })
            // Just for log purpose
            .compose(showDebugMessages("takeUntil"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber());
    }

    /**
     * This example polls the server until it gets a job done using a variant of an exponential backoff to increase the time it waits before a new
     * server request. Once it gets a job considered done, it resets the exponential backoff counter and re-start pooling the server.
     *
     * emitRandomJob() returns a random job, so we do not know when it will return a job done or not. Actually there are 15% of chance to get a job done
     * and 85% of change to get one that is not done.
     *
     * Note that it will poll the server forever, or until you click on Stop button.
     *
     * @return
     */
    private Subscription startPolling_repeatWhen_exponentialBackoff() {
    	
    	// This method will emit one job at a time.
    	return emitRandomJob()

            // Just for log purpose
            .compose(showDebugMessages("emitData"))

            .repeatWhen(observable ->  {
                Log.v(TAG, "repeatWhen()");

                // While we get a job that is not finished yet, we will keep polling server but using an exponential backoff. This is done by the
                // method getSecondsToSleep() which will return the number of seconds to sleep based on the number of attempts.
                return observable.concatMap(o ->  {
                    Integer numOfSeconds = getSecondsToSleep(pollingAttempt);
                    Log.v(TAG, "flatMap() - Attempt: " + pollingAttempt + " - Waiting for " + numOfSeconds + " second(s) prior next server polling...");

                    // Sleep the number of seconds returned by the getSecondsToSleep() method.
                    return Observable.timer(numOfSeconds, TimeUnit.SECONDS);
                });
            })

            // Just for log purpose
            .compose(showDebugMessages("repeatWhen"))

            .observeOn(AndroidSchedulers.mainThread())

            .filter(fakeJob ->  {
                if (fakeJob.isJobDone()) {
                    Log.v(TAG, "filter() - FakeJob done. Reset retryMatrix counter.");

                    // When we get a done job, we will not filtering it, meaning it will be propagate downstream the subscriber.
                    // Also we will reset attemptCount so that we will keep polling server for the next done job but using
                    // retryMatrix from its initial values (i.e.: sleeps for 5 seconds in the first minute, 10 sec next four minutes,
                    // and so on).
                    pollingAttempt = 1;

                } else {
                    Log.v(TAG, "filter() - FakeJob not finished yet");

                    // Since we are in a filter() operator, job is not done, no notification is propagated down the chain, and
                    // recycler view is not updated with such information. So, we add it here.
                    mSimpleStringAdapter.addString("Attempt: " + pollingAttempt + " - Job not finished. Waiting " + getSecondsToSleep(pollingAttempt) + " sec");
                    mListView.scrollToPosition(mSimpleStringAdapter.getItemCount()-1);
                }

                return fakeJob.isJobDone();
            })

            // Just for log purpose
            .compose(showDebugMessages("filter"))

            // Now, apply on which thread observable will run and also on which one it will be observed.
            .compose(applySchedulers())

            // Finally subscribe it.
            .subscribe(resultSubscriber());
    }

    @NonNull
    private Subscriber<FakeJob> resultSubscriber() {
        return new Subscriber<FakeJob>() {

            @Override
            public void onCompleted() {
                Log.v(TAG, "subscribe.onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.v(TAG, "subscribe.doOnError: " + e.getMessage());
            }

            @Override
            public void onNext(FakeJob fakeJob) {

                Log.v(TAG, "subscribe.onNext");

                String msg = "Attempt: " + pollingAttempt;
                if (fakeJob.isJobDone()) {
                    msg = msg + " - Job done";
                } else {
                    msg = msg + " - Job not finished yet";
                }
                mSimpleStringAdapter.addString(msg);
                mListView.scrollToPosition(mSimpleStringAdapter.getItemCount()-1);
            }
        };
    }
    
    /**
     * This method returns the number of seconds an observer will sleeps based on the retry count. It extracts this value
     * from the retryMatrix
     *
     * @param attempt
     * @return
     */
    private Integer getSecondsToSleep(Integer attempt) {

        Integer secondsToSleep = 0;
        for( int i = 0; i < retryMatrix.size() && retryMatrix.get(i).first <= attempt; i++ ) {
            secondsToSleep = retryMatrix.get(i).second;
        }

        Log.v(TAG, "getSecondsToSleep() - attempt: " + attempt + " - secondsToSleep: " + secondsToSleep);

        return secondsToSleep;
    }
}
