# scalajs-fun

Trying out ScalaJS.

I'll try to port CycleJS to ScalaJS in order to learn both of it.

The egghead course looks like a good way to start 🤔
https://egghead.io/courses/cycle-js-fundamentals

## Used libraries

If you want to use a library with Scala.js, you need to use one that is exported to
`<group>.<identifier>_sjs<version>` (just like Scala `_2.10` or `_2.11`). RxScala is currently not supporting this,
according to this issue: https://github.com/ReactiveX/RxScala/issues/161

Therefore, I went with [scala.rx](https://github.com/lihaoyi/scala.rx). It is not really the same as RxScala, but you
can have reactive variables with it. It has a different API, but may be working in a similar way.

## Video 01

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
