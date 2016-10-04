import backend.GameState;
import backend.Vehicle;
import frontend.AnimatedBoat;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        GameState gameState = new GameState();
        AnimatedBoat img = new AnimatedBoat(gameState);

        timer(gameState);

        gameState.addPlayer();
        gameState.addPlayer();

        gameState.addPlanet();
        gameState.addPlanet();
    }

    public static void timer(final GameState gameState) {
        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                for (Vehicle vehicle: gameState.players) {
                    double x = (Math.random()-0.5)*10;
                    double y = (Math.random()-0.5)*10;
                    vehicle.motion.position.addPosition(x, y);
                }
            }

        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }
}

