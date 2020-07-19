package engine.gameState

import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.dynamics.contacts.Contact

class GameContactListener(val gameState: GameState) : ContactListener {

    override fun beginContact(contact: Contact) {
    }

    override fun endContact(contact: Contact) {
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        val bodies = listOf(contact.fixtureA, contact.fixtureB).map { it.body }

        bodies.filter { it.userData is Warhead && !(it.userData as Warhead).freeBodyCallback.isHandled }
            .forEach { warheadBody ->
                val warhead = warheadBody.userData as Warhead
                val otherBody = bodies.find { it != warheadBody }!!

                gameState.activeCallbacks.add { warhead.freeBodyCallback.callback(warhead, otherBody) }
                warhead.freeBodyCallback.isHandled = true
            }

        bodies.filter { it.userData is Vehicle }
            .forEach { (it.userData as Vehicle).hasCollided = true }

    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
    }

}
