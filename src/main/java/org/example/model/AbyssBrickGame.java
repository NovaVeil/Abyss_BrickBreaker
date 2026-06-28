package org.example.model;

import org.example.service.LevelManager;
import org.example.service.ScoreFile;
import org.example.service.ScoreManager;
import org.example.util.GameConstant;
import org.example.service.AudioManager;
import org.example.util.CollisionDetector;

import java.io.*;
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
    private boolean victoryScreen;
    private int victoryCountdownSeconds;
    private long lastVictoryCountdownTime;

    private static final String SAVE_DIR_NAME = ".abyss_brickbreaker";
    private static final String SAVE_FILE_NAME = "max_unlocked_level.dat";
    private static final File SAVE_DIR = new File(System.getProperty("user.home"), SAVE_DIR_NAME);
    private static final File SAVE_FILE = new File(SAVE_DIR, SAVE_FILE_NAME);

    public AbyssBrickGame() {
        ensureSaveDirectoryExists();
        
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
        
        this.maxUnlockedLevel = loadMaxUnlockedLevel();
        System.out.println("=== 游戏初始化完成，最大解锁关卡: " + this.maxUnlockedLevel + " ===");
        
        selectingLevel = false;
        selectedCampaignLevel = 1;

        // 读取历史最高分
        this.highScore = ScoreFile.loadHighScore();


        double baffleX = GAME_WIDTH / 2.0 - GameConstant.BAFFLE_WIDTH / 2.0;
        baffle = new Baffle(baffleX, GAME_HEIGHT - GameConstant.BAFFLE_HEIGHT - 10, currentLevel);

        audioManager.playBGM();
    }

    private void ensureSaveDirectoryExists() {
        try {
            if (!SAVE_DIR.exists()) {
                SAVE_DIR.mkdirs();
                System.out.println(">>> 创建存档目录: " + SAVE_DIR.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println(">>> 创建存档目录失败: " + e.getMessage());
        }
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
        
        ballList.clear();
        virtualBallList.clear();
        aliveBrickCount = 0;
        
        levelManager.initLevelStyle(currentLevel);
        initLevelBrick(currentLevel);
        this.lifeCount = GameConstant.LIVES_COUNT;
        
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
            System.out.println("游戏异常结束");
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
        if (currentTime - lastVictoryCountdownTime >= 1000000000L) {
            victoryCountdownSeconds--;
            lastVictoryCountdownTime = currentTime;

            if (victoryCountdownSeconds <= 0) {
                victoryScreen = false;
                goToNextLevel();
            }
        }
    }

    public void skipVictoryScreen() {
        if (victoryScreen) {
            victoryScreen = false;
            goToNextLevel();
            System.out.println(">>> 跳过胜利画面，直接进入下一关");
        }
    }

    private void goToNextLevel() {
        if (currentMode == GameMode.CAMPAIGN) {
            if (currentLevel < 10) {
                currentLevel++;
                if (currentLevel > maxUnlockedLevel) {
                    maxUnlockedLevel = currentLevel;
                    saveMaxUnlockedLevel(maxUnlockedLevel);
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

    private void checkAllCollision() {
        List<Ball> ballsToAdd = new ArrayList<>();
        Iterator<Ball> ballIterator = ballList.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();

            CollisionDetector.checkBaffleCollision(ball, baffle);

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
        if (aliveBrickCount <= 0 && lifeCount > 0) {
            nextLevel();
        }
    }


    private void nextLevel() {
        if (lifeCount <= 0) {
            gameOver();
            return;
        }
        
        victoryScreen = true;
        victoryCountdownSeconds = 3;
        lastVictoryCountdownTime = 0;
        gameRunning = false;
        
        ballList.clear();
        brickList.clear();
        virtualBallList.clear();
        aliveBrickCount = 0;
        
        System.out.println(">>> 关卡完成！显示胜利画面，3秒后进入第 " + (currentLevel + 1) + " 关");
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
        levelManager.setGameMode(GameMode.CAMPAIGN);
        
        this.maxUnlockedLevel = loadMaxUnlockedLevel();
        System.out.println("=== 游戏重启，重新加载最大解锁关卡: " + this.maxUnlockedLevel + " ===");

        ballList.clear();
        brickList.clear();
        virtualBallList.clear();
        aliveBrickCount = 0;
        audioManager.playBGM();
    }

    public void restartCurrentLevel() {
        int savedLevel = currentLevel;
        GameMode savedMode = currentMode;
        
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
        this.modeSelected = false;
        this.currentMode = null;
        this.levelManager.setGameMode(GameMode.CAMPAIGN);
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

    public LevelManager getLevelManager() {
        return levelManager;
    }

    private int loadMaxUnlockedLevel() {
        try {
            if (SAVE_FILE.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE));
                String line = reader.readLine();
                int level = Integer.parseInt(line.trim());
                reader.close();
                System.out.println(">>> ✓ 从文件读取到最大解锁关卡: " + level);
                System.out.println(">>>   文件路径: " + SAVE_FILE.getAbsolutePath());
                return Math.max(1, level);
            } else {
                System.out.println(">>> ℹ 存档文件不存在，使用默认值 1");
                System.out.println(">>>   预期文件路径: " + SAVE_FILE.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println(">>> ✗ 读取关卡解锁状态失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 1;
    }

    private void saveMaxUnlockedLevel(int level) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE));
            writer.write(String.valueOf(level));
            writer.flush();
            writer.close();
            System.out.println(">>> ✓ 已保存最大解锁关卡到文件: " + level);
            System.out.println(">>>   文件路径: " + SAVE_FILE.getAbsolutePath());
            
            verifySaveFile(level);
        } catch (Exception e) {
            System.err.println(">>> ✗ 保存关卡解锁状态失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void verifySaveFile(int expectedLevel) {
        try {
            if (SAVE_FILE.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE));
                String line = reader.readLine();
                int savedLevel = Integer.parseInt(line.trim());
                reader.close();
                
                if (savedLevel == expectedLevel) {
                    System.out.println(">>> ✓ 文件验证成功，保存的关卡: " + savedLevel);
                } else {
                    System.err.println(">>> ✗ 文件验证失败！期望: " + expectedLevel + "，实际: " + savedLevel);
                }
            } else {
                System.err.println(">>> ✗ 文件验证失败！文件不存在");
            }
        } catch (Exception e) {
            System.err.println(">>> ✗ 文件验证异常: " + e.getMessage());
        }
    }
}