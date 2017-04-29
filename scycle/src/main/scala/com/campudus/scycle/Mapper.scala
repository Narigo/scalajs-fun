package com.campudus.scycle

import com.campudus.scycle.Scycle.{SinkMapper, SourcesMapper}
import rxscalajs.Observable

abstract class Mapper[Source <: Driver[_], Sink] {

  type Key

  implicit val keyToSink: SinkMapper[Key, Observable[Sink]] = new SinkMapper
  implicit val keyToSource: SourcesMapper[Key, Source] = new SourcesMapper

}
