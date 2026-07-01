package org.example.model;

public class GiftBrick extends Brick {
    // 定义礼物是否触发的变量
    private boolean giftTriggered = false;

    public GiftBrick(double x, double y) {
        super(x, y,3);
    }

    //判断是否触发礼物
    public boolean isTiggerGift(){
        return hp <= 0 && !giftTriggered;
    }
    // 标记礼物已触发
    public void markTriggered() {
        giftTriggered = true;
    }
}
