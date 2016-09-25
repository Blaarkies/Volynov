package frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.TimerTask;
import javax.swing.*;

public class DirectDrawDemo extends JPanel {

    private BufferedImage canvas;

    public DirectDrawDemo(int width, int height) {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        fillCanvas(Color.BLACK);
        fillStars(150);
//        drawRect(Color.RED, 0, 0, width/2, height/2);
//        drawNoise(0, 0, width/1, height/1);

//
//        java.util.Timer t = new java.util.Timer();
//        t.schedule(new TimerTask() {
//                       @Override
//                       public void run() {
//                           drawNoise(0, 0, canvas.getWidth(), canvas.getHeight());
//                       }
//                   },
//                17,
//                17);
    }

    public void drawNoise(int x1, int y1, int width, int height) {
        for (int x = x1; x < x1 + width; x++) {
            for (int y = y1; y < y1 + height; y++) {
                Color color = new Color(
                        (int)(255 * Math.random()),
                        (int)(255 * Math.random()),
                        (int)(255 * Math.random())
                );
                canvas.setRGB(x, y, color.getRGB());
            }
        }
        repaint();
    }

    public Dimension getPreferredSize() {
        return new Dimension(canvas.getWidth(), canvas.getHeight());
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
    }

    private void fillCanvas(Color c) {
        int color = c.getRGB();
        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                canvas.setRGB(x, y, color);
            }
        }

        repaint();
    }

    private void fillStars(int totalStars) {
        for (int starCount = 0; starCount < totalStars + 1; starCount++) {
            int x = (int)(Math.random()*(canvas.getWidth()-2) +1);
            int y = (int)(Math.random()*(canvas.getHeight()-2) +1);
            double starBrightnessScale = Math.random()/1.5 + 0.3;

            Color starColor = new Color(
                    (int)(255*starBrightnessScale),
                    (int)(250*starBrightnessScale),
                    (int)(248*starBrightnessScale));
            int starEdge = starColor.darker().getRGB();
            int starCore = starColor.getRGB();
            canvas.setRGB(x, y, starCore);
            canvas.setRGB(x, y+1, starEdge);
            canvas.setRGB(x+1, y, starEdge);
            canvas.setRGB(x, y-1, starEdge);
            canvas.setRGB(x-1, y, starEdge);
        }
        repaint();
    }


//    public void drawLine(Color c, int x1, int y1, int x2, int y2) {
//        // Implement line drawing
//        repaint();
//    }

//    public void drawRect(Color c, int x1, int y1, int width, int height) {
//        int color = c.getRGB();
//        // Implement rectangle drawing
//        for (int x = x1; x < x1 + width; x++) {
//            for (int y = y1; y < y1 + height; y++) {
//                canvas.setRGB(x, y, color);
//            }
//        }
//        repaint();
//    }

//    public void drawOval(Color c, int x1, int y1, int width, int height) {
//        // Implement oval drawing
//        repaint();
//    }

    public void makeWindow() {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        JFrame frame = new JFrame("Direct draw demo");

        DirectDrawDemo panel = new DirectDrawDemo(width, height);

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}

