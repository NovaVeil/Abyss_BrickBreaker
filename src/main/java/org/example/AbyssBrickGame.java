package org.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbyssBrickGame {
    private final AudioManager audioManager = AudioManager.getInstance();

    public static final int GAME_WIDTH = GameConstant.GAME_WIDTH;
    public static final int GAME_HEIGHT = GameConstant.GAME_HEIGHT;

    private List<Ball> ballList;
    private Baffle baffle;
    private List<Brick> brickList;
    private List<VirtualBall> virtualBallList;
    private int aliveBrickCount = 0;

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
    private int maxUnlockedLevel;
    private boolean selectingLevel;
    private int selectedCampaignLevel;
    private int highScore = 0;

    public AbyssBrickGame() {
        brickList = new ArrayList<>();
        ballList = new ArrayList<>();
        virtualBallList = new ArrayList<>();
        currentLevel = 1;
        lifeCount = GameConstant.LIVES_COUNT;
        gameRunning = false;
        countdownActive = false;
        countdownSeconds = 3;
        scoreManager = new ScoreManager();
        levelManager = new LevelManager();
        currentMode = null;
        modeSelected = false;
        maxUnlockedLevel = 1;
        selectingLevel = false;
        selectedCampaignLevel = 1;

        // 读取历史最高分
        this.highScore = ScoreFile.loadHighScore();


        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);

        audioManager.playBGM();
    }

    public void startWithMode(GameMode mode) {
        this.currentMode = mode;
        this.modeSelected = true;
        levelManager.setGameMode(mode);
        
        if (mode == GameMode.CAMPAIGN) {
            this.selectingLevel = true;
        } else {
            this.selectingLevel = false;
            this.currentLevel = 1;
            this.lifeCount = GameConstant.LIVES_COUNT;
            levelManager.initLevelStyle(currentLevel);
            initLevelBrick(currentLevel);
            startCountdown();
        }
    }
    
    public void startCampaignLevel(int level) {
        this.selectingLevel = false;
        this.currentLevel = level;
        this.selectedCampaignLevel = level;
        levelManager.initLevelStyle(currentLevel);
        initLevelBrick(currentLevel);
        this.lifeCount = GameConstant.LIVES_COUNT;
        startCountdown();
    }

    public void startCountdown() {
        countdownActive = true;
        countdownSeconds = 3;
        lastCountdownTime = System.currentTimeMillis();
        gameRunning = false;
    }

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
                respawnBallAtPaddle();
            }
        }
    }

    private void respawnBallAtPaddle() {
        double ballStartX = baffle.getX() + baffle.getWidth() / 2.0;
        double ballStartY = baffle.getY() - GameConstant.BALL_RADIUS;
        Ball ball = new Ball(ballStartX, ballStartY);
        int initialDx = (Math.random() > 0.5 ? 1 : -1) * GameConstant.BALL_SPEED_X;
        ball.setDx(initialDx);
        ball.setDy(-GameConstant.BALL_SPEED_Y);
        ballList.add(ball);
    }

    private void initLevelBrick(int level) {
        try {
            brickList.clear();
            brickList.addAll(levelManager.generateBricks(level));
            aliveBrickCount = brickList.size();
            virtualBallList.clear();
            virtualBallList.addAll(levelManager.generateVirtualBalls(level, brickList));
        } catch (Exception e) {
            System.out.println("关卡初始化失败");
            gameRunning = false;
        }
    }

    public void update() {
        if (lifeCount <= 0) {
            return;
        }
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

    private void checkAllCollision() {
        List<Ball> ballsToAdd = new ArrayList<>();
        Iterator<Ball> ballIterator = ballList.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();

            CollisionDetector.checkBaffileCollision(ball, baffle);

            for (Brick brick : brickList) {
                int hpBefore = brick.getHp();
                CollisionDetector.checkBrickCollision(ball, brick);
                int hpAfter = brick.getHp();

                if (hpBefore > hpAfter && hpAfter <= 0) {
                    scoreManager.addScoreForBrick(brick);
                    aliveBrickCount--; // 计数器递减
                }

                if (brick instanceof GiftBrick) {
                    GiftBrick gb = (GiftBrick) brick;
                    if (gb.isTiggerGift()) {
                        CollisionDetector.triggerGiftSkill(brick, brickList, scoreManager, this);

                    }
                }
            }

            List<Ball> newBalls = checkVirtualBallCollision(ball);
            if (newBalls != null && !newBalls.isEmpty()) {
                ballsToAdd.addAll(newBalls);
            }
        }

        ballList.removeIf(ball -> CollisionDetector.isBallFallOut(ball));

        ballList.addAll(ballsToAdd);

        if (ballList.isEmpty() && lifeCount > 0) {
            lifeCount--;
            if (lifeCount > 0) {
                respawnBallAtPaddle();
            } else {
                gameOver();
            }
        }
    }

    public void decrementAliveBrickCount() {
        aliveBrickCount--;
    }

    private List<Ball> checkVirtualBallCollision(Ball realBall) {
        List<VirtualBall> toRemove = new ArrayList<>();
        List<Ball> toAdd = new ArrayList<>();
        for (VirtualBall virtualBall : virtualBallList) {
            if (!virtualBall.isActive()) {
                continue;
            }

            double dx = realBall.getX() - virtualBall.getX();
            double dy = realBall.getY() - virtualBall.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < (realBall.getRadius() + virtualBall.getRadius())) {
                Ball newBall = new Ball(virtualBall.getX(), virtualBall.getY());

                int newDx = (Math.random() > 0.5 ? 1 : -1) * GameConstant.BALL_SPEED_X;
                newBall.setDy(-GameConstant.BALL_SPEED_Y); // 向上
                newBall.setDx(newDx);

                toAdd.add(newBall);
                virtualBall.deactivate();
                toRemove.add(virtualBall);

                scoreManager.addScore(50);
            }
        }
        virtualBallList.removeAll(toRemove);
        return toAdd;
    }

    private void checkGameStatus() {
        if (aliveBrickCount <= 0) {
            nextLevel();
        }
    }


    private void nextLevel() {
        if (currentMode == GameMode.CAMPAIGN) {
            if (currentLevel < 10) {
                currentLevel++;
                if (currentLevel > maxUnlockedLevel) {
                    maxUnlockedLevel = currentLevel;
                }
                scoreManager.nextLevel();
                levelManager.initLevelStyle(currentLevel);

                this.lifeCount = GameConstant.LIVES_COUNT;
                ballList.clear();
                initLevelBrick(currentLevel);

                double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
                baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);

                AudioManager.getInstance().playLevelUpSound();

                startCountdown();
            } else {
                gameOverWin();
            }
        } else {
            currentLevel++;
            scoreManager.nextLevel();
            levelManager.initLevelStyle(currentLevel);

            this.lifeCount = GameConstant.LIVES_COUNT;
            ballList.clear();
            initLevelBrick(currentLevel);

            double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
            baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);

            AudioManager.getInstance().playLevelUpSound();

            startCountdown();
        }
    }
    
    private void gameOverWin() {
        gameRunning = false;
        countdownActive = false;
        AudioManager.getInstance().stopBGM();
    }

    private void gameOver() {
        gameRunning = false;
        scoreManager.resetCombo();
        countdownActive = false;
        AudioManager.getInstance().stopBGM();
        ballList.clear();
        virtualBallList.clear();
        brickList.clear();
        aliveBrickCount = 0;
        lifeCount = 0;
        ScoreFile.saveHighScore(scoreManager.getScoreValue());
    }

    public void restart() {
        currentLevel = 1;
        lifeCount = GameConstant.LIVES_COUNT;
        gameRunning = false;
        countdownActive = false;
        scoreManager = new ScoreManager();
        modeSelected = false;
        currentMode = null;
        selectingLevel = false;
        selectedCampaignLevel = 1;

        ballList.clear();
        brickList.clear();
        virtualBallList.clear();
        aliveBrickCount = 0;
        audioManager.playBGM();
    }

    public void resetModeSelection() {
        this.modeSelected = false;
        this.currentMode = null;
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
    
    public boolean isSelectingLevel() {
        return selectingLevel;
    }
    
    public int getMaxUnlockedLevel() {
        return maxUnlockedLevel;
    }
    
    public int getSelectedCampaignLevel() {
        return selectedCampaignLevel;
    }
    public int getHighScore() {
        return ScoreFile.loadHighScore();
    }

    public int getCurrentScore() {
        return scoreManager.getScoreValue();
    }
}