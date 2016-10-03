package frontend;

import backend.GameState;
import backend.Vehicle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class AnimationPane extends JPanel {

    private BufferedImage boat = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    private GameState gameState;

    public AnimationPane(GameState gameState) {
        this.gameState = gameState;

        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testpaint();
            }

        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }

    private void testpaint() {
        int x = (int)gameState.players.get(0).motion.postition.x;
        int y = (int)gameState.players.get(0).motion.postition.y;
        System.out.println(x);
        drawNoise(x,y,10,10);

        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return boat == null ? super.getPreferredSize() : new Dimension(boat.getWidth(), boat.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(boat, 0, 0, this);
    }

    public void drawNoise(int x1, int y1, int width, int height) {
        for (int x = x1; x < x1 + width; x++) {
            for (int y = y1; y < y1 + height; y++) {
                Color color = new Color(
                        (int) (255 * Math.random()),
                        (int) (255 * Math.random()),
                        (int) (255 * Math.random())
                );
                boat.setRGB(x, y, color.getRGB());
            }
        }
    }

}