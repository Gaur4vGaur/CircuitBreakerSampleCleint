package connector

import com.hootsuite.circuitbreaker.listeners.{CircuitBreakerInvocationListener, CircuitBreakerStateChangeListener}
import play.api.Logger

object NotifyHelper {

  //HootSuite
  def defaultLoggingInvocationListener: CircuitBreakerInvocationListener =
    new CircuitBreakerInvocationListener {

      override def onInvocationInFlowState(name: String): Unit =
        Logger.debug(s"----------Circuit breaker \'$name\' invoked in closed/flow state")

      override def onInvocationInBrokenState(name: String): Unit =
        Logger.debug(s"----------Circuit breaker \'$name\' invoked in open/broken state")
    }

  def defaultLoggingStateChangeListener: CircuitBreakerStateChangeListener =
    new CircuitBreakerStateChangeListener {

      override def onInit(name: String): Unit =
        Logger.info(s"----------Initializing circuit breaker \'$name\'")

      override def onTrip(name: String): Unit =
        Logger.warn(s"----------Circuit breaker \'$name\' was TRIPPED; circuit is now open/broken")

      override def onReset(name: String): Unit =
        Logger.warn(s"----------Circuit breaker \'$name\' was RESET; circuit is now closed/flowing")
    }

}
