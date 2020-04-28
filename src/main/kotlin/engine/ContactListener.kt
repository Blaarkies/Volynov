package engine

import engine.freeBody.Warhead
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.dynamics.contacts.Contact

class ContactListener(val gameState: GameState) : ContactListener {

    override fun beginContact(contact: Contact) {
        val bodies = listOf(contact.fixtureA, contact.fixtureB).map { it.body }
        bodies.mapNotNull { it.userData }
            .map { it as FreeBodyCallback }
            .filter { it.freeBody is Warhead && !it.isHandled}
            .forEach { warhead ->
                val otherBody = bodies.find { body -> body != warhead.freeBody.worldBody }!!
                gameState.activeCallbacks.add { warhead.callback(warhead.freeBody, otherBody) }
                warhead.isHandled = true
            }

    }

    override fun endContact(contact: Contact) {
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
    }

}
