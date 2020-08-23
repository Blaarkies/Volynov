package engine.gameState

import engine.freeBody.Planet
import engine.freeBody.Vehicle
import engine.freeBody.Warhead
import game.shields.VehicleShield
import org.jbox2d.callbacks.ContactImpulse
import org.jbox2d.callbacks.ContactListener
import org.jbox2d.collision.Manifold
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.contacts.Contact

class GameContactListener(val gameState: GameState) : ContactListener {

    override fun beginContact(contact: Contact) {
    }

    override fun endContact(contact: Contact) {
    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {
        val bodies = listOf(contact.fixtureA, contact.fixtureB).map { it.body }

        when {
            bodies.any { it.userData is Warhead } -> handleWarhead(bodies, contact)
            bodies.any { it.userData is Vehicle } -> handleVehicle(bodies)
            bodies.any { it.userData is Planet } -> handlePlanet(bodies)
        }

    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {
    }

    private fun handleWarhead(bodies: List<Body>,
                              contact: Contact) {
        bodies.filter { (it.userData is Warhead) && !(it.userData as Warhead).freeBodyCallback.isHandled }
            .forEach { warheadBody ->
                val warhead = warheadBody.userData as Warhead
                val otherBody = bodies.find { it != warheadBody }!!

                if (otherBody.userData is VehicleShield) {
                    (otherBody.userData as VehicleShield).hit(warhead, contact)
                } else {
                    gameState.activeCallbacks.add { warhead.freeBodyCallback.callback(warhead, otherBody) }
                    warhead.freeBodyCallback.isHandled = true
                }
            }
    }

    private fun handleVehicle(bodies: List<Body>) {
        bodies.filter { it.userData is Vehicle }
            .forEach { (it.userData as Vehicle).hasCollided = true }
    }

    private fun handlePlanet(bodies: List<Body>) {
        println("when worlds collide!!!")
    }

}
