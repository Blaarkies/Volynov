package frontend;

import backend.FreeBody;
import backend.GameState;
import backend.motion.PositionDouble;
import backend.motion.Trailer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

public class AnimationPane extends JPanel {

    private BufferedImage canvasBackground = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage canvasTrailers = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
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

        Timer timer = new Timer(33, new ActionListener() {
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
        Graphics2D graphicsTrailers = canvasTrailers.createGraphics();
        graphicsTrailers.setBackground(new Color(0,0,0,0));
        graphicsTrailers.clearRect(0,0,500,500);
        paintTrailers(graphicsTrailers);

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
        graphicsDisplay.drawImage(canvasTrailers, 0, 0, null);
        graphicsDisplay.drawImage(canvasPlanets, 0, 0, null);
        graphicsDisplay.drawImage(canvasVehicles, 0, 0, null);
    }

    private <T extends FreeBody> void paintFreeBodies(Graphics2D graphics, List<T> list, Color paint) {
        for (T element : list) {
            graphics.setPaint(paint);

            PositionDouble elementPosition = element.motion.getPosition();
            int x = (int)elementPosition.x;
            int y = (int)elementPosition.y;
            int size = (int)element.radius;

            graphics.fillOval(x - size, y - size, size*2, size*2);

            graphics.setPaint(Color.CYAN);
            graphics.drawString(element.id, x, y);
        }
    }

    private <T extends FreeBody> void paintTrailers(Graphics2D graphics, List<T> list, Color paint) {
        for (T element : list) {

            int x = (int)element.motion.trailers.get(0).position.x;
            int y = (int)element.motion.trailers.get(0).position.y;
            double size = (double)element.motion.trailers.size();
            int index = 0;
            for (Trailer trailer : element.motion.trailers) {

                Color fadedPaint = new Color(
                        paint.getRed(),
                        paint.getGreen(),
                        paint.getBlue(),
                        (int)(index*(255/size))
                );

                graphics.setPaint(fadedPaint);
                graphics.drawLine(x, y, (int)trailer.position.x, (int)trailer.position.y);
                x = (int)trailer.position.x;
                y = (int)trailer.position.y;
                index++;
            }
        }
    }

    private void paintVehicles(Graphics2D graphicsInput) {
        Graphics2D graphics = (Graphics2D)graphicsInput.create();
        paintFreeBodies(graphics, gameState.players, Color.RED.darker());

        graphics.dispose();
    }

    private void paintPlanets(Graphics2D graphicsInput) {
        Graphics2D graphics = (Graphics2D)graphicsInput.create();
        paintFreeBodies(graphics, gameState.planets, Color.BLUE.darker());

        graphics.dispose();
    }

    private void paintTrailers(Graphics2D graphicsInput) {
        Graphics2D graphics = (Graphics2D)graphicsInput.create();
        paintTrailers(graphics, gameState.players, Color.YELLOW);
        paintTrailers(graphics, gameState.planets, Color.YELLOW.darker());

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