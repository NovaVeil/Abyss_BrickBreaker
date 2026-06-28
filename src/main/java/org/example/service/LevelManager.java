package org.example.service;

import javafx.scene.paint.Color;
import org.example.model.*;
import org.example.util.GameConstant;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private int currentLevel;
    private Color backgroundColor;
    private Color ballColor;
    private Color brickStyle;
    private boolean isRectangularBricks;
    private List<VirtualBall> virtualBalls;
    private GameMode currentMode;

//    构造函数 - 初始化游戏从第一关开始

    public LevelManager() {
        this.currentLevel = 1;
        this.virtualBalls = new ArrayList<>();
        this.currentMode = GameMode.CAMPAIGN;
        initLevelStyle(1);
    }


    /* 初始化指定关卡的主题样式
     每5个关卡为一个主题循环（level % 5）
     @param level 关卡编号*/

    public void initLevelStyle(int level) {
        this.currentLevel = level;

        // 根据关卡号对5取余，确定使用哪种主题风格
        switch (level % 5) {
            case 1: // 星空蓝主题
                backgroundColor = Color.web("#0a0e27");  // 深蓝色背景
                ballColor = Color.WHITE;                  // 白色小球
                brickStyle = Color.web("#4A90E2");       // 蓝色砖块
                isRectangularBricks = true;               // 使用矩形砖块
                break;
            case 2: // 梦幻紫主题
                backgroundColor = Color.web("#1a0f2e");  // 紫色背景
                ballColor = Color.web("#FFD700");        // 金色小球
                brickStyle = Color.web("#FF6B9D");       // 粉色砖块
                isRectangularBricks = false;              // 不使用矩形砖块
                break;
            case 3: // 深海青主题
                backgroundColor = Color.web("#0d1b2a");  // 深青色背景
                ballColor = Color.web("#00FF88");        // 绿色小球
                brickStyle = Color.web("#F5A623");       // 橙色砖块
                isRectangularBricks = true;               // 使用矩形砖块
                break;
            case 4: // 玫瑰红主题
                backgroundColor = Color.web("#2d132c");  // 深红色背景
                ballColor = Color.web("#FF69B4");        // 粉红色小球
                brickStyle = Color.web("#7ED321");       // 绿色砖块
                isRectangularBricks = false;              // 不使用矩形砖块
                break;
            case 0: // 暗夜黑主题（关卡号为5的倍数时触发）
                backgroundColor = Color.web("#1b1b2f");  // 黑色背景
                ballColor = Color.web("#00D9FF");        // 青色小球
                brickStyle = Color.web("#E74C3C");       // 红色砖块
                isRectangularBricks = true;               // 使用矩形砖块
                break;
        }
    }


   /* 设置当前游戏模式
    @param mode 游戏模式*/

    public void setGameMode(GameMode mode) {
        this.currentMode = mode;
    }


    /*获取当前游戏模式
    @return 当前游戏模式*/

    public GameMode getCurrentMode() {
        return currentMode;
    }


    /*生成指定关卡的砖块列表（根据游戏模式自动选择生成策略）
    @param level 关卡编号
    @return 生成的砖块列表*/

    public List<Brick> generateBricks(int level) {
        if (currentMode == GameMode.ENDLESS) {
            return generateEndlessBricks(level);
        } else {
            return generateCampaignBricks(level);
        }
    }


     //闯关模式：生成图形化砖块布局

    private List<Brick> generateCampaignBricks(int level) {
        // 第6-10关：使用每个主题的“高级”生成器（更高密度和更高难度的砖块分布），第1-5关仍为基础图案
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

    // Helper: 根据关卡难度返回三种主要类型的概率（normal, hard, triangle），礼物砖概率为剩余
    private double[] getTypeRates(int level, boolean advanced) {
        // 基础概率
        double normal = 0.55;
        double hard = 0.25; // 累计至 0.55+0.25=0.8
        double triangle = 0.05;

        // 随关卡升高，增加难度（减少普通砖，增加坚硬和三角）
        double levelFactor = Math.min(1.0, (level - 1) / 9.0); // 0 for level1, 1 for level10
        normal -= 0.15 * levelFactor; // 最多下降0.15
        hard += 0.10 * levelFactor; // 最多增加0.10
        triangle += 0.10 * levelFactor; // 最多增加0.10

        // Advanced 关进一步提高硬砖与三角比例
        if (advanced) {
            hard += 0.05;
            triangle += 0.05;
            normal -= 0.10;
        }

        // 保证在合理范围
        normal = Math.max(0.15, normal);
        hard = Math.max(0.05, hard);
        triangle = Math.max(0.0, triangle);

        // 若总和超过1，按比例缩放到0.85，剩余0.15给礼物砖（保证礼物存在）
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

    // 高级主题生成器（用于第6-10关），每个主题用不同的生成算法，密度和难度更高
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

    //无尽模式：生成高密度随机砖块

    private List<Brick> generateEndlessBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        // 无尽模式：更多行列，更高密度
        int colCount = GameConstant.ENDLESS_BRICK_COLS_BASE + (level / 2); // 第1关14列，之后每2关+1列
        if (colCount > 20) colCount = 20; // 最多20列
        
        int rowCount = 6 + level / 2; // 第1关6行，之后每2关+1行
        if (rowCount > 12) rowCount = 12; // 最多12行

        // 砖块布局参数
        double startX = 10;                          // 起始X坐标（更小边距）
        double startY = 40;                          // 起始Y坐标
        double brickW = GameConstant.BRICK_WIDTH;   // 砖块宽度
        double brickH = GameConstant.BRICK_HEIGHT;  // 砖块高度
        double gap = GameConstant.BRICK_GAP;         // 砖块间距

        // 砖块类型概率配置（无尽模式更难）
        double normalRate = 0.55;  // 普通砖块55%
        double hardRate = 0.80;    // 坚硬砖块累计80%（占25%）
        double triangleRate = 0.0; // 三角形砖块概率
                                   // 礼物砖块20%
        
        // 随关卡提高难度
        if (level >= 3) {
            normalRate = 0.45;  // 普通砖块45%
            hardRate = 0.75;    // 坚硬砖块累计75%（占30%）
            triangleRate = 0.10; // 三角形砖块10%
                                 // 礼物砖块15%
        }
        if (level >= 5) {
            normalRate = 0.40;  // 普通砖块40%
            hardRate = 0.70;    // 坚硬砖块累计70%（占30%）
            triangleRate = 0.15; // 三角形砖块15%
                                 // 礼物砖块15%
        }

        // 双重循环生成砖块网格
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                // 计算当前砖块的位置坐标
                double x = startX + col * (brickW + gap);
                double y = startY + row * (brickH + gap);

                // 检查是否超出右边界
                if (x + brickW > GameConstant.GAME_WIDTH - 10) {
                    continue;
                }

                // 根据概率随机决定砖块类型
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
  //生成圆形图案（使用距离公式循环判定，半径随关卡递增）
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

    //生成星形图案（使用极坐标五星方程循环生成，尺寸随关卡递增）
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

  /* 生成爱心形状的砖块布局（使用心形隐式方程循环生成，尺寸随关卡递增）
   @param level 关卡编号
   @return 爱心形状的砖块列表*/
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

    /*生成自定义图形砖块（支持多种图案）
    @param level 关卡编号
    @param patternType 图案类型 ("heart", "diamond", "windmill")
    @return 图形砖块列表*/
    public List<Brick> generatePatternBricks(int level, String patternType) {
        switch (patternType.toLowerCase()) {
            case "heart":
                return generateHeartPatternBricks(level);
            case "diamond":
                return generateDiamondPatternBricks(level);
            case "windmill":
                return generateWindmillPatternBricks(level);
            default:
                return generateBricks(level);
        }
    }

    //生成菱形图案（使用曼哈顿距离循环判定，尺寸随关卡递增）
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

    //生成风车图案（使用旋转矩形算法循环生成四个叶片，尺寸随关卡递增）
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
    /*生成虚拟小球
    在砖块间隙处放置，数量随关卡递增
    @param level 关卡编号
    @param brickList 砖块列表（用于避开砖块位置）
    @return 虚拟小球列表*/
    public List<VirtualBall> generateVirtualBalls(int level, List<Brick> brickList) {
        virtualBalls.clear();
        
        int virtualBallCount = calculateVirtualBallCount(level);
        
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = GameConstant.BRICK_GAP;
        
        // 定义可能的虚拟小球位置（砖块间隙）
        List<double[]> possiblePositions = new ArrayList<>();
        
        // 在砖块网格的间隙处生成候选位置
        int colCount = GameConstant.CAMPAIGN_BRICK_COLS;
        int rowCount = 3 + level / 2;
        
        // 无尽模式调整行列数
        if (currentMode == GameMode.ENDLESS) {
            colCount = GameConstant.ENDLESS_BRICK_COLS_BASE + (level / 2);
            if (colCount > 20) colCount = 20;
            rowCount = 6 + level / 2;
            if (rowCount > 12) rowCount = 12;
            gap = GameConstant.BRICK_GAP;
        }
        
        double startX = 30;
        double startY = 40;
        
        if (currentMode == GameMode.ENDLESS) {
            startX = 10;
        }
        
        // 为避免虚拟小球总是集中在左上角，随机移动候选网格的水平偏移
        double maxOffset = Math.max(0, GameConstant.GAME_WIDTH / 4.0);
        double gridOffsetX = (Math.random() - 0.5) * maxOffset; // 左右小范围随机偏移
        startX += gridOffsetX;
        
        for (int row = 0; row <= rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                // 计算间隙位置（砖块之间）
                double x = startX + col * (brickW + gap) + brickW + gap / 2;
                double y = startY + row * (brickH + gap) + brickH / 2;
                
                // 检查该位置是否与砖块重叠
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
                    // 在格点上添加少量抖动，使位置更随机而不是总在格点中心
                    double jitterX = (Math.random() - 0.5) * (brickW / 2.0);
                    double jitterY = (Math.random() - 0.5) * (brickH / 2.0);
                    possiblePositions.add(new double[]{x + jitterX, y + jitterY});
                }
             }
         }
        
        // 从候选位置中选择指定数量的位置，确保彼此之间有足够距离
        double minDistance = GameConstant.VIRTUAL_BALL_MIN_DISTANCE;
        
        for (int i = 0; i < virtualBallCount && !possiblePositions.isEmpty(); i++) {
            // 筛选出与已选位置距离足够的位置
            List<double[]> validPositions = new ArrayList<>();
            
            if (virtualBalls.isEmpty()) {
                // 第一个虚拟小球，所有位置都有效
                validPositions.addAll(possiblePositions);
            } else {
                // 后续虚拟小球，需要检查与已有小球的距离
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
            
            // 如果没有足够远的位置，放宽距离要求
            if (validPositions.isEmpty()) {
                minDistance *= 0.7; // 降低最小距离要求
                continue; // 重新尝试
            }
            
            // 随机选择一个有效位置（并再加一点抖动）
            int index = (int) (Math.random() * validPositions.size());
            double[] selectedPos = validPositions.get(index);
            double extraJitterX = (Math.random() - 0.5) * (brickW / 3.0);
            double extraJitterY = (Math.random() - 0.5) * (brickH / 3.0);
            double finalX = selectedPos[0] + extraJitterX;
            double finalY = selectedPos[1] + extraJitterY;
            // 确保不出界
            finalX = Math.max(20, Math.min(finalX, GameConstant.GAME_WIDTH - 20));
            finalY = Math.max(20, Math.min(finalY, GameConstant.GAME_HEIGHT - 120));
            virtualBalls.add(new VirtualBall(finalX, finalY));

            // 创建 final 副本用于 lambda 表达式
            final double finalMinDistance = minDistance;

            // 从候选列表中移除已选位置及其附近位置
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
        
        // 如果是闯关模式并且是第6-10关，使用第1-5关的基础数量，并在基础上每关+1
        if (currentMode == GameMode.CAMPAIGN && level >= 6 && level <= 10) {
            int baseLevel = level - 5;
            int baseCount = 3 + (baseLevel - 1);
            virtualBallCount = baseCount + 1;
        } else {
            virtualBallCount = 3 + (level - 1);
        }
        
        // 无尽模式增加虚拟小球数量
        if (currentMode == GameMode.ENDLESS) {
            virtualBallCount += 2;
        }
        
        return virtualBallCount;
    }

    //获取虚拟小球列表
    public List<VirtualBall> getVirtualBalls() {
        return virtualBalls;
    }


   //获取当前关卡的背景颜色

    public Color getBackgroundColor() {
        return backgroundColor;
    }
  //获取当前关卡的小球颜色
    public Color getBallColor() {
        return ballColor;
    }

  //获取当前关卡的砖块颜色
    public Color getBrickStyle() {
        return brickStyle;
    }
  //判断当前关卡是否使用矩形砖块
    public boolean isRectangularBricks() {
        return isRectangularBricks;
    }
    //获取当前关卡编号
    public int getCurrentLevel() {
        return currentLevel;
    }


 //获取当前关卡的主题名称（用于UI显示）@return 主题名称字符串

    public String getLevelThemeName() {
        if (currentMode == GameMode.ENDLESS) {
            return "无尽关卡 " + currentLevel;
        }
        switch (currentLevel % 5) {
            case 1: return "极光之镜";
            case 2: return "褪色森林";
            case 3: return "黄金树";
            case 4: return "星月律法";
            case 0: return "斜阳冰川";
            default: return "经典";
        }
    }
}
