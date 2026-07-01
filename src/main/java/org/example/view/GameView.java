package org.example.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import org.example.model.*;
import org.example.service.LevelManager;
import org.example.service.ScoreFile;
import org.example.service.ScoreManager;

/**
 * 游戏视图类（View层）
 * 负责所有画面的绘制，是玩家看到的唯一窗口
 * 不修改任何游戏数据，只从 Model 读取数据并渲染
 */
public class GameView {
    // ==================== 画布相关 ====================
    private Canvas canvas;                    // 游戏画布（所有绘制都在这块"画布"上进行）
    private GraphicsContext gc;               // 画笔对象（用它在画布上画各种图形、文字、图片）
    private AbyssBrickGame game;              // 游戏模型引用（用来读取游戏数据，如分数、关卡、砖块等）

    // ==================== 暂停菜单按钮尺寸常量 ====================
    private static final int PAUSE_BUTTON_WIDTH = 200;    // 按钮宽度
    private static final int PAUSE_BUTTON_HEIGHT = 50;    // 按钮高度
    private static final int PAUSE_BUTTON_GAP = 20;       // 按钮之间的间距
    private static final int PAUSE_START_Y = 250;         // 第一个按钮的Y坐标起始位置

    // ==================== 暂停菜单的三个按钮 ====================
    private PauseButton resumeButton;         // "继续游戏"按钮
    private PauseButton restartButton;        // "重新开始"按钮
    private PauseButton exitButton;           // "退出到主菜单"按钮

    // ==================== 按钮悬停状态（鼠标移上去时高亮） ====================
    private boolean resumeHovered = false;    // 继续按钮是否被鼠标悬停
    private boolean restartHovered = false;   // 重新开始按钮是否被鼠标悬停
    private boolean exitHovered = false;      // 退出按钮是否被鼠标悬停

    // ==================== 字体常量（统一使用微软雅黑） ====================
    private static final Font FONT_UI_SMALL = Font.font("Microsoft YaHei", 14);    // 小字体（操作提示）
    private static final Font FONT_UI_MEDIUM = Font.font("Microsoft YaHei", 18);   // 中字体（UI信息）
    private static final Font FONT_UI_LARGE = Font.font("Microsoft YaHei", 24);    // 大字体（按钮文字）
    private static final Font FONT_TITLE = Font.font("Microsoft YaHei", 48);       // 标题字体（暂停/结束）
    private static final Font FONT_VICTORY = Font.font("Microsoft YaHei", 60);     // 胜利标题字体
    private static final Font FONT_COUNTDOWN = Font.font("Microsoft YaHei", 120);  // 倒计时数字字体（超大）

    // ==================== 构造方法 ====================

    /**
     * 创建游戏视图
     * @param stage  游戏窗口
     * @param canvas 画布
     * @param game   游戏模型
     */
    public GameView(Stage stage, Canvas canvas, AbyssBrickGame game) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();  // 从画布获取画笔
        this.game = game;

        initializePauseButtons();  // 初始化暂停菜单按钮

        // 创建布局：BorderPane 是最基本的 JavaFX 布局，把画布放在正中间
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        // 创建场景（Scene），设置大小为游戏窗口大小
        stage.setScene(new javafx.scene.Scene(root, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT));
        stage.setTitle("Abyss Brick Breaker - 打砖块游戏");
        stage.setResizable(false);  // 禁止调整窗口大小

        // 让窗口在屏幕正中央显示
        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        stage.setX((screenWidth - AbyssBrickGame.GAME_WIDTH) / 2);
        stage.setY((screenHeight - AbyssBrickGame.GAME_HEIGHT) / 2);
    }

    /**
     * 初始化暂停菜单的三个按钮
     * 三个按钮水平居中，垂直排列，间距为 PAUSE_BUTTON_GAP
     */
    private void initializePauseButtons() {
        double centerX = AbyssBrickGame.GAME_WIDTH / 2.0;       // 屏幕中心X
        double buttonX = centerX - PAUSE_BUTTON_WIDTH / 2.0;    // 按钮左边缘X（让按钮居中）
        
        // 第一个按钮：继续游戏
        resumeButton = new PauseButton(buttonX, PAUSE_START_Y, PAUSE_BUTTON_WIDTH, PAUSE_BUTTON_HEIGHT, "继续游戏");
        // 第二个按钮：重新开始（在第一个按钮下方，间隔20像素）
        restartButton = new PauseButton(buttonX, PAUSE_START_Y + PAUSE_BUTTON_HEIGHT + PAUSE_BUTTON_GAP, 
                                       PAUSE_BUTTON_WIDTH, PAUSE_BUTTON_HEIGHT, "重新开始");
        // 第三个按钮：退出到主菜单（在第二个按钮下方）
        exitButton = new PauseButton(buttonX, PAUSE_START_Y + (PAUSE_BUTTON_HEIGHT + PAUSE_BUTTON_GAP) * 2, 
                                    PAUSE_BUTTON_WIDTH, PAUSE_BUTTON_HEIGHT, "退出到主菜单");
    }

    // ==================== 渲染主方法（核心入口） ====================

    /**
     * 渲染主方法，每帧调用一次
     * 按照从底到顶的图层顺序绘制，类似游戏引擎的渲染管线
     * 
     * @param showingModeSelection  是否显示模式选择界面
     * @param showingLevelSelection 是否显示关卡选择界面
     * @param showingPauseMenu      是否显示暂停菜单
     * @param modeSelector          模式选择器
     * @param levelSelector         关卡选择器
     * @param gamePaused            游戏是否暂停
     */
    public void render(boolean showingModeSelection, boolean showingLevelSelection, boolean showingPauseMenu,
                       GameModeSelector modeSelector, LevelSelector levelSelector, boolean gamePaused) {
        // ① 如果正在显示模式选择界面 → 委托给 modeSelector 绘制，直接返回
        if (showingModeSelection) {
            modeSelector.render();
            return;
        }

        // ② 如果正在显示关卡选择界面 → 委托给 levelSelector 绘制，直接返回
        if (showingLevelSelection) {
            levelSelector.render();
            return;
        }

        // ③ 正式游戏画面：按图层顺序从底到顶绘制
        LevelManager levelManager = game.getLevelManager();

        // ③-1 绘制背景：根据当前关卡获取对应主题的背景图
        ThemeType theme = ImageLoader.getThemeByLevel(game.getCurrentLevel());
        Image bg = ImageLoader.getBackgroundByTheme(theme);

        if (bg != null) {
            // 有背景图 → 全屏绘制
            gc.drawImage(bg, 0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        } else {
            // 没有背景图 → 填充黑色
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        }

        // ③-2 绘制游戏元素（从底到顶）
        drawBricks();          // 砖块层
        drawBaffle();          // 挡板
        drawBalls();           // 真实小球
        drawVirtualBalls();    // 虚拟小球（虚线描边 + 外发光效果）
        drawUI(levelManager);  // 顶部信息栏（分数、关卡、生命等）

        // ③-3 绘制各种遮罩层（可选）
        if (game.isCountdownActive()) {
            drawCountdownOverlay();  // 开局3秒倒计时遮罩
        }

        if (showingPauseMenu || gamePaused) {
            drawPauseMenu();         // 暂停菜单遮罩
        }

        if (game.isVictoryScreen()) {
            drawVictoryOverlay();    // 胜利画面遮罩
        }

        // ③-4 游戏结束画面（生命归零且不在胜利画面时）
        if (!game.isGameRunning() && game.getLifeCount() <= 0 && !game.isVictoryScreen()) {
            drawGameOverOverlay();
        }
    }

    // ==================== 暂停菜单交互 ====================

    /**
     * 处理鼠标移动事件：更新按钮的悬停状态
     * 鼠标移到按钮上时，按钮会高亮显示
     */
    public void handlePauseMenuMouseMove(double mouseX, double mouseY) {
        resumeHovered = resumeButton.contains(mouseX, mouseY);
        restartHovered = restartButton.contains(mouseX, mouseY);
        exitHovered = exitButton.contains(mouseX, mouseY);
    }

    /**
     * 处理暂停菜单的鼠标点击事件
     * @return 点击的按钮类型："resume"=继续, "restart"=重新开始, "exit"=退出, null=没点到按钮
     */
    public String handlePauseMenuClick(double mouseX, double mouseY) {
        if (resumeButton.contains(mouseX, mouseY)) {
            return "resume";
        } else if (restartButton.contains(mouseX, mouseY)) {
            return "restart";
        } else if (exitButton.contains(mouseX, mouseY)) {
            return "exit";
        }
        return null;
    }

    // ==================== 倒计时画面 ====================

    /**
     * 绘制开局倒计时遮罩（3、2、1）
     * 半透明黑色背景 + 大号倒计时数字 + "准备开始！"文字
     */
    private void drawCountdownOverlay() {
        // 半透明黑色遮罩（alpha=0.6）
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        // 金黄色大号倒计时数字（如 "3"、"2"、"1"）
        gc.setFill(Color.web("#FFD93D"));
        gc.setFont(FONT_COUNTDOWN);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(game.getCountdownSeconds()),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 40);

        // 白色 "准备开始！" 文字
        gc.setFill(Color.WHITE);
        gc.setFont(FONT_UI_LARGE);
        gc.fillText("准备开始！",
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 100);
    }

    // ==================== 砖块绘制 ====================

    /**
     * 绘制所有存活的砖块
     * 三角形砖块需要特殊处理：用 clip() 裁剪为三角区域再贴图
     * 其他砖块直接绘制矩形图片
     * 血量越低的砖块越透明（通过 setGlobalAlpha 实现）
     */
    private void drawBricks() {

        for (Brick brick : game.getBrickList()) {
            if (brick.isDead()) {
                continue;  // 跳过已死亡的砖块
            }

            Image brickImage = getBrickImage(brick);  // 根据砖块类型获取对应图片

            if (brickImage != null) {
                double alpha = getADouble(brick);  // 根据血量计算透明度

                gc.setGlobalAlpha(alpha);  // 设置透明度（血量越低越透明）
                if (brick.getShape() == Brick.BrickShape.TRIANGLE) {
                    // 三角形砖块：需要裁剪
                    gc.save();

                    double x = brick.getX();
                    double y = brick.getY();
                    double w = brick.getWidth();
                    double h = brick.getHeight();

                    // 创建三角形路径：顶点在上方中间，底边在下方
                    gc.beginPath();
                    gc.moveTo(x + w / 2, y);       // 顶部中点
                    gc.lineTo(x + w, y + h);        // 右下角
                    gc.lineTo(x, y + h);            // 左下角
                    gc.closePath();

                    gc.clip();  // 裁剪：后续绘制只在三角形区域内显示
                    gc.drawImage(brickImage, x, y, w, h);
                    gc.restore();  // 恢复画布状态，取消裁剪
                } else {
                    // 矩形砖块：直接绘制
                    gc.drawImage(brickImage, brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
                }
                gc.setGlobalAlpha(1.0);  // 恢复透明度为1.0（不影响后续绘制）
            }
        }
    }

    /**
     * 根据砖块的血量计算透明度
     * 血量越低，透明度越低（看起来更"虚弱"）
     * 
     * @param brick 砖块对象
     * @return 透明度值（0.0~1.0）
     */
    private static double getADouble(Brick brick) {
        double alpha = 1.0;  // 默认完全不透明

        if (brick instanceof HardBrick) {
            // 硬砖块：血量越低越透明
            if (brick.getHp() == 1) {
                alpha = 0.5;    // 1滴血：50%透明
            } else if (brick.getHp() == 2) {
                alpha = 0.75;   // 2滴血：75%不透明
            }
        } else if (brick instanceof GiftBrick) {
            // 礼物砖块：透明度随血量线性变化
            int maxHp = 3;
            double currentHp = brick.getHp();
            alpha = 0.4 + (currentHp / maxHp) * 0.6;  // HP=3时alpha=1.0，HP=1时alpha≈0.6
        }
        return alpha;
    }

    /**
     * 根据砖块类型获取对应的图片
     * 使用 instanceof 判断砖块的具体类型
     * 
     * @param brick 砖块对象
     * @return 对应的砖块图片
     */
    private static Image getBrickImage(Brick brick) {
        Image brickImage;

        if (brick instanceof NormalBrick) {
            brickImage = ImageLoader.BRICK_NORMAL_IMG;      // 普通砖块图片
        } else if (brick instanceof HardBrick) {
            brickImage = ImageLoader.BRICK_HARD_IMG;        // 硬砖块图片
        } else if (brick instanceof GiftBrick) {
            brickImage = ImageLoader.BRICK_GIFT_IMG;        // 礼物砖块图片
        } else if (brick instanceof TriangleBrick) {
            brickImage = ImageLoader.BRICK_TRIANGLE_IMG;    // 三角形砖块图片
        } else {
            brickImage = ImageLoader.BRICK_NORMAL_IMG;      // 默认用普通砖块图片
        }
        return brickImage;
    }

    // ==================== 挡板绘制 ====================

    /**
     * 绘制挡板
     * 先填充颜色，再画白色边框，使用圆角矩形（8像素圆角）
     */
    private void drawBaffle() {
        Baffle baffle = game.getBaffle();
        // 填充挡板颜色
        gc.setFill(baffle.getColor());
        gc.fillRoundRect(baffle.getX(), baffle.getY(),
                baffle.getWidth(), baffle.getHeight(), 8, 8);

        // 画白色边框
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(baffle.getX(), baffle.getY(),
                baffle.getWidth(), baffle.getHeight(), 8, 8);
    }

    // ==================== 小球绘制 ====================

    /**
     * 绘制所有真实小球
     * 每个小球：先填充颜色画实心圆，再画白色描边
     * 注意：fillOval/strokeOval 的参数是左上角坐标，不是中心坐标
     * 所以要用 (x - radius, y - radius) 作为起点
     */
    private void drawBalls() {
        for (Ball ball : game.getBallList()) {
            // 填充小球颜色
            gc.setFill(ball.getColor());
            gc.fillOval(ball.getX() - ball.getRadius(),
                    ball.getY() - ball.getRadius(),
                    ball.getRadius() * 2, ball.getRadius() * 2);

            // 画白色描边
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeOval(ball.getX() - ball.getRadius(),
                    ball.getY() - ball.getRadius(),
                    ball.getRadius() * 2, ball.getRadius() * 2);
        }
    }

    /**
     * 绘制所有虚拟小球
     * 虚拟球是"假球"，真实球碰到它会变成新球
     * 视觉效果：虚线描边 + 外发光（粉色光晕）
     */
    private void drawVirtualBalls() {
        for (VirtualBall virtualBall : game.getVirtualBallList()) {
            if (!virtualBall.isActive()) {
                continue;  // 跳过已失效的虚拟球
            }

            // 填充虚拟球颜色
            gc.setFill(virtualBall.getColor());
            gc.fillOval(virtualBall.getX() - virtualBall.getRadius(),
                    virtualBall.getY() - virtualBall.getRadius(),
                    virtualBall.getRadius() * 2, virtualBall.getRadius() * 2);

            // 画白色虚线描边（5像素线段，5像素间隔）
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.setLineDashes(5, 5);  // 设置虚线样式
            gc.strokeOval(virtualBall.getX() - virtualBall.getRadius(),
                    virtualBall.getY() - virtualBall.getRadius(),
                    virtualBall.getRadius() * 2, virtualBall.getRadius() * 2);
            gc.setLineDashes(0);  // 恢复实线（不影响后续绘制）

            // 画外发光效果（粉色半透明光晕）
            gc.setStroke(Color.web("#FF6B9D", 0.3));  // 30%透明度的粉色
            gc.setLineWidth(4);
            gc.strokeOval(virtualBall.getX() - virtualBall.getRadius() - 3,
                    virtualBall.getY() - virtualBall.getRadius() - 3,
                    virtualBall.getRadius() * 2 + 6, virtualBall.getRadius() * 2 + 6);
        }
    }

    // ==================== UI信息绘制 ====================

    /**
     * 绘制顶部UI信息栏
     * 左侧：分数、关卡、最高分
     * 右侧：生命值、剩余小球数
     * 中间：操作提示
     */
    private void drawUI(LevelManager levelManager) {
        ScoreManager scoreManager = game.getScoreManager();

        // ===== 左侧信息 =====
        gc.setFill(Color.WHITE);
        gc.setFont(FONT_UI_MEDIUM);
        gc.setTextAlign(TextAlignment.LEFT);  // 左对齐

        gc.fillText("分数: " + scoreManager.getScoreValue(), 10, 25);

        gc.fillText(
                "关卡: " + game.getCurrentLevel() + " - " + levelManager.getLevelThemeName(),
                10, 50
        );

        // 根据当前模式读取对应最高分（闯关模式和无尽模式分开记录）
        int highScore = ScoreFile.loadHighScore(game.getCurrentMode());
        gc.fillText("最高分: " + highScore, 10, 75);

        // ===== 右侧信息 =====
        gc.setTextAlign(TextAlignment.RIGHT);  // 右对齐
        gc.fillText("生命值: " + game.getLifeCount(),
                AbyssBrickGame.GAME_WIDTH - 10, 25);
        gc.fillText("剩余小球: " + game.getBallList().size(),
                AbyssBrickGame.GAME_WIDTH - 10, 50);

        // ===== 中间操作提示 =====
        gc.setTextAlign(TextAlignment.CENTER);  // 居中对齐
        gc.setFont(FONT_UI_SMALL);
        gc.fillText("鼠标/A D键移动挡板 | ESC暂停/菜单",
                AbyssBrickGame.GAME_WIDTH / 2.0, 20);
    }

    // ==================== 暂停菜单绘制 ====================

    /**
     * 绘制暂停菜单
     * 半透明黑色遮罩 + "游戏暂停"标题 + 三个按钮
     */
    private void drawPauseMenu() {
        // 半透明黑色遮罩（70%不透明）
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        // 金黄色 "游戏暂停" 标题
        gc.setFill(Color.web("#FFD93D"));
        gc.setFont(FONT_TITLE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("游戏暂停",
                AbyssBrickGame.GAME_WIDTH / 2.0,
                180);

        // 绘制三个按钮（传入悬停状态，悬停的按钮会高亮）
        resumeButton.render(gc, resumeHovered);
        restartButton.render(gc, restartHovered);
        exitButton.render(gc, exitHovered);
    }

    // ==================== 胜利画面绘制 ====================

    /**
     * 绘制胜利画面遮罩
     * 由多个子方法组合绘制：背景 → 标题 → 统计 → 评级 → 倒计时 → 提示 → 粒子
     */
    private void drawVictoryOverlay() {
        // 半透明黑色背景
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        drawVictoryBackground();    // ① 渐变背景条
        drawVictoryTitle();         // ② "关卡完成！" 标题
        drawVictoryStats();         // ③ 本关统计数据（得分、关卡、生命）
        drawVictoryRating();        // ④ 星级评价（1~3星）
        drawVictoryCountdown();     // ⑤ 进入下一关的倒计时
        drawVictoryHint();          // ⑥ "点击鼠标左键跳过" 提示
        drawVictoryParticles();     // ⑦ 彩色粒子特效
    }
    
    /**
     * 绘制胜利画面的渐变背景条
     * 使用线性渐变：金黄色 → 粉红色 → 天蓝色
     */
    private void drawVictoryBackground() {
        double centerY = AbyssBrickGame.GAME_HEIGHT / 2.0;
        
        // 定义渐变色标（3个颜色过渡点）
        javafx.scene.paint.Stop[] stops = new javafx.scene.paint.Stop[] {
            new javafx.scene.paint.Stop(0, Color.web("#FFD93D", 0.3)),    // 起点：金黄色
            new javafx.scene.paint.Stop(0.5, Color.web("#FF6B9D", 0.2)),  // 中间：粉红色
            new javafx.scene.paint.Stop(1, Color.web("#4A90E2", 0.3))     // 终点：天蓝色
        };
        
        // 创建从上到下的线性渐变
        javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
            0, 0, 0, AbyssBrickGame.GAME_HEIGHT, true, 
            javafx.scene.paint.CycleMethod.NO_CYCLE, stops);
        
        // 在屏幕中间画一个300像素高的渐变条
        gc.setFill(gradient);
        gc.fillRect(0, centerY - 150, AbyssBrickGame.GAME_WIDTH, 300);
    }
    
    /**
     * 绘制胜利标题 "🎉 关卡完成！ 🎉"
     * 先画描边（橙色），再画填充（金黄色），形成立体效果
     */
    private void drawVictoryTitle() {
        double centerY = AbyssBrickGame.GAME_HEIGHT / 2.0;
        
        gc.setFill(Color.web("#FFD93D"));//金黄色
        gc.setFont(FONT_VICTORY);
        gc.setTextAlign(TextAlignment.CENTER);
        
        // 先画橙色描边
        gc.setStroke(Color.web("#FFA500"));
        gc.setLineWidth(3);
        gc.strokeText("🎉 关卡完成！ 🎉",
                AbyssBrickGame.GAME_WIDTH / 2.0,
                centerY - 100);
        // 再画金黄色填充
        gc.fillText("🎉 关卡完成！ 🎉",
                AbyssBrickGame.GAME_WIDTH / 2.0,
                centerY - 100);
        
        gc.setLineWidth(1);  // 恢复默认线宽
    }
    
    /**
     * 绘制本关统计数据
     * 显示：本关得分、当前关卡、剩余生命
     */
    private void drawVictoryStats() {
        double centerY = AbyssBrickGame.GAME_HEIGHT / 2.0;
        double startX = AbyssBrickGame.GAME_WIDTH / 2.0 - 150;  // 文字起始X
        double startY = centerY - 40;                            // 文字起始Y
        
        gc.setFont(FONT_UI_MEDIUM);
        gc.setTextAlign(TextAlignment.LEFT);
        
        // 本关得分（金黄色数字）
        gc.setFill(Color.WHITE);
        gc.fillText("本关得分:", startX, startY);
        gc.setFill(Color.web("#FFD93D"));
        gc.fillText(String.valueOf(game.getScoreManager().getScoreValue()), startX + 120, startY);
        
        // 当前关卡（绿色数字）
        gc.setFill(Color.WHITE);
        gc.fillText("当前关卡:", startX, startY + 35);
        gc.setFill(Color.web("#00FF88"));
        gc.fillText(String.valueOf(game.getCurrentLevel()), startX + 120, startY + 35);
        
        // 剩余生命（粉色数字）
        gc.setFill(Color.WHITE);
        gc.fillText("剩余生命:", startX, startY + 70);
        gc.setFill(Color.web("#FF6B9D"));
        gc.fillText(String.valueOf(game.getLifeCount()), startX + 120, startY + 70);
    }
    
    /**
     * 绘制星级评价
     * 根据分数评定1~3星，并显示对应的评价文字
     * 分数 > 1000 → 3星 "太棒了！"
     * 分数 > 500  → 2星 "不错哦！"
     * 其他       → 1星 "继续加油！"
     */
    private void drawVictoryRating() {
        double centerY = AbyssBrickGame.GAME_HEIGHT / 2.0;
        double centerX = AbyssBrickGame.GAME_WIDTH / 2.0;
        
        // 根据分数计算星星数量
        int score = game.getScoreManager().getScoreValue();
        int stars = 1;
        if (score > 1000) stars = 3;
        else if (score > 500) stars = 2;
        
        // 拼接星星emoji字符串
        StringBuilder starStr = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            starStr.append("⭐");
        }
        
        // 绘制大号星星
        gc.setFont(Font.font("Microsoft YaHei", 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web("#FFD93D"));
        gc.fillText(starStr.toString(), centerX, centerY + 140);
        
        // 绘制评价文字
        String ratingText;
        if (stars == 3) ratingText = "太棒了！";
        else if (stars == 2) ratingText = "不错哦！";
        else ratingText = "继续加油！";
        
        gc.setFont(FONT_UI_MEDIUM);
        gc.setFill(Color.web("#00FF88"));
        gc.fillText(ratingText, centerX, centerY + 175);
    }
    
    /**
     * 绘制胜利画面的倒计时数字
     * 显示还有几秒进入下一关，带蓝色描边效果
     */
    private void drawVictoryCountdown() {
        double centerY = AbyssBrickGame.GAME_HEIGHT / 2.0;
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 36));
        gc.setTextAlign(TextAlignment.CENTER);
        
        // 先画蓝色描边
        gc.setStroke(Color.web("#4A90E2"));
        gc.setLineWidth(2);
        gc.strokeText(String.valueOf(game.getVictoryCountdownSeconds()),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                centerY + 230);
        // 再画白色填充
        gc.fillText(String.valueOf(game.getVictoryCountdownSeconds()),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                centerY + 230);
        
        gc.setLineWidth(1);  // 恢复默认线宽
    }
    
    /**
     * 绘制胜利画面的提示文字
     * 告诉玩家可以点击鼠标跳过倒计时
     */
    private void drawVictoryHint() {
        double centerY = AbyssBrickGame.GAME_HEIGHT / 2.0;
        
        gc.setFont(FONT_UI_MEDIUM);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.fillText("点击鼠标左键跳过",
                AbyssBrickGame.GAME_WIDTH / 2.0,
                centerY + 280);
    }
    
    /**
     * 绘制胜利画面的粒子特效
     * 遍历所有存活粒子，调用每个粒子的 render 方法
     */
    private void drawVictoryParticles() {
        for (org.example.model.VictoryParticle particle : game.getVictoryParticles()) {
            particle.render(gc);
        }
    }
    
    // ==================== 游戏结束画面 ====================

    /**
     * 绘制游戏结束遮罩
     * 显示：游戏结束标题、最终分数、到达关卡、历史最高分、是否新纪录
     */
    private void drawGameOverOverlay() {
        // 深色遮罩（80%不透明）
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        // 红色 "游戏结束" 标题
        gc.setFill(Color.RED);
        gc.setFont(FONT_TITLE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("游戏结束", AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 - 80);

        // 白色统计信息
        gc.setFill(Color.WHITE);
        gc.setFont(FONT_UI_LARGE);
        gc.fillText("最终分数: " + game.getScoreManager().getScoreValue(),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 - 20);

        gc.fillText("到达关卡: " + game.getCurrentLevel(),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 20);

        // 金黄色历史最高分
        int highScore = ScoreFile.loadHighScore(game.getCurrentMode());
        gc.setFill(Color.web("#FFD93D"));
        gc.fillText("历史最高分: " + highScore,
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 60);

        // 如果打破纪录，显示绿色 "新纪录！"
        if (game.getCurrentScore() >= highScore && game.getCurrentScore() > 0) {
            gc.setFill(Color.web("#00FF88"));
            gc.setFont(FONT_UI_MEDIUM);
            gc.fillText("新纪录！",
                    AbyssBrickGame.GAME_WIDTH / 2.0,
                    AbyssBrickGame.GAME_HEIGHT / 2.0 + 95);
        }

        // 白色提示文字
        gc.setFill(Color.WHITE);
        gc.setFont(FONT_UI_MEDIUM);
        gc.fillText("点击鼠标重新开始", AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 130);
    }

    // ==================== Getter方法 ====================

    public double getCanvasHeight() {
        return canvas.getHeight();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    // ==================== 内部类：暂停菜单按钮 ====================

    /**
     * 暂停菜单按钮的内部类
     * 每个按钮有位置、大小、文字，支持悬停高亮和点击检测
     */
    private static class PauseButton {
        private double x, y, width, height;  // 按钮的位置和尺寸
        private String text;                  // 按钮上显示的文字

        /**
         * 创建按钮
         * @param x      左上角X坐标
         * @param y      左上角Y坐标
         * @param width  宽度
         * @param height 高度
         * @param text   按钮文字
         */
        public PauseButton(double x, double y, double width, double height, String text) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
        }

        /**
         * 绘制按钮
         * @param gc      画笔
         * @param hovered 是否被鼠标悬停（悬停时高亮）
         */
        public void render(GraphicsContext gc, boolean hovered) {
            if (hovered) {
                // 悬停状态：蓝色半透明填充 + 粗白边
                gc.setFill(Color.web("#4A90E2", 0.8));
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(3);
            } else {
                // 普通状态：白色半透明填充 + 细白边
                gc.setFill(Color.web("#FFFFFF", 0.3));
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
            }

            // 画圆角矩形（10像素圆角）
            gc.fillRoundRect(x, y, width, height, 10, 10);
            gc.strokeRoundRect(x, y, width, height, 10, 10);

            // 画按钮文字（居中）
            gc.setFill(Color.WHITE);
            gc.setFont(FONT_UI_LARGE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(text, x + width / 2, y + height / 2 + 7);  // +7 是为了视觉居中
        }

        /**
         * 判断鼠标是否在按钮范围内
         * @param mouseX 鼠标X坐标
         * @param mouseY 鼠标Y坐标
         * @return true=鼠标在按钮内，false=鼠标在按钮外
         */
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                   mouseY >= y && mouseY <= y + height;
        }
    }
}
