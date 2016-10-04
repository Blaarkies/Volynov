package frontend;

import backend.GameState;

import javax.swing.*;
import java.awt.*;

public class AnimatedBoat {

    public AnimationPane animationPane;

    public AnimatedBoat(GameState gameState) {
        this.animationPane = new AnimationPane(gameState);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName()
                    );
                } catch (ClassNotFoundException
                        | InstantiationException
                        | IllegalAccessException
                        | UnsupportedLookAndFeelException ex) {
                }

                JFrame frame = new JFrame("Test");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(animationPane);

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }

        });
    }
}