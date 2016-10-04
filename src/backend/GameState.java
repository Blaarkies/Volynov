package backend;

import backend.FreeBody;
import backend.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class GameState {

    public List<Vehicle> players = new ArrayList<>();
    public List<FreeBody> planets = new ArrayList<>();
    // planets = particles(worlds, asteroids, sun, warheads) for now

    public void addPlayer() {
        players.add(new Vehicle());
    }

    public void addPlanet() {
        planets.add(new FreeBody());
    }
}
