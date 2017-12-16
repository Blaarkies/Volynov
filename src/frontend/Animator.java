package frontend;

import backend.GameState;

import javax.swing.*;
import java.awt.*;

public class Animator {

    public AnimationPane animationPane;

    public Animator(GameState gameState, int displayWidth, int displayHeight) {
        this.animationPane = new AnimationPane(gameState, displayWidth, displayHeight);
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

                JFrame frame = new JFrame("Volynov");
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