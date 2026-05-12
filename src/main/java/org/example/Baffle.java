package org.example;

public class Baffle {
    public double x,y;
    public double width;
    public double height;
    public double speed;

    // 构造方法1:完全自定义挡板属性
    public Baffle(double x, double y, double width, double height, double speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    // 构造方法2:使用默认常量创建标准挡板
    public Baffle(double x, double y) {
        this(x, y, GameConstant.BAFFLE_WIDTH,
                GameConstant.BAFFLE_HEIGHT,
                GameConstant.BAFFLe_SPEED);
    }

    // 构造方法3:根据关卡难度调整挡板大小
    public Baffle(double x, double y, int level) {
        this.x = x;
        this.y = y;
        this.height = GameConstant.BAFFLE_HEIGHT;  // 高度固定
        this.speed = GameConstant.BAFFLe_SPEED;     // 速度固定

        // 根据关卡难度调整挡板宽度(关卡越高,挡板越窄)
        if (level <= 3) {
            this.width = GameConstant.BAFFLE_WIDTH * 1.5;
        } else if (level <= 6) {
            this.width = GameConstant.BAFFLE_WIDTH;
        } else {
            this.width = GameConstant.BAFFLE_WIDTH * 0.7;
        }
    }

    //挡板左右移动
    public void moveLeft() {
        if (x > 0) x -= speed;
    }

    public void moveRight() {
        if (x + width < GameConstant.GAME_WIDTH - width)
            x += speed;
    }

    public void moveUp() {
        if (y > 0) y -= speed;
    }

    public void moveDown() {
        if (y + height < GameConstant.GAME_HEIGHT - height)
            y += speed;
    }

    //用鼠标移动（期待实现）
    public void move(String direction) {
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
}
