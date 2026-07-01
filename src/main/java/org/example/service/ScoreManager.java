package org.example.service;
// 计分管理器：计分机制
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.example.model.Brick;
import org.example.model.GiftBrick;
import org.example.model.HardBrick;

public class ScoreManager {
    private final IntegerProperty score;
    private int hitCount;      // 有效碰撞次数（碰砖）
    private int comboScore;    // 连击得分（独立）
    private int currentLevel;

    // 基础分数配置
    private static final int NORMAL_BRICK_SCORE = 10;
    private static final int HARD_BRICK_SCORE = 20;
    private static final int GIFT_BRICK_SCORE = 30;


    public ScoreManager() {
        this.score = new SimpleIntegerProperty(0);
        this.currentLevel = 1;

        this.hitCount = 0;
        this.comboScore = 0;
    }

    public void addScoreForBrick(Brick brick) {
        int baseScore = getBaseScore(brick);
        int finalScore = baseScore * currentLevel;
        score.set(score.get() + finalScore);
    }

    public void addScore(int points) {
        score.set(score.get() + points);
    }

    private int getBaseScore(Brick brick) {
        if (brick instanceof GiftBrick) {
            return GIFT_BRICK_SCORE;
        } else if (brick instanceof HardBrick) {
            return HARD_BRICK_SCORE;
        } else {
            return NORMAL_BRICK_SCORE;
        }
    }


    /**
     * 小球碰撞砖块（无论是否击碎）
     */
    public void registerBrickHit() {
        hitCount++;
    }

    /**
     * 球死亡 / 碰墙 / 碰挡板时结算
     */
    public void settleCombo() {
        if (hitCount >= 2) {
            comboScore = hitCount * 20;
            score.set(score.get() + comboScore);
        }
        resetComboState();
    }

    private void resetComboState() {
        hitCount = 0;
        comboScore = 0;
    }

    public void nextLevel() {
        currentLevel++;
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    public void resetAll() {
        this.score.set(0);
        this.currentLevel = 1;
        this.hitCount = 0;
        this.comboScore = 0;
    }

    public int getScoreValue() {
        return score.get();
    }
}
