package org.example;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
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

import java.util.Optional;

public class GameView {
    private Stage primaryStage;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    private AbyssBrickGame game;
    private LevelManager levelManager;
    private GameModeSelector modeSelector;

    private AnimationTimer gameLoop;
    private boolean showingModeSelection;

    public GameView(Stage stage) {
        this.primaryStage = stage;
        this.levelManager = new LevelManager();
        this.showingModeSelection = true;

        BorderPane root = new BorderPane();

        this.canvas = new Canvas(AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();

        root.setCenter(canvas);

        this.scene = new Scene(root, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        initGame();
        setupInputHandlers();
        setupGameLoop();
        centerWindow();
    }

    private void initGame() {
        game = new AbyssBrickGame();
        modeSelector = new GameModeSelector(canvas);
    }

    private void setupInputHandlers() {
        canvas.setFocusTraversable(true);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.P && !showingModeSelection) {
                togglePause();
            }
            if (code == KeyCode.ESCAPE && !showingModeSelection) {
                showPauseMenu();
            }
        });

        scene.setOnKeyReleased(event -> {
        });

        canvas.setOnMouseMoved(event -> {
            if (showingModeSelection) {
                modeSelector.handleMouseMove(event.getX(), event.getY());
            } else {
                double mouseX = event.getX();
                // 挡板只在水平方向移动，Y坐标固定在窗口底部
                double paddleNewX = mouseX - game.getBaffle().getWidth() / 2;
                double paddleFixedY = GameConstant.GAME_HEIGHT - game.getBaffle().getHeight() - 10;
                game.getBaffle().moveTo(paddleNewX, paddleFixedY);
            }
        });

        canvas.setOnMouseClicked(event -> {
            if (showingModeSelection) {
                GameMode selectedMode = modeSelector.handleClick(event.getX(), event.getY());
                if (selectedMode != null) {
                    showingModeSelection = false;
                    game.startWithMode(selectedMode);
                }
            } else if (!game.isGameRunning()) {
                // 关键修复：先重置游戏内部状态（清除上一局的 lifeCount=0 等死亡标记）
                game.restart();
                // 再显示模式选择界面
                showingModeSelection = true;
            }
        });
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                game.update();
                render();
            }
        };
    }

    private void render() {
        if (showingModeSelection) {
            modeSelector.render();
            return;
        }

        gc.setFill(levelManager.getBackgroundColor());
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        drawBricks();
        drawBaffle();
        drawBalls();
        drawVirtualBalls();
        drawUI();
        
        // 如果正在倒计时，显示倒计时界面
        if (game.isCountdownActive()) {
            drawCountdownOverlay();
        }

        if (!game.isGameRunning() && game.getLifeCount() <= 0) {
            drawGameOverOverlay();
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

    private void drawBricks() {
        boolean isRectangular = levelManager.isRectangularBricks();

        for (Brick brick : game.getBrickList()) {
            if (!brick.isAlive()) {
                continue;
            }

            Color brickColor;
            if (brick instanceof NormalBrick) {
                brickColor = levelManager.getBrickStyle();
            } else if (brick instanceof HardBrick) {
                brickColor = Color.web("#F5A623");
            } else if (brick instanceof GiftBrick) {
                brickColor = Color.web("#7ED321");
            } else if (brick instanceof TriangleBrick) {
                brickColor = Color.web("#9B59B6");
            } else {
                brickColor = Color.WHITE;
            }

            gc.setFill(brickColor);

            if (brick.getShape() == Brick.BrickShape.TRIANGLE) {
                drawTriangle(brick);
            } else {
                if (isRectangular) {
                    gc.fillRoundRect(brick.getX(), brick.getY(),
                            brick.getWidth(), brick.getHeight(), 5, 5);
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(1);
                    gc.strokeRoundRect(brick.getX(), brick.getY(),
                            brick.getWidth(), brick.getHeight(), 5, 5);
                } else {
                    gc.fillRect(brick.getX(), brick.getY(),
                            brick.getWidth(), brick.getHeight());
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(2);
                    gc.strokeRect(brick.getX(), brick.getY(),
                            brick.getWidth(), brick.getHeight());
                }
            }
        }
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

    private void drawBaffle() {
        Baffle baffle = game.getBaffle();
        gc.setFill(baffle.getColor());
        gc.fillRoundRect(baffle.getX(), baffle.getY(),
                baffle.getWidth(), baffle.getHeight(), 8, 8);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRoundRect(baffle.getX(), baffle.getY(),
                baffle.getWidth(), baffle.getHeight(), 8, 8);
    }

    private void drawBalls() {
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

    private void drawVirtualBalls() {
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

    private void drawUI() {
        ScoreManager scoreManager = game.getScoreManager();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 18));
        gc.setTextAlign(TextAlignment.LEFT);

        gc.fillText("分数: " + scoreManager.getScoreValue(), 10, 25);
        gc.fillText("关卡: " + game.getCurrentLevel() + " - " + levelManager.getLevelThemeName(), 10, 50);
        gc.fillText("连击: " + scoreManager.getComboValue(), 10, 75);
        gc.fillText("倍率: x" + String.format("%.1f", scoreManager.getComboMultiplierValue()), 10, 100);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("生命值: " + game.getLifeCount(), AbyssBrickGame.GAME_WIDTH - 10, 25);
        gc.fillText("剩余小球: " + game.getBallList().size(), AbyssBrickGame.GAME_WIDTH - 10, 50);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Microsoft YaHei", 14));
        gc.fillText("鼠标移动挡板 | P暂停 | ESC菜单",
                AbyssBrickGame.GAME_WIDTH / 2.0, AbyssBrickGame.GAME_HEIGHT - 10);
    }

    private void drawGameOverOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Microsoft YaHei", 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("游戏结束", AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 - 80);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 24));
        gc.fillText("最终分数: " + game.getScoreManager().getScoreValue(),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 - 20);
        gc.fillText("到达关卡: " + game.getCurrentLevel(),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 20);
        gc.setFont(Font.font("Microsoft YaHei", 18));
        gc.fillText("点击鼠标返回模式选择界面", AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 70);
    }

    private void centerWindow() {
        primaryStage.setTitle("Abyss Brick Breaker - 打砖块游戏");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

        primaryStage.setX((screenWidth - AbyssBrickGame.GAME_WIDTH) / 2);
        primaryStage.setY((screenHeight - AbyssBrickGame.GAME_HEIGHT) / 2);
    }

    private void togglePause() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏暂停");
        alert.setHeaderText("游戏已暂停");
        alert.setContentText("点击确定继续游戏");
        alert.showAndWait();
    }

    private void showPauseMenu() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("游戏菜单");
        alert.setHeaderText("游戏暂停");
        alert.setContentText("选择操作：");

        ButtonType resumeButton = new ButtonType("继续游戏");
        ButtonType restartButton = new ButtonType("重新开始");
        ButtonType exitButton = new ButtonType("退出游戏");

        alert.getButtonTypes().setAll(resumeButton, restartButton, exitButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == restartButton) {
            game.restart(); showingModeSelection = true;
        } else if (result.isPresent() && result.get() == exitButton) {
            primaryStage.close();
        }
    }

    private void showGameOverDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏结束");
        alert.setHeaderText("游戏结束");
        alert.setContentText("最终分数: " + game.getScoreManager().getScoreValue() +
                "\n到达关卡: " + game.getCurrentLevel());
        alert.showAndWait();
    }



    public void show() {
        primaryStage.show();
        canvas.requestFocus();
        gameLoop.start();
        
        // 窗口关闭时停止游戏循环（为数据记录预留时间）
        primaryStage.setOnCloseRequest(event -> {
            gameLoop.stop();
            // TODO: 这里可以调用数据保存方法
            // DataManager.saveGameData(game);
        });
    }
}
