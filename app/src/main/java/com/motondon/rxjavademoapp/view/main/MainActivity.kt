package com.motondon.rxjavademoapp.view.main

import android.os.Bundle
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.adapter.MainActivityAdapter
import com.motondon.rxjavademoapp.view.backpressure.BackpressureManualRequestExampleActivity
import com.motondon.rxjavademoapp.view.backpressure.BackpressureBasicExampleActivity
import com.motondon.rxjavademoapp.view.backpressure.BackpressureReactivePullExampleActivity
import com.motondon.rxjavademoapp.view.backpressure.BackpressureSpecificOperatorsExampleActivity
import com.motondon.rxjavademoapp.view.generalexamples.BroadcastSystemStatusExampleActivity
import com.motondon.rxjavademoapp.view.generalexamples.DrawingExampleActivity
import com.motondon.rxjavademoapp.view.generalexamples.TypingIndicatorExampleActivity
import com.motondon.rxjavademoapp.view.hotobservables.HotObservableCacheExampleActivity
import com.motondon.rxjavademoapp.view.hotobservables.HotObservableConnectExampleActivity
import com.motondon.rxjavademoapp.view.hotobservables.HotObservableRefCountExampleActivity
import com.motondon.rxjavademoapp.view.hotobservables.HotObservableReplayExampleActivity
import com.motondon.rxjavademoapp.view.operators.AggregateOperatorsExampleActivity
import com.motondon.rxjavademoapp.view.operators.CombiningObservablesExampleActivity
import com.motondon.rxjavademoapp.view.operators.ConcatMapAndFlatMapExampleActivity
import com.motondon.rxjavademoapp.view.operators.ConditionalOperatorsExampleActivity
import com.motondon.rxjavademoapp.view.operators.ErrorHandlingExampleActivity
import com.motondon.rxjavademoapp.view.operators.FilteringExampleActivity
import com.motondon.rxjavademoapp.view.operators.JoinExampleActivity
import com.motondon.rxjavademoapp.view.operators.MoreFilteringOperatorsExampleActivity
import com.motondon.rxjavademoapp.view.operators.RetryExampleActivity
import com.motondon.rxjavademoapp.view.operators.TimeoutExampleActivity
import com.motondon.rxjavademoapp.view.operators.TransformingOperatorsExampleActivity
import com.motondon.rxjavademoapp.view.parallelization.ParallelizationExampleActivity
import com.motondon.rxjavademoapp.view.generalexamples.ServerPollingAfterDataProcessingExampleActivity
import com.motondon.rxjavademoapp.view.generalexamples.ServerPollingExampleActivity

import java.util.ArrayList

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    /**
     * This method is intended to hold a list of a Pair object that contains:
     * - First arg: List of CategoryItem
     * - Second arg: Pair of String, String
     *
     * @return
     */
    private val categoriesList: List<Pair<List<CategoryItem>, Pair<String, String>>>
        get() {
            val exampleTypesList = ArrayList<Pair<List<CategoryItem>, Pair<String, String>>>()

            exampleTypesList.add(Pair(operatorsCategoryExamples,
                    Pair(
                            "RxJava Operators Examples",
                            "Contain examples of RxJava operators such as filtering, combining, conditional, etc.")))
            exampleTypesList.add(Pair(backpressureCategoryExamples,
                    Pair(
                            "Backpressure Examples",
                            "Contain examples of how to deal with backpressure issues by presenting some strategies in order to alleviate it.")))
            exampleTypesList.add(Pair(hotObservablesCategoryExamples,
                    Pair(
                            "Hot Observable Examples",
                            "Contain examples of hot observables.")))
            exampleTypesList.add(Pair(parallelizationCategoryExamples,
                    Pair(
                            "Parallelization",
                            "Contain examples of how to implement parallelization using different approaches.")))
            exampleTypesList.add(Pair(generalCategoryExamples,
                    Pair(
                            "General Examples",
                            "This option includes some general examples such as polling server data, some examples on how to deal with UI components in a reactive way, etc.")))
            return exampleTypesList
        }

    private val operatorsCategoryExamples: List<CategoryItem>
        get() {
            val operatorsExamples = ArrayList<CategoryItem>()

            operatorsExamples.add(CategoryItem(
                    FilteringExampleActivity::class.java,
                    "Filtering operators",
                    "Show some operators which filter out emitted items. They are: first(), firstOrDefault(), takeFirst(), single(), singleOrDefault(), elementAt(), last(), lastOrDefault(), take(), takeLast() and filter()."))

            operatorsExamples.add(CategoryItem(
                    MoreFilteringOperatorsExampleActivity::class.java,
                    "More about filtering operators",
                    "Show some other filtering operators, such as sample(), sample(w/ Observable), debounce(), throttleLast(), etc."))

            operatorsExamples.add(CategoryItem(
                    CombiningObservablesExampleActivity::class.java,
                    "Combining Operators",
                    "Show operators that are used to combine emissions (zip(), merge(), etc)."))

            operatorsExamples.add(CategoryItem(
                    ConditionalOperatorsExampleActivity::class.java,
                    "Conditional Operators",
                    "Demonstrate some conditional operators like skipWhile(), skipUntil(), etc."))

            operatorsExamples.add(CategoryItem(
                    TransformingOperatorsExampleActivity::class.java,
                    "Transforming Operators",
                    "Show different variations of buffer() operator as well as windows() and scan()."))

            operatorsExamples.add(CategoryItem(
                    TimeoutExampleActivity::class.java,
                    "Timeout",
                    "Implement different variations of timeout() operator."))

            operatorsExamples.add(CategoryItem(
                    AggregateOperatorsExampleActivity::class.java,
                    "Aggregate Operators",
                    "Show operators that fit on the aggregate category such as collect(), reduce(), etc."))

            operatorsExamples.add(CategoryItem(
                    JoinExampleActivity::class.java,
                    "Join",
                    "Show join() operator by allowing users to set different values and quick check how join() behaves."))

            operatorsExamples.add(CategoryItem(
                    RetryExampleActivity::class.java,
                    "Retry and RetryWhen",
                    "Simulate network requests errors and use retry() and retryWhen() operators in order to retry the requests by using different approaches."))

            operatorsExamples.add(CategoryItem(
                    ConcatMapAndFlatMapExampleActivity::class.java,
                    "ConcatMap and FlatMap",
                    "Show how they differ from each other. Basically concatMap() cares about ordering while flatMap() does not."))

            operatorsExamples.add(CategoryItem(
                    ErrorHandlingExampleActivity::class.java,
                    "Error Handling",
                    "Show how to deal with error handling in the reactive way. It implements examples of checked and unchecked exceptions, as well as onErrorReturn(), onErrorResumeNext() and onExceptionResumeNext() operators."))

            return operatorsExamples
        }

    private val hotObservablesCategoryExamples: List<CategoryItem>
        get() {
            val hotObservablesExample = ArrayList<CategoryItem>()

            hotObservablesExample.add(CategoryItem(
                    HotObservableConnectExampleActivity::class.java,
                    "Hot Observables - Connect",
                    "Show how to use connect() operator."))

            hotObservablesExample.add(CategoryItem(
                    HotObservableRefCountExampleActivity::class.java,
                    "Hot Observables - RefCount",
                    "Show how to use refCount() operator."))

            hotObservablesExample.add(CategoryItem(
                    HotObservableReplayExampleActivity::class.java,
                    "Hot Observables - Replay",
                    "Show how to use some replay() operator variants."))

            hotObservablesExample.add(CategoryItem(
                    HotObservableCacheExampleActivity::class.java,
                    "Hot Observables - Cache",
                    "Show how to use cache() operator."))

            return hotObservablesExample
        }

    private val backpressureCategoryExamples: List<CategoryItem>
        get() {
            val backpressureExample = ArrayList<CategoryItem>()

            backpressureExample.add(CategoryItem(
                    BackpressureBasicExampleActivity::class.java,
                    "Backpressure - MissingBackpressureException and throttle()",
                    "Show what happens when an observable produces items much faster than they are consumed by the observers."))

            backpressureExample.add(CategoryItem(
                    BackpressureSpecificOperatorsExampleActivity::class.java,
                    "Backpressure - Specifc Operators",
                    "Show how to use backpressureBuffer() and backpressureDrop() operators."))

            backpressureExample.add(CategoryItem(
                    BackpressureReactivePullExampleActivity::class.java,
                    "Backpressure - Pull Request",
                    "Show how to deal with backpressure by using Subscriber::request() method."))

            backpressureExample.add(CategoryItem(
                    BackpressureManualRequestExampleActivity::class.java,
                    "Backpressure - Manual Request",
                    "Show how to use request() operator in order to request items manually."))

            return backpressureExample
        }

    private val parallelizationCategoryExamples: List<CategoryItem>
        get() {
            val parallelizationExamples = ArrayList<CategoryItem>()

            parallelizationExamples.add(CategoryItem(
                    ParallelizationExampleActivity::class.java,
                    "Parallelization",
                    "Show how to implement parallelization in RxJava"))

            return parallelizationExamples
        }

    private val generalCategoryExamples: List<CategoryItem>
        get() {
            val activityAndNameHelpers = ArrayList<CategoryItem>()

            activityAndNameHelpers.add(CategoryItem(
                    ServerPollingExampleActivity::class.java,
                    "Simulate Server Polling",
                    "This example simulates a server polling by using different operators. Basically it analyzes each data received by the server and check whether it can be considered done or not. Depends on the example, it can poll the server until data is (considered) done or just for a fixed number of times."))

            activityAndNameHelpers.add(CategoryItem(
                    ServerPollingAfterDataProcessingExampleActivity::class.java,
                    "Server Polling After Local Data Processing",
                    "This example also shows a server polling, but it differs from the previous example in a way it simulates a local data processing, and only then it polls the server again, no matter how long it takes to process that data locally."))

            activityAndNameHelpers.add(CategoryItem(
                    TypingIndicatorExampleActivity::class.java,
                    "Typing Indicator",
                    "Show how to implement 'typing indicator' feature by using RxJava"))

            activityAndNameHelpers.add(CategoryItem(
                    DrawingExampleActivity::class.java,
                    "Reactive Drawing Example",
                    "Show how to react to mouse events by using RxBindings library. Also demonstrates how to listen for SeekBar and menu item click events."))

            activityAndNameHelpers.add(CategoryItem(
                    BroadcastSystemStatusExampleActivity::class.java,
                    "Broadcast System Status",
                    "Show how to listen for broadcast system messages by using fromBroadcast() method from RxBroadcast library."))
            return activityAndNameHelpers
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionBar = supportActionBar
        actionBar?.setTitle(R.string.example_list_title)

        exampleCategoriesList.setHasFixedSize(true)
        exampleCategoriesList.layoutManager = LinearLayoutManager(this)
        exampleCategoriesList.adapter = MainActivityAdapter(this, categoriesList)
    }
}
