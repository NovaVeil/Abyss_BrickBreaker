package org.example;

import java.util.ArrayList;
import java.util.List;

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
    private ScoreManager scoreManager;
    private LevelManager levelManager;

    public AbyssBrickGame() {
        brickList = new ArrayList<>();
        ballList = new ArrayList<>();
        currentLevel = 1;
        lifeCount = 3;
        gameRunning = true;
        scoreManager = new ScoreManager();
        levelManager = new LevelManager();

        // 初始化游戏、第一关砖块
        initGame();

        initLevelBrick(currentLevel);
    }

    //    初始化小球、挡板
    private void initGame() {
        // 先创建挡板
        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);

        respawnBallAtPaddle();
    }

    private void respawnBallAtPaddle() {
        double ballStartX = baffle.getX() + baffle.getWidth() / 2.0;
        double ballStartY = baffle.getY() - 15;
        Ball ball = new Ball(ballStartX, ballStartY);
        ballList.add(ball);
    }

    //根据关卡等级生成砖块
    private void initLevelBrick(int level) {
        brickList.clear();
        brickList.addAll(levelManager.generateBricks(level));
    }

    public void update() {
        if (!gameRunning) {
            return;
        }

        for (Ball ball : ballList) {
            ball.move();
        }

        checkAllCollision();
        checkGameStatus();
    }

    //    全部碰撞调度
    private void checkAllCollision() {
        // 遍历所有小球进行碰撞检测
        for (Ball ball : ballList) {
            // 小球 —— 挡板碰撞
            CollisionDetector.checkBaffileCollision(ball, baffle);

            // 小球 —— 所有砖块碰撞
            for (Brick brick : brickList) {
                int hpBefore = brick.getHp();
                CollisionDetector.checkBrickCollision(ball, brick);
                int hpAfter = brick.getHp();

                // 如果砖块HP减少且变为0，说明刚被击碎，加分
                if (hpBefore > hpAfter && hpAfter <= 0) {
                    scoreManager.addScoreForBrick(brick);
                }

                // 礼物砖块被击碎，触发整行整列扣血
                if (brick instanceof GiftBrick) {
                    GiftBrick gb = (GiftBrick) brick;
                    if (gb.isTiggerGift()) {
                        CollisionDetector.triggerGiftSkill(brick, brickList, scoreManager);
                    }
                }
            }
        }

        // 移除已掉落的小球
        ballList.removeIf(ball -> CollisionDetector.isBallFallOut(ball));

        if (ballList.isEmpty() && lifeCount > 0) {
            lifeCount--;
            if (lifeCount > 0) {
                respawnBallAtPaddle();
            }
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
            nextLevel();
        }

        if (ballList.isEmpty() && lifeCount <= 0) {
            gameOver();
        }
    }

    private void nextLevel() {
        currentLevel++;
        scoreManager.nextLevel();
        levelManager.initLevelStyle(currentLevel);

        ballList.clear();
        initLevelBrick(currentLevel);
        
        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);
        
        respawnBallAtPaddle();
    }

    private void gameOver() {
        gameRunning = false;
        scoreManager.resetCombo();
    }

    public void restart() {
        currentLevel = 1;
        lifeCount = 3;
        gameRunning = true;
        scoreManager = new ScoreManager();
        
        ballList.clear();
        brickList.clear();
        
        initGame();
        initLevelBrick(currentLevel);
    }

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

    public int getLifeCount() {
        return lifeCount;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }
}