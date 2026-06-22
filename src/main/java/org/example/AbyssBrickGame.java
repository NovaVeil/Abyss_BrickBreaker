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
    private int aliveBrickCount = 0;

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
        lifeCount = GameConstant.BAFFLE_HEIGHT;
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
    //小球从挡板随机往左右方向弹出
    private void respawnBallAtPaddle() {
        double ballStartX = baffle.getX() + baffle.getWidth() / 2.0;
        double ballStartY = baffle.getY() - GameConstant.BALL_RADIUS;
        Ball ball = new Ball(ballStartX, ballStartY);
        int initialDx = (Math.random() >0.5?1:-1)*GameConstant.BALL_SPEED_X;
        ball.setDx(initialDx);
        ball.setDy(-GameConstant.BALL_SPEED_Y);
        ballList.add(ball);
    }

    //根据关卡等级生成砖块
    private void initLevelBrick(int level) {
        try {
            brickList.clear();
            brickList.addAll(levelManager.generateBricks(level));
            aliveBrickCount = brickList.size();
            // 生成虚拟小球
            virtualBallList.clear();
            virtualBallList.addAll(levelManager.generateVirtualBalls(level, brickList));
        } catch (Exception e) {
            System.out.println("关卡初始化失败");
            gameRunning = false;
        }
    }

    public void update() {
        // 更新倒计时
        try {
            updateCountdown();
            if (!gameRunning) {
                return;
            }

            for (Ball ball : ballList) {
                ball.move();
            }

            checkAllCollision();
            checkGameStatus();
        } catch (Exception e) {
            gameRunning = false;
            System.out.println("游戏异常结束");
        }
    }

    //    全部碰撞调度
    private void checkAllCollision() {
        // 遍历所有小球进行碰撞检测
        Iterator<Ball> ballIterator = ballList.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            
            // 小球 —— 挡板碰撞
            CollisionDetector.checkBaffileCollision(ball, baffle);

            // 小球 —— 砖块碰撞
            for (Brick brick : brickList) {
                int hpBefore = brick.getHp();
                CollisionDetector.checkBrickCollision(ball, brick);
                int hpAfter = brick.getHp();

                // 如果砖块HP减少且变为0，说明刚被击碎，加分
                if (hpBefore > hpAfter && hpAfter <= 0) {
                    scoreManager.addScoreForBrick(brick);
                    aliveBrickCount--; // 计数器递减
                }

                // 礼物砖块被击碎，触发整行整列扣血
                if (brick instanceof GiftBrick) {
                    GiftBrick gb = (GiftBrick) brick;
                    if (gb.isTiggerGift()) {
                        CollisionDetector.triggerGiftSkill(brick, brickList, scoreManager,this);

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
            }else{
                gameOver();
            }
        }
    }
    public void decrementAliveBrickCount() {
        aliveBrickCount--;
    }
   //检测实体小球与虚拟小球的碰撞,碰撞后在虚拟小球位置生成新的实体小球
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

    //    当一个关卡中的砖块都被击碎，进入下一关
    private void checkGameStatus() {
        if (aliveBrickCount <= 0) {
            nextLevel();
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
    //游戏结束的方法
    private void gameOver() {
        gameRunning = false;
        scoreManager.resetCombo();
        countdownActive = false;
        ballList.clear();
        virtualBallList.clear();
        brickList.clear();
        aliveBrickCount = 0;
    }
   //重新开始的方法
    public void restart() {
        currentLevel = 1;
        lifeCount = GameConstant.LIVES_COUNT;
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
//
    public boolean isCountdownActive() {
        return countdownActive;
    }
// 获取倒计时秒数
    public int getCountdownSeconds() {
        return countdownSeconds;
    }
// 获取小球列表
    public List<Ball> getBallList() {
        return ballList;
    }
// 获取挡板
    public Baffle getBaffle() {
        return baffle;
    }
// 获取砖块列表
    public List<Brick> getBrickList() {
        return brickList;
    }
// 获取虚拟小球列表
    public List<VirtualBall> getVirtualBallList() {
        return virtualBallList;
    }
// 判断游戏是否运行中
    public boolean isGameRunning() {
        return gameRunning;
    }
// 获取生命值
    public int getLifeCount() {
        return lifeCount;
    }
// 获取当前关卡
    public int getCurrentLevel() {
        return currentLevel;
    }
// 获取分数管理器
    public ScoreManager getScoreManager() {
        return scoreManager;
    }
// 获取当前游戏模式
    public GameMode getCurrentMode() {
        return currentMode;
    }
// 判断游戏模式是否已选择
    public boolean isModeSelected() {
        return modeSelected;
    }
}