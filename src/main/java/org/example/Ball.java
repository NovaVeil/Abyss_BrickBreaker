package org.example;

import javafx.scene.paint.Color;

public class Ball {
    //小球属性
    private double x,y;
    private final int radius;
    private double dx,dy;
    private Color color;
    private int bricksHitcount = 0;  // 记录碰撞的砖块数

    //构造方法:初始化小球位置、半径、速度、颜色
    public Ball(double x, double y, int radius, double dx, double dy, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.dx = dx;
        this.dy = dy;
        this.color = Color.WHITE;
    }

    // 使用默认常量创建小球的构造方法
    public Ball(double x, double y) {
        this(x, y, GameConstant.BALL_RADIUS, 
             GameConstant.BALL_SPEED_X, 
             GameConstant.BALL_SPEED_Y, Color.WHITE);
    }
    public void incrementBricksHit() {
        this.bricksHitcount++;
    }

    public int getBricksHit() {
        return bricksHitcount;
    }
    public void resetBricksHit() {
        this.bricksHitcount = 0;
    }

    //移动方法：更新小球的位置
    public void move() {
        x += dx;
        y += dy;
        checkWallTopleftRight();
    }
    //小球碰左、右、上墙
    private void checkWallTopleftRight() {
        //左墙
        if (x - radius <= 0) {
            x = radius;
            reflectHorizontal();
        }
        //右墙
        if (x + radius >= GameConstant.GAME_WIDTH) {
            x = GameConstant.GAME_WIDTH - radius;
            reflectHorizontal();
        }
        //上墙
        if (y - radius <= 0) {
            y = radius;
            reflectVertical();
        }
    }

    //水平反弹方法（碰左右墙、砖块）
    public void reflectHorizontal() {
        dx = -dx;
    }

    //竖直反弹方法（碰上下墙、挡板、砖块）
    public void reflectVertical() {
        dy = -dy;
    }

    //Getter方法，给其他类访问属性
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    public void setDx(int dx) {
        this.dx = dx;
    }
}
