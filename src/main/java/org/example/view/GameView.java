package org.example.view;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import org.example.model.*;
import org.example.service.LevelManager;
import org.example.service.ScoreManager;


public class GameView {
    private Stage primaryStage;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    private AbyssBrickGame game;
    private LevelManager levelManager;
    private GameModeSelector modeSelector;
    private LevelSelector levelSelector;

    private AnimationTimer gameLoop;
    private boolean showingModeSelection;
    private boolean showingLevelSelection;
    
    private boolean moveLeftPressed = false;
    private boolean moveRightPressed = false;

    public GameView(Stage stage) {
        this.primaryStage = stage;
        this.levelManager = new LevelManager();
        this.showingModeSelection = true;
        this.showingLevelSelection = false;

        BorderPane root = new BorderPane();

        this.canvas = new Canvas(AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.game = game;

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        stage.setScene(new javafx.scene.Scene(root, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT));
        stage.setTitle("Abyss Brick Breaker - 打砖块游戏");
        stage.setResizable(false);

        this.scene = new Scene(root, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        initGame();
        setupInputHandlers();
        setupGameLoop();
        centerWindow();
    }

    private void initGame() {
        game = new AbyssBrickGame();
        modeSelector = new GameModeSelector(canvas);
        levelSelector = new LevelSelector(canvas, game.getMaxUnlockedLevel());
    }

    private void setupInputHandlers() {
        canvas.setFocusTraversable(true);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.P && !showingModeSelection && !showingLevelSelection) {
                togglePause();
            }
            if (code == KeyCode.ESCAPE && !showingModeSelection && !showingLevelSelection) {
                showPauseMenu();
            }
            if (!showingModeSelection && !showingLevelSelection) {
                if (code == KeyCode.A || code == KeyCode.LEFT) {
                    moveLeftPressed = true;
                }
                if (code == KeyCode.D || code == KeyCode.RIGHT) {
                    moveRightPressed = true;
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if (!showingModeSelection && !showingLevelSelection) {
                if (code == KeyCode.A || code == KeyCode.LEFT) {
                    moveLeftPressed = false;
                }
                if (code == KeyCode.D || code == KeyCode.RIGHT) {
                    moveRightPressed = false;
                }
            }
        });

        canvas.setOnMouseMoved(event -> {
            if (showingModeSelection) {
                modeSelector.handleMouseMove(event.getX(), event.getY());
            } else if (showingLevelSelection) {
                levelSelector.handleMouseMove(event.getX(), event.getY());
            } else {
                double mouseX = event.getX();
                double paddleNewX = mouseX - game.getBaffle().getWidth() / 2;
                double paddleFixedY = GameConstant.GAME_HEIGHT - game.getBaffle().getHeight() - 10;
                game.getBaffle().moveTo(paddleNewX, paddleFixedY);
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (showingModeSelection) {
                GameMode selectedMode = modeSelector.handleClick(event.getX(), event.getY());
                if (selectedMode != null) {
                    if (selectedMode == GameMode.CAMPAIGN) {
                        showingModeSelection = false;
                        showingLevelSelection = true;
                        levelSelector.updateMaxUnlockedLevel(game.getMaxUnlockedLevel());
                    } else {
                        showingModeSelection = false;
                        game.startWithMode(selectedMode);
                    }
                }
            } else if (showingLevelSelection) {
                Integer selectedLevel = levelSelector.handleClick(event.getX(), event.getY());
                if (selectedLevel != null) {
                    showingLevelSelection = false;
                    game.startCampaignLevel(selectedLevel);
                }
            } else if (!game.isGameRunning()) {
                game.restart();
                showingModeSelection = true;
                showingLevelSelection = false;
            }
        });
        
        canvas.requestFocus();
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleKeyboardMovement();
                game.update(now);
                render();
            }
        };
    }

    private void handleKeyboardMovement() {
        if (showingModeSelection || game == null || game.getBaffle() == null) {
            return;
        }
        
        Baffle baffle = game.getBaffle();
        if (moveLeftPressed) {
            baffle.moveLeft();
        }
        if (moveRightPressed) {
            baffle.moveRight();
        }
        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        stage.setX((screenWidth - AbyssBrickGame.GAME_WIDTH) / 2);
        stage.setY((screenHeight - AbyssBrickGame.GAME_HEIGHT) / 2);
    }

    public void render(boolean showingModeSelection, boolean showingLevelSelection,
                       GameModeSelector modeSelector, LevelSelector levelSelector) {
        if (showingModeSelection) {
            modeSelector.render();
            return;
        }
        
        if (showingLevelSelection) {
            levelSelector.render();
            return;
        }

        LevelManager levelManager = game.getLevelManager();

        ThemeType theme = ImageLoader.getThemeByLevel(game.getCurrentLevel());
        javafx.scene.image.Image bg = ImageLoader.getBackgroundByTheme(theme);

        if (bg != null) {
            gc.drawImage(bg, 0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        }

        drawBricks(levelManager, game);
        drawBaffle(game);
        drawBalls(game);
        drawVirtualBalls(game);
        drawUI(levelManager, game);

        if (game.isCountdownActive()) {
            drawCountdownOverlay();
        }

        if (!game.isGameRunning() && game.getLifeCount() <= 0) {
            drawGameOverOverlay(game);
        }
    }
    //绘制倒计时覆盖层
    private void drawCountdownOverlay() {
        // 半透明黑色背景
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        
        // 倒计时数字
        gc.setFill(Color.web("#FFD93D"));
        gc.setFont(Font.font("Microsoft YaHei", 120));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(game.getCountdownSeconds()), 
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 40);
        
        // 提示文字
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 24));
        gc.fillText("准备开始！", 
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 100);
    }

    private void drawBricks(LevelManager levelManager, AbyssBrickGame game) {
        boolean isRectangular = levelManager.isRectangularBricks();

        for (Brick brick : game.getBrickList()) {
            if (!brick.isAlive()) {
                continue;
            }

            // 1. 根据砖块类型选图
            Image brickImage = null; // 【修改1】先初始化为 null，避免报错

            if (brick instanceof NormalBrick) {
                brickImage = ImageLoader.BRICK_NORMAL_IMG;
            } else if (brick instanceof HardBrick) {
                brickImage = ImageLoader.BRICK_HARD_IMG;
            } else if (brick instanceof GiftBrick) {
                brickImage = ImageLoader.BRICK_GIFT_IMG;
            } else if (brick instanceof TriangleBrick) {
                brickImage = ImageLoader.BRICK_TRIANGLE_IMG;
            } else {
                // 默认是普通砖块
                brickImage = ImageLoader.BRICK_NORMAL_IMG;
            }

// 2. 绘制图片
// 【修改2】加上 != null 判断，如果图片没加载出来，就不会画，防止游戏崩溃
            if (brickImage != null) {
                // 根据剩余 HP 计算透明度
                double alpha = 1.0;

                if (brick instanceof HardBrick && brick.getHp() == 1) {
                    alpha = 0.5; // 被打一次后变暗 50%
                }

                gc.setGlobalAlpha(alpha);
                if (brick.getShape() == Brick.BrickShape.TRIANGLE) {

                    // 1. 保存当前画布状态
                    gc.save();

                    // 2. 定义三角形路径
                    double x = brick.getX();
                    double y = brick.getY();
                    double w = brick.getWidth();
                    double h = brick.getHeight();

                    gc.beginPath();
                    gc.moveTo(x + w / 2, y);           // 上顶点
                    gc.lineTo(x + w, y + h);           // 右下
                    gc.lineTo(x, y + h);               // 左下
                    gc.closePath();

                    // 3. 裁剪
                    gc.clip();

                    // 4. 在裁剪区域画图片
                    gc.drawImage(brickImage, x, y, w, h);

                    // 5. 恢复画布
                    gc.restore();

                } else {
                    // 普通矩形砖块
                    gc.drawImage(brickImage, brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
                }
                gc.setGlobalAlpha(1.0); // 一定要恢复！
            }
        }
    }
    
    private int getMaxHpForBrick(Brick brick) {
        if (brick instanceof GiftBrick) {
            return 3;
        } else if (brick instanceof HardBrick) {
            return 2;
        } else {
            return 1;
        }
    }
    
    private Color adjustColorBrightness(Color baseColor, int currentHp, int maxHp) {
        if (maxHp <= 1 || currentHp == maxHp) {
            return baseColor;
        }
        
        double brightnessFactor = (double) currentHp / maxHp;
        brightnessFactor = 0.5 + (brightnessFactor * 0.5);
        
        double r = baseColor.getRed() * brightnessFactor;
        double g = baseColor.getGreen() * brightnessFactor;
        double b = baseColor.getBlue() * brightnessFactor;
        
        return Color.color(r, g, b, baseColor.getOpacity());
    }

    private void drawTriangle(Brick brick) {
        double x = brick.getX();
        double y = brick.getY();
        double w = brick.getWidth();
        double h = brick.getHeight();

        gc.beginPath();
        gc.moveTo(x + w / 2, y);
        gc.lineTo(x + w, y + h);
        gc.lineTo(x, y + h);
        gc.closePath();

        gc.setFill(gc.getFill());
        gc.fill();

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.stroke();
    }

    private void drawBaffle(AbyssBrickGame game) {
        Baffle baffle = game.getBaffle();
        gc.setFill(baffle.getColor());
        gc.fillRoundRect(baffle.getX(), baffle.getY(),
                baffle.getWidth(), baffle.getHeight(), 8, 8);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(baffle.getX(), baffle.getY(),
                baffle.getWidth(), baffle.getHeight(), 8, 8);
    }

    private void drawBalls(AbyssBrickGame game) {
        for (Ball ball : game.getBallList()) {
            gc.setFill(ball.getColor());
            gc.fillOval(ball.getX() - ball.getRadius(),
                    ball.getY() - ball.getRadius(),
                    ball.getRadius() * 2, ball.getRadius() * 2);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeOval(ball.getX() - ball.getRadius(),
                    ball.getY() - ball.getRadius(),
                    ball.getRadius() * 2, ball.getRadius() * 2);
        }
    }

    private void drawVirtualBalls(AbyssBrickGame game) {
        for (VirtualBall virtualBall : game.getVirtualBallList()) {
            if (!virtualBall.isActive()) {
                continue;
            }
            
            // 绘制半透明的虚拟小球
            gc.setFill(virtualBall.getColor());
            gc.fillOval(virtualBall.getX() - virtualBall.getRadius(),
                    virtualBall.getY() - virtualBall.getRadius(),
                    virtualBall.getRadius() * 2, virtualBall.getRadius() * 2);

            // 绘制虚线边框
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.setLineDashes(5, 5);
            gc.strokeOval(virtualBall.getX() - virtualBall.getRadius(),
                    virtualBall.getY() - virtualBall.getRadius(),
                    virtualBall.getRadius() * 2, virtualBall.getRadius() * 2);
            gc.setLineDashes(0);
            
            // 绘制发光效果
            gc.setStroke(Color.web("#FF6B9D", 0.3));
            gc.setLineWidth(4);
            gc.strokeOval(virtualBall.getX() - virtualBall.getRadius() - 3,
                    virtualBall.getY() - virtualBall.getRadius() - 3,
                    virtualBall.getRadius() * 2 + 6, virtualBall.getRadius() * 2 + 6);
        }
    }

    private void drawUI(LevelManager levelManager, AbyssBrickGame game) {
        ScoreManager scoreManager = game.getScoreManager();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 18));
        gc.setTextAlign(TextAlignment.LEFT);

        gc.fillText("分数: " + scoreManager.getScoreValue(), 10, 25);
        gc.fillText("关卡: " + game.getCurrentLevel() + " - " + levelManager.getLevelThemeName(), 10, 50);
        gc.fillText("连击: " + scoreManager.getComboValue(), 10, 75);
        gc.fillText("倍率: x" + String.format("%.1f", scoreManager.getComboMultiplierValue()), 10, 100);

        //实时显示最高分
        gc.fillText("最高分: " + game.getHighScore(), 10, 125);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("生命值: " + game.getLifeCount(), AbyssBrickGame.GAME_WIDTH - 10, 25);
        gc.fillText("剩余小球: " + game.getBallList().size(), AbyssBrickGame.GAME_WIDTH - 10, 50);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Microsoft YaHei", 14));
        gc.fillText("鼠标/A D键移动挡板 | P暂停 | ESC菜单",
                AbyssBrickGame.GAME_WIDTH / 2.0, 20);
    }

    private void drawGameOverOverlay(AbyssBrickGame game) {
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        // 游戏结束标题
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Microsoft YaHei", 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("游戏结束", AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 - 80);

        // 当前分数
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 24));
        gc.fillText("最终分数: " + game.getScoreManager().getScoreValue(),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 - 20);

        // 到达关卡
        gc.fillText("到达关卡: " + game.getCurrentLevel(),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 20);

        // ✅ 历史最高分
        int highScore = game.getHighScore();
        gc.setFill(Color.web("#FFD93D"));
        gc.fillText("历史最高分: " + highScore,
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 60);

        // ✅ 新纪录提示
        if (game.getCurrentScore() >= highScore && game.getCurrentScore() > 0) {
            gc.setFill(Color.web("#00FF88"));
            gc.setFont(Font.font("Microsoft YaHei", 18));
            gc.fillText("🎉 新纪录！",
                    AbyssBrickGame.GAME_WIDTH / 2.0,
                    AbyssBrickGame.GAME_HEIGHT / 2.0 + 95);
        }

        // 重新开始提示
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 18));
        gc.fillText("点击鼠标重新开始", AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 130);
    }

    public double getCanvasHeight() {
        return canvas.getHeight();
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
