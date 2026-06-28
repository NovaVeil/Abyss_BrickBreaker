package org.example.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

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
    private boolean showingPauseMenu;
    private boolean gamePaused;

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
        this.showingPauseMenu = false;
        this.gamePaused = false;

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
            
            if (code == KeyCode.ESCAPE && !showingModeSelection && !showingLevelSelection && !showingPauseMenu && game.isGameRunning()) {
                gamePaused = true;
                showingPauseMenu = true;
                System.out.println(">>> 游戏暂停，小球位置已保存");
            }
            
            if (!showingModeSelection && !showingLevelSelection && !showingPauseMenu && !gamePaused) {
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
            if (!showingModeSelection && !showingLevelSelection && !showingPauseMenu) {
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
            } else if (showingPauseMenu) {
                view.handlePauseMenuMouseMove(event.getX(), event.getY());
            } else if (!gamePaused && game.isGameRunning()) {
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
                        int currentMaxLevel = game.getMaxUnlockedLevel();
                        System.out.println(">>> 进入关卡选择器，当前最大解锁关卡: " + currentMaxLevel);
                        levelSelector.updateMaxUnlockedLevel(currentMaxLevel);
                    } else {
                        showingModeSelection = false;
                        game.startWithMode(selectedMode);
                    }
                }
            } else if (showingLevelSelection) {
                Integer selectedLevel = levelSelector.handleClick(event.getX(), event.getY());
                if (selectedLevel != null) {
                    System.out.println(">>> 选择了关卡: " + selectedLevel);
                    showingLevelSelection = false;
                    game.startCampaignLevel(selectedLevel);
                }
            } else if (game.isVictoryScreen()) {
                game.skipVictoryScreen();
            } else if (showingPauseMenu) {
                String action = view.handlePauseMenuClick(event.getX(), event.getY());
                if (action != null) {
                    handlePauseMenuAction(action);
                }
                return;
            } else if (!game.isGameRunning() && !game.isCountdownActive()) {
                game.restart();
                showingModeSelection = true;
                showingLevelSelection = false;
                showingPauseMenu = false;
                gamePaused = false;
            }
        });

        canvas.requestFocus();
    }

    private void handlePauseMenuAction(String action) {
        switch (action) {
            case "resume":
                gamePaused = false;
                showingPauseMenu = false;
                resetBallUpdateTime();
                System.out.println(">>> 继续游戏，从暂停位置恢复，小球位置和所有数据保持不变");
                break;
            case "restart":
                game.restartCurrentLevel();
                showingPauseMenu = false;
                gamePaused = false;
                System.out.println(">>> 重新开始当前关卡，所有状态重置（包括生命值）");
                break;
            case "exit":
                game.resetModeSelection();
                showingModeSelection = true;
                showingLevelSelection = false;
                showingPauseMenu = false;
                gamePaused = false;
                System.out.println(">>> 退出到主菜单");
                break;
        }
    }

    private void resetBallUpdateTime() {
        for (org.example.model.Ball ball : game.getBallList()) {
            ball.resetLastUpdateTime();
        }
    }

    private void setupGameLoop(Canvas canvas) {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gamePaused && !showingPauseMenu) {
                    handleKeyboardMovement();
                    game.update(now);
                }
                view.render(showingModeSelection, showingLevelSelection, showingPauseMenu, 
                           modeSelector, levelSelector, gamePaused);
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
