package org.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbyssBrickGame {
    // 游戏窗口尺寸
    public static final int GAME_WIDTH = GameConstant.GAME_WIDTH;
    public static final int GAME_HEIGHT = GameConstant.GAME_HEIGHT;

    // 游戏实体
    private List<Ball> ballList;
    private Baffle baffle;
    private List<Brick> brickList;
    private List<VirtualBall> virtualBallList;

    // 游戏状态
    private boolean gameRunning;
    private boolean countdownActive;
    private int currentLevel;
    private int lifeCount;
    private int countdownSeconds;
    private long lastCountdownTime;
    private ScoreManager scoreManager;
    private LevelManager levelManager;
    private GameMode currentMode;
    private boolean modeSelected;

    public AbyssBrickGame() {
        brickList = new ArrayList<>();
        ballList = new ArrayList<>();
        virtualBallList = new ArrayList<>();
        currentLevel = 1;
        lifeCount = 3;
        gameRunning = false;
        countdownActive = false;
        countdownSeconds = 3;
        scoreManager = new ScoreManager();
        levelManager = new LevelManager();
        currentMode = null;
        modeSelected = false;

        // 初始化挡板
        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);
    }
    //设置游戏模式并开始游戏
    public void startWithMode(GameMode mode) {
        this.currentMode = mode;
        this.modeSelected = true;
        levelManager.setGameMode(mode);
        levelManager.initLevelStyle(currentLevel);
        initLevelBrick(currentLevel);
        startCountdown();
    }

   // 开始3秒倒计时
    public void startCountdown() {
        countdownActive = true;
        countdownSeconds = 3;
        lastCountdownTime = System.currentTimeMillis();
        gameRunning = false;
    }
    //更新倒计时
    private void updateCountdown() {
        if (!countdownActive) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCountdownTime >= 1000) {
            countdownSeconds--;
            lastCountdownTime = currentTime;
            
            if (countdownSeconds <= 0) {
                countdownActive = false;
                gameRunning = true;
                // 倒计时结束，生成初始小球
                respawnBallAtPaddle();
            }
        }
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
        
        // 生成虚拟小球
        virtualBallList.clear();
        virtualBallList.addAll(levelManager.generateVirtualBalls(level, brickList));
    }

    public void update() {
        // 更新倒计时
        updateCountdown();
        
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
        Iterator<Ball> ballIterator = ballList.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            
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
            
            // 小球 —— 虚拟小球碰撞检测
            checkVirtualBallCollision(ball);
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
   /*检测实体小球与虚拟小球的碰撞
   碰撞后在虚拟小球位置生成新的实体小球*/
    private void checkVirtualBallCollision(Ball realBall) {
        Iterator<VirtualBall> virtualIterator = virtualBallList.iterator();
        while (virtualIterator.hasNext()) {
            VirtualBall virtualBall = virtualIterator.next();
            if (!virtualBall.isActive()) {
                continue;
            }
            
            // 计算距离
            double dx = realBall.getX() - virtualBall.getX();
            double dy = realBall.getY() - virtualBall.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // 如果碰撞（两个球半径之和）
            if (distance < (realBall.getRadius() + virtualBall.getRadius())) {
                // 在虚拟小球位置生成新的实体小球
                Ball newBall = new Ball(virtualBall.getX(), virtualBall.getY());
                
                // 随机设置新球的方向
                int newDx = (Math.random() > 0.5 ? 1 : -1) * GameConstant.BALL_SPEED_X;
                int newDy = -GameConstant.BALL_SPEED_Y; // 向上
                newBall.setDx(newDx);
                
                ballList.add(newBall);
                
                // 停用虚拟小球
                virtualBall.deactivate();
                virtualIterator.remove();
                
                // 加分
                scoreManager.addScore(50);
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
        
        // 下一关也需要倒计时
        startCountdown();
    }

    private void gameOver() {
        gameRunning = false;
        scoreManager.resetCombo();
    }

    public void restart() {
        currentLevel = 1;
        lifeCount = 3;
        gameRunning = false;
        countdownActive = false;
        scoreManager = new ScoreManager();
        
        ballList.clear();
        brickList.clear();
        virtualBallList.clear();
        
        if (currentMode != null) {
            levelManager.setGameMode(currentMode);
            levelManager.initLevelStyle(currentLevel);
            initLevelBrick(currentLevel);
            startCountdown();
        }
    }

    public boolean isCountdownActive() {
        return countdownActive;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
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

    public List<VirtualBall> getVirtualBallList() {
        return virtualBallList;
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

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public boolean isModeSelected() {
        return modeSelected;
    }
}