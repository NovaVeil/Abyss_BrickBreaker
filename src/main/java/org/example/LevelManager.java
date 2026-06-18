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

    /**
     * 设置当前游戏模式
     * @param mode 游戏模式
     */
    public void setGameMode(GameMode mode) {
        this.currentMode = mode;
    }

    /**
     * 获取当前游戏模式
     * @return 当前游戏模式
     */
    public GameMode getCurrentMode() {
        return currentMode;
    }

    /**
     * 生成指定关卡的砖块列表（根据游戏模式自动选择生成策略）
     * 
     * @param level 关卡编号
     * @return 生成的砖块列表
     */
    public List<Brick> generateBricks(int level) {
        if (currentMode == GameMode.ENDLESS) {
            return generateEndlessBricks(level);
        } else {
            return generateCampaignBricks(level);
        }
    }

    /**
     * 闯关模式：生成图形化砖块布局
     */
    private List<Brick> generateCampaignBricks(int level) {
        switch (level % 5) {
            case 1:
                return generateHeartPatternBricks(level);
            case 2:
                return generateDiamondPatternBricks(level);
            case 3:
                return generateArrowPatternBricks(level);
            case 4:
                return generateCirclePatternBricks(level);
            case 0:
                return generateStarPatternBricks(level);
            default:
                return generateHeartPatternBricks(level);
        }
    }

    /**
     * 无尽模式：生成高密度随机砖块
     */
    private List<Brick> generateEndlessBricks(int level) {
        List<Brick> bricks = new ArrayList<>();

        // 无尽模式：更多行列，更高密度
        int colCount = 14 + (level / 2); // 第1关14列，之后每2关+1列
        if (colCount > 20) colCount = 20; // 最多20列
        
        int rowCount = 6 + level / 2; // 第1关6行，之后每2关+1行
        if (rowCount > 12) rowCount = 12; // 最多12行

        // 砖块布局参数
        double startX = 10;                          // 起始X坐标（更小边距）
        double startY = 40;                          // 起始Y坐标
        double brickW = GameConstant.BRICK_WIDTH;   // 砖块宽度
        double brickH = GameConstant.BRICK_HEIGHT;  // 砖块高度
        double gap = 6;                              // 砖块间距（减小以增加密度）

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

    /**
     * 生成圆形图案
     */
    public List<Brick> generateCirclePatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();
        
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 150;
        double radius = 120;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;
        
        // 在圆形区域内生成砖块
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 12; col++) {
                double x = centerX - 6 * (brickW + gap) + col * (brickW + gap);
                double y = centerY - 4 * (brickH + gap) + row * (brickH + gap);
                
                // 计算到圆心的距离
                double dx = x + brickW / 2 - centerX;
                double dy = y + brickH / 2 - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                // 如果在圆形范围内，则生成砖块
                if (distance <= radius) {
                    double normalRate = 0.50;
                    double hardRate = 0.75;
                    
                    if (level >= 3) {
                        normalRate = 0.40;
                        hardRate = 0.65;
                    }
                    
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
        }
        
        return bricks;
    }

    /**
     * 生成星形图案
     */
    public List<Brick> generateStarPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();
        
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double startY = 60;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;
        
        // 五角星图案（简化版）
        int[][] starPattern = {
            {0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 0, 0, 0, 1, 0, 0},
            {0, 1, 0, 0, 0, 0, 0, 1, 0}
        };
        
        for (int row = 0; row < starPattern.length; row++) {
            for (int col = 0; col < starPattern[row].length; col++) {
                if (starPattern[row][col] == 1) {
                    double x = centerX - (4.5 * (brickW + gap)) + col * (brickW + gap);
                    double y = startY + row * (brickH + gap);
                    
                    double rand = Math.random();
                    Brick brick;
                    if (rand < 0.5) {
                        brick = new NormalBrick(x, y);
                    } else if (rand < 0.75) {
                        brick = new HardBrick(x, y);
                    } else {
                        brick = new GiftBrick(x, y);
                    }
                    bricks.add(brick);
                }
            }
        }
        
        return bricks;
    }

    /**
     * 生成爱心形状的砖块布局
     * 
     * @param level 关卡编号
     * @return 爱心形状的砖块列表
     */
    public List<Brick> generateHeartPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();
        
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double startY = 60;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;
        
        // 爱心图案设计（使用网格坐标）
        // 第1行：   X X     X X
        // 第2行： X X X X X X X
        // 第3行： X X X X X X X
        // 第4行：   X X X X X
        // 第5行：     X X X
        // 第6行：       X
        
        int[][] heartPattern = {
            {0, 1, 0, 0, 1, 0, 0},  // 第1行
            {1, 1, 1, 1, 1, 1, 1},  // 第2行
            {1, 1, 1, 1, 1, 1, 1},  // 第3行
            {0, 1, 1, 1, 1, 1, 0},  // 第4行
            {0, 0, 1, 1, 1, 0, 0},  // 第5行
            {0, 0, 0, 1, 0, 0, 0}   // 第6行
        };
        
        // 砖块类型概率
        double normalRate = 0.5;
        double hardRate = 0.75;
        double triangleRate = 0.10;
        
        if (level >= 3) {
            normalRate = 0.40;
            hardRate = 0.65;
        }
        
        for (int row = 0; row < heartPattern.length; row++) {
            for (int col = 0; col < heartPattern[row].length; col++) {
                if (heartPattern[row][col] == 1) {
                    double x = centerX - (3.5 * (brickW + gap)) + col * (brickW + gap);
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
        }
        
        return bricks;
    }

    /**
     * 生成自定义图形砖块（支持多种图案）
     * 
     * @param level 关卡编号
     * @param patternType 图案类型 ("heart", "diamond", "arrow")
     * @return 图形砖块列表
     */
    public List<Brick> generatePatternBricks(int level, String patternType) {
        switch (patternType.toLowerCase()) {
            case "heart":
                return generateHeartPatternBricks(level);
            case "diamond":
                return generateDiamondPatternBricks(level);
            case "arrow":
                return generateArrowPatternBricks(level);
            default:
                return generateBricks(level);
        }
    }

    /**
     * 生成菱形图案
     */
    public List<Brick> generateDiamondPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();
        
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double startY = 60;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;
        
        int[][] diamondPattern = {
            {0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 0, 0, 0}
        };
        
        for (int row = 0; row < diamondPattern.length; row++) {
            for (int col = 0; col < diamondPattern[row].length; col++) {
                if (diamondPattern[row][col] == 1) {
                    double x = centerX - (3 * (brickW + gap)) + col * (brickW + gap);
                    double y = startY + row * (brickH + gap);
                    
                    if (row == 0 || row == diamondPattern.length - 1) {
                        bricks.add(new GiftBrick(x, y));
                    } else if (row == 1 || row == diamondPattern.length - 2) {
                        bricks.add(new HardBrick(x, y));
                    } else {
                        bricks.add(new NormalBrick(x, y));
                    }
                }
            }
        }
        
        return bricks;
    }

    /**
     * 生成箭头图案
     */
    public List<Brick> generateArrowPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();
        
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double startY = 60;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;
        
        int[][] arrowPattern = {
            {0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0}
        };
        
        for (int row = 0; row < arrowPattern.length; row++) {
            for (int col = 0; col < arrowPattern[row].length; col++) {
                if (arrowPattern[row][col] == 1) {
                    double x = centerX - (3 * (brickW + gap)) + col * (brickW + gap);
                    double y = startY + row * (brickH + gap);
                    bricks.add(new NormalBrick(x, y));
                }
            }
        }
        
        return bricks;
    }

    /**
     * 生成虚拟小球
     * 在砖块间隙处放置，数量随关卡递增
     * 
     * @param level 关卡编号
     * @param brickList 砖块列表（用于避开砖块位置）
     * @return 虚拟小球列表
     */
    public List<VirtualBall> generateVirtualBalls(int level, List<Brick> brickList) {
        virtualBalls.clear();
        
        // 虚拟小球数量：第1关3个，之后每关+1
        int virtualBallCount = 3 + (level - 1);
        
        // 无尽模式增加虚拟小球数量
        if (currentMode == GameMode.ENDLESS) {
            virtualBallCount += 2;
        }
        
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;
        
        // 定义可能的虚拟小球位置（砖块间隙）
        List<double[]> possiblePositions = new ArrayList<>();
        
        // 在砖块网格的间隙处生成候选位置
        int colCount = 10;
        int rowCount = 3 + level / 2;
        
        // 无尽模式调整行列数
        if (currentMode == GameMode.ENDLESS) {
            colCount = 14 + (level / 2);
            if (colCount > 20) colCount = 20;
            rowCount = 6 + level / 2;
            if (rowCount > 12) rowCount = 12;
            gap = 6;
        }
        
        double startX = 30;
        double startY = 40;
        
        if (currentMode == GameMode.ENDLESS) {
            startX = 10;
        }
        
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
                    possiblePositions.add(new double[]{x, y});
                }
            }
        }
        
        // 从候选位置中选择指定数量的位置，确保彼此之间有足够距离
        double minDistance = 60.0; // 最小间距60像素
        
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
            
            // 随机选择一个有效位置
            int index = (int) (Math.random() * validPositions.size());
            double[] selectedPos = validPositions.get(index);
            virtualBalls.add(new VirtualBall(selectedPos[0], selectedPos[1]));
            
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

    /**
     * 获取虚拟小球列表
     */
    public List<VirtualBall> getVirtualBalls() {
        return virtualBalls;
    }


//    获取当前关卡的背景颜色

    public Color getBackgroundColor() {
        return backgroundColor;
    }
//    获取当前关卡的小球颜色
    public Color getBallColor() {
        return ballColor;
    }

//    获取当前关卡的砖块颜色
    public Color getBrickStyle() {
        return brickStyle;
    }
//    判断当前关卡是否使用矩形砖块
    public boolean isRectangularBricks() {
        return isRectangularBricks;
    }

    /**
     * 获取当前关卡编号
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 获取当前关卡的主题名称（用于UI显示）
     *
     * @return 主题名称字符串
     */
    public String getLevelThemeName() {
        if (currentMode == GameMode.ENDLESS) {
            return "无尽关卡 " + currentLevel;
        }
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
