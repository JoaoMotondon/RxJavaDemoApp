# RxJava for Android (100+ Examples Pack) - &#x1F539;Now in Kotlin!!! &#x1F539;
This repository is intended to provide in a single place (i.e. a single Android app) over than 100 examples of RxJava for Android which you can use as a reference when dealing with RxJava. 

# **_UPDATE: This project was entirely convert to Kotlin._**

![](https://user-images.githubusercontent.com/4574670/38424192-a2a65350-3986-11e8-980b-a359764b2e9d.png)
![](https://user-images.githubusercontent.com/4574670/38424190-a2611f60-3986-11e8-9f19-8993a905d2bf.png)

![](https://user-images.githubusercontent.com/4574670/38424191-a2839194-3986-11e8-9779-5df3a3416113.png)
![](https://user-images.githubusercontent.com/4574670/38424193-a2ca1628-3986-11e8-9610-5b4daa6b30bb.png)

In order to make it easy to find the examples throughout the app, we divided them in different categories. They are:
  - **Operators Examples:** Basically it contains examples of operators used to filtering, combining, transforming data, etc as well as some error handling examples.
  - **Backpressure Examples:** Contain different approaches to deal with backpressure.
  - **Hot Observable Examples:** Show how to use Hot Observables by using operators such as cache, replay, etc.
  - **Parallelization:** Demonstrate how to do real parallelization using RxJava operators and schedulers.
  - **General Examples:** Contain some examples that do not fit in any of the previous categories.

Some examples were already covered into details in one of the articles of our RxJava series (you can find the links below), while some other were based on external articles, so you can go directly to those articles and see the the authors' explanations.

It's worth mentioning whenever an example was based on an external article, we added a reference to the sources, so all the credits go to the authors.

You can find details about this repository [here](http://androidahead.com/2018/04/06/rxjava-for-android-100-examples-pack/).

Here is a list of all covered examples on the demo app and the articles on which they were based:

  - **Operators Examples**
    -  [Filtering Operators](http://androidahead.com/2017/09/11/rxjava-operators-part-1-filtering-operators/) (12 examples)    
    -  [More about Filtering Operators](http://androidahead.com/2017/09/25/rxjava-operators-part-2-more-about-filtering-operators/) (5 examples)    
    -  [Combining Observables](http://androidahead.com/2017/10/17/rxjava-operators-part-3-combining-observables/) (9 examples)    
    -  [Conditional Operators](http://androidahead.com/2017/10/31/rxjava-operators-part-4-conditional-operators/) (10 examples)    
    -  [Transforming Operators](http://androidahead.com/2017/11/16/rxjava-operators-part-5-transforming-operators/) (9 examples)    
    -  [Timeout Operator](http://androidahead.com/2017/12/05/rxjava-operators-part-6-timeout-operator/) (7 examples)
     -  [Mathematical and Aggregate Operators](http://androidahead.com/2017/12/21/rxjava-operators-part-7-mathematical-and-aggregate-operators/) (7 examples)    
    -  [Join Operator](http://androidahead.com/2018/01/09/rxjava-operators-part-8-join-operator/) (1 examples)    
    -  [Retry and RetryWhen](http://blog.danlew.net/2016/01/25/rxjavas-repeatwhen-and-retrywhen-explained/) (6 examples)
     -  [ConcatMap and FlatMap](http://fernandocejas.com/2015/01/11/rxjava-observable-tranformation-concatmap-vs-flatmap/) (2 examples)
      - [Error Handling](http://blog.danlew.net/2015/12/08/error-handling-in-rxjava/) (5 examples)
  
  - **Back Pressure Examples**
      - [Backpressure](http://androidahead.com/2018/01/30/rxjava-operators-part-9-backpressure/) (8 examples)
  
  - **Hot Observable Examples**
    - [Hot Observables](http://androidahead.com/2018/02/17/rxjava-operators-part-10-hot-observables/)` (6 examples)
  
  - **Parallelization Examples**
    - [Achiving Parallelization](http://tomstechnicalblog.blogspot.com.br/2015/11/rxjava-achieving-parallelization.html) and [Maximizing parallelization](http://tomstechnicalblog.blogspot.com.br/2016/02/rxjava-maximizing-parallelization.html) (8 examples)
  
  - **General Examples**
     -  [Simulate Server Polling](https://medium.com/@v.danylo/server-polling-and-retrying-failed-operations-with-retrofit-and-rxjava-8bcc7e641a5a) (4 examples) - This example is useful when we need to poll a server periodically until it finishes a certain task. 
    -  [Server Polling After Local Data Processing](https://github.com/ReactiveX/RxJava/issues/448) (3 examples) - This example is useful when we need to poll a server and process some data locally, and only after finish local processing, poll the server again. 
    -  [Typing Indicator](http://androidahead.com/2017/04/03/typing-indicator-using-rxjava/) (1 example)  
    -  [Drawing Example](http://choruscode.blogspot.com.br/2014/07/rxjava-for-ui-events-on-android-example.html) (1 example) - This example shows how to react to mouse events as well as listen for SeekBar and menu item click events by using RxBindings library.  
    -  Broadcast System Status (1 example) - This example shows how to listen for broadcast system messages by using RxBroadcast library.
    
# License
This project is licensed under the Apache-2.0 - see the [LICENSE](LICENSE) file for details
