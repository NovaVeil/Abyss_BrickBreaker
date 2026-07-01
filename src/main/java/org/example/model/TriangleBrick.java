package org.example.model;

//三角形类砖块
public class TriangleBrick extends Brick {
    public TriangleBrick(double x, double y) {
        super(x, y, 1);
        this.shape = BrickShape.TRIANGLE;
    }

}
