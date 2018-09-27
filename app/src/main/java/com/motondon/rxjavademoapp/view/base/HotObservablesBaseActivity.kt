package com.motondon.rxjavademoapp.view.base

import android.util.Log
import android.widget.Toast

import rx.Subscription
import rx.observables.ConnectableObservable

open class HotObservablesBaseActivity : BaseActivity() {

    protected var connectable: ConnectableObservable<Long>? = null
    protected var firstSubscription: Subscription? = null
    protected var secondSubscription: Subscription? = null

    /**
     * When unsubscribing a subscriber, that means it will stop receiving emitted items.
     *
     */
    protected fun unsubscribeFirst(showMessage: Boolean) {
        Log.v(TAG, "unsubscribeFirst()")

        firstSubscription?.let {
            if (!it.isUnsubscribed) {
                Log.v(TAG, "unsubscribeFirst() - Calling unsubscribe...")
                it.unsubscribe()
            } else {
                if (showMessage) {
                    Toast.makeText(applicationContext, "Subscriber not started", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            if (showMessage) {
                Toast.makeText(applicationContext, "Subscriber not started", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * When unsubscribing a subscriber, that means it will stop receiving emitted items.
     *
     */
    protected fun unsubscribeSecond(showMessage: Boolean) {
        Log.v(TAG, "unsubscribeSecond()")

        secondSubscription?.let {
            if (!it.isUnsubscribed) {
                Log.v(TAG, "unsubscribeSecond() - Calling unsubscribe...")
                it.unsubscribe()
            } else {
                if (showMessage) {
                    Toast.makeText(applicationContext, "Subscriber not started", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            if (showMessage) {
                Toast.makeText(applicationContext, "Subscriber not started", Toast.LENGTH_SHORT).show()
            }
        }
    }

    protected fun isFirstSubscriptionUnsubscribed(): Boolean {
        return firstSubscription?.let {
            it.isUnsubscribed
        } ?: true
    }

    protected fun isSecondSubscriptionUnsubscribed(): Boolean {
        return secondSubscription?.let {
            it.isUnsubscribed
        } ?: true
    }

    companion object {
        private val TAG = HotObservablesBaseActivity::class.java.simpleName
    }
}
