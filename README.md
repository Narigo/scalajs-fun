# scalajs-fun

Trying out ScalaJS.

I'll try to port CycleJS to ScalaJS in order to learn both of it.

The egghead course looks like a good way to start 🤔
https://egghead.io/courses/cycle-js-fundamentals

## Used libraries

If you want to use a library with Scala.js, you need to use one that is exported to
`<group>.<identifier>_sjs<version>` (just like Scala `_2.10` or `_2.11`). RxScala is currently not supporting this,
according to [their issue #161](https://github.com/ReactiveX/RxScala/issues/161).

Therefore, I went with [scala.rx](https://github.com/lihaoyi/scala.rx). It is not really the same as RxScala, but you
can have reactive variables with it. It has a different API, but may be working in a similar way.

**Update:** After fiddling around a lot with scala.rx, I've switched to another library. This results in an 
implementation that looks a lot more similar to the real CycleJS implementation shown in the videos.

## Video 01 - The Cycle.js principle: separating logic from effects

As you cannot map with scala.rx but have `Var` and `Rx{}`, the code looks a bit different.

## Video 02 - Main function and effects function

To separate logic and effects, the code is split into two functions. As this was done, the next step is to use the sink
(the `Rx[String]` in our case) and hand it over to multiple effects: Writing into DOM and writing into console.

The main function is called `logic()` as `main()` is needed by `js.App` as main entry point.

## Video 03 - Customizing effects from the main function

In this case, the main function (called `logic` in our case) is split into separate sinks. This is done through a map in
the video. It's not possible to return `Obs` directly and use it as the value of the observed `Rx` is not passed into
the callback.

Weird behavior: When using `i() = i() * 2` instead of `i() = i() + 2`, the `consoleLogEffect` does not fire anymore.
After fiddling around with this a bit more, I realized that `i` was set to `Var(0)` initially. Every update `* 2` made
it be `0` again, so it does not update internally and therefore does not fire again. To prevent that, one should use the
`.propagate()` method on the `Rx` / `Obs` that listens on `i`.

When using [scala.rx](https://github.com/lihaoyi/scala.rx) version `0.3.0+`, it added Ownership context. This is used to
prevent creating leaky `Rx` when nesting them. The documentation doesn't really say how to create a safe context, so in
the current code, the line `implicit private val ctx = Ctx.Owner.safe()` is added to the App object as it doesn't seem
to be possible to create this in the `main()` method itself.

## Video 04 - Introducing run() and driver functions

This is more or less a matter of renaming things: `effects` are called `drivers` now and there is a map to show the
respective drivers that are at work right now. If one is not used / needed, one can comment it out without having to
touch multiple lines now.

## Video 05 - Read effects from the DOM: click events

In this video, the drivers start returning sources. The proxy logic is different to JavaScript as we are usually not
passing around mutable state. It needs to be done in a more scala-like way.

The drivers have to use input and output parameters in order to use the same Variable as the `proxySource`. Using the
`.getOrElseUpdate()` method on the `proxySource` `Map`, we can initialize the sources in the `main` method. This keeps
the whole logic and variable initialization inside the main method.

## Video 06 - Generalizing run() function for more types of sources

Using the `trigger` method, we can actually get rid of the output parameter in the drivers method. This way, we have two
separate variables, but this seems to be the way that Cycle does it itself.

To truly generalize the `run` method now, it's been pulled out into the `Scycle` object. Using type aliases makes it a
bit easier to understand which types are used for what. It also shows that the logic method does a bit more right now
than just taking the drivers output and giving some output to feed into the respective driver.

### Note about implementation of the timer

A small thing which is different to the implementation of the JavaScript is that the interval timer does not get reset
every time the document is clicked. If we were starting a new interval timer on each click, we would need to introduce a
variable that can be used to clear the old timer and remembers the new one. So it's possible to do but in order to keep
the implementation of the core Cycle principle in focus, we will continue with the single timer.

## Video 07 - Making our toy DOM Driver more flexible

The latest refactorings are done to abstract the driver and let it do a lot more for us. It receives a specific input 
(in case of the `DomDriver` it wants a `dom.Element`) and returns functions that can be used to receive events in the 
main function. Called `selectEvents`, it takes the currently available DOM elements and adds event listeners on them. In
the current implementation, it cannot add listeners to the added elements yet, as the selectEvents call is done before 
the elements are added to the DOM.

## Video 08 - Fine-grained control over the DOM Source

The new implementation of the DomDriver takes all events that occur on the document and then filters out the ones that
we created listeners for. This ensures to catch events of newly created DOM elements as well.

## Video 09 - Hyperscript as our alternative to template languages

In this video, we see a small improvement on creating and handling the DOM elements. It uses Hyperscript, which is 
basically functions wrapping around the creation of DOM elements. Something similar is implemented with the 
`Hyperscript` trait and the `HyperScriptElement` classes. The `Text` case is somewhat special, as we need to wrap a text
node into a span to let it count as an element. With an implicit conversion of `String` to `Text` (`stringToTextNode`)
we can get rid of extra calls to `Text()`.

## Video 10 - From toy DOM Driver to real DOM Driver

When creating the DOM Driver, we now pass a selector to it, to select the container element. The container element is 
still the one we were using before (`#app`), so the result does not change. The `makeXXXDriver` functions now return a
function expecting a `LogicOutput` and return a `Driver`. This way, we do not need to pass an explicit `input` and can
just use / cast from the `LogicOutput` (that is what the `input` variable has been before anyways).

The drivers were moved into a separate package. During this refactoring, the `ConsoleDriver` received the `input` 
parameter just like the `DomDriver`.

> Latest refactor: Move `Hyperscript` elements into `dom` package. Remove `ConsoleDriver` to focus on single `dom` 
driver and start with next lesson.

## Video 11 - Hello World in Cycle.js

First, we create a few more helpers for the `Hyperscript`. A `label`, `input` and `hr` element helps to build the GUI.
Doing a complete replacement of `container.innerHTML` with the `outerHTML` of `input()`, the GUI updates with a quirk: 
The input field looses focus whenever the view updates.

When using a virtual dom implementation that would not replace the whole `input` DOM element, we could mitigate this
problem. But that means we need to implement a virtual dom before continuing.  

---

### Starting a virtual dom implementation

Finally something we can test! Let's start by adding a scalatest dependency. A small test to check that the test setup 
works is a nice trick to be sure that you're not wasting time debugging your tests when in reality the build setup is 
not correct.

To be sure that the code can be tested correctly, we need to test within a browser instead of testing in Rhino 
environment. This can be done by either installing PhantomJS (`npm i -g phantomjs`) and disabling Rhino in sbt (by
`set scalaJSUseRhino in Global := false` in the sbt REPL) or testing with a browser directly through the new HTML Test 
Runner feature in ScalaJS (use `testHtmlFastOpt` and open the path to the file presented in the output in the browser).

After building a simple virtual dom diffing algorithm, it turns out that adding "simple" optimizations may take some 
time to get them right. There may still be some bugs lurking in the current algorithm but we'll try to refocus on the
real implementation of CycleJS again.

---

Being done with the virtual dom implementation for now, we can update our DOM driver to use it. It made sense to let the
main function return a virtual dom representation as well instead of mapping from and to real HTML elements. This way, 
the driver only needs the virtual dom representation and the user code (our `ScycleApp` `logic` function) does not need
to deal with the real DOM itself.

## Video 12 - An interactive counter in Cycle.js

Before beginning the implementation of the next video, we are going to need a few more HTML elements: `button` and `p`. 
We are going to implement them just like all other of our `Hyperscript` elements for now.

After all the work with virtual dom was done, setting up the GUI is pretty straight forward. The two buttons, a 
paragraph and text label. In the video, we see how to merge and scan `Rx.Observable` streams. With the `ScalaRx` 
implementation we can use the `triggerLater` methods to update the `result`, which basically is our counter state.  

To make it more in sync with the JavaScript implementation, we took the `Rx` context out of the Map. This way it will 
re-evaluate the `Map` with the calculations outside of the values we return for the drivers.

## Video 13 - Using the HTTP Driver

We start by making a new GUI for the desired result. The second step is to hook up the `Rx` variables of an `HttpDriver`
to the app.

> After struggling a lot trying to get the value of an `Option`, it looks like ScalaJS or rx.Scala do not like using 
> `Option.map` or `Option.get`. The underlying problem seems to be that `Rx[Something]` does not like having null values
> assigned to it. We need to investigate this behavior further to see what we can do. Maybe having a `Non-Request` class
> would help.
> 
> Turns out that `NonRequest` doesn't really help. It's also not very clear to me why using `response()` in `ScycleApp`
> does not fire. A lot of debugging code exists in this commit now, but I don't think I'll come around to fix this. As 
> it doesn't really look like the `scala.rx` library gets much love, I will push this on a branch and restart this 
> project with another library: https://github.com/LukaJCB/rxscala-js
>
> Using the rxscala-js library, the code will be much closer to the videos anyways. Let's see how far we get using that.

## Restarting with rxscala-js

After trying out rxscala for the first few videos, we're going to restart implementing [Cycle.js](http://cycle.js.org/) 
with [rxscala-js](https://github.com/LukaJCB/rxscala-js). As it is based on Observables, we can implement it more easily
following the videos.

To begin, we have to rewrite the main logic around observables. We can keep the `VirtualDom` implementation for now and 
see how far we get with the new library. First, we need to depend on the new library by changing the dependencies in our
`build.sbt` file using `"com.github.lukajcb" %%% "rxscala-js" % "0.4.0"`.

Another thing we need to do is add the `js-deps` script to our `index-dev.html`, as `rxscala-js` has a "real" JavaScript
dependency. If we don't do that, we get errors in the browser later, even though everything compiles.

### Implementing the main function again

As a first step in the rewrite, we implement the core functions again, namely `Scycle.run`. The first working iteration
is a simple `Observable[_] => Observable[_]`. This looks similar to what we had before, but to get there, we need to use
sub functions to let the compiler be able inferring the correct types. The functions `wireProxyToSink` and `createProxy`
do that.

This is not really the ideal scenario, especially since we still need to cast in the main (`logic`) function in our 
`ScycleApp` to get the correct `Observable`.

The first step to do less type casting is to make the `apply` method of the Driver cast itself to the correct types 
instead of having the user cast it.

## Video 14 - Body Mass Index calculator built in Cycle.js

During this video, we can see how to build a small application in Cycle.js that calculates a BMI. With a few range 
sliders (input fields of type range), we should get a calculated value. The first thing we do is to build structure of 
the resulting DOM tree.

