package org.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class LevelSelector {
    private Canvas canvas;
    private GraphicsContext gc;

    private static final int TOTAL_LEVELS = 10;
    private static final int LEVELS_PER_ROW = 5;
    private static final int BUTTON_SIZE = 80;
    private static final int BUTTON_GAP = 20;
    private static final int START_X = 140;
    private static final int START_Y = 150;

    private LevelButton[] levelButtons;
    private int maxUnlockedLevel;

    public LevelSelector(Canvas canvas, int maxUnlockedLevel) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.maxUnlockedLevel = maxUnlockedLevel;
        initializeButtons();
    }

    private void initializeButtons() {
        levelButtons = new LevelButton[TOTAL_LEVELS];

        for (int i = 0; i < TOTAL_LEVELS; i++) {
            int row = i / LEVELS_PER_ROW;
            int col = i % LEVELS_PER_ROW;

            double x = START_X + col * (BUTTON_SIZE + BUTTON_GAP);
            double y = START_Y + row * (BUTTON_SIZE + BUTTON_GAP);

            boolean isLocked = (i + 1) > maxUnlockedLevel;
            levelButtons[i] = new LevelButton(x, y, BUTTON_SIZE, BUTTON_SIZE, i + 1, isLocked);
        }
    }

    public void render() {
        gc.setFill(Color.rgb(0, 0, 0, 0.85));
        gc.fillRect(0, 0, GameConstant.GAME_WIDTH, GameConstant.GAME_HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 32));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("选择关卡", GameConstant.GAME_WIDTH / 2.0, 80);

        gc.setFont(Font.font("Microsoft YaHei", 16));
        gc.fillText("共 " + TOTAL_LEVELS + " 关 | 当前解锁到第 " + maxUnlockedLevel + " 关",
                GameConstant.GAME_WIDTH / 2.0, 110);

        for (LevelButton button : levelButtons) {
            button.render(gc);
        }

        drawBrickLegend();
    }

    private void drawBrickLegend() {
        double legendX = 50;
        double legendY = GameConstant.GAME_HEIGHT - 150;

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("砖块类型说明：", legendX, legendY);

        gc.setFill(Color.web("#4A90E2"));
        gc.fillRoundRect(legendX, legendY + 10, 30, 20, 3, 3);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 14));
        gc.fillText("普通砖块 (1滴血)", legendX + 40, legendY + 27);

        gc.setFill(Color.web("#F5A623"));
        gc.fillRoundRect(legendX, legendY + 40, 30, 20, 3, 3);
        gc.setFill(Color.WHITE);
        gc.fillText("坚硬砖块 (2滴血)", legendX + 40, legendY + 57);

        gc.setFill(Color.web("#7ED321"));
        gc.fillRoundRect(legendX, legendY + 70, 30, 20, 3, 3);
        gc.setFill(Color.WHITE);
        gc.fillText("礼物砖块 (3滴血)", legendX + 40, legendY + 87);

        gc.setFill(Color.web("#9B59B6"));
        gc.fillRoundRect(legendX, legendY + 100, 30, 20, 3, 3);
        gc.setFill(Color.WHITE);
        gc.fillText("三角砖块 (1滴血)", legendX + 40, legendY + 117);
    }

    public Integer handleClick(double mouseX, double mouseY) {
        for (LevelButton button : levelButtons) {
            if (button.contains(mouseX, mouseY) && !button.isLocked()) {
                return button.getLevel();
            }
        }
        return null;
    }

    public void handleMouseMove(double mouseX, double mouseY) {
        for (LevelButton button : levelButtons) {
            if (button.contains(mouseX, mouseY)) {
                button.setHovered(!button.isLocked());
            } else {
                button.setHovered(false);
            }
        }
    }

    public void updateMaxUnlockedLevel(int level) {
        this.maxUnlockedLevel = level;
        for (LevelButton button : levelButtons) {
            button.setLocked(button.getLevel() > maxUnlockedLevel);
        }
    }

    private static class LevelButton {
        private double x, y, width, height;
        private int level;
        private boolean locked;
        private boolean hovered;

        public LevelButton(double x, double y, double width, double height, int level, boolean locked) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.level = level;
            this.locked = locked;
            this.hovered = false;
        }

        public void render(GraphicsContext gc) {
            if (locked) {
                gc.setFill(Color.web("#555555"));
                gc.setStroke(Color.web("#777777"));
                gc.setLineWidth(2);
            } else if (hovered) {
                gc.setFill(Color.web("#4A90E2", 0.6));
                gc.setStroke(Color.web("#4A90E2"));
                gc.setLineWidth(3);
            } else {
                gc.setFill(Color.web("#FFFFFF", 0.2));
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
            }

            gc.fillRoundRect(x, y, width, height, 10, 10);
            gc.strokeRoundRect(x, y, width, height, 10, 10);

            if (locked) {
                gc.setFill(Color.web("#AAAAAA"));
                gc.setFont(Font.font("Microsoft YaHei", 24));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("🔒", x + width / 2, y + height / 2 - 5);
                gc.setFont(Font.font("Microsoft YaHei", 12));
                gc.fillText("第" + level + "关", x + width / 2, y + height / 2 + 20);
            } else {
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Microsoft YaHei", 24));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(String.valueOf(level), x + width / 2, y + height / 2 + 8);
            }
        }

        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                   mouseY >= y && mouseY <= y + height;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean isHovered() {
            return hovered;
        }

        public void setHovered(boolean hovered) {
            this.hovered = hovered;
        }

        public int getLevel() {
            return level;
        }
    }
}

