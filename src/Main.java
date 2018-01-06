import backend.GameState;
import frontend.Animator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        GameState gameState = new GameState();
        Animator display = new Animator(gameState, 800, 600);

        gameState.addPlayer(300+120, 300, 0, 0, -1.5, 0.1, "1", 20);
        gameState.addPlayer(300, 300-120, 0, -1.5, 0, 0.1, "2", 20);
        gameState.addPlayer(300-120, 300, 0, 0, 1.5, 0.1, "3", 20);
        gameState.addPlayer(300, 300+120, 0, 1.5, 0, 0.1, "4", 20);

        gameState.addPlanet(300, 300, 0, 0, 0, -0.01, "A", 20, 100, 5000);


        timer(gameState);
    }

    private static int fps = 60;
    private static int msPerFrame = 1000 / fps;

    public static void timer(final GameState gameState) {
        Timer timer = new Timer(msPerFrame, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameState.tickPositionChanges();

                gameState.tickGravityChanges();
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

