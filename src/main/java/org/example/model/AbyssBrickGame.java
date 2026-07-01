package org.example.model;

import org.example.service.LevelManager;
import org.example.service.ScoreFile;
import org.example.service.ScoreManager;
import org.example.util.GameConstant;
import org.example.service.AudioManager;
import org.example.util.CollisionDetector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map;

public class AbyssBrickGame {
    private final AudioManager audioManager = AudioManager.getInstance();

    public static final int GAME_WIDTH = GameConstant.GAME_WIDTH;
    public static final int GAME_HEIGHT = GameConstant.GAME_HEIGHT;

    private List<Ball> ballList;
    private Baffle baffle;
    private List<Brick> brickList;
    private List<VirtualBall> virtualBallList;
    private int aliveBrickCount = 0;

    private final List<Ball> ballsToAddBuffer = new ArrayList<>();

    private boolean gameRunning;
    private boolean countdownActive;
    private int currentLevel;
    private int lifeCount;
    private int countdownSeconds;
    private long lastCountdownTime;
    private ScoreManager scoreManager;
    private LevelManager levelManager;
    private GameMode currentMode;
    private int maxUnlockedLevel;
    private boolean victoryScreen;
    private int victoryCountdownSeconds;
    private long lastVictoryCountdownTime;
    private Map<Integer, Integer> levelScores;
    private List<VictoryParticle> victoryParticles;
    private long lastParticleSpawnTime;

    private static final long COUNTDOWN_INTERVAL_MS = 1000;
    private static final long VICTORY_COUNTDOWN_INTERVAL_NS = 1_000_000_000L;

    public AbyssBrickGame() {
        brickList = new ArrayList<>();
        ballList = new ArrayList<>();
        virtualBallList = new ArrayList<>();
        victoryParticles = new ArrayList<>();
        currentLevel = 1;
        lifeCount = GameConstant.LIVES_COUNT;
        gameRunning = false;
        countdownActive = false;
        countdownSeconds = 3;
        scoreManager = new ScoreManager();
        levelManager = new LevelManager();
        currentMode = null;
        
        this.maxUnlockedLevel = ScoreFile.loadMaxUnlockedLevel();
        System.out.println("=== 游戏初始化成功，最大解锁关卡: " + this.maxUnlockedLevel + " ===");
        
        this.levelScores = ScoreFile.loadLevelScores();

        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);
        
        audioManager.playBGM();
    }

    public void startWithMode(GameMode mode) {
        this.currentMode = mode;
        levelManager.setGameMode(mode);
        
        if (mode == GameMode.CAMPAIGN) {
            this.levelScores = ScoreFile.loadLevelScores();
        } else {
            this.currentLevel = 1;
            this.lifeCount = GameConstant.LIVES_COUNT;
            
            ballList.clear();
            virtualBallList.clear();
            aliveBrickCount = 0;
            
            levelManager.initLevelStyle(currentLevel);
            initLevelBrick(currentLevel);
            
            double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
            baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);
            
            startCountdown();
        }
    }
    
    public void startCampaignLevel(int level) {
        this.currentMode = GameMode.CAMPAIGN;
        this.currentLevel = level;
        
        ballList.clear();
        virtualBallList.clear();
        aliveBrickCount = 0;
        
        levelManager.initLevelStyle(currentLevel);
        initLevelBrick(currentLevel);
        this.lifeCount = GameConstant.LIVES_COUNT;

        scoreManager.resetAll();
        if (level > 1) {
            int inheritedScore = levelScores.getOrDefault(level - 1, 0);
            scoreManager.setScore(inheritedScore);
        }


        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);
        
        startCountdown();
    }

    public void startCountdown() {
        countdownActive = true;
        countdownSeconds = 3;
        lastCountdownTime = System.currentTimeMillis();
        gameRunning = false;
    }

    private void updateCountdown() {
        if (!countdownActive || victoryScreen) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCountdownTime >= COUNTDOWN_INTERVAL_MS) {
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
            System.err.println("关卡初始化失败: " + e.getMessage());
            e.printStackTrace();
            gameRunning = false;
        }
    }

    public void update(long now) {
        if (lifeCount <= 0) {
            return;
        }
        try {
            updateCountdown();
            if (!gameRunning) {
                if (victoryScreen) {
                    updateVictoryCountdown(now);
                }
                return;
            }

            for (Ball ball : ballList) {
                ball.move(now);
            }


            checkAllCollision();
            checkGameStatus();
        } catch (Exception e) {
            gameRunning = false;
            System.err.println("游戏异常结束: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateVictoryCountdown(long now) {
        if (!victoryScreen) {
            return;
        }

        if (lastVictoryCountdownTime == 0) {
            lastVictoryCountdownTime = now;
            return;
        }

        long currentTime = now;
        if (currentTime - lastVictoryCountdownTime >= VICTORY_COUNTDOWN_INTERVAL_NS) {
            victoryCountdownSeconds--;
            lastVictoryCountdownTime = currentTime;

            if (victoryCountdownSeconds <= 0) {
                victoryScreen = false;
                goToNextLevel();
            }
        }
    }

    public void skipVictoryScreen() {
        if (victoryScreen && !gameRunning) {
            victoryScreen = false;
            goToNextLevel();
            System.out.println(">>> 跳过胜利画面，直接进入下一关");
        }
    }

    private void goToNextLevel() {
        if (currentMode == GameMode.CAMPAIGN) {
            if (currentLevel < 10) {
                levelScores.put(currentLevel, scoreManager.getScoreValue());
                ScoreFile.saveLevelScore(currentLevel, scoreManager.getScoreValue());

                currentLevel++;
                if (currentLevel > maxUnlockedLevel) {
                    maxUnlockedLevel = currentLevel;
                    ScoreFile.saveMaxUnlockedLevel(maxUnlockedLevel); // ✅ 仅闯关
                }
                scoreManager.nextLevel();
                levelManager.initLevelStyle(currentLevel);

                this.lifeCount = GameConstant.LIVES_COUNT;
                ballList.clear();
                initLevelBrick(currentLevel);

                double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
                baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);

                startCountdown();
            } else {
                gameOverWin();
            }
        } else {
            // ✅ 无尽模式：只加等级，不存档
            currentLevel++;
            scoreManager.nextLevel();
            initNextLevel();
        }
    }

    private void initNextLevel() {
        levelManager.initLevelStyle(currentLevel);

            this.lifeCount = GameConstant.LIVES_COUNT;
            ballList.clear();
            initLevelBrick(currentLevel);

            double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
            baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);

        startCountdown();
    }

    private void checkAllCollision() {
        ballsToAddBuffer.clear();
        
        Iterator<Ball> ballIterator = ballList.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();

            CollisionDetector.checkBaffleCollision(ball, baffle);

            for (Brick brick : brickList) {
                int hpBefore = brick.getHp();

                // ✅ 只要发生碰撞检测（返回值可改，或用标志位），
                //    我们约定：checkBrickCollision 内部返回 true 表示发生了砖块碰撞
                boolean brickHit = CollisionDetector.checkBrickCollision(ball, brick);
                int hpAfter = brick.getHp();

                // ✅ 有效碰撞（碰砖），非礼物砖块才计入连击
                if (brickHit && !(brick instanceof GiftBrick)) {
                    scoreManager.registerBrickHit();
                }

                // ✅ 击碎砖块 → 基础分（礼物砖块也加基础分）
                if (hpBefore > hpAfter && hpAfter <= 0) {
                    scoreManager.addScoreForBrick(brick);
                    aliveBrickCount--;
                }

                if (brick instanceof GiftBrick gb && gb.isTiggerGift()) {
                    CollisionDetector.triggerGiftSkill(brick, brickList, scoreManager, this);
                }
            }

            List<Ball> newBalls = checkVirtualBallCollision(ball);
            if (newBalls != null && !newBalls.isEmpty()) {
                ballsToAddBuffer.addAll(newBalls);
            }
        }

        ballList.removeIf(ball -> CollisionDetector.isBallFallOut(ball));

        ballList.addAll(ballsToAddBuffer);

        if (ballList.isEmpty() && lifeCount > 0) {
            lifeCount--;
            if (lifeCount > 0) {
                scoreManager.settleCombo();
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
                newBall.setDy(-GameConstant.BALL_SPEED_Y);
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
        if (aliveBrickCount <= 0 && lifeCount > 0 && !victoryScreen) {
            nextLevel();
        }
    }


    private void nextLevel() {
        if (lifeCount <= 0) {
            gameOver();
            return;
        }

        System.out.println(">>> [DEBUG] nextLevel: currentMode=" + currentMode + ", currentLevel=" + currentLevel + ", maxUnlockedLevel=" + maxUnlockedLevel);

        if (currentMode == GameMode.CAMPAIGN) {
            int nextLevel = currentLevel + 1;
            if (nextLevel > maxUnlockedLevel) {
                maxUnlockedLevel = nextLevel;
            }
        }

        int currentScore = scoreManager.getScoreValue();
        int oldScore = levelScores.getOrDefault(currentLevel, 0);
        if (currentScore > oldScore) {
            levelScores.put(currentLevel, currentScore);
        }
        ScoreFile.saveLevelScore(currentLevel, currentScore);
        ScoreFile.saveMaxUnlockedLevel(maxUnlockedLevel);

        victoryScreen = true;
        victoryCountdownSeconds = 3;
        lastVictoryCountdownTime = 0;
        lastParticleSpawnTime = 0;
        victoryParticles.clear();
        gameRunning = false;
        countdownActive = false;

        ballList.clear();
        brickList.clear();
        virtualBallList.clear();
        aliveBrickCount = 0;
        
        spawnCelebrationParticles();
        
        System.out.println(">>> 关卡完成！显示胜利画面，3秒后进入第 " + (currentLevel + 1) + " 关");
    }
    
    private void spawnCelebrationParticles() {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            double x = random.nextDouble() * GAME_WIDTH;
            double y = GAME_HEIGHT + random.nextDouble() * 100;
            victoryParticles.add(new VictoryParticle(x, y));
        }
    }
    
    public void updateVictoryParticles(long now) {
        if (lastParticleSpawnTime == 0) {
            lastParticleSpawnTime = now;
        }
        
        if (now - lastParticleSpawnTime > 100000000L) {
            Random random = new Random();
            int count = random.nextInt(3) + 2;
            for (int i = 0; i < count; i++) {
                double x = random.nextDouble() * GAME_WIDTH;
                double y = GAME_HEIGHT + 20;
                victoryParticles.add(new VictoryParticle(x, y));
            }
            lastParticleSpawnTime = now;
        }
        
        Iterator<VictoryParticle> iterator = victoryParticles.iterator();
        while (iterator.hasNext()) {
            VictoryParticle particle = iterator.next();
            particle.update(0.016);
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }
    
    public List<VictoryParticle> getVictoryParticles() {
        return victoryParticles;
    }
    
    private void gameOverWin() {
        gameRunning = false;
        countdownActive = false;
        AudioManager.getInstance().stopBGM();
    }

    private void gameOver() {
        gameRunning = false;
        scoreManager.settleCombo();
        countdownActive = false;
        AudioManager.getInstance().stopBGM();
        ballList.clear();
        virtualBallList.clear();
        brickList.clear();
        // ✅ 按模式保存最高分
        ScoreFile.saveHighScore(
                scoreManager.getScoreValue(),
                currentMode
        );
        aliveBrickCount = 0;
        lifeCount = 0;
    }

    public void restart() {
        currentLevel = 1;
        lifeCount = GameConstant.LIVES_COUNT;
        gameRunning = false;
        countdownActive = false;
        victoryScreen = false;
        scoreManager = new ScoreManager();

        this.maxUnlockedLevel = ScoreFile.loadMaxUnlockedLevel();
        this.levelScores = ScoreFile.loadLevelScores();
        System.out.println("=== 游戏重启，重新加载最大解锁关卡: " + this.maxUnlockedLevel + " ===");

        ballList.clear();
        brickList.clear();
        virtualBallList.clear();
        victoryParticles.clear();
        aliveBrickCount = 0;

        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, 1);

        audioManager.playBGM();
    }

    public void restartCurrentLevel() {
        int savedLevel = currentLevel;
        GameMode savedMode = currentMode;
        
        scoreManager.settleCombo();
        gameRunning = false;
        countdownActive = false;
        scoreManager = new ScoreManager();
        lifeCount = GameConstant.LIVES_COUNT;
        
        ballList.clear();
        brickList.clear();
        virtualBallList.clear();
        aliveBrickCount = 0;
        
        currentLevel = savedLevel;
        currentMode = savedMode;
        
        levelManager.initLevelStyle(currentLevel);
        initLevelBrick(currentLevel);
        
        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);
        
        startCountdown();
        
        System.out.println(">>> 重新开始当前关卡: " + currentLevel + "，生命值重置为 " + lifeCount);
    }

    public void resetModeSelection() {
        this.currentMode = null;
        this.levelManager.setGameMode(GameMode.CAMPAIGN);

        this.gameRunning = false;
        this.countdownActive = false;
        this.victoryScreen = false;
        this.lifeCount = GameConstant.LIVES_COUNT;
        this.currentLevel = 1;
        this.scoreManager = new ScoreManager();

        this.ballList.clear();
        this.brickList.clear();
        this.virtualBallList.clear();
        this.victoryParticles.clear();
        this.aliveBrickCount = 0;

        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, 1);
    }

    public boolean isVictoryScreen() {
        return victoryScreen;
    }

    public int getVictoryCountdownSeconds() {
        return victoryCountdownSeconds;
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
    
    public int getMaxUnlockedLevel() {
        return maxUnlockedLevel;
    }
    public int getCurrentScore() {
        return scoreManager.getScoreValue();
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }
}