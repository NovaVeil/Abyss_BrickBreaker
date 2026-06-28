package org.example.service;
// 计分管理器：计分机制
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.example.model.Brick;
import org.example.model.GiftBrick;
import org.example.model.HardBrick;

public class ScoreManager {
    private final IntegerProperty score;
    private final IntegerProperty combo;
    private final DoubleProperty comboMultiplier;
    private int currentLevel;

    // 基础分数配置
    private static final int NORMAL_BRICK_SCORE = 10;
    private static final int HARD_BRICK_SCORE = 20;
    private static final int GIFT_BRICK_SCORE = 30;

    // 连击配置
    private static final int COMBO_INCREMENT = 5;
    private static final double COMBO_MULTIPLIER_STEP = 0.5;

    public ScoreManager() {
        this.score = new SimpleIntegerProperty(0);
        this.combo = new SimpleIntegerProperty(0);
        this.comboMultiplier = new SimpleDoubleProperty(1.0);
        this.currentLevel = 1;
    }

    public void addScoreForBrick(Brick brick) {
        int baseScore = getBaseScore(brick);
        int finalScore = (int)(baseScore * currentLevel * comboMultiplier.get());
        score.set(score.get() + finalScore);

        combo.set(combo.get() + 1);

        if (combo.get() % COMBO_INCREMENT == 0) {
            comboMultiplier.set(comboMultiplier.get() + COMBO_MULTIPLIER_STEP);
        }
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

    public void resetCombo() {
        combo.set(0);
        comboMultiplier.set(1.0);
    }

    public void nextLevel() {
        currentLevel++;
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    public void resetAll() {
        this.score.set(0);
        this.combo.set(0);
        this.comboMultiplier.set(1.0);
        this.currentLevel = 1;
    }

    public int getScoreValue() {
        return score.get();
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public int getComboValue() {
        return combo.get();
    }

    public IntegerProperty comboProperty() {
        return combo;
    }

    public double getComboMultiplierValue() {
        return comboMultiplier.get();
    }

    public DoubleProperty comboMultiplierProperty() {
        return comboMultiplier;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
}
