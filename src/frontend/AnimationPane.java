package frontend;

import backend.FreeBody;
import backend.GameState;
import backend.Vehicle;
import backend.motion.PositionDouble;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class AnimationPane extends JPanel {

    private BufferedImage canvasBackground = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage canvasVehicles = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage canvasPlanets = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage canvasFrontend = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage canvasDisplay = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);

    private GameState gameState;

    public AnimationPane(GameState gameState) {
        this.gameState = gameState;

        Graphics2D graphicsBackground = canvasBackground.createGraphics();
        fillCanvas(graphicsBackground, Color.BLACK, 500, 500);
        fillStars(graphicsBackground, 150, 500, 500);
        graphicsBackground.dispose();

        Timer timer = new Timer(133, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paintTheUniverse();
                repaint();
            }

        });
        timer.setRepeats(true);
        timer.setCoalesce(true);
        timer.start();
    }

    private void paintTheUniverse() {
        Graphics2D graphicsPlanets = canvasPlanets.createGraphics();
        graphicsPlanets.setBackground(new Color(0,0,0,0));
        graphicsPlanets.clearRect(0,0,500,500);
        paintPlanets(graphicsPlanets);

        Graphics2D graphicsVehicles = canvasVehicles.createGraphics();
        graphicsVehicles.setBackground(new Color(0,0,0,0));
        graphicsVehicles.clearRect(0,0,500,500);
        paintVehicles(graphicsVehicles);

        Graphics2D graphicsDisplay = canvasDisplay.createGraphics();
        graphicsDisplay.drawImage(canvasBackground, 0, 0, null);
        graphicsDisplay.drawImage(canvasPlanets, 0, 0, null);
        graphicsDisplay.drawImage(canvasVehicles, 0, 0, null);
    }

    private <T extends FreeBody> void paintElements(Graphics2D graphics, List<T> list) {
        for (T element : list) {
            PositionDouble positionDouble = element.motion.getPosition();
            int x = (int) positionDouble.x;
            int y = (int) positionDouble.y;

            Paint paint = Color.RED;

            graphics.setPaint(paint);

            graphics.drawOval(x - 5, y - 5, 10, 10);
        }
    }

    private void paintVehicles(Graphics2D graphicsInput) {
        Graphics2D graphics = (Graphics2D)graphicsInput.create();
        paintElements(graphics, gameState.players);

        graphics.dispose();
    }

    private void paintPlanets(Graphics2D graphicsInput) {
        Graphics2D graphics = (Graphics2D)graphicsInput.create();
        paintElements(graphics, gameState.planets);

        graphics.dispose();
    }

    private void fillStars(Graphics2D graphicsInput, int totalStars, int width, int height) {
        Graphics2D graphics = (Graphics2D)graphicsInput.create();

        Color starColor = new Color(255,250,248);

        for (int starCount = 0; starCount < totalStars; starCount++) {
            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);

            int starSize = (int)(Math.random()*4+1);
            float starSizeF = (float)starSize;
            float[] fractions = new float[]{0.0f, 1.0f};
            Color[] colors = new Color[]{starColor, new Color(Color.TRANSLUCENT)};

            Paint paint = new RadialGradientPaint(x, y, starSizeF, fractions, colors);
            graphics.setPaint(paint);
            graphics.fillOval(x-starSize/2,y-starSize/2,starSize,starSize);

            fractions = new float[]{0.3f, 1.0f};
            paint = new RadialGradientPaint(x, y, starSizeF, fractions, colors);
            graphics.setPaint(paint);
            graphics.drawLine(x - starSize, y, x + starSize, y);
            graphics.drawLine(x, y - starSize, x, y + starSize);
        }
        graphics.dispose();
    }

    private void fillCanvas(Graphics2D graphicsInput, Color color, int width, int height) {
        Graphics2D graphics = (Graphics2D)graphicsInput.create();

        Paint paint = color;
        graphics.setPaint(paint);

        graphics.fillRect(0,0,width,height);
        graphics.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return canvasDisplay == null ?
                super.getPreferredSize() :
                new Dimension(canvasDisplay.getWidth(), canvasDisplay.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvasDisplay, 0, 0, this);
    }
}