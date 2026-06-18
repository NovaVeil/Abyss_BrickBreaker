package org.example;

import javafx.scene.paint.Color;

public class Baffle {
    public double x,y;
    public double width;
    public double height;
    public double speed;
    private Color color;

    // 构造方法1:完全自定义挡板属性
    public Baffle(double x, double y, double width, double height, double speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.color = Color.web("#BD10E0");
    }

    // 构造方法2:使用默认常量创建标准挡板
    public Baffle(double x, double y) {
        this(x, y, GameConstant.BAFFLE_WIDTH,
                GameConstant.BAFFLE_HEIGHT,
                GameConstant.BAFFLE_SPEED);
    }

    // 构造方法3:根据关卡难度调整挡板大小
    public Baffle(double x, double y, int level) {
        this.x = x;
        this.y = y;
        this.height = GameConstant.BAFFLE_HEIGHT;  // 高度固定
        this.speed = GameConstant.BAFFLE_SPEED;     // 速度固定

        // 根据关卡难度调整挡板宽度(关卡越高,挡板越窄)
        if (level <= 3) {
            this.width = GameConstant.BAFFLE_WIDTH * 1.5;
        } else if (level <= 6) {
            this.width = GameConstant.BAFFLE_WIDTH;
        } else {
            this.width = GameConstant.BAFFLE_WIDTH * 0.7;
        }

        this.color = getLevelColor(level);
    }

    //挡板左右移动
    public void moveLeft() {
        if (x > 0) x -= speed;
    }

    public void moveRight() {
        if (x + width < GameConstant.GAME_WIDTH)
            x += speed;
    }

    //用鼠标移动挡板（仅水平移动）
    public void moveTo(double newX, double newY) {
        this.x = newX;
        
        // 边界检查 - 左边界
        if (this.x < 0) {
            this.x = 0;
        }
        // 右边界
        if (this.x + this.width > GameConstant.GAME_WIDTH) {
            this.x = GameConstant.GAME_WIDTH - this.width;
        }
    }

    // 仅更新X坐标（兼容旧代码）
    public void moveTo(double newX) {
        moveTo(newX, this.y);
    }

    // 根据关卡获取挡板颜色
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

    //Getter方法，给其他类访问属性
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
