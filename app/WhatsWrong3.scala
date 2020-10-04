package com.particeep.test

import akka.actor.Actor

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Do you see anything that could lead to potential problems ?
 * What would you do to fix it ?
 * Do not mind about the not implemented code
 */

// The code contains a race condition when we get two messages in succession -
// as the order when the two futures complete is not deterministic. The second
// future might return before the first future. This might give us an
// inconsistent state. The idea behind an actor is that messages are handled
// in the order they are received and that state changes are done synchronously
// - the main guarantee of the actor.

// One way to fix this, is to make the `internalState` a `Future[String]`, if
// that is possible. Then we can actually handleResponse synchronously in the
// actor and the order of mutating the state will stay correct.

// Another way to fix this, would be to use `Stash`
// (https://doc.akka.io/docs/akka/current/actors.html?language=scala#stash).
// Thereby we can stash incoming messages (save messages in a queue) while we
// are waiting for the future to complete and then unstashAll (continue sending
// messages to the actors mailbox in the correct order). But this has also
// drawbacks, because the stash might be bounded, which could lead to an
// overflow of the stash queue. Furthermore, we stop processing messages while
// waiting, which could become a bottleneck.
class WhatsWrong3 extends Actor {

  // There is no reason for having this state public, make it private
  private var internalState = Future.successful("internal state")

  def receive: Receive = {
    case "a query" => {
      val requestF: Future[String] = queryAsyncServer()
      handleResponse(internalState.flatMap { state =>
        requestF.recover { case NonFatal(t) =>
          t.printStackTrace()
          state
        }
      })
    }
  }

  def handleResponse(r: Future[String]) = ??? // mutate internal state

  def queryAsyncServer(): Future[String] = ???
}
