import backend.GameState;
import frontend.Animator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        GameState gameState = new GameState();
        Animator display = new Animator(gameState, 800, 600);

        gameState.addPlayer(250,100,0, 1.2 ,    0, 1, "1", 500);
        gameState.addPlayer(298,300,0,    0, -0.3, 0, "2", 500);
        gameState.addPlayer(350,480,0, -1.3,    0, 3, "3", 500);
//        gameState.addPlayer(500,600,0, 0,   0,0, "3", 500);

        gameState.addPlanet(250, 50, 0,  -0.3,0,1, "A", 500);
        gameState.addPlanet(250, 450,0, 0.3,0,-0.5, "B", 500);

        timer(gameState);
    }

    public static void timer(final GameState gameState) {
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameState.tickPositionChanges();

                gameState.tickAccelerationChanges();
                gameState.tickCollisionChanges();
                gameState.tickFrictionChanges();

                gameState.tickVelocityChanges();
            }

        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }
}

