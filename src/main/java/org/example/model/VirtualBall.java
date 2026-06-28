package org.example.model;

import javafx.scene.paint.Color;
import org.example.util.GameConstant;

public class VirtualBall {
    private double x, y;
    private final int radius;
    private Color color;
    private boolean isActive;

    public VirtualBall(double x, double y) {
        this.x = x;
        this.y = y;
        this.radius = GameConstant.BALL_RADIUS;
        this.color = Color.web("#FF6B9D", 0.5);
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
