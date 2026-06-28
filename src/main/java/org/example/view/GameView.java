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
import org.example.service.ScoreManager;

public class GameView {
    private Canvas canvas;
    private GraphicsContext gc;
    private AbyssBrickGame game;

    private static final int PAUSE_BUTTON_WIDTH = 200;
    private static final int PAUSE_BUTTON_HEIGHT = 50;
    private static final int PAUSE_BUTTON_GAP = 20;
    private static final int PAUSE_START_Y = 250;

    private PauseButton resumeButton;
    private PauseButton restartButton;
    private PauseButton exitButton;

    private boolean resumeHovered = false;
    private boolean restartHovered = false;
    private boolean exitHovered = false;

    public GameView(Stage stage, Canvas canvas, AbyssBrickGame game) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.game = game;

        initializePauseButtons();

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        stage.setScene(new javafx.scene.Scene(root, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT));
        stage.setTitle("Abyss Brick Breaker - 打砖块游戏");
        stage.setResizable(false);

        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        stage.setX((screenWidth - AbyssBrickGame.GAME_WIDTH) / 2);
        stage.setY((screenHeight - AbyssBrickGame.GAME_HEIGHT) / 2);
    }

    private void initializePauseButtons() {
        double centerX = AbyssBrickGame.GAME_WIDTH / 2.0;
        double buttonX = centerX - PAUSE_BUTTON_WIDTH / 2.0;
        
        resumeButton = new PauseButton(buttonX, PAUSE_START_Y, PAUSE_BUTTON_WIDTH, PAUSE_BUTTON_HEIGHT, "继续游戏");
        restartButton = new PauseButton(buttonX, PAUSE_START_Y + PAUSE_BUTTON_HEIGHT + PAUSE_BUTTON_GAP, 
                                       PAUSE_BUTTON_WIDTH, PAUSE_BUTTON_HEIGHT, "重新开始");
        exitButton = new PauseButton(buttonX, PAUSE_START_Y + (PAUSE_BUTTON_HEIGHT + PAUSE_BUTTON_GAP) * 2, 
                                    PAUSE_BUTTON_WIDTH, PAUSE_BUTTON_HEIGHT, "退出到主菜单");
    }

    public void render(boolean showingModeSelection, boolean showingLevelSelection, boolean showingPauseMenu,
                       GameModeSelector modeSelector, LevelSelector levelSelector, boolean gamePaused) {
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
        Image bg = ImageLoader.getBackgroundByTheme(theme);

        if (bg != null) {
            gc.drawImage(bg, 0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        }

        drawBricks();
        drawBaffle();
        drawBalls();
        drawVirtualBalls();
        drawUI(levelManager);

        if (game.isCountdownActive()) {
            drawCountdownOverlay();
        }

        if (showingPauseMenu || gamePaused) {
            drawPauseMenu();
        }

        if (!game.isGameRunning() && game.getLifeCount() <= 0) {
            drawGameOverOverlay();
        }
    }

    public void handlePauseMenuMouseMove(double mouseX, double mouseY) {
        resumeHovered = resumeButton.contains(mouseX, mouseY);
        restartHovered = restartButton.contains(mouseX, mouseY);
        exitHovered = exitButton.contains(mouseX, mouseY);
    }

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

    private void drawCountdownOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        gc.setFill(Color.web("#FFD93D"));
        gc.setFont(Font.font("Microsoft YaHei", 120));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(game.getCountdownSeconds()),
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 40);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 24));
        gc.fillText("准备开始！",
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 100);
    }

    private void drawBricks() {

        for (Brick brick : game.getBrickList()) {
            if (!brick.isAlive()) {
                continue;
            }

            Image brickImage = null;

            if (brick instanceof NormalBrick) {
                brickImage = ImageLoader.BRICK_NORMAL_IMG;
            } else if (brick instanceof HardBrick) {
                brickImage = ImageLoader.BRICK_HARD_IMG;
            } else if (brick instanceof GiftBrick) {
                brickImage = ImageLoader.BRICK_GIFT_IMG;
            } else if (brick instanceof TriangleBrick) {
                brickImage = ImageLoader.BRICK_TRIANGLE_IMG;
            } else {
                brickImage = ImageLoader.BRICK_NORMAL_IMG;
            }

            if (brickImage != null) {
                double alpha = 1.0;
                if (brick instanceof HardBrick && brick.getHp() == 1) {
                    alpha = 0.5;
                }

                gc.setGlobalAlpha(alpha);
                if (brick.getShape() == Brick.BrickShape.TRIANGLE) {
                    gc.save();

                    double x = brick.getX();
                    double y = brick.getY();
                    double w = brick.getWidth();
                    double h = brick.getHeight();

                    gc.beginPath();
                    gc.moveTo(x + w / 2, y);
                    gc.lineTo(x + w, y + h);
                    gc.lineTo(x, y + h);
                    gc.closePath();

                    gc.clip();
                    gc.drawImage(brickImage, x, y, w, h);
                    gc.restore();
                } else {
                    gc.drawImage(brickImage, brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
                }
                gc.setGlobalAlpha(1.0);
            }
        }
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

            gc.setFill(virtualBall.getColor());
            gc.fillOval(virtualBall.getX() - virtualBall.getRadius(),
                    virtualBall.getY() - virtualBall.getRadius(),
                    virtualBall.getRadius() * 2, virtualBall.getRadius() * 2);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.setLineDashes(5, 5);
            gc.strokeOval(virtualBall.getX() - virtualBall.getRadius(),
                    virtualBall.getY() - virtualBall.getRadius(),
                    virtualBall.getRadius() * 2, virtualBall.getRadius() * 2);
            gc.setLineDashes(0);

            gc.setStroke(Color.web("#FF6B9D", 0.3));
            gc.setLineWidth(4);
            gc.strokeOval(virtualBall.getX() - virtualBall.getRadius() - 3,
                    virtualBall.getY() - virtualBall.getRadius() - 3,
                    virtualBall.getRadius() * 2 + 6, virtualBall.getRadius() * 2 + 6);
        }
    }

    private void drawUI(LevelManager levelManager) {
        ScoreManager scoreManager = game.getScoreManager();

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 18));
        gc.setTextAlign(TextAlignment.LEFT);

        gc.fillText("分数: " + scoreManager.getScoreValue(), 10, 25);
        gc.fillText("关卡: " + game.getCurrentLevel() + " - " + levelManager.getLevelThemeName(), 10, 50);
        gc.fillText("连击: " + scoreManager.getComboValue(), 10, 75);
        gc.fillText("倍率: x" + String.format("%.1f", scoreManager.getComboMultiplierValue()), 10, 100);
        gc.fillText("最高分: " + game.getHighScore(), 10, 125);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("生命值: " + game.getLifeCount(), AbyssBrickGame.GAME_WIDTH - 10, 25);
        gc.fillText("剩余小球: " + game.getBallList().size(), AbyssBrickGame.GAME_WIDTH - 10, 50);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Microsoft YaHei", 14));
        gc.fillText("鼠标/A D键移动挡板 | ESC暂停/菜单",
                AbyssBrickGame.GAME_WIDTH / 2.0, 20);
    }

    private void drawPauseMenu() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        gc.setFill(Color.web("#FFD93D"));
        gc.setFont(Font.font("Microsoft YaHei", 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("游戏暂停",
                AbyssBrickGame.GAME_WIDTH / 2.0,
                180);

        resumeButton.render(gc, resumeHovered);
        restartButton.render(gc, restartHovered);
        exitButton.render(gc, exitHovered);
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

        int highScore = game.getHighScore();
        gc.setFill(Color.web("#FFD93D"));
        gc.fillText("历史最高分: " + highScore,
                AbyssBrickGame.GAME_WIDTH / 2.0,
                AbyssBrickGame.GAME_HEIGHT / 2.0 + 60);

        if (game.getCurrentScore() >= highScore && game.getCurrentScore() > 0) {
            gc.setFill(Color.web("#00FF88"));
            gc.setFont(Font.font("Microsoft YaHei", 18));
            gc.fillText("新纪录！",
                    AbyssBrickGame.GAME_WIDTH / 2.0,
                    AbyssBrickGame.GAME_HEIGHT / 2.0 + 95);
        }

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

    private static class PauseButton {
        private double x, y, width, height;
        private String text;

        public PauseButton(double x, double y, double width, double height, String text) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
        }

        public void render(GraphicsContext gc, boolean hovered) {
            if (hovered) {
                gc.setFill(Color.web("#4A90E2", 0.8));
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(3);
            } else {
                gc.setFill(Color.web("#FFFFFF", 0.3));
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
            }

            gc.fillRoundRect(x, y, width, height, 10, 10);
            gc.strokeRoundRect(x, y, width, height, 10, 10);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Microsoft YaHei", 20));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(text, x + width / 2, y + height / 2 + 7);
        }

        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                   mouseY >= y && mouseY <= y + height;
        }
    }
}
