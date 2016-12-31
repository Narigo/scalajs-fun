package com.campudus.scycle.http

import com.campudus.scycle.Scycle.DriverFunction
import rxscalajs._

class HttpDriver extends DriverFunction[Request, UserResponse] {

  override def apply(
    stream: Observable[Request],
    driverName: String
  ): Observable[UserResponse] = {
    stream.map(_ => null)
  }

  val responses: Subject[User] = Subject()
  val lastResponse$: Observable[User] = responses.startWith(null)

  def requestUser(number: Double): Observable[User] = {
    work(Get(s"http://jsonplaceholder.typicode.com/users/$number")).map(user => {
      responses.next(user)
      user
    })
  }

  private def work(request: Request): Observable[User] = {
    Observable
      .ajax(request.url)
      .map(p => {
        val user = p.response
        User(user.username.toString, user.email.toString, user.website.toString)
      })
  }

}
