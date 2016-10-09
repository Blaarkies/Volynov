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
import java.util.Iterator;
import java.util.List;

public class AnimationPane extends JPanel {

    private GameState gameState;

    private BufferedImage canvasBackground;
    private BufferedImage canvasTrailers;
    private BufferedImage canvasVehicles;
    private BufferedImage canvasPlanets;
    private BufferedImage canvasFrontend;
    private BufferedImage canvasDisplay;

    public AnimationPane(GameState gameState, int displayWidth, int displayHeight) {
        this.gameState = gameState;

        this.canvasBackground = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
        this.canvasTrailers = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
        this.canvasVehicles = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
        this.canvasPlanets = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
        this.canvasFrontend = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);
        this.canvasDisplay = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphicsBackground = canvasBackground.createGraphics();
        fillCanvas(graphicsBackground, Color.BLACK, displayWidth, displayHeight);
        fillStars(graphicsBackground, 150, displayWidth, displayHeight);
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
        Graphics2D graphicsTrailers = clearCanvas(canvasTrailers);
        paintTrailers(graphicsTrailers);

        Graphics2D graphicsPlanets = clearCanvas(canvasPlanets);
        paintPlanets(graphicsPlanets);

        Graphics2D graphicsVehicles = clearCanvas(canvasVehicles);
        paintVehicles(graphicsVehicles);

        Graphics2D graphicsDisplay = clearCanvas(canvasDisplay);
        graphicsDisplay.drawImage(canvasBackground, 0, 0, null);
        graphicsDisplay.drawImage(canvasTrailers, 0, 0, null);
        graphicsDisplay.drawImage(canvasPlanets, 0, 0, null);
        graphicsDisplay.drawImage(canvasVehicles, 0, 0, null);
    }

    private Graphics2D clearCanvas(BufferedImage canvas) {
        Graphics2D graphics = canvas.createGraphics();
        graphics.setBackground(new Color(0, 0, 0, 0)); // todo: should this be created every time?
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        return graphics;
    }

    private <T extends FreeBody> void paintFreeBodies(Graphics2D graphics, List<T> list, Color paint) {
        for (T element : list) {
            graphics.setPaint(paint); // todo: label drawing should be separated function

            int x = (int)element.motion.position.x;
            int y = (int)element.motion.position.y;
            int size = (int)element.radius;

            int scale = (int)(100*Math.log(100000*element.motion.acceleration.getMagnitude()+1));
            int ddx = (int)(element.motion.acceleration.ddx*scale);
            int ddy = (int)(element.motion.acceleration.ddy*scale);

            graphics.fillOval(x - size, y - size, size*2, size*2);

            graphics.setPaint(Color.CYAN);
            graphics.drawString(element.id, x-3, y+5);

            graphics.drawLine(x, y, x+ddx, y+ddy);
            graphics.drawString("●", x-5+ddx, y+5+ddy);
        }
    }

    private <T extends FreeBody> void paintTrailers(Graphics2D graphics, List<T> list, Color paint, float intensity) {
        for (T element : list) {
            double size = (double)element.motion.trailers.size();

            Trailer trailerPrev = null;
            Iterator<Trailer> iterator = element.motion.trailers.iterator();
            for (int index=0;iterator.hasNext();index++) {
                Trailer trailerNext = iterator.next();
                if (trailerPrev == null) {
                    trailerPrev = trailerNext;
                    continue;
                }

                int xPrev = (int)trailerPrev.position.x;
                int yPrev = (int)trailerPrev.position.y;
                int xNext = (int)trailerNext.position.x;
                int yNext = (int)trailerNext.position.y;

                Color fadedPaint = new Color(
                        paint.getRed(),
                        paint.getGreen(),
                        paint.getBlue(),
                        (int) (index * ((255*intensity) / size))
                );
                graphics.setPaint(fadedPaint);
                graphics.drawLine(xPrev, yPrev, xNext, yNext);

                trailerPrev = trailerNext;
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
        paintTrailers(graphics, gameState.players, Color.RED, 0.3f);
        paintTrailers(graphics, gameState.planets, Color.BLUE, 0.3f);
        graphics.dispose();
    }

    private void fillStars(Graphics2D graphicsInput, int totalStars, int width, int height) {
        Graphics2D graphics = (Graphics2D)graphicsInput.create();

        Color starColor = new Color(255,250,248);
        Color transparent = new Color(0, 0, 0, 0); // Color.TRANSLUCENT behaves unexpected

        for (int starCount = 0; starCount < totalStars; starCount++) {
            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);

            int starSize = (int)(Math.random()*4+1);
            float starSizeF = (float)starSize;

            float[] fractions = new float[]{0.0f, 1.0f};
            Color[] colors = new Color[]{starColor, transparent};
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

        graphics.setPaint(color);
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