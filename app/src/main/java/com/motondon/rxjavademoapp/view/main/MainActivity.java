package com.motondon.rxjavademoapp.view.main;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.adapter.MainActivityAdapter;
import com.motondon.rxjavademoapp.view.backpressure.BackpressureManualRequestExampleActivity;
import com.motondon.rxjavademoapp.view.backpressure.BackpressureBasicExampleActivity;
import com.motondon.rxjavademoapp.view.backpressure.BackpressureReactivePullExampleActivity;
import com.motondon.rxjavademoapp.view.backpressure.BackpressureSpecificOperatorsExampleActivity;
import com.motondon.rxjavademoapp.view.generalexamples.BroadcastSystemStatusExampleActivity;
import com.motondon.rxjavademoapp.view.generalexamples.DrawingExampleActivity;
import com.motondon.rxjavademoapp.view.generalexamples.TypingIndicatorExampleActivity;
import com.motondon.rxjavademoapp.view.hotobservables.HotObservableCacheExampleActivity;
import com.motondon.rxjavademoapp.view.hotobservables.HotObservableConnectExampleActivity;
import com.motondon.rxjavademoapp.view.hotobservables.HotObservableRefCountExampleActivity;
import com.motondon.rxjavademoapp.view.hotobservables.HotObservableReplayExampleActivity;
import com.motondon.rxjavademoapp.view.operators.AggregateOperatorsExampleActivity;
import com.motondon.rxjavademoapp.view.operators.CombiningObservablesExampleActivity;
import com.motondon.rxjavademoapp.view.operators.ConcatMapAndFlatMapExampleActivity;
import com.motondon.rxjavademoapp.view.operators.ConditionalOperatorsExampleActivity;
import com.motondon.rxjavademoapp.view.operators.ErrorHandlingExampleActivity;
import com.motondon.rxjavademoapp.view.operators.FilteringExampleActivity;
import com.motondon.rxjavademoapp.view.operators.JoinExampleActivity;
import com.motondon.rxjavademoapp.view.operators.MoreFilteringOperatorsExampleActivity;
import com.motondon.rxjavademoapp.view.operators.RetryExampleActivity;
import com.motondon.rxjavademoapp.view.operators.TimeoutExampleActivity;
import com.motondon.rxjavademoapp.view.operators.TransformingOperatorsExampleActivity;
import com.motondon.rxjavademoapp.view.parallelization.ParallelizationExampleActivity;
import com.motondon.rxjavademoapp.view.generalexamples.ServerPollingAfterDataProcessingExampleActivity;
import com.motondon.rxjavademoapp.view.generalexamples.ServerPollingExampleActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.category_list) RecyclerView exampleCategoriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.example_list_title);
        }

        exampleCategoriesList.setHasFixedSize(true);
        exampleCategoriesList.setLayoutManager(new LinearLayoutManager(this));
        exampleCategoriesList.setAdapter(new MainActivityAdapter(this, getCategoriesList()));
    }

    /**
     * This method is intended to hold a list of a Pair object that contains:
     *   - First arg: List of CategoryItem
     *   - Second arg: Pair of String, String
     *
     * @return
     */
    private List<Pair<List<CategoryItem>, Pair<String, String>>> getCategoriesList() {
        List<Pair<List<CategoryItem>, Pair<String, String>>> exampleTypesList = new ArrayList<>();

        exampleTypesList.add(new Pair<>(getOperatorsCategoryExamples(),
                new Pair<>(
                "RxJava Operators Examples",
                "Contain examples of RxJava operators such as filtering, combining, conditional, etc.")));
        exampleTypesList.add(new Pair<>(getBackpressureCategoryExamples(),
                new Pair<>(
                "Backpressure Examples",
                "Contain examples of how to deal with backpressure issues by presenting some strategies in order to alleviate it.")));
        exampleTypesList.add(new Pair<>(getHotObservablesCategoryExamples(),
                new Pair<>(
                "Hot Observable Examples",
                "Contain examples of hot observables.")));
        exampleTypesList.add(new Pair<>(getParallelizationCategoryExamples(),
                new Pair<>(
                "Parallelization",
                "Contain examples of how to implement parallelization using different approaches.")));
        exampleTypesList.add(new Pair<>(getGeneralCategoryExamples(),
                new Pair<>(
                "General Examples",
                "This option includes some general examples such as polling server data, some examples on how to deal with UI components in a reactive way, etc.")));
        return exampleTypesList;
    }

    private List<CategoryItem> getOperatorsCategoryExamples() {
        List<CategoryItem> operatorsExamples = new ArrayList<>();

        operatorsExamples.add(new CategoryItem(
                FilteringExampleActivity.class,
                "Filtering operators",
                "Show some operators which filter out emitted items. They are: first(), firstOrDefault(), takeFirst(), single(), singleOrDefault(), elementAt(), last(), lastOrDefault(), take(), takeLast() and filter()."));

        operatorsExamples.add(new CategoryItem(
                MoreFilteringOperatorsExampleActivity.class,
                "More about filtering operators",
                "Show some other filtering operators, such as sample(), sample(w/ Observable), debounce(), throttleLast(), etc."));

        operatorsExamples.add(new CategoryItem(
                CombiningObservablesExampleActivity.class,
                "Combining Operators",
                "Show operators that are used to combine emissions (zip(), merge(), etc)."));

        operatorsExamples.add(new CategoryItem(
                ConditionalOperatorsExampleActivity.class,
                "Conditional Operators",
                "Demonstrate some conditional operators like skipWhile(), skipUntil(), etc."));

        operatorsExamples.add(new CategoryItem(
                TransformingOperatorsExampleActivity.class,
                "Transforming Operators",
                "Show different variations of buffer() operator as well as windows() and scan()."));

        operatorsExamples.add(new CategoryItem(
                TimeoutExampleActivity.class,
                "Timeout",
                "Implement different variations of timeout() operator."));

        operatorsExamples.add(new CategoryItem(
                AggregateOperatorsExampleActivity.class,
                "Aggregate Operators",
                "Show operators that fit on the aggregate category such as collect(), reduce(), etc."));

        operatorsExamples.add(new CategoryItem(
                JoinExampleActivity.class,
                "Join",
                "Show join() operator by allowing users to set different values and quick check how join() behaves."));

        operatorsExamples.add(new CategoryItem(
                RetryExampleActivity.class,
                "Retry and RetryWhen",
                "Simulate network requests errors and use retry() and retryWhen() operators in order to retry the requests by using different approaches."));

        operatorsExamples.add(new CategoryItem(
                ConcatMapAndFlatMapExampleActivity.class,
                "ConcatMap and FlatMap",
                "Show how they differ from each other. Basically concatMap() cares about ordering while flatMap() does not."));

        operatorsExamples.add(new CategoryItem(
                ErrorHandlingExampleActivity.class,
                "Error Handling",
                "Show how to deal with error handling in the reactive way. It implements examples of checked and unchecked exceptions, as well as onErrorReturn(), onErrorResumeNext() and onExceptionResumeNext() operators."));

        return operatorsExamples;
    }

    private List<CategoryItem> getHotObservablesCategoryExamples() {
        List<CategoryItem> hotObservablesExample = new ArrayList<>();

        hotObservablesExample.add(new CategoryItem(
                HotObservableConnectExampleActivity.class,
                "Hot Observables - Connect",
                "Show how to use connect() operator."));

        hotObservablesExample.add(new CategoryItem(
                HotObservableRefCountExampleActivity.class,
                "Hot Observables - RefCount",
                "Show how to use refCount() operator."));

        hotObservablesExample.add(new CategoryItem(
                HotObservableReplayExampleActivity.class,
                "Hot Observables - Replay",
                "Show how to use some replay() operator variants."));

        hotObservablesExample.add(new CategoryItem(
                HotObservableCacheExampleActivity.class,
                "Hot Observables - Cache",
                "Show how to use cache() operator."));

        return hotObservablesExample;
    }

    private List<CategoryItem> getBackpressureCategoryExamples() {
        List<CategoryItem> backpressureExample = new ArrayList<>();

        backpressureExample.add(new CategoryItem(
                BackpressureBasicExampleActivity.class,
                "Backpressure - MissingBackpressureException and throttle()",
                "Show what happens when an observable produces items much faster than they are consumed by the observers."));

        backpressureExample.add(new CategoryItem(
                BackpressureSpecificOperatorsExampleActivity.class,
                "Backpressure - Specifc Operators",
                "Show how to use backpressureBuffer() and backpressureDrop() operators."));

        backpressureExample.add(new CategoryItem(
                BackpressureReactivePullExampleActivity.class,
                "Backpressure - Pull Request",
                "Show how to deal with backpressure by using Subscriber::request() method."));

        backpressureExample.add(new CategoryItem(
                BackpressureManualRequestExampleActivity.class,
                "Backpressure - Manual Request",
                "Show how to use request() operator in order to request items manually."));

        return backpressureExample;
    }

    private List<CategoryItem> getParallelizationCategoryExamples() {
        List<CategoryItem> parallelizationExamples = new ArrayList<>();

        parallelizationExamples.add(new CategoryItem(
                ParallelizationExampleActivity.class,
                "Parallelization",
                "Show how to implement parallelization in RxJava"));

        return parallelizationExamples;
    }

    private List<CategoryItem> getGeneralCategoryExamples() {
        List<CategoryItem> activityAndNameHelpers = new ArrayList<>();

        activityAndNameHelpers.add(new CategoryItem(
                ServerPollingExampleActivity.class,
                "Simulate Server Polling",
                "This example simulates a server polling by using different operators. Basically it analyzes each data received by the server and check whether it can be considered done or not. Depends on the example, it can poll the server until data is (considered) done or just for a fixed number of times."));

        activityAndNameHelpers.add(new CategoryItem(
                ServerPollingAfterDataProcessingExampleActivity.class,
                "Server Polling After Local Data Processing",
                "This example also shows a server polling, but it differs from the previous example in a way it simulates a local data processing, and only then it polls the server again, no matter how long it takes to process that data locally."));

        activityAndNameHelpers.add(new CategoryItem(
                TypingIndicatorExampleActivity.class,
                "Typing Indicator",
                "Show how to implement 'typing indicator' feature by using RxJava"));

        activityAndNameHelpers.add(new CategoryItem(
                DrawingExampleActivity.class,
                "Reactive Drawing Example",
                "Show how to react to mouse events by using RxBindings library. Also demonstrates how to listen for SeekBar and menu item click events."));

        activityAndNameHelpers.add(new CategoryItem(
                BroadcastSystemStatusExampleActivity.class,
                "Broadcast System Status",
                "Show how to listen for broadcast system messages by using fromBroadcast() method from RxBroadcast library."));
        return activityAndNameHelpers;
    }
}
