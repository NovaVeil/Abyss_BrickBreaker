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

    private AnimationTimer gameLoop;

    public GameView(Stage stage) {
        this.primaryStage = stage;
        this.levelManager = new LevelManager();

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
        levelManager.initLevelStyle(1);
    }

    private void setupInputHandlers() {
        canvas.setFocusTraversable(true);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.LEFT || code == KeyCode.A) {
                game.getBaffle().moveLeft();
            }
            if (code == KeyCode.RIGHT || code == KeyCode.D) {
                game.getBaffle().moveRight();
            }
            if (code == KeyCode.P) {
                togglePause();
            }
            if (code == KeyCode.ESCAPE) {
                showPauseMenu();
            }
        });

        scene.setOnKeyReleased(event -> {
        });

        canvas.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double paddleNewX = mouseX - game.getBaffle().getWidth() / 2;
            game.getBaffle().moveTo(paddleNewX);
        });

        canvas.setOnMouseClicked(event -> {
            if (!game.isGameRunning()) {
                restartGame();
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
        gc.setFill(levelManager.getBackgroundColor());
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        drawBricks();
        drawBaffle();
        drawBalls();
        drawUI();

        if (!game.isGameRunning() && game.getLifeCount() <= 0) {
            drawGameOverOverlay();
        }
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
            } else {
                brickColor = Color.WHITE;
            }

            gc.setFill(brickColor);

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
        gc.fillText("方向键←→/AD移动 | 鼠标控制 | P暂停 | ESC菜单",
                AbyssBrickGame.GAME_WIDTH / 2, AbyssBrickGame.GAME_HEIGHT - 10);
    }

    private void drawGameOverOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);

        gc.setFill(Color.RED);
        gc.setFont(Font.font("Microsoft YaHei", 48));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("游戏结束", AbyssBrickGame.GAME_WIDTH / 2,
                AbyssBrickGame.GAME_HEIGHT / 2 - 80);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 24));
        gc.fillText("最终分数: " + game.getScoreManager().getScoreValue(),
                AbyssBrickGame.GAME_WIDTH / 2,
                AbyssBrickGame.GAME_HEIGHT / 2 - 20);
        gc.fillText("到达关卡: " + game.getCurrentLevel(),
                AbyssBrickGame.GAME_WIDTH / 2,
                AbyssBrickGame.GAME_HEIGHT / 2 + 20);

        gc.setFont(Font.font("Microsoft YaHei", 18));
        gc.fillText("点击鼠标重新开始", AbyssBrickGame.GAME_WIDTH / 2,
                AbyssBrickGame.GAME_HEIGHT / 2 + 70);
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
            restartGame();
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

    private void restartGame() {
        game.restart();
        levelManager.initLevelStyle(1);
    }

    public void show() {
        primaryStage.show();
        canvas.requestFocus();
        gameLoop.start();
    }
}
