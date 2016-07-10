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
