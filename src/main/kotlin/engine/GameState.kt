package engine

import engine.utilities.Utilities.joinLists
import engine.motion.ContactEvent
import engine.motion.Force
import engine.physics.Contact
import engine.physics.Gravity

class GameState {

    var paused = false

    var vehicles = mutableListOf<Vehicle>()
    var planets = mutableListOf<Planet>()

//    private var worlds = mutableListOf<Planet>()
//    private var asteroids = mutableListOf<Planet>()
//    private var stars = mutableListOf<Planet>()
//    private var warheads = mutableListOf<Planet>()

    private val locationTickables
        get() = vehicles + planets

    fun addPlayer(
        x: Double, y: Double, h: Double,
        dx: Double, dy: Double, dh: Double,
        id: String
    ) {
        vehicles.add(
            Vehicle("Player $id", x, y, h, dx, dy, dh)
        )
    }

    fun addPlanet(
        x: Double, y: Double, h: Double,
        dx: Double, dy: Double, dh: Double,
        id: String,
        temperature: Double, radius: Double, mass: Double
    ) {
        planets.add(
            Planet("Planet $id", x, y, h, dx, dy, dh, temperature, mass, radius)
        )
    }

    private fun tickLocationChanges() {
        locationTickables.forEach { it.motion.updateLocationChanges() }
    }

    private fun tickVelocityChanges() {
        locationTickables.forEach { it.motion.updateVelocityChanges() }
    }

    private fun tickGravityChanges() {
        joinLists(locationTickables, locationTickables)
            .filter { (it1, it2) -> it1 != it2 }
            .forEach { (server, client) ->
                client.motion.acceleration.addAcceleration(
                    Gravity.gravitationalForce(server, client),
                    client.mass
                )
            }

/*        //        Vehicles
        vehicles.forEach { vehicle ->
            val forceSum = Force()
            planets.forEach { planet -> forceSum.addForce(Gravity.gravitationalForce(planet, vehicle)) }
            vehicle.motion.acceleration.addAcceleration(forceSum, vehicle.mass)
        }

        //        Planets
        planets.forEach { planet ->
            val forceSum = Force()
            planets.filter { it.hashCode() != planet.hashCode() }
                .forEach { otherPlanet -> forceSum.addForce(Gravity.gravitationalForce(planet, otherPlanet)) }
            planet.motion.acceleration.addAcceleration(forceSum, planet.mass)
        }*/
    }

    private fun tickContactChanges() {
        val accelerationRecords = mutableListOf<AccelerationRecord>()

        joinLists(locationTickables, locationTickables)
            .filter { (it1, it2) -> it1 != it2 && it1.getDistance(it2) <= it1.radius + it2.radius }
            .forEach { (it1, it2) ->
                accelerationRecords.add(
                    AccelerationRecord(it1, calculateContactForces(it1, it2))
                )
            }

        accelerationRecords.forEach {
            it.motion.acceleration.addAcceleration(it.acceleration)
        }

        /*vehicles.forEach { vehicle ->
            val forceSum = Force()
            planets.filter { it.getDistance(vehicle) <= it.radius + vehicle.radius }
                .forEach { planet -> forceSum.addForce(calculateContactForces(planet, vehicle)) }

            vehicles.filter { it.hashCode() != vehicle.hashCode() && it.getDistance(vehicle) <= it.radius + vehicle.radius }
                .forEach { otherVehicle -> forceSum.addForce(calculateContactForces(otherVehicle, vehicle)) }

            accelerationRecords.add(AccelerationRecord(vehicle.motion, forceSum, vehicle.mass))
        }
        addRecordsToAcceleration(accelerationRecords)

        accelerationRecords.clear()
        planets.forEach { planet ->
            val forceSum = Force()
            planets.filter { it.hashCode() != planet.hashCode() && it.getDistance(planet) <= it.radius + planet.radius }
                .forEach { otherPlanet -> forceSum.addForce(calculateContactForces(otherPlanet, planet)) }

            accelerationRecords.add(AccelerationRecord(planet.motion, forceSum, planet.mass))
        }
        addRecordsToAcceleration(accelerationRecords)*/
    }

    private fun tickFrictionChanges() {
    }

    private fun calculateContactForces(server: FreeBody, client: FreeBody): Force {
        val Fn = Contact.contactNormalForce(server, client)
        client.motion.contactEvents.add(ContactEvent(Fn, server))

        return Force()
    }

    fun tickClock() {
        if (!paused) {
            tickLocationChanges()
            tickGravityChanges()
//            tickContactChanges()
            tickFrictionChanges()
            tickVelocityChanges()
        }
    }
}
