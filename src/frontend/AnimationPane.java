package frontend;

import backend.FreeBody;
import backend.GameState;
import backend.motion.Trailer;
import com.sun.javafx.geom.Vec3d;

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
        graphics.setBackground(new Color(0, 0, 0, 0));
        // todo: should this be created every time?
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        return graphics;
    }

    private int getValidColourValue(double value) {
        if (value > 255) {
            return 255;
        } else if (value < 0) {
            return 0;
        }
        return (int) value;
    }

    private Vec3d getPersonalityVector(int value) {
        int red = Integer.parseInt(Integer.toString(value).substring(0, 1));
        int green = Integer.parseInt(Integer.toString(value).substring(1, 2));
        int blue = Integer.parseInt(Integer.toString(value).substring(2, 3));
        return new Vec3d(red, green, blue);
    }

    private <T extends FreeBody> void paintFreeBodies(Graphics2D graphics,
                                                      List<T> list,
                                                      Color paint) {
        for (T element : list) {

            int x = (int) element.motion.position.x;
            int y = (int) element.motion.position.y;
            int size = (int) element.radius;

            int scale = (int) (100 * Math.log(100000 * element.motion.acceleration.getMagnitude() + 1));
            int ddx = (int) (element.motion.acceleration.ddx * scale);
            int ddy = (int) (element.motion.acceleration.ddy * scale);

            Vec3d personalityVector = getPersonalityVector(element.hashCode());
            int red = getValidColourValue(paint.getRed() + personalityVector.x * 5 - 45);
            int green = getValidColourValue(paint.getGreen() + personalityVector.y * 5 - 45);
            int blue = getValidColourValue(paint.getBlue() + personalityVector.z * 5 - 45);

            Color variantPaint = new Color(red, green, blue);
            graphics.setPaint(variantPaint);
            graphics.fillOval(x - size, y - size, size * 2, size * 2);

            double h = element.motion.position.h;
            int xh = x + ((int) (element.radius * Math.cos(h)));
            int yh = y + ((int) (element.radius * Math.sin(h)));
            graphics.setPaint(Color.YELLOW);
            graphics.drawLine(x, y, xh, yh);

            graphics.setPaint(Color.DARK_GRAY);
            graphics.drawString("●", x - 5 + ddx, y + 5 + ddy);
            graphics.drawLine(x, y, x + ddx, y + ddy);

            graphics.setPaint(Color.WHITE);
            graphics.drawString(element.id, x - 3, y + 5);
        }
    }

    private <T extends FreeBody> void paintTrailers(Graphics2D graphics,
                                                    List<T> list,
                                                    Color paint,
                                                    float intensity) {
        float maxIntensity = 255 * intensity;

        for (T element : list) {
            double size = (double) element.motion.trailers.size();

            Trailer preTrailer = null;
            Iterator<Trailer> iterator = element.motion.trailers.iterator();
            for (int index = 0; iterator.hasNext(); index++) {

                Trailer nowTrailer = iterator.next();
                if (preTrailer == null) {
                    preTrailer = nowTrailer;
                    continue;
                }

                int xPre = (int) preTrailer.position.x;
                int yPre = (int) preTrailer.position.y;
//                int xNow = (int) nowTrailer.position.x;
//                int yNow = (int) nowTrailer.position.y;

                double h = -preTrailer.position.getDirection(nowTrailer.position) + Math.PI / 2;
                double r = preTrailer.position.getDistance(nowTrailer.position);
                r--;
                int xNow = xPre + ((int) (r * Math.cos(h)));
                int yNow = yPre + ((int) (r * Math.sin(h)));

                Color fadedPaint = new Color(
                        paint.getRed(),
                        paint.getGreen(),
                        paint.getBlue(),
                        (int) (index * (maxIntensity / size) + 32)
                );
                graphics.setPaint(fadedPaint);
                graphics.drawLine(xPre, yPre, xNow, yNow);

                preTrailer = nowTrailer;
            }
        }
    }

    private void paintVehicles(Graphics2D graphicsInput) {
        Graphics2D graphics = (Graphics2D) graphicsInput.create();
        paintFreeBodies(graphics, gameState.players, Color.RED.darker());
        graphics.dispose();
    }

    private void paintPlanets(Graphics2D graphicsInput) {
        Graphics2D graphics = (Graphics2D) graphicsInput.create();
        paintFreeBodies(graphics, gameState.planets, Color.BLUE.darker());
        graphics.dispose();
    }

    private void paintTrailers(Graphics2D graphicsInput) {
        Graphics2D graphics = (Graphics2D) graphicsInput.create();
        paintTrailers(graphics, gameState.players, Color.RED, 0.3f);
        paintTrailers(graphics, gameState.planets, Color.BLUE, 0.3f);
        graphics.dispose();
    }

    private void fillStars(Graphics2D graphicsInput, int totalStars, int width, int height) {
        Graphics2D graphics = (Graphics2D) graphicsInput.create();

        Color starColor = new Color(255, 250, 248);
        Color transparent = new Color(0, 0, 0, 0);
        // Color.TRANSLUCENT behaves unexpected

        for (int starCount = 0; starCount < totalStars; starCount++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            int starSize = (int) (Math.random() * 4 + 1);
            float starSizeF = (float) starSize;

            float[] fractions = new float[]{0.0f, 1.0f};
            Color[] colors = new Color[]{starColor, transparent};
            Paint paint = new RadialGradientPaint(x, y, starSizeF, fractions, colors);
            graphics.setPaint(paint);
            graphics.fillOval(x - starSize / 2, y - starSize / 2, starSize, starSize);

            fractions = new float[]{0.3f, 1.0f};
            paint = new RadialGradientPaint(x, y, starSizeF, fractions, colors);
            graphics.setPaint(paint);
            graphics.drawLine(x - starSize, y, x + starSize, y);
            graphics.drawLine(x, y - starSize, x, y + starSize);
        }
        graphics.dispose();
    }

    private void fillCanvas(Graphics2D graphicsInput,
                            Color color,
                            int width,
                            int height) {
        Graphics2D graphics = (Graphics2D) graphicsInput.create();

        graphics.setPaint(color);
        graphics.fillRect(0, 0, width, height);
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