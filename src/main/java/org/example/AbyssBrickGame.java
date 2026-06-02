package org.example;
//             游戏主类:运行此类使游戏开始
import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

//游戏主入口 + 主循环 + 关卡管理
public class AbyssBrickGame {
    // 游戏窗口尺寸
    public static final int GAME_WIDTH = GameConstant.GAME_WIDTH;
    public static final int GAME_HEIGHT = GameConstant.GAME_HEIGHT;

    // 游戏实体
    private List<Ball> ballList;
    private Baffle baffle;
    private List<Brick> brickList;

    // 游戏状态
    private boolean gameRunning;
    private int currentLevel;
    private int lifeCount;

    public AbyssBrickGame() {
        brickList = new ArrayList<>();
        ballList = new ArrayList<>();
        currentLevel = 1;
        gameRunning = true;

        // 初始化游戏、第一关砖块
        initGame();

        initLevelBrick(currentLevel);
    }

    //    初始化小球、挡板
    private void initGame() {
        // 先创建挡板
        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10);

        // 小球放在挡板正上方 15px 位置
        double ballStartX = baffle.getX() + GameConstant.BAFFLE_WIDTH / 2.0;
        double ballStartY = baffle.getY() - 15;
        Ball ball = new Ball(ballStartX, ballStartY);
        ballList.add(ball);
    }

    //根据关卡等级生成砖块
    private void initLevelBrick(int level) {
        brickList.clear();

        int colCount = 10;
        // 关卡越高行数越多
        int rowCount = 3 + level / 2;

        double startX = 30;
        double startY = 40;
        double brickW = GameConstant.BRICK_WIDTH;
        double brickH = GameConstant.BRICK_HEIGHT;
        double gap = 8;

        // 随关卡调整生成概率
        double normalRate = 0.65;
        double hardRate = 0.85;
        if (level == 2) {
            normalRate = 0.55;
            hardRate = 0.80;
        }
        if (level == 3) {
            normalRate = 0.45;
            hardRate = 0.75;
        }

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                double x = startX + col * (brickW + gap);
                double y = startY + row * (brickH + gap);

                double rand = Math.random();
                Brick brick;
                if (rand < normalRate) {
                    brick = new NormalBrick(x, y);
                } else if (rand < hardRate) {
                    brick = new HardBrick(x, y);
                } else {
                    brick = new GiftBrick(x, y);
                }
                brickList.add(brick);
            }
        }
    }

    //    游戏主循环
    public void gameLoop() {
        while (gameRunning) {
            // 1. 所有小球移动（内部自带墙碰撞）
            for (Ball ball : ballList) {
                ball.move();
            }

            // 2. 统一所有碰撞检测
            checkAllCollision();

            // 3. 检查游戏胜负、下一关、生命值
            checkGameStatus();

            // 控制帧率 约60帧
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //    全部碰撞调度
    private void checkAllCollision() {
        // 遍历所有小球进行碰撞检测
        for (Ball ball : ballList) {
            // 小球 —— 挡板碰撞
            CollisionDetector.checkBaffileCollision(ball, baffle);

            // 小球 —— 所有砖块碰撞
            for (Brick brick : brickList) {
                CollisionDetector.checkBrickCollision(ball, brick);

                // 礼物砖块被击碎，触发整行整列扣血
                if (brick instanceof GiftBrick) {
                    GiftBrick gb = (GiftBrick) brick;
                    if (gb.isTiggerGift()) {
                        CollisionDetector.triggerGiftSkill(brick, brickList);
                    }
                }
            }
        }

        // 移除已掉落的小球
        ballList.removeIf(ball -> CollisionDetector.isBallFallOut(ball));

        // 所有小球都掉落：游戏结束
        if (ballList.isEmpty()) {
            gameRunning = false;
        }
    }

    //    检查通关、生命值、下一关
    private void checkGameStatus() {
        // 判断本关所有砖块是否全部阵亡
        boolean allBrickDead = true;
        for (Brick b : brickList) {
            if (b.isAlive()) {
                allBrickDead = false;
                break;
            }
        }

        // 全部打完 → 下一关
        if (allBrickDead) {
            currentLevel++;
            
            // 清空小球列表
            ballList.clear();
            
            // 重新生成砖块
            initLevelBrick(currentLevel);
            
            // 重新初始化一个小球（和第一关一样的规则）
            double ballStartX = baffle.getX() + GameConstant.BAFFLE_WIDTH / 2.0;
            double ballStartY = baffle.getY() - 15;
            Ball newBall = new Ball(ballStartX, ballStartY);
            ballList.add(newBall);
        }
    }

    // ========== 给组员2 提供getter 用来绘制和键盘控制 ==========
    public List<Ball> getBallList() {
        return ballList;
    }

    public Baffle getBaffle() {
        return baffle;
    }

    public List<Brick> getBrickList() {
        return brickList;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public Baffle getGameBaffle() {
        return baffle;
    }
}