package backend;

import backend.motion.Acceleration;
import backend.physics.Contact;
import backend.physics.Gravity;
import com.sun.javafx.geom.Vec2d;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    public List<Vehicle> players = new ArrayList<>();
    public List<FreeBody> planets = new ArrayList<>();
    // planets = particles(worlds, asteroids, sun, warheads) for now

    public void addPlayer(double x, double y, double h,
                          double dx, double dy, double dh,
                          String id, int trailersPopulation) {
        players.add(
                new Vehicle("Player " + id, 100, 800, 0, 0,
                        new FreeBody(1, 325, 10,
                                x, y, h, dx, dy, dh,
                                id, trailersPopulation
                        )
                )
        );
    }

    public void addPlanet(double x, double y, double h,
                          double dx, double dy, double dh,
                          String id, int trailersPopulation, int radius, int mass) {
        planets.add(
                new FreeBody(
                        mass,
                        325,
                        radius,
                        x, y, h, dx, dy, dh,
                        id, trailersPopulation
                )
        );
    }

    //    need some iterator function, that cycles through every FreeBody in gameState?
    public void tickPositionChanges() {
        for (Vehicle vehicle : players) {
            vehicle.motion.updatePositionChanges();
        }

        for (FreeBody planet : planets) {
            planet.motion.updatePositionChanges();
        }
    }

    //    need some iterator function, that cycles through every FreeBody in gameState?
    public void tickAccelerationChanges() {
//        Vehicles
        for (Vehicle vehicle : players) {
            double xF = 0;
            double yF = 0;
            for (FreeBody planet : planets) {
                Vec2d f = Gravity.gravitationalForce(planet, vehicle);
                xF += f.x;
                yF += f.y;
            }
            vehicle.motion.acceleration.setAcceleration(
                    xF / vehicle.mass,
                    yF / vehicle.mass,
                    0);
        }

//        Planets
        for (FreeBody planet : planets) {
            double xF = 0;
            double yF = 0;
            for (FreeBody otherPlanet : planets) {
                if (otherPlanet.hashCode() != planet.hashCode()) {
                    Vec2d f = Gravity.gravitationalForce(otherPlanet, planet);
                    xF += f.x;
                    yF += f.y;
                }
            }
            planet.motion.acceleration.setAcceleration(
                    xF / planet.mass,
                    yF / planet.mass,
                    0);
        }
    }

    public void tickContactChanges() {
        List<AccelerationRecord> accelerationRecords = new ArrayList<>();
        for (Vehicle vehicle : players) {
            double xF = 0;
            double yF = 0;

            for (FreeBody planet : planets) {

                double distanceBetweenEntities = planet.motion.position.distance(vehicle.motion.position);
                if (distanceBetweenEntities <= planet.radius + vehicle.radius) {
                    Vec2d f = Contact.contactNormalForce(planet, vehicle);
                    xF += f.x;
                    yF += f.y;
                }
            }

            for (Vehicle otherVehicle : players) {

                if (otherVehicle.hashCode() != vehicle.hashCode()) {
                    double distanceBetweenEntities = otherVehicle.motion.position.distance(vehicle.motion.position);
                    if (distanceBetweenEntities <= otherVehicle.radius + vehicle.radius) {
                        Vec2d f = Contact.contactNormalForce(otherVehicle, vehicle);
                        xF += f.x;
                        yF += f.y;
                    }
                }
            }

            accelerationRecords.add(new AccelerationRecord(
                    vehicle.motion,
                    new Acceleration(
                            xF / vehicle.mass,
                            yF / vehicle.mass,
                            0))
            );
        }
        for (AccelerationRecord item : accelerationRecords) { // TODO: extract method
            item.motion.acceleration.addToAcceleration(
                    item.accelerationToAdd.ddx,
                    item.accelerationToAdd.ddy,
                    item.accelerationToAdd.ddh);
        }

        accelerationRecords = new ArrayList<>();
        for (FreeBody planet : planets) {
            double xF = 0;
            double yF = 0;

            for (FreeBody otherPlanet : planets) {
                if (otherPlanet.hashCode() != planet.hashCode()) {
                    double distanceBetweenEntities = otherPlanet.motion.position.distance(planet.motion.position);
                    if (distanceBetweenEntities <= otherPlanet.radius + planet.radius) {
                        Vec2d f = Contact.contactNormalForce(otherPlanet, planet);
                        xF += f.x;
                        yF += f.y;
                    }
                }
            }

            accelerationRecords.add(new AccelerationRecord(
                    planet.motion,
                    new Acceleration(
                            xF / planet.mass,
                            yF / planet.mass,
                            0))
            );
        }
        for (AccelerationRecord item : accelerationRecords) {
            item.motion.acceleration.addToAcceleration(
                    item.accelerationToAdd.ddx,
                    item.accelerationToAdd.ddy,
                    item.accelerationToAdd.ddh);
        }
    }

    public void tickFrictionChanges() {
    }

    //    need some iterator function, that cycles through every FreeBody in gameState?
    public void tickVelocityChanges() {
        for (Vehicle vehicle : players) {
            vehicle.motion.updateVelocityChanges();
        }

        for (FreeBody planet : planets) {
            planet.motion.updateVelocityChanges();
        }
    }
}
