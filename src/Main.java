import backend.GameState;
import frontend.Animator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {

        GameState gameState = new GameState();
        Animator img = new Animator(gameState);

        timer(gameState);

        gameState.addPlayer(100,100,0, 0, 0.3,0, "1");
        gameState.addPlayer(305,300,0, 0,-0.3,0, "2");

        gameState.addPlanet(250, 50, 0,  0.3,0,1, "A");
        gameState.addPlanet(250, 450,0, -0.3,0,-0.5, "B");
    }

    public static void timer(final GameState gameState) {
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                gameState.tickPositionChanges();
                gameState.tickVelocityChanges();
            }

        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }
}

