package backend;

import com.sun.javafx.geom.Vec2d;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    public List<Vehicle> players = new ArrayList<>();
    public List<FreeBody> planets = new ArrayList<>();
    // planets = particles(worlds, asteroids, sun, warheads) for now

    public void addPlayer(double x, double y, double h, double dx, double dy, double dh,
                          String id) {
        players.add(
                new Vehicle("Player 1", 100, 800, 0, 0,
                        new FreeBody(1, 325, 10,
                                x, y, h, dx, dy, dh,
                                id
                        )
                )
        );
    }

    public void addPlanet(double x, double y, double h, double dx, double dy, double dh,
                          String id) {
        planets.add(
            new FreeBody(
                    1000,
                    325,
                    30,
                    x, y, h, dx, dy, dh,
                    id
            )
        );
    }

    public void tickPositionChanges() {
        for (Vehicle vehicle: players) {
            vehicle.motion.updatePositionChanges();
        }

        for (FreeBody planet: planets) {
            planet.motion.updatePositionChanges();
        }
    }

    public void tickVelocityChanges() {
        for (Vehicle vehicle: players) {

            double xF = 0; // Force in x direction
            double yF = 0;
            for (FreeBody planet: planets) {
                Vec2d f = planet.gravityForce(vehicle);
                xF += f.x;
                yF += f.y;
            }

            vehicle.motion.velocity.addToVelocity(xF/vehicle.mass,yF/vehicle.mass, 0);
        }



        for (FreeBody planet: planets) {

            double xF = 0; // Force in x direction
            double yF = 0;
            for (FreeBody otherPlanet: planets) {
                if (!otherPlanet.id.equals(planet.id)) {
                    Vec2d f = otherPlanet.gravityForce(planet);
                    xF += f.x;
                    yF += f.y;
                }
            }

            planet.motion.velocity.addToVelocity(xF/planet.mass,yF/planet.mass, 0);
        }
    }
}
