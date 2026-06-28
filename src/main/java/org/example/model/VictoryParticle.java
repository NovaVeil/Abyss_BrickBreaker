package org.example.model;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class VictoryParticle {
    private double x, y;
    private double vx, vy;
    private double size;
    private Color color;
    private double alpha;
    private double rotation;
    private double rotationSpeed;
    private ParticleType type;

    public enum ParticleType {
        STAR, SQUARE, CIRCLE, CONFETTI
    }

    private static final Random random = new Random();
    private static final Color[] CELEBRATION_COLORS = {
        Color.web("#FFD93D"),
        Color.web("#FF6B9D"),
        Color.web("#00FF88"),
        Color.web("#4A90E2"),
        Color.web("#FFA500"),
        Color.web("#FF1493")
    };

    public VictoryParticle(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = (random.nextDouble() - 0.5) * 200;
        this.vy = random.nextDouble() * -300 - 100;
        this.size = random.nextDouble() * 8 + 4;
        this.color = CELEBRATION_COLORS[random.nextInt(CELEBRATION_COLORS.length)];
        this.alpha = 1.0;
        this.rotation = random.nextDouble() * 360;
        this.rotationSpeed = (random.nextDouble() - 0.5) * 360;
        this.type = ParticleType.values()[random.nextInt(ParticleType.values().length)];
    }

    public void update(double deltaTime) {
        x += vx * deltaTime;
        y += vy * deltaTime;
        vy += 150 * deltaTime;
        rotation += rotationSpeed * deltaTime;
        alpha -= 0.3 * deltaTime;

        if (alpha < 0) alpha = 0;
    }

    public void render(javafx.scene.canvas.GraphicsContext gc) {
        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.setFill(color);
        gc.translate(x, y);
        gc.rotate(rotation);

        switch (type) {
            case STAR:
                drawStar(gc);
                break;
            case SQUARE:
                gc.fillRect(-size/2, -size/2, size, size);
                break;
            case CIRCLE:
                gc.fillOval(-size/2, -size/2, size, size);
                break;
            case CONFETTI:
                gc.fillRect(-size/2, -size/4, size, size/2);
                break;
        }

        gc.restore();
    }

    private void drawStar(javafx.scene.canvas.GraphicsContext gc) {
        double innerRadius = size / 2;
        double outerRadius = size;

        gc.beginPath();
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(i * 72 - 18);
            double x1 = Math.cos(angle) * outerRadius;
            double y1 = Math.sin(angle) * outerRadius;

            angle = Math.toRadians(i * 72 + 18);
            double x2 = Math.cos(angle) * innerRadius;
            double y2 = Math.sin(angle) * innerRadius;

            if (i == 0) {
                gc.moveTo(x1, y1);
            } else {
                gc.lineTo(x1, y1);
            }
            gc.lineTo(x2, y2);
        }
        gc.closePath();
        gc.fill();
    }

    public boolean isAlive() {
        return alpha > 0;
    }
}
