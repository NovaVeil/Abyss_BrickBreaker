package org.example.model;
//枚举类：两种游戏模式
public enum GameMode {
    CAMPAIGN("闯关模式", "挑战精心设计的图形关卡，每关图案不同"),
    ENDLESS("无尽模式", "无限随机生成的砖块，挑战最高分数");
    
    private final String name;
    private final String description;
    
    GameMode(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}
