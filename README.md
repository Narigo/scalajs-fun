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

## First video

As you cannot map with scala.rx but have `Var` and `Rx{}`, the code looks a bit different. Anyways, I think one can see
the intention as it was in the courses video.

