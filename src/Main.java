import backend.GameState;
import frontend.Animator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        GameState gameState = new GameState();
        Animator display = new Animator(gameState, 800, 600);

        gameState.addPlayer(100, 100, 0, 0.8, 0.3, 0.1, "1", 20);
        gameState.addPlayer(200, 118, 0, 0, 1, 0.1, "2", 20);

        gameState.addPlayer(670, 300, 0, 0, -1, 0.2, "3", 20);
        gameState.addPlayer(600, 300, 0, 0, 0.4, 0.2, "4", 20);

        gameState.addPlanet(300, 300, 0, 0, -0.7, -0.01, "A", 20, 30, 1000);
        gameState.addPlanet(500, 300, 0, 0, 0.3, -0.01, "B", 20, 40, 2000);


        timer(gameState);
    }

    private static int fps = 60;
    private static int msPerFrame = 1000 / fps;

    public static void timer(final GameState gameState) {
        Timer timer = new Timer(msPerFrame, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameState.paused) {

                    gameState.tickPositionChanges();

                    gameState.tickGravityChanges();
                    gameState.tickContactChanges();
                    gameState.tickFrictionChanges();

                    gameState.tickVelocityChanges();
                }
            }

        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }
}

