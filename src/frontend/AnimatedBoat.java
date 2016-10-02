package frontend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AnimatedBoat {

    public AnimationPane animationPane = new AnimationPane();

    public static void main(String[] args) {
        new AnimatedBoat();
    }

    public AnimatedBoat() {
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
//                frame.add(new AnimationPane());
                frame.add(animationPane);

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }

        });
    }

    public class AnimationPane extends JPanel {
        private BufferedImage boat = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);

        public AnimationPane() {
//            try {
//                boat = ImageIO.read(new File("star.png"));
                Timer timer = new Timer(100, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
//                        drawNoise(0, 0, 150, 150);

                        repaint();
                    }

                });
                timer.setRepeats(true);
                timer.setCoalesce(true);
                timer.start();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
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
                            (int)(255 * Math.random()),
                            (int)(255 * Math.random()),
                            (int)(255 * Math.random())
                    );
                    boat.setRGB(x, y, color.getRGB());
                }
            }
//            repaint();
        }

    }

}