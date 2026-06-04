package org.example;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private int currentLevel;
    private Color backgroundColor;
    private Color ballColor;
    private Color brickStyle;
    private boolean isRectangularBricks;

    public LevelManager() {
        this.currentLevel = 1;
        initLevelStyle(1);
    }

    public void initLevelStyle(int level) {
        this.currentLevel = level;

        switch (level % 5) {
            case 1:
                backgroundColor = Color.web("#0a0e27");
                ballColor = Color.WHITE;
                brickStyle = Color.web("#4A90E2");
                isRectangularBricks = true;
                break;
            case 2:
                backgroundColor = Color.web("#1a0f2e");
                ballColor = Color.web("#FFD700");
                brickStyle = Color.web("#FF6B9D");
                isRectangularBricks = false;
                break;
            case 3:
                backgroundColor = Color.web("#0d1b2a");
                ballColor = Color.web("#00FF88");
                brickStyle = Color.web("#F5A623");
                isRectangularBricks = true;
                break;
            case 4:
                backgroundColor = Color.web("#2d132c");
                ballColor = Color.web("#FF69B4");
                brickStyle = Color.web("#7ED321");
                isRectangularBricks = false;
                break;
            case 0:
                backgroundColor = Color.web("#1b1b2f");
                ballColor = Color.web("#00D9FF");
                brickStyle = Color.web("#E74C3C");
                isRectangularBricks = true;
                break;
        }
    }

    public List<Brick> generateBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        int colCount = 10;
        int rowCount = 3 + level / 2;

        double startX = 30;
        double startY = 40;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;

        double normalRate = 0.65;
        double hardRate = 0.85;
        if (level == 2) {
            normalRate = 0.55;
            hardRate = 0.80;
        }
        if (level >= 3) {
            normalRate = 0.45;
            hardRate = 0.75;
        }

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                double x = startX + col * (brickW + gap);
                double y = startY + row * (brickH + gap);

                double rand = Math.random();
                Brick brick;
                if (rand < normalRate) {
                    brick = new NormalBrick(x, y);
                } else if (rand < hardRate) {
                    brick = new HardBrick(x, y);
                } else {
                    brick = new GiftBrick(x, y);
                }
                bricks.add(brick);
            }
        }

        return bricks;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getBallColor() {
        return ballColor;
    }

    public Color getBrickStyle() {
        return brickStyle;
    }

    public boolean isRectangularBricks() {
        return isRectangularBricks;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public String getLevelThemeName() {
        switch (currentLevel % 5) {
            case 1: return "星空蓝";
            case 2: return "梦幻紫";
            case 3: return "深海青";
            case 4: return "玫瑰红";
            case 0: return "暗夜黑";
            default: return "经典";
        }
    }
}
