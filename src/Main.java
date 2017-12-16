import backend.GameState;
import frontend.Animator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        GameState gameState = new GameState();
        Animator display = new Animator(gameState, 800, 600);

        gameState.addPlayer(700, 0, 0, 0.25, 0.6, 0.1, "1", 500);
        gameState.addPlayer(700, 400, 0, 0.15, -0.2, 0.2, "2", 500);
        gameState.addPlayer(200, 200, 0, 0.5, -0.7, -0.05, "3", 500);

//        gameState.addPlayer(200, 200, 0, 0, 0.1, 1, "D", 500);
//        gameState.addPlayer(580, 200, 0, 0, -1.4, 0, "2", 500);
//        gameState.addPlayer(225, 0, 0, 0, 1.3, 1, "3", 500);

        gameState.addPlanet(300, 200, 0, 0, 0.4, -0.01, "A", 1000, 20, 400);
        gameState.addPlanet(500, 200, 0, 0, -0.4, 0.04, "B", 1000, 30, 1000);
//        gameState.addPlanet(500, 200, 0, 0, -0.3, -0.1, "B", 1000, 30, 1000);

        timer(gameState);
    }

    public static void timer(final GameState gameState) {
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameState.tickPositionChanges();

                gameState.tickAccelerationChanges();
                gameState.tickContactChanges();
                gameState.tickFrictionChanges();

                gameState.tickVelocityChanges();
            }

        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }
}

