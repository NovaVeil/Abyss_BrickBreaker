package org.example;

//父类 brick（抽象类）
public abstract class Brick {
    protected double x, y;
    protected double width, height;
    protected int hp;
    protected BrickShape shape;

    // 砖块形状枚举
    public enum BrickShape {
        RECTANGLE,    // 矩形（默认）
        TRIANGLE      // 三角形
    }

    //：构造方法：创建砖块
    public Brick(double x, double y, int hp) {
        this.x = x;
        this.y = y;
        this.width = GameConstant.BRICK_WIDTH;
        this.height = GameConstant.BRICK_HEIGHT;
        this.hp = hp;
        this.shape = BrickShape.RECTANGLE; // 默认为矩形
    }

    //普通方法：砖块被击中后生命值减一
    public void isHit(){
        hp--;
    }

    //普通方法：砖块被击破
    public boolean isAlive() {
        return hp > 0;
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

    public int getHp() {
        return hp;
    }

    public BrickShape getShape() {
        return shape;
    }

    public void setShape(BrickShape shape) {
        this.shape = shape;
    }
}
