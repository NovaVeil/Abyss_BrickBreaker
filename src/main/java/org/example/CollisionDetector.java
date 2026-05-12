package org.example;

import java.util.List;

/*
 碰撞检测工具类
 负责：球碰挡板、球碰砖块
 所有方法都是静态工具方法，直接调用
 */
public class CollisionDetector {

    // 小球 碰 挡板
    public static boolean checkBaffileCollision(Ball ball, Baffle baffle) {
        boolean hit = ball.getX() + ball.getRadius() > baffle.getX()
                && ball.getX() - ball.getRadius() < baffle.getX() + baffle.getWidth()
                && ball.getY() + ball.getRadius() > baffle.getY()
                && ball.getY() - ball.getRadius() < baffle.getY() + baffle.getHeight();

        if (hit) {
            // 核心：只反向竖直速度，水平速度保留，严格符合物理反射
            ball.reflectVertical();
            // 可玩性微调
            double bafflieCenter = baffle.getX() + baffle.getWidth() / 2;
            double offset = (ball.getX() - bafflieCenter) / (baffle.getWidth() / 2);
            double tweakFactor = 0.5;
            int newDx = (int) (ball.getDx() + offset * 3 * tweakFactor);
            ball.setDx(newDx);
        }
        return hit;
    }

    // 小球 碰 砖块（优化版，分边反弹）
    public static boolean checkBrickCollision(Ball ball, Brick brick) {
        if (brick.isAlive()) {
            return true;
        }

        boolean hit = ball.getX() + ball.getRadius() > brick.getX()
                && ball.getX() - ball.getRadius() < brick.getX() + brick.getWidth()
                && ball.getY() + ball.getRadius() > brick.getY()
                && ball.getY() - ball.getRadius() < brick.getY() + brick.getHeight();

        if (hit) {
            // 判断是撞在上下边还是左右边
            double overlapLeft = (ball.getX() + ball.getRadius()) - brick.getX();
            double overlapRight = (brick.getX() + brick.getWidth()) - (ball.getX() - ball.getRadius());
            double overlapTop = (ball.getY() + ball.getRadius()) - brick.getY();
            double overlapBottom = (brick.getY() + brick.getHeight()) - (ball.getY() - ball.getRadius());

            double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

            if (minOverlap == overlapTop || minOverlap == overlapBottom) {
                // 撞在上下边 → 竖直反弹
                ball.reflectVertical();
            } else {
                // 撞在左右边 → 水平反弹
                ball.reflectHorizontal();
            }

            brick.isHit();
        }
        return hit;
    }

    // 判断小球是否掉出底部
    public static boolean isBallFallOut(Ball ball) {
        return ball.getY() - ball.getRadius() > GameConstant.GAME_HEIGHT;
    }
//    礼物砖道具功能：对相对应的giftbrick的同一行和同一列所有存活砖块血量-1
    public static void triggerGiftSkill(Brick giftBrick, List<Brick> allBricks) {
        double targetX = giftBrick.getX();
        double targetY = giftBrick.getY();

        for (Brick b : allBricks) {
            // 已经碎掉的不处理
            if (!b.isAlive()) {
                continue;
            }
            // 判断同一行 或 同一列
            boolean sameRow = Math.abs(b.getY() - targetY) < 2;
            boolean sameCol = Math.abs(b.getX() - targetX) < 2;

            if (sameRow || sameCol) {
                b.isHit();
            }
        }
    }
}

