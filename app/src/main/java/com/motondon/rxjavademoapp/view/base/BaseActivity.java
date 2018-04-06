package com.motondon.rxjavademoapp.view.base;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected Subscription subscription;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    protected  <T> Subscriber<T> resultSubscriber(final TextView view) {
        return new Subscriber<T>() {

            @Override
            public void onCompleted() {
                Log.v(TAG, "subscribe.onCompleted");
                view.setText(view.getText() + " - onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.v(TAG, "subscribe.doOnError: " + e.getMessage());
                view.setText(view.getText() + " - doOnError");
            }

            @Override
            public void onNext(T number) {

                Log.v(TAG, "subscribe.onNext");
                view.setText(view.getText() + " " + number);
            }
        };
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
     */
    protected <T> Observable.Transformer<T, T> showDebugMessages(final String operatorName) {

        return (observable) -> observable
                .doOnSubscribe(() -> Log.v(TAG, operatorName + ".doOnSubscribe"))
                .doOnUnsubscribe(() -> Log.v(TAG, operatorName + ".doOnUnsubscribe"))
                .doOnNext((doOnNext) -> Log.v(TAG, operatorName + ".doOnNext. Data: " + doOnNext))
                .doOnCompleted(() -> Log.v(TAG, operatorName + ".doOnCompleted"))
                .doOnTerminate(() -> Log.v(TAG, operatorName + ".doOnTerminate"))
                .doOnError((throwable) -> Log.v(TAG, operatorName + ".doOnError: " + throwable.getMessage()));
    }

    /**
     * Code downloaded from: http://blog.danlew.net/2015/03/02/dont-break-the-chain/
     *
     * @param <T>
     * @return
     */
    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
