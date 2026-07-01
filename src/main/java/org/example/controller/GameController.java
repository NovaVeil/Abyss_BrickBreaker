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

/**
 * 游戏控制器类（Controller层 - MVC架构的"总指挥"）
 * 负责：
 * 1. 接收玩家输入（键盘、鼠标）
 * 2. 驱动游戏主循环（每帧更新逻辑 + 渲染画面）
 * 3. 协调 Model（游戏数据）和 View（画面显示）
 */
public class GameController {
    // ==================== 核心组件引用 ====================
    private AbyssBrickGame game;          // 游戏模型（管理所有游戏数据和逻辑）
    private GameView view;                // 游戏视图（负责所有画面绘制）
    private GameModeSelector modeSelector; // 模式选择器（主菜单的"闯关模式/无尽模式"选择界面）
    private LevelSelector levelSelector;   // 关卡选择器（闯关模式的关卡选择界面）

    // ==================== 游戏循环 ====================
    private AnimationTimer gameLoop;      // 游戏主循环（JavaFX的AnimationTimer，每帧自动调用handle方法）

    // ==================== 4个状态标志位（决定游戏当前处于什么"界面"） ====================
    private boolean showingModeSelection;   // true = 正在显示模式选择界面（主菜单）
    private boolean showingLevelSelection;  // true = 正在显示关卡选择界面
    private boolean showingPauseMenu;       // true = 正在显示暂停菜单遮罩
    private boolean gamePaused;             // true = 游戏暂停中（不更新逻辑，但仍在渲染）

    // ==================== 键盘方向标志位 ====================
    // 设计思路：按键时不直接移动挡板，而是设置标志位，在游戏循环中每帧检查
    // 这样按住不放时挡板能持续移动（因为每帧都检查并移动）
    private boolean moveLeftPressed = false;   // true = A键或←键正在被按住
    private boolean moveRightPressed = false;  // true = D键或→键正在被按住

    // ==================== 构造方法（游戏初始化） ====================

    /**
     * 创建游戏控制器，初始化所有组件
     * @param primaryStage 游戏主窗口
     */
    public GameController(Stage primaryStage) {
        // ① 创建游戏模型（核心数据：球、砖块、挡板、分数、关卡等）
        this.game = new AbyssBrickGame();

        // ② 创建画布（800×600像素的游戏画面）
        Canvas canvas = new Canvas(AbyssBrickGame.GAME_WIDTH, AbyssBrickGame.GAME_HEIGHT);
        
        // ③ 创建游戏视图（绑定窗口、画布、模型，负责所有绘制）
        this.view = new GameView(primaryStage, canvas, game);

        // ④ 创建模式选择器和关卡选择器（都画在同一个canvas上）
        this.modeSelector = new GameModeSelector(canvas);
        this.levelSelector = new LevelSelector(canvas, game.getMaxUnlockedLevel());

        // ⑤ 初始化状态：启动时显示模式选择界面（主菜单）
        this.showingModeSelection = true;
        this.showingLevelSelection = false;
        this.showingPauseMenu = false;
        this.gamePaused = false;

        // ⑥ 设置输入处理（监听键盘和鼠标事件）
        setupInputHandlers(primaryStage, canvas);
        
        // ⑦ 设置游戏主循环（每帧执行update + render）
        setupGameLoop(canvas);
    }

    // ==================== 启动游戏 ====================

    /**
     * 启动游戏：显示窗口、启动游戏循环
     * @param primaryStage 游戏主窗口
     * @param canvas       画布
     */
    public void start(Stage primaryStage, Canvas canvas) {
        primaryStage.show();         // 显示窗口
        canvas.requestFocus();       // 让画布获得焦点（否则收不到键盘输入）
        gameLoop.start();            // 启动游戏主循环！从这里开始每帧执行

        // 窗口焦点监听：切换到其他窗口再切回来时，确保画布还能接收键盘输入
        primaryStage.getScene().windowProperty().addListener((obs, oldWindow, newWindow) -> {
            if (newWindow != null) {
                newWindow.focusedProperty().addListener((obs2, oldFocused, newFocused) -> {
                    if (newFocused) {
                        canvas.requestFocus();  // 窗口获得焦点时，让画布也获得焦点
                    }
                });
            }
        });

        // 关闭窗口时停止游戏循环（释放资源）
        primaryStage.setOnCloseRequest(event -> {
            gameLoop.stop();
        });
    }

    // ==================== 输入处理（核心！） ====================

    /**
     *
     * 设置所有输入处理器（键盘+鼠标）
     * 根据当前状态标志位，把输入事件分发给不同的处理器
     */
    private void setupInputHandlers(Stage primaryStage, Canvas canvas) {
        Scene scene = primaryStage.getScene();
        canvas.setFocusTraversable(true);  // 允许画布获得键盘焦点

        // ==================== ① 键盘按下事件 ====================
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();  // 获取按下的键
            
            // ESC键 → 暂停游戏（只在正式游戏中、没暂停时有效）
            if (code == KeyCode.ESCAPE && !showingModeSelection && !showingLevelSelection && !showingPauseMenu && game.isGameRunning()) {
                gamePaused = true;           // 标记游戏暂停
                showingPauseMenu = true;     // 显示暂停菜单
                System.out.println(">>> 游戏暂停，小球位置已保存");
            }
            
            // A/← 或 D/→ 键 → 设置方向标志位（不直接移动挡板！）
            // 只在非菜单、非暂停状态下才响应
            if (!showingModeSelection && !showingLevelSelection && !showingPauseMenu && !gamePaused) {
                if (code == KeyCode.A || code == KeyCode.LEFT) {
                    moveLeftPressed = true;   // 标记向左
                }
                if (code == KeyCode.D || code == KeyCode.RIGHT) {
                    moveRightPressed = true;  // 标记向右
                }
            }
        });

        // ==================== ② 键盘释放事件 ====================
        scene.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            // 只在非菜单、非暂停状态下才响应
            if (!showingModeSelection && !showingLevelSelection && !showingPauseMenu) {
                if (code == KeyCode.A || code == KeyCode.LEFT) {
                    moveLeftPressed = false;   // 松开 → 停止向左
                }
                if (code == KeyCode.D || code == KeyCode.RIGHT) {
                    moveRightPressed = false;  // 松开 → 停止向右
                }
            }
        });

        // ==================== ③ 鼠标移动事件 ====================
        // 根据当前状态，把鼠标事件分发给不同的处理器
        canvas.setOnMouseMoved(event -> {
            if (showingModeSelection) {
                // 模式选择界面 → 委托给 modeSelector（处理按钮高亮）
                modeSelector.handleMouseMove(event.getX(), event.getY());
            } else if (showingLevelSelection) {
                // 关卡选择界面 → 委托给 levelSelector（处理关卡按钮高亮）
                levelSelector.handleMouseMove(event.getX(), event.getY());
            } else if (showingPauseMenu) {
                // 暂停菜单 → 委托给 view（处理暂停按钮高亮）
                view.handlePauseMenuMouseMove(event.getX(), event.getY());
            } else if (!gamePaused && game.isGameRunning()) {
                // 正式游戏中 → 鼠标X坐标控制挡板位置
                double mouseX = event.getX();
                // 让挡板中心对准鼠标X位置
                double baffleNewX = mouseX - game.getBaffle().getWidth() / 2;
                game.getBaffle().moveTo(baffleNewX);
            }
        });

        // ==================== ④ 鼠标点击事件（状态机） ====================
        // 根据当前状态标志位，执行不同的点击逻辑
        canvas.setOnMouseClicked(event -> {
            if (showingModeSelection) {
                // 状态1：模式选择界面 → 选择游戏模式
                GameMode selectedMode = modeSelector.handleClick(event.getX(), event.getY());
                if (selectedMode != null) {
                    if (selectedMode == GameMode.CAMPAIGN) {
                        // 选了闯关模式 → 进入关卡选择界面
                        showingModeSelection = false;
                        showingLevelSelection = true;
                        int currentMaxLevel = game.getMaxUnlockedLevel();
                        System.out.println(">>> 进入关卡选择器，当前最大解锁关卡: " + currentMaxLevel);
                        levelSelector.updateMaxUnlockedLevel(currentMaxLevel);
                    } else {
                        // 选了无尽模式 → 直接开始游戏
                        showingModeSelection = false;
                        game.startWithMode(selectedMode);
                    }
                }
            } else if (showingLevelSelection) {
                // 状态2：关卡选择界面 → 选择关卡
                Integer selectedLevel = levelSelector.handleClick(event.getX(), event.getY());
                if (selectedLevel != null) {
                    System.out.println(">>> 选择了关卡: " + selectedLevel);
                    showingLevelSelection = false;
                    game.startCampaignLevel(selectedLevel);  // 开始指定关卡
                }
            } else if (game.isVictoryScreen()) {
                // 状态3：胜利画面 → 点击跳过
                game.skipVictoryScreen();
            } else if (showingPauseMenu) {
                // 状态4：暂停菜单 → 处理按钮点击
                String action = view.handlePauseMenuClick(event.getX(), event.getY());
                if (action != null) {
                    handlePauseMenuAction(action);  // 执行对应操作（继续/重开/退出）
                }
            } else if (!game.isGameRunning() && !game.isCountdownActive()) {
                // 状态5：游戏结束 → 重启并回到主菜单
                game.restart();
                game.resetModeSelection();
                showingModeSelection = true;
                showingLevelSelection = false;
                showingPauseMenu = false;
                gamePaused = false;
            }
        });

        canvas.requestFocus();  // 确保画布获得焦点
    }

    // ==================== 暂停菜单处理 ====================

    /**
     * 处理暂停菜单的按钮操作
     * @param action 按钮类型："resume"=继续, "restart"=重新开始, "exit"=退出
     */
    private void handlePauseMenuAction(String action) {
        switch (action) {
            case "resume":
                // 继续游戏：恢复所有状态，从暂停位置继续
                gamePaused = false;
                showingPauseMenu = false;
                resetBallUpdateTime();  // 重置小球时间戳（防止暂停后小球瞬移）
                System.out.println(">>> 继续游戏，从暂停位置恢复，小球位置和所有数据保持不变");
                break;
            case "restart":
                // 重新开始当前关卡：重置所有状态（生命、砖块、球、分数）
                game.restartCurrentLevel();
                showingPauseMenu = false;
                gamePaused = false;
                System.out.println(">>> 重新开始当前关卡，所有状态重置（包括生命值）");
                break;
            case "exit":
                // 退出到主菜单：重置模式选择，回到模式选择界面
                game.resetModeSelection();
                showingModeSelection = true;
                showingLevelSelection = false;
                showingPauseMenu = false;
                gamePaused = false;
                System.out.println(">>> 退出到主菜单");
                break;
        }
    }

    /**
     * 重置所有小球的最后更新时间
     * 防止暂停后恢复时，deltaTime过大导致小球瞬移穿墙
     * 原理：重置为0后，Ball.move()的第一帧会跳过移动，重新记录时间
     */
    private void resetBallUpdateTime() {
        for (org.example.model.Ball ball : game.getBallList()) {
            ball.resetLastUpdateTime();
        }
    }

    // ==================== 游戏主循环 ====================

    /**
     * 设置游戏主循环
     * 使用JavaFX的AnimationTimer，每帧自动调用handle()方法（约60次/秒）
     * 每帧执行：更新逻辑 → 渲染画面
     */
    private void setupGameLoop(Canvas canvas) {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // ① 如果没暂停 → 更新游戏逻辑
                if (!gamePaused && !showingPauseMenu) {
                    handleKeyboardMovement();  // 检查键盘标志位，移动挡板
                    game.update(now);          // 更新游戏（球移动、碰撞检测、状态检查）
                }
                
                // ② 胜利画面时 → 始终更新粒子（即使暂停也继续飘粒子）
                if (game.isVictoryScreen()) {
                    game.updateVictoryParticles(now);
                }
                
                // ③ 无论什么状态 → 都要渲染画面（暂停时也要画暂停菜单）
                view.render(showingModeSelection, showingLevelSelection, showingPauseMenu, 
                           modeSelector, levelSelector, gamePaused);
            }
        };
    }

    // ==================== 键盘移动挡板 ====================

    /**
     * 处理键盘移动挡板
     * 每帧调用一次，检查方向标志位，如果为true则移动挡板
     * 这样按住A/D键不放时，挡板能持续移动
     */
    private void handleKeyboardMovement() {
        // 安全检查：模式选择界面或游戏没开始 → 不处理
        if (showingModeSelection || game == null || game.getBaffle() == null) {
            return;
        }

        Baffle baffle = game.getBaffle();
        if (moveLeftPressed) {
            baffle.moveLeft();    // A键按住 → 每帧向左移动一步
        }
        if (moveRightPressed) {
            baffle.moveRight();   // D键按住 → 每帧向右移动一步
        }
    }

    // ==================== Getter方法 ====================

    public GameView getView() {
        return view;
    }
}
