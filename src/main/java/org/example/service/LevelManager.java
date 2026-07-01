package org.example.service;

import org.example.model.*;
import org.example.util.GameConstant;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private int currentLevel;
    private List<VirtualBall> virtualBalls;
    private GameMode currentMode;

    public LevelManager() {
        this.currentLevel = 1;
        this.virtualBalls = new ArrayList<>();
        this.currentMode = GameMode.CAMPAIGN;
    }

    public void initLevelStyle(int level) {
        this.currentLevel = level;
    }

    public void setGameMode(GameMode mode) {
        this.currentMode = mode;
    }

    public List<Brick> generateBricks(int level) {
        if (currentMode == GameMode.ENDLESS) {
            return generateEndlessBricks(level);
        } else {
            return generateCampaignBricks(level);
        }
    }

    private List<Brick> generateCampaignBricks(int level) {
        if (level >= 6 && level <= 10) {
            int themeIndex = level % 5; // 6->1,7->2,...,10->0
            switch (themeIndex) {
                case 1:
                    return generateAdvancedTheme1(level);
                case 2:
                    return generateAdvancedTheme2(level);
                case 3:
                    return generateAdvancedTheme3(level);
                case 4:
                    return generateAdvancedTheme4(level);
                case 0:
                    return generateAdvancedTheme5(level);
                default:
                    return generateHeartPatternBricks(level);
            }
        }

        switch (level % 5) {
            case 1:
                return generateHeartPatternBricks(level);
            case 2:
                return generateDiamondPatternBricks(level);
            case 3:
                return generateWindmillPatternBricks(level);
            case 4:
                return generateCirclePatternBricks(level);
            case 0:
                return generateStarPatternBricks(level);
        }
        return generateHeartPatternBricks(level);
    }

    private double[] getTypeRates(int level, boolean advanced) {
        double normal = 0.55;
        double hard = 0.25; // 累计至 0.55+0.25=0.8
        double triangle = 0.05;

        double levelFactor = Math.min(1.0, (level - 1) / 9.0); // 0 for level1, 1 for level10
        normal -= 0.15 * levelFactor; // 最多下降0.15
        hard += 0.10 * levelFactor; // 最多增加0.10
        triangle += 0.10 * levelFactor; // 最多增加0.10

        if (advanced) {
            hard += 0.05;
            triangle += 0.05;
            normal -= 0.10;
        }

        normal = Math.max(0.15, normal);
        hard = Math.max(0.05, hard);
        triangle = Math.max(0.0, triangle);

        double sum = normal + hard + triangle;
        double gift = 0.15;
        if (sum + gift > 1.0) {
            double scale = (1.0 - gift) / sum;
            normal *= scale;
            hard *= scale;
            triangle *= scale;
        }

        return new double[]{normal, hard, triangle, gift};
    }

    private Brick createBrickByRates(double x, double y, double[] rates) {
        double r = Math.random();
        double normal = rates[0];
        double hard = rates[1];
        double triangle = rates[2];
        // gift = rates[3]
        if (r < normal) return new NormalBrick(x, y);
        r -= normal;
        if (r < hard) return new HardBrick(x, y);
        r -= hard;
        if (r < triangle) return new TriangleBrick(x, y);
        return new GiftBrick(x, y);
    }

    private List<Brick> generateAdvancedTheme1(int level) {
        List<Brick> bricks = new ArrayList<>();
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double[] rates = getTypeRates(level, true);
        double cellSize = brickW + gap;

        double scale = 1.0 + (level - 1) * 0.15;
        int gridW = (int)(9 * scale);
        int gridH = (int)(8 * scale);

        for (int row = 0; row < gridH; row++) {
            for (int col = 0; col < gridW; col++) {
                double nx = (col - gridW / 2.0) / (gridW / 2.5);
                double ny = -(row - gridH / 2.0) / (gridH / 3.2);
                double a = nx * nx + ny * ny - 1;
                double heartValue = a * a * a - nx * nx * ny * ny * ny;

                if (heartValue <= 0) {
                    double x = centerX - (gridW / 2.0) * cellSize + col * cellSize;
                    double y = centerY - (gridH / 2.0) * cellSize + row * cellSize;
                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        if (!overlapsExisting(bricks, x, y, brickW, brickH)) {
                            bricks.add(createBrickByRates(x, y, rates));
                        }
                    }
                }
            }
        }

        return bricks;
    }

    private List<Brick> generateAdvancedTheme2(int level) {
        List<Brick> bricks = new ArrayList<>();
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double cellSize = brickW + gap;
        double[] rates = getTypeRates(level, true);

        int halfSize = 4 + (level - 6);
        for (int row = -halfSize; row <= halfSize; row++) {
            for (int col = -halfSize; col <= halfSize; col++) {
                int dx = Math.abs(col);
                int dy = Math.abs(row);
                if (dx + dy <= halfSize) {
                    double x = centerX + col * cellSize - brickW / 2;
                    double y = centerY + row * cellSize - brickH / 2;
                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        if (!overlapsExisting(bricks, x, y, brickW, brickH)) {
                            if (dx + dy <= 1) {
                                bricks.add(new GiftBrick(x, y));
                            } else {
                                double[] localRates = rates.clone();
                                if (dx + dy == halfSize) {
                                    localRates[1] = Math.max(localRates[1], 0.40);
                                }
                                bricks.add(createBrickByRates(x, y, localRates));
                            }
                        }
                    }
                }
            }
        }

        return bricks;
    }

    private List<Brick> generateAdvancedTheme3(int level) {
        List<Brick> bricks = new ArrayList<>();
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double cellSize = brickW + gap;
        double[] rates = getTypeRates(level, true);

        double baseRadius = 110 + (level - 6) * 15;
        double innerRadius = baseRadius * 0.55;
        int gridHalf = (int) Math.ceil(baseRadius / cellSize) + 1;

        for (int row = -gridHalf; row <= gridHalf; row++) {
            for (int col = -gridHalf; col <= gridHalf; col++) {
                double cellCX = centerX + col * cellSize;
                double cellCY = centerY + row * cellSize;
                double dx = cellCX - centerX;
                double dy = cellCY - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= baseRadius && distance >= innerRadius) {
                    double x = cellCX - brickW / 2;
                    double y = cellCY - brickH / 2;
                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        if (!overlapsExisting(bricks, x, y, brickW, brickH)) {
                            bricks.add(createBrickByRates(x, y, rates));
                        }
                    }
                }
            }
        }

        double fillRadius = innerRadius * 0.6;
        int fillHalf = (int) Math.ceil(fillRadius / cellSize);
        for (int row = -fillHalf; row <= fillHalf; row++) {
            for (int col = -fillHalf; col <= fillHalf; col++) {
                double cellCX = centerX + col * cellSize;
                double cellCY = centerY + row * cellSize;
                double dx = cellCX - centerX;
                double dy = cellCY - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance <= fillRadius) {
                    double x = cellCX - brickW / 2;
                    double y = cellCY - brickH / 2;
                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        if (!overlapsExisting(bricks, x, y, brickW, brickH)) {
                            bricks.add(new GiftBrick(x, y));
                        }
                    }
                }
            }
        }

        return bricks;
    }

    private List<Brick> generateAdvancedTheme4(int level) {
        List<Brick> bricks = new ArrayList<>();
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double cellSize = brickW + gap;
        double[] rates = getTypeRates(level, true);

        int bladeLen = 4 + level / 2;
        int bladeW = 2 + level / 5;
        int gridHalf = bladeLen + 2;

        for (int row = -gridHalf; row <= gridHalf; row++) {
            for (int col = -gridHalf; col <= gridHalf; col++) {
                if (col == 0 && row == 0) {
                    double x = centerX - brickW / 2;
                    double y = centerY - brickH / 2;
                    bricks.add(new GiftBrick(x, y));
                    continue;
                }

                boolean inBlade = false;
                int[] dirs = {0, 1, 2, 3};
                for (int d : dirs) {
                    int along, perpendicular;
                    switch (d) {
                        case 0: along = col;  perpendicular = row;  break;
                        case 1: along = -row; perpendicular = col;  break;
                        case 2: along = -col; perpendicular = -row; break;
                        default: along = row;  perpendicular = -col; break;
                    }
                    if (along >= 1 && along <= bladeLen && Math.abs(perpendicular) <= bladeW) {
                        inBlade = true;
                        break;
                    }
                }

                if (inBlade) {
                    double x = centerX + col * cellSize - brickW / 2;
                    double y = centerY + row * cellSize - brickH / 2;
                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        if (!overlapsExisting(bricks, x, y, brickW, brickH)) {
                            bricks.add(createBrickByRates(x, y, rates));
                        }
                    }
                }
            }
        }

        return bricks;
    }

    private List<Brick> generateAdvancedTheme5(int level) {
        List<Brick> bricks = new ArrayList<>();
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double cellSize = brickW + gap;
        double[] rates = getTypeRates(level, true);

        double baseRadius = 110 + (level - 6) * 12;
        double innerRadius = baseRadius * 0.38;
        int gridHalf = (int) Math.ceil(baseRadius / cellSize) + 1;

        for (int row = -gridHalf; row <= gridHalf; row++) {
            for (int col = -gridHalf; col <= gridHalf; col++) {
                double cellCX = centerX + col * cellSize;
                double cellCY = centerY + row * cellSize;
                double dx = cellCX - centerX;
                double dy = cellCY - centerY;
                double r = Math.sqrt(dx * dx + dy * dy);

                if (r > 0 && r <= baseRadius) {
                    double angle = Math.atan2(dy, dx);
                    if (angle < 0) angle += 2 * Math.PI;
                    double starR = innerRadius + (baseRadius - innerRadius) * Math.pow(Math.abs(Math.cos(2.5 * angle)), 1.5);
                    if (r <= starR) {
                        double x = cellCX - brickW / 2;
                        double y = cellCY - brickH / 2;
                        if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                            if (!overlapsExisting(bricks, x, y, brickW, brickH)) {
                                bricks.add(createBrickByRates(x, y, rates));
                            }
                        }
                    }
                }
            }
        }

        return bricks;
    }

    private boolean overlapsExisting(List<Brick> bricks, double x, double y, double w, double h) {
        for (Brick brick : bricks) {
            if (x < brick.getX() + brick.getWidth() && x + w > brick.getX() &&
                y < brick.getY() + brick.getHeight() && y + h > brick.getY()) {
                return true;
            }
        }
        return false;
    }

    private List<Brick> generateEndlessBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        int colCount = GameConstant.ENDLESS_BRICK_COLS_BASE + (level / 2);
        if (colCount > 20) colCount = 20;
        
        int rowCount = 6 + level / 2;
        if (rowCount > 12) rowCount = 12;

        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;

        double totalWidth = colCount * brickW + (colCount - 1) * gap;
        double startX = (GameConstant.GAME_WIDTH - totalWidth) / 2.0;
        double startY = 40;

        double normalRate = 0.55;
        double hardRate = 0.80;
        double triangleRate = 0.0;
        
        if (level >= 3) {
            normalRate = 0.45;
            hardRate = 0.75;
            triangleRate = 0.10;
        }
        if (level >= 5) {
            normalRate = 0.40;
            hardRate = 0.70;
            triangleRate = 0.15;
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
                } else if (triangleRate > 0 && rand < hardRate + triangleRate) {
                    brick = new TriangleBrick(x, y);
                } else {
                    brick = new GiftBrick(x, y);
                }
                bricks.add(brick);
            }
        }

        return bricks;
    }

    public List<Brick> generateCirclePatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;

        double baseRadius = 100 + (level - 1) * 10;
        double cellSize = brickW + gap;
        int halfCells = (int) Math.ceil(baseRadius / cellSize) + 1;

        for (int row = -halfCells; row <= halfCells; row++) {
            for (int col = -halfCells; col <= halfCells; col++) {
                double cellCX = centerX + col * cellSize;
                double cellCY = centerY + row * cellSize;
                double dx = cellCX - centerX;
                double dy = cellCY - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= baseRadius) {
                    double x = cellCX - brickW / 2;
                    double y = cellCY - brickH / 2;
                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        double[] rates = getTypeRates(level, false);
                        bricks.add(createBrickByRates(x, y, rates));
                    }
                }
            }
        }

        return bricks;
    }

    public List<Brick> generateStarPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;

        double baseRadius = 100 + (level - 1) * 10;
        double innerRadius = baseRadius * 0.38;
        double cellSize = brickW + gap;
        int gridHalf = (int) Math.ceil(baseRadius / cellSize) + 1;

        for (int row = -gridHalf; row <= gridHalf; row++) {
            for (int col = -gridHalf; col <= gridHalf; col++) {
                double cellCX = centerX + col * cellSize;
                double cellCY = centerY + row * cellSize;
                double dx = cellCX - centerX;
                double dy = cellCY - centerY;
                double r = Math.sqrt(dx * dx + dy * dy);

                if (r > 0 && r <= baseRadius) {
                    double angle = Math.atan2(dy, dx);
                    if (angle < 0) angle += 2 * Math.PI;
                    double starR = innerRadius + (baseRadius - innerRadius) * Math.pow(Math.abs(Math.cos(2.5 * angle)), 1.5);
                    if (r <= starR) {
                        double x = cellCX - brickW / 2;
                        double y = cellCY - brickH / 2;
                        if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                            double[] rates = getTypeRates(level, false);
                            bricks.add(createBrickByRates(x, y, rates));
                        }
                    }
                }
            }
        }

        return bricks;
    }

    public List<Brick> generateHeartPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;

        double scale = 1.0 + (level - 1) * 0.15;
        int gridW = (int)(7 * scale);
        int gridH = (int)(6 * scale);
        double cellSize = brickW + gap;

        for (int row = 0; row < gridH; row++) {
            for (int col = 0; col < gridW; col++) {
                double nx = (col - gridW / 2.0) / (gridW / 2.5);
                double ny = -(row - gridH / 2.0) / (gridH / 2.5);
                double a = nx * nx + ny * ny - 1;
                double heartValue = a * a * a - nx * nx * ny * ny * ny;

                if (heartValue <= 0) {
                    double x = centerX - (gridW / 2.0) * cellSize + col * cellSize;
                    double y = centerY - (gridH / 2.0) * cellSize + row * cellSize;
                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        double[] rates = getTypeRates(level, false);
                        bricks.add(createBrickByRates(x, y, rates));
                    }
                }
            }
        }

        return bricks;
    }

    public List<Brick> generateDiamondPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;

        int halfSize = 3 + (level - 1) / 2;
        double cellSize = brickW + gap;

        for (int row = -halfSize; row <= halfSize; row++) {
            for (int col = -halfSize; col <= halfSize; col++) {
                int dx = Math.abs(col);
                int dy = Math.abs(row);

                if (dx + dy <= halfSize) {
                    double x = centerX + col * cellSize - brickW / 2;
                    double y = centerY + row * cellSize - brickH / 2;

                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        if (dx + dy == halfSize) {
                            double[] rates = getTypeRates(level, false);
                            rates[1] = Math.max(rates[1], 0.35);
                            bricks.add(createBrickByRates(x, y, rates));
                        } else if (dx + dy <= 1) {
                            bricks.add(new GiftBrick(x, y));
                        } else {
                            double[] rates = getTypeRates(level, false);
                            bricks.add(createBrickByRates(x, y, rates));
                        }
                    }
                }
            }
        }

        return bricks;
    }

    public List<Brick> generateWindmillPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 180;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;
        double cellSize = brickW + gap;

        int bladeLen = 3 + level / 2;
        int bladeW = 1 + level / 5;
        int gridHalf = bladeLen + 1;

        for (int row = -gridHalf; row <= gridHalf; row++) {
            for (int col = -gridHalf; col <= gridHalf; col++) {
                if (col == 0 && row == 0) {
                    double x = centerX - brickW / 2;
                    double y = centerY - brickH / 2;
                    bricks.add(new GiftBrick(x, y));
                    continue;
                }

                boolean inBlade = false;
                int[] dirs = {0, 1, 2, 3};
                for (int d : dirs) {
                    int along, perpendicular;// along: 沿叶片方向的距离，perpendicular: 垂直于叶片方向的距离
                    switch (d) {
                        case 0: along = col;  perpendicular = row;  break;
                        case 1: along = -row; perpendicular = col;  break;
                        case 2: along = -col; perpendicular = -row; break;
                        default: along = row;  perpendicular = -col; break;
                    }
                    if (along >= 1 && along <= bladeLen && Math.abs(perpendicular) <= bladeW) {
                        inBlade = true;
                        break;
                    }
                }

                if (inBlade) {
                    double x = centerX + col * cellSize - brickW / 2;
                    double y = centerY + row * cellSize - brickH / 2;
                    if (x >= 5 && x + brickW <= GameConstant.GAME_WIDTH - 5 && y >= 5 && y + brickH <= GameConstant.GAME_HEIGHT - 80) {
                        double[] rates = getTypeRates(level, false);
                        bricks.add(createBrickByRates(x, y, rates));
                    }
                }
            }
        }

        return bricks;
    }

    public List<VirtualBall> generateVirtualBalls(int level, List<Brick> brickList) {
        virtualBalls.clear();
        
        int virtualBallCount = calculateVirtualBallCount(level);
        
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;
        
        List<double[]> possiblePositions = new ArrayList<>();
        
        int colCount = GameConstant.CAMPAIGN_BRICK_COLS;
        int rowCount = 3 + level / 2;
        
        if (currentMode == GameMode.ENDLESS) {
            colCount = GameConstant.ENDLESS_BRICK_COLS_BASE + (level / 2);
            if (colCount > 20) colCount = 20;
            rowCount = 6 + level / 2;
            if (rowCount > 12) rowCount = 12;
        }
        
        double startX = 30;
        double startY = 40;
        
        if (currentMode == GameMode.ENDLESS) {
            startX = 10;
        }
        
        double maxOffset = Math.max(0, GameConstant.GAME_WIDTH / 4.0);
        double gridOffsetX = (Math.random() - 0.5) * maxOffset; // 左右小范围随机偏移
        startX += gridOffsetX;
        
        for (int row = 0; row <= rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                double x = startX + col * (brickW + gap) + brickW + gap / 2;
                double y = startY + row * (brickH + gap) + brickH / 2;
                
                boolean overlaps = false;
                for (Brick brick : brickList) {
                    if (x > brick.getX() - 15 && x < brick.getX() + brick.getWidth() + 15 &&
                        y > brick.getY() - 15 && y < brick.getY() + brick.getHeight() + 15) {
                        overlaps = true;
                        break;
                    }
                }
                
                if (!overlaps && x > 50 && x < GameConstant.GAME_WIDTH - 50 &&
                    y > 50 && y < GameConstant.GAME_HEIGHT - 100) {
                    double jitterX = (Math.random() - 0.5) * (brickW / 2.0);
                    double jitterY = (Math.random() - 0.5) * (brickH / 2.0);
                    possiblePositions.add(new double[]{x + jitterX, y + jitterY});
                }
             }
         }
        
        double minDistance = GameConstant.VIRTUAL_BALL_MIN_DISTANCE;
        
        for (int i = 0; i < virtualBallCount && !possiblePositions.isEmpty(); i++) {
            List<double[]> validPositions = new ArrayList<>();
            
            if (virtualBalls.isEmpty()) {
                validPositions.addAll(possiblePositions);
            } else {
                for (double[] pos : possiblePositions) {
                    boolean tooClose = false;
                    for (VirtualBall existingBall : virtualBalls) {
                        double dx = pos[0] - existingBall.getX();
                        double dy = pos[1] - existingBall.getY();
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        if (distance < minDistance) {
                            tooClose = true;
                            break;
                        }
                    }
                    if (!tooClose) {
                        validPositions.add(pos);
                    }
                }
            }
            
            if (validPositions.isEmpty()) {
                minDistance *= 0.7; // 降低最小距离要求
                continue; // 重新尝试
            }
            
            int index = (int) (Math.random() * validPositions.size());
            double[] selectedPos = validPositions.get(index);
            double extraJitterX = (Math.random() - 0.5) * (brickW / 3.0);
            double extraJitterY = (Math.random() - 0.5) * (brickH / 3.0);
            double finalX = selectedPos[0] + extraJitterX;
            double finalY = selectedPos[1] + extraJitterY;
            finalX = Math.max(20, Math.min(finalX, GameConstant.GAME_WIDTH - 20));
            finalY = Math.max(20, Math.min(finalY, GameConstant.GAME_HEIGHT - 120));
            virtualBalls.add(new VirtualBall(finalX, finalY));

            final double finalMinDistance = minDistance;

            possiblePositions.removeIf(pos -> {
                double dx = pos[0] - selectedPos[0];
                double dy = pos[1] - selectedPos[1];
                return Math.sqrt(dx * dx + dy * dy) < finalMinDistance;
            });
        }
        
        return virtualBalls;
    }

    private int calculateVirtualBallCount(int level) {
        int virtualBallCount;
        
        if (currentMode == GameMode.CAMPAIGN && level >= 6 && level <= 10) {
            int baseLevel = level - 5;
            int baseCount = 3 + (baseLevel - 1);
            virtualBallCount = baseCount + 1;
        } else {
            virtualBallCount = 3 + (level - 1);
        }
        
        if (currentMode == GameMode.ENDLESS) {
            virtualBallCount += 2;
        }
        
        return virtualBallCount;
    }

    public String getLevelThemeName() {
        if (currentMode == GameMode.ENDLESS) {
            return "无尽关卡 " + currentLevel;
        }
        switch (currentLevel % 5) {
            case 1: return "黄金树";
            case 2: return "褪色森林";
            case 3: return "星月律法";
            case 4: return "斜阳冰川";
            case 0: return "极光之镜";
            default: return "经典";
        }
    }
}
