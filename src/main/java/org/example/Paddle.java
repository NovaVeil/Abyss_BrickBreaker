package org.example;

import javafx.scene.paint.Color;

public class Paddle {
    private double x, y;
    private double width, height;
    private double speed;
    private Color color;

    public Paddle(double x, double y) {
        this.x = x;
        this.y = y;
        this.width = GameConstant.BAFFLE_WIDTH;
        this.height = GameConstant.BAFFLE_HEIGHT;
        this.speed = GameConstant.BAFFLe_SPEED;
        this.color = Color.web("#BD10E0");
    }

    public Paddle(double x, double y, int level) {
        this.x = x;
        this.y = y;
        this.height = GameConstant.BAFFLE_HEIGHT;
        this.speed = GameConstant.BAFFLe_SPEED;

        if (level <= 3) {
            this.width = GameConstant.BAFFLE_WIDTH * 1.5;
        } else if (level <= 6) {
            this.width = GameConstant.BAFFLE_WIDTH;
        } else {
            this.width = GameConstant.BAFFLE_WIDTH * 0.7;
        }

        this.color = getLevelColor(level);
    }

    public void moveLeft() {
        if (x > 0) {
            x -= speed;
        }
    }

    public void moveRight() {
        if (x + width < GameConstant.GAME_WIDTH) {
            x += speed;
        }
    }

    public void moveTo(double newX) {
        this.x = newX;
        if (this.x < 0) {
            this.x = 0;
        }
        if (this.x + this.width > GameConstant.GAME_WIDTH) {
            this.x = GameConstant.GAME_WIDTH - this.width;
        }
    }

    private Color getLevelColor(int level) {
        switch (level % 5) {
            case 0: return Color.web("#BD10E0");
            case 1: return Color.web("#00D9FF");
            case 2: return Color.web("#FF6B9D");
            case 3: return Color.web("#FFD93D");
            case 4: return Color.web("#6BCF7F");
            default: return Color.web("#BD10E0");
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getSpeed() {
        return speed;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
