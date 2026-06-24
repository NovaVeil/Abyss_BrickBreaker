package org.example;

public class GiftBrick extends  Brick {
    private boolean giftTriggered = false;

    public GiftBrick(double x, double y) {
        super(x, y,3);
    }

    //判断是否触发礼物
    public boolean isTiggerGift(){
        return hp <= 0 && !giftTriggered;
    }

    public void markTriggered() {
        giftTriggered = true;
    }
}
