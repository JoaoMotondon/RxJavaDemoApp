package com.motondon.rxjavademoapp.view.base

import android.content.Intent
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.TextView

import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

open class BaseActivity : AppCompatActivity() {

    protected var mSubscription: Subscription? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                val intent = NavUtils.getParentActivityIntent(this)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                NavUtils.navigateUpTo(this, intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun <T> resultSubscriber(view: TextView): Subscriber<T> {
        return object : Subscriber<T>() {

            override fun onCompleted() {
                Log.v(TAG, "subscribe.onCompleted")
                view.text = "${view.text} - onCompleted"
            }

            override fun onError(e: Throwable) {
                Log.v(TAG, "subscribe.doOnError: ${e.message}")
                view.text = "${view.text} - doOnError"
            }

            override fun onNext(number: T) {
                Log.v(TAG, "subscribe.onNext")
                view.text = "${view.text} $number"
            }
        }
    }

    /**
     * Print log messages for some side effects methods (or utility methods).
     *
     * Note that, when calling this method by using Java 7, we must inform explicitly the type T. Otherwise it will assume the Object type, which
     * is not compatible,and we will get an error. But when using Java 8, this is no longer needed, since the compiler will infer the correct type.
     * This is called a type witness.
     *
     * From the docs:
     *
     * "The Java SE 7 compiler [...] requires a value for the type argument T so it starts with the value Object. Consequently, the invocation
     * of Collections.emptyList returns a value of type List<Object>, which is incompatible with the method..."
     *
     * "This is no longer necessary in Java SE 8. The notion of what is a target type has been expanded to include method arguments [...]"
     * * http://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html
     *
     * @param operatorName
     * @param <T>
     * @return
    </T></Object> */
    protected fun <T> showDebugMessages(operatorName: String): Observable.Transformer<T, T> {

        return Observable.Transformer { observable ->
            observable
                .doOnSubscribe { Log.v(TAG, "$operatorName.doOnSubscribe") }
                .doOnUnsubscribe { Log.v(TAG, "$operatorName.doOnUnsubscribe") }
                .doOnNext { doOnNext -> Log.v(TAG, "$operatorName.doOnNext. Data: $doOnNext") }
                .doOnCompleted { Log.v(TAG, "$operatorName.doOnCompleted") }
                .doOnTerminate { Log.v(TAG, "$operatorName.doOnTerminate") }
                .doOnError { throwable -> Log.v(TAG, "$operatorName.doOnError: ${throwable.message}") }
        }
    }

    /**
     * Code downloaded from: http://blog.danlew.net/2015/03/02/dont-break-the-chain/
     *
     * @param <T>
     * @return
    </T> */
    protected fun <T> applySchedulers(): Observable.Transformer<T, T> {
        return Observable.Transformer { observable ->
            observable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    protected fun isUnsubscribed(): Boolean {
        return mSubscription?.let {
            it.isUnsubscribed
        } ?: true
    }

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
    }
}
