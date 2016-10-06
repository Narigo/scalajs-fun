package com.campudus.scycle

import org.scalatest.FunSpec

class ScycleTest extends FunSpec {

  describe("Scycle") {
    it("works with empty maps") {
      Scycle.run(Map.empty, Map.empty)
    }
  }

}
