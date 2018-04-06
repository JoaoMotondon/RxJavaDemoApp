package com.motondon.rxjavademoapp.view.base;

import android.util.Log;
import android.widget.Toast;

import rx.Subscription;
import rx.observables.ConnectableObservable;

public class HotObservablesBaseActivity extends BaseActivity {

    private static final String TAG = HotObservablesBaseActivity.class.getSimpleName();

    protected ConnectableObservable<Long> connectable;
    protected Subscription firstSubscription;
    protected Subscription secondSubscription;

    /**
     * When unsubscribing a subscriber, that means it will stop receiving emitted items.
     *
     */
    protected void unsubscribeFirst(boolean showMessage) {
        Log.v(TAG, "unsubscribeFirst()");
        if (firstSubscription != null && !firstSubscription.isUnsubscribed()) {
            Log.v(TAG, "unsubscribeFirst() - Calling unsubscribe...");
            firstSubscription.unsubscribe();
        } else {
            if (showMessage) {
                Toast.makeText(getApplicationContext(), "Subscriber not started", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * When unsubscribing a subscriber, that means it will stop receiving emitted items.
     *
     */
    protected void unsubscribeSecond(boolean showMessage) {
        Log.v(TAG, "unsubscribeSecond()");
        if (secondSubscription != null && !secondSubscription.isUnsubscribed()) {
            Log.v(TAG, "unsubscribeSecond() - Calling unsubscribe...");
            secondSubscription.unsubscribe();
        } else {
            if (showMessage) {
                Toast.makeText(getApplicationContext(), "Subscriber not started", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
