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
                return generateArrowPatternBricks(level);
            case 4:
                return generateCirclePatternBricks(level);
            case 0:
                return generateStarPatternBricks(level);
            default:
                return generateHeartPatternBricks(level);
        }
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
        // 以爱心图案为基础，叠加一圈外延，间距更小，密度更高
        List<Brick> bricks = generateHeartPatternBricks(level);
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 6;
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double startY = 40;
        double[] rates = getTypeRates(level, true);

        // 外环：中心上下左右小十字
        double[][] offsets = {{0, - (brickH + gap)}, {0, (brickH + gap)}, {-(brickW + gap), 0}, {(brickW + gap), 0}};
        for (double[] off : offsets) {
            double x = centerX + off[0];
            double y = startY + 2 * (brickH + gap) + off[1];
            bricks.add(createBrickByRates(x, y, rates));
        }

        // 在心形主要区域附近增加少量随机砖块以提高数量
        for (int i = 0; i < 12; i++) {
            double x = centerX + (Math.random() - 0.5) * 220;
            double y = startY + Math.random() * 180;
            if (x > 10 && x < GameConstant.GAME_WIDTH - 10) {
                bricks.add(createBrickByRates(x, y, rates));
            }
        }

        return bricks;
    }

    private List<Brick> generateAdvancedTheme2(int level) {
        // 菱形基础上做多层填充，外层出现更多坚硬砖
        List<Brick> bricks = new ArrayList<>();
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 6;
        double[] rates = getTypeRates(level, true);

        int layers = 4 + level / 3; // 增加层数
        for (int layer = 0; layer < layers; layer++) {
            int cols = 7 + layer;
            for (int c = 0; c < cols; c++) {
                double x = centerX - (cols / 2.0) * (brickW + gap) + c * (brickW + gap);
                double y = 50 + layer * (brickH + gap);
                bricks.add(createBrickByRates(x, y, rates));
            }
        }

        // 随机插入一些礼物砖
        for (int i = 0; i < 4; i++) {
            double x = 80 + Math.random() * (GameConstant.GAME_WIDTH - 160);
            double y = 60 + Math.random() * 200;
            bricks.add(new GiftBrick(x, y));
        }

        return bricks;
    }

    private List<Brick> generateAdvancedTheme3(int level) {
        // 圆形/环形叠加：更密集的圆环与中心聚集
        List<Brick> bricks = new ArrayList<>();
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = 150;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double[] rates = getTypeRates(level, true);

        int rings = 3 + level / 3;
        for (int r = 0; r < rings; r++) {
            double radius = 60 + r * 30;
            int segments = 10 + r * 2;
            for (int s = 0; s < segments; s++) {
                double angle = 2 * Math.PI * s / segments;
                double x = centerX + Math.cos(angle) * radius - brickW / 2;
                double y = centerY + Math.sin(angle) * radius - brickH / 2;
                bricks.add(createBrickByRates(x, y, rates));
            }
        }

        // 中心密集填充
        for (int i = 0; i < 8 + level; i++) {
            double x = centerX + (Math.random() - 0.5) * 80;
            double y = centerY + (Math.random() - 0.5) * 80;
            bricks.add(createBrickByRates(x, y, rates));
        }

        return bricks;
    }

    private List<Brick> generateAdvancedTheme4(int level) {
        // 箭头/向心排列叠加，增加行数并混合类型
        List<Brick> bricks = new ArrayList<>();
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 6;
        double[] rates = getTypeRates(level, true);

        int rows = 8 + level / 2;
        for (int row = 0; row < rows; row++) {
            int cols = 7 + (row % 3);
            for (int col = 0; col < cols; col++) {
                double x = centerX - (cols / 2.0) * (brickW + gap) + col * (brickW + gap);
                double y = 40 + row * (brickH + gap);
                bricks.add(createBrickByRates(x, y, rates));
            }
        }

        return bricks;
    }

    private List<Brick> generateAdvancedTheme5(int level) {
        // 星形加强版：在原星形的基础上增加外圈与随机填充
        List<Brick> base = generateStarPatternBricks(level);
        List<Brick> bricks = new ArrayList<>(base);
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double[] rates = getTypeRates(level, true);

        // 增加外圈星点
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double startY = 60;
        for (int i = 0; i < 12; i++) {
            double x = centerX + (Math.random() - 0.5) * 340;
            double y = startY + Math.random() * 220;
            bricks.add(createBrickByRates(x, y, rates));
        }

        return bricks;
    }
    //无尽模式：生成高密度随机砖块

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
  //生成圆形图案
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
                    
                    double[] rates = getTypeRates(level, false);
                    bricks.add(createBrickByRates(x, y, rates));
                }
            }
        }
        
        return bricks;
    }
    //生成星形图案
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
                    
                    double[] rates = getTypeRates(level, false);
                    bricks.add(createBrickByRates(x, y, rates));
                }
            }
        }
        
        return bricks;
    }
  /* 生成爱心形状的砖块布局
   @param level 关卡编号
   @return 爱心形状的砖块列表*/
    public List<Brick> generateHeartPatternBricks(int level) {
        List<Brick> bricks = new ArrayList<>();
        
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double startY = 60;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;
       /* 爱心图案设计（使用网格坐标）
        第1行：   X X     X X
        第2行： X X X X X X X
        第3行： X X X X X X X
        第4行：   X X X X X
        第5行：     X X X
        第6行：       X*/
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
                    
                    double[] rates = getTypeRates(level, false);
                    bricks.add(createBrickByRates(x, y, rates));
                }
            }
        }
        
        return bricks;
    }
    /*生成自定义图形砖块（支持多种图案）
    @param level 关卡编号
    @param patternType 图案类型 ("heart", "diamond", "arrow")
    @return 图形砖块列表*/
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
    //生成菱形图案
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
                        // 外层第二行优先为坚硬砖，但仍用概率以便随关卡变化
                        double[] rates = getTypeRates(level, false);
                        // 提高 hard 的权重临时替换
                        rates[1] = Math.max(rates[1], 0.35);
                        bricks.add(createBrickByRates(x, y, rates));
                    } else {
                        double[] rates = getTypeRates(level, false);
                        bricks.add(createBrickByRates(x, y, rates));
                    }
                }
            }
        }
        
        return bricks;
    }
    //生成箭头图案
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
                    double[] rates = getTypeRates(level, false);
                    bricks.add(createBrickByRates(x, y, rates));
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
        
        // 虚拟小球数量：第1关3个，之后每关+1
        int virtualBallCount;
        
        // 如果是闯关模式并且是第6-10关，使用第1-5关的基础数量，并在基础上每关+1（即对应该主题的基础数量再+1）
        if (currentMode == GameMode.CAMPAIGN && level >= 6 && level <= 10) {
            int baseLevel = level - 5;
            int baseCount = 3 + (baseLevel - 1); // 基础数量来自对应的第1-5关
            virtualBallCount = baseCount + 1; // 在基础上每关添加1个
        } else {
            virtualBallCount = 3 + (level - 1);
        }
        
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
