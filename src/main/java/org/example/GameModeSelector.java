package org.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;

public class GameModeSelector {
    private Canvas canvas;
    private GraphicsContext gc;
    private Image backgroundImage;
    
    private ModeButton campaignButton;
    private ModeButton endlessButton;
    
    private boolean showingDescription;
    private GameMode hoveredMode;
    
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 150;
    private static final int BUTTON_GAP = 50;
    
    public GameModeSelector(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.showingDescription = false;
        this.hoveredMode = null;
        
        initializeButtons();
        loadBackgroundImage();
    }
    
    private void initializeButtons() {
        double centerX = GameConstant.GAME_WIDTH / 2.0;
        double centerY = GameConstant.GAME_HEIGHT / 2.0;
        
        double campaignX = centerX - BUTTON_WIDTH - BUTTON_GAP / 2.0;
        double campaignY = centerY - BUTTON_HEIGHT / 2.0;
        
        double endlessX = centerX + BUTTON_GAP / 2.0;
        double endlessY = centerY - BUTTON_HEIGHT / 2.0;
        
        this.campaignButton = new ModeButton(campaignX, campaignY, BUTTON_WIDTH, BUTTON_HEIGHT, GameMode.CAMPAIGN);
        this.endlessButton = new ModeButton(endlessX, endlessY, BUTTON_WIDTH, BUTTON_HEIGHT, GameMode.ENDLESS);
    }
    
   private void loadBackgroundImage() {
    try {
        Image img = new Image(getClass().getResourceAsStream("/mode_select_bg.png"));
        if (!img.isError()) {
            this.backgroundImage = img;
        }
    } catch (Exception e) {
        this.backgroundImage = null;  // 使用默认背景色
    }
}

    
    public void render() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, GameConstant.GAME_WIDTH, GameConstant.GAME_HEIGHT);
        } else {
            gc.setFill(Color.web("#0a0e27"));
            gc.fillRect(0, 0, GameConstant.GAME_WIDTH, GameConstant.GAME_HEIGHT);
        }
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", Font.getDefault().getSize() * 2));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("选择游戏模式", GameConstant.GAME_WIDTH / 2.0, 100);
        
        campaignButton.render(gc);
        endlessButton.render(gc);
        
        if (showingDescription && hoveredMode != null) {
            drawDescriptionBox(hoveredMode);
        }
        
        drawInputMethodNotice();
    }
    
    private void drawInputMethodNotice() {
        double noticeY = GameConstant.GAME_HEIGHT - 40;
        
        gc.setFill(Color.web("#FFD93D", 0.9));
        gc.setFont(Font.font("Microsoft YaHei", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚠ 温馨提示：请将输入法切换为英文模式，即可用键盘操作",
                GameConstant.GAME_WIDTH / 2.0, noticeY);
    }
    
    private void drawDescriptionBox(GameMode mode) {
        double boxWidth = 400;
        double boxHeight = 80;
        double boxX = (GameConstant.GAME_WIDTH - boxWidth) / 2;
        double boxY = GameConstant.GAME_HEIGHT - 150;
        
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        
        gc.setStroke(Color.web("#4A90E2"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Microsoft YaHei", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(mode.getDescription(), GameConstant.GAME_WIDTH / 2.0, boxY + 45);
    }
    
    public GameMode handleMouseMove(double mouseX, double mouseY) {
        campaignButton.setHovered(false);
        endlessButton.setHovered(false);
        showingDescription = false;
        hoveredMode = null;
        
        if (campaignButton.contains(mouseX, mouseY)) {
            campaignButton.setHovered(true);
            showingDescription = true;
            hoveredMode = GameMode.CAMPAIGN;
        } else if (endlessButton.contains(mouseX, mouseY)) {
            endlessButton.setHovered(true);
            showingDescription = true;
            hoveredMode = GameMode.ENDLESS;
        }
        
        return null;
    }
    
    public GameMode handleClick(double mouseX, double mouseY) {
        if (campaignButton.contains(mouseX, mouseY)) {
            return GameMode.CAMPAIGN;
        } else if (endlessButton.contains(mouseX, mouseY)) {
            return GameMode.ENDLESS;
        }
        return null;
    }
    
    private static class ModeButton {
        private double x, y, width, height;
        private GameMode mode;
        private boolean hovered;
        
        public ModeButton(double x, double y, double width, double height, GameMode mode) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.mode = mode;
            this.hovered = false;
        }
        
        public void render(GraphicsContext gc) {
            if (hovered) {
                gc.setFill(Color.web("#4A90E2", 0.3));
                gc.setStroke(Color.web("#4A90E2"));
                gc.setLineWidth(3);
            } else {
                gc.setFill(Color.web("#FFFFFF", 0.1));
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2);
            }
            
            gc.fillRoundRect(x, y, width, height, 15, 15);
            gc.strokeRoundRect(x, y, width, height, 15, 15);
            
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Microsoft YaHei", 20));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(mode.getName(), x + width / 2, y + height / 2 - 10);
            
            gc.setFont(Font.font("Microsoft YaHei", 14));
            gc.fillText("点击选择", x + width / 2, y + height / 2 + 20);
        }
        
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                   mouseY >= y && mouseY <= y + height;
        }
        
        public void setHovered(boolean hovered) {
            this.hovered = hovered;
        }
    }
}
