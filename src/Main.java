import backend.GameState;
import frontend.Animator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        GameState gameState = new GameState();
        Animator display = new Animator(gameState, 800, 600);

        gameState.addPlayer(160, 200, 0, 0, -1.9, 0.1, "1", 20);
        gameState.addPlayer(250, 200, 0, 1.48, 0, 0.2, "2", 20);

        gameState.addPlanet(200, 200, 0, 0, 0, -0.01, "A", 20, 30, 1000);

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

