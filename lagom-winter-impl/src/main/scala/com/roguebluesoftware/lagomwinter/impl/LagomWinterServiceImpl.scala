package com.roguebluesoftware.lagomwinter.impl

import com.roguebluesoftware.lagomwinter.api
import com.roguebluesoftware.lagomwinter.api.LagomWinterService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

/**
  * Implementation of the LagomWinterService.
  */
class LagomWinterServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends LagomWinterService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the Lagom Winter entity for the given ID.
    val ref = persistentEntityRegistry.refFor[LagomWinterEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the Lagom Winter entity for the given ID.
    val ref = persistentEntityRegistry.refFor[LagomWinterEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }


  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(LagomWinterEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[LagomWinterEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
