package org.example;
//三角形类砖块
public class TriangleBrick extends Brick {
    public TriangleBrick(double x, double y) {
        super(x, y, 1);
        this.shape = BrickShape.TRIANGLE;
    }

    public TriangleBrick(double x, double y, int hp) {
        super(x, y, hp);
        this.shape = BrickShape.TRIANGLE;
    }
}
