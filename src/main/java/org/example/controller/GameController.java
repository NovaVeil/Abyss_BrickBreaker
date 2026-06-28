package org.example.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.Optional;

import org.example.model.AbyssBrickGame;
import org.example.model.Baffle;
import org.example.model.GameMode;
import org.example.view.GameModeSelector;
import org.example.view.GameView;
import org.example.view.LevelSelector;

public class GameController {
    private AbyssBrickGame game;
    private GameView view;
    private GameModeSelector modeSelector;
    private LevelSelector levelSelector;

    private AnimationTimer gameLoop;
    private boolean showingModeSelection;
    private boolean showingLevelSelection;

    private boolean moveLeftPressed = false;
    private boolean moveRightPressed = false;

    public GameController(Stage primaryStage) {
        this.game = new AbyssBrickGame();

        Canvas canvas = new Canvas(AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        this.view = new GameView(primaryStage, canvas, game);

        this.modeSelector = new GameModeSelector(canvas);
        this.levelSelector = new LevelSelector(canvas, game.getMaxUnlockedLevel());

        this.showingModeSelection = true;
        this.showingLevelSelection = false;

        setupInputHandlers(primaryStage, canvas);
        setupGameLoop(canvas);
    }

    public void start(Stage primaryStage, Canvas canvas) {
        primaryStage.show();
        canvas.requestFocus();
        gameLoop.start();

        primaryStage.getScene().windowProperty().addListener((obs, oldWindow, newWindow) -> {
            if (newWindow != null) {
                newWindow.focusedProperty().addListener((obs2, oldFocused, newFocused) -> {
                    if (newFocused) {
                        canvas.requestFocus();
                    }
                });
            }
        });

        primaryStage.setOnCloseRequest(event -> {
            gameLoop.stop();
        });
    }

    private void setupInputHandlers(Stage primaryStage, Canvas canvas) {
        Scene scene = primaryStage.getScene();
        canvas.setFocusTraversable(true);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.P && !showingModeSelection && !showingLevelSelection) {
                togglePause();
            }
            if (code == KeyCode.ESCAPE && !showingModeSelection && !showingLevelSelection) {
                showPauseMenu(primaryStage);
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
                double paddleFixedY = view.getCanvasHeight() - game.getBaffle().getHeight() - 10;
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

    private void setupGameLoop(Canvas canvas) {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleKeyboardMovement();
                game.update();
                view.render(showingModeSelection, showingLevelSelection, modeSelector, levelSelector);
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
    }

    private void togglePause() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏暂停");
        alert.setHeaderText("游戏已暂停");
        alert.setContentText("点击确定继续游戏");
        alert.showAndWait();
    }

    private void showPauseMenu(Stage primaryStage) {
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
            game.restart();
            showingModeSelection = true;
        } else if (result.isPresent() && result.get() == exitButton) {
            primaryStage.close();
        }
    }

    public AbyssBrickGame getGame() {
        return game;
    }

    public GameView getView() {
        return view;
    }

    public AnimationTimer getGameLoop() {
        return gameLoop;
    }
}
