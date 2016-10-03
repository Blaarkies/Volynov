import backend.GameState;
import backend.Vehicle;
import backend.motion.PositionDouble;
import frontend.AnimatedBoat;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {
        GameState gameState = new GameState();
        AnimatedBoat img = new AnimatedBoat(gameState);

        timer(gameState);
//        img.animationPane.drawNoise(0,0,100,100);

//        DirectDrawDemo frontendWindow = new DirectDrawDemo(1000, 1000);

        gameState.addPlayer();
        System.out.println(gameState.players.get(0).name);

//        frontendWindow.startup();
//        frontendWindow.makeWindow();
//        frontendWindow.drawNoise(250,250,500,500);

    }

    public static void timer(final GameState gameState) {
        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Vehicle player: gameState.players) {
                    player.motion.postition.x++;
                    player.motion.postition.y++;
                }
            }

        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }
}

