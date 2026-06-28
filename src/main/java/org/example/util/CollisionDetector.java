package org.example.util;

import org.example.model.*;
import org.example.service.ScoreManager;
import org.example.service.AudioManager;

import java.util.List;

/*
 碰撞检测工具类
 负责：球碰挡板、球碰砖块检测、球掉出底部检测、碰碎gift砖块检测并触发相应道具功能
 所有方法都是静态工具方法，直接调用
 */
public class CollisionDetector {

    // 小球 碰 挡板
    public static void checkBaffleCollision(Ball ball, Baffle baffle) {
        boolean hit = ball.getX() + ball.getRadius() > baffle.getX()
                && ball.getX() - ball.getRadius() < baffle.getX() + baffle.getWidth()
                && ball.getY() + ball.getRadius() > baffle.getY()
                && ball.getY() - ball.getRadius() < baffle.getY() + baffle.getHeight();

        if (hit) {
            // 判断小球是从上方还是下方撞击挡板
            double ballBottom = ball.getY() + ball.getRadius();
            double ballTop = ball.getY() - ball.getRadius();
            double baffleTop = baffle.getY();
            double baffleBottom = baffle.getY() + baffle.getHeight();
            
            // 计算重叠量
            double overlapFromTop = ballBottom - baffleTop;
            double overlapFromBottom = baffleBottom - ballTop;
            
            if (overlapFromTop < overlapFromBottom) {
                // 从上方撞击 - 将小球推到挡板上方
                ball.setY(baffleTop - ball.getRadius());
                
                // 确保向上反弹（强制设置向上的速度）
                if (ball.getDy() > 0) {
                    ball.setDy((int) -Math.abs(ball.getDy()));
                } else if (ball.getDy() == 0) {
                    ball.setDy(-GameConstant.BALL_SPEED_Y);
                }
            } else {
                // 从下方撞击 - 将小球推到挡板下方
                ball.setY(baffleBottom + ball.getRadius());
                
                // 确保向下反弹（强制设置向下的速度）
                if (ball.getDy() < 0) {
                    ball.setDy((int) Math.abs(ball.getDy()));
                } else if (ball.getDy() == 0) {
                    ball.setDy(GameConstant.BALL_SPEED_Y);
                }
            }
            
            // 可玩性微调 - 根据撞击位置调整水平速度
            double baffleCenter = baffle.getX() + baffle.getWidth() / 2;
            double offset = (ball.getX() - baffleCenter) / (baffle.getWidth() / 2);
            double tweakFactor = 0.5;
            int newDx = (int) (ball.getDx() + offset * 3 * tweakFactor);
            ball.setDx(newDx);

            AudioManager.getInstance().playHitSound();
        }
    }

    // 小球 碰 砖块（分边反弹）
    public static boolean checkBrickCollision(Ball ball, Brick brick) {
        if (!brick.isAlive()) {
            return false;
        }

        boolean hit;
        
        if (brick.getShape() == Brick.BrickShape.TRIANGLE) {
            hit = checkTriangleCollision(ball, brick);
        } else {
            hit = checkRectangleCollision(ball, brick);
        }

        if (hit) {
            AudioManager.getInstance().playHitSound();
            if (brick.getShape() == Brick.BrickShape.TRIANGLE) {
                ball.reflectVertical();
                double brickCenterY = brick.getY() + brick.getHeight() / 2.0;
                if (ball.getY() < brickCenterY) {
                    ball.setY(brick.getY() - ball.getRadius());
                } else {
                    ball.setY(brick.getY() + brick.getHeight() + ball.getRadius());
                }
            } else {
                double overlapLeft = (ball.getX() + ball.getRadius()) - brick.getX();
                double overlapRight = (brick.getX() + brick.getWidth()) - (ball.getX() - ball.getRadius());
                double overlapTop = (ball.getY() + ball.getRadius()) - brick.getY();
                double overlapBottom = (brick.getY() + brick.getHeight()) - (ball.getY() - ball.getRadius());

                double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

                if (minOverlap == overlapTop) {
                    ball.reflectVertical();
                    ball.setY(brick.getY() - ball.getRadius());
                } else if (minOverlap == overlapBottom) {
                    ball.reflectVertical();
                    ball.setY(brick.getY() + brick.getHeight() + ball.getRadius());
                } else if (minOverlap == overlapLeft) {
                    ball.reflectHorizontal();
                    ball.setX(brick.getX() - ball.getRadius());
                } else {
                    ball.reflectHorizontal();
                    ball.setX(brick.getX() + brick.getWidth() + ball.getRadius());
                }
            }

            brick.isHit();

        }
        return hit;
    }

    private static boolean checkRectangleCollision(Ball ball, Brick brick) {
        return ball.getX() + ball.getRadius() > brick.getX()
                && ball.getX() - ball.getRadius() < brick.getX() + brick.getWidth()
                && ball.getY() + ball.getRadius() > brick.getY()
                && ball.getY() - ball.getRadius() < brick.getY() + brick.getHeight();
    }

    private static boolean checkTriangleCollision(Ball ball, Brick brick) {
        double x = brick.getX();
        double y = brick.getY();
        double w = brick.getWidth();
        double h = brick.getHeight();

        double centerX = x + w / 2;
        double centerY = y + h / 2;
        double distance = Math.sqrt((ball.getX() - centerX) * (ball.getX() - centerX) + 
                                     (ball.getY() - centerY) * (ball.getY() - centerY));

        return distance < (ball.getRadius() + Math.min(w, h) / 2);
    }

    // 判断小球是否掉出底部
    public static boolean isBallFallOut(Ball ball) {
        return ball.getY() - ball.getRadius() > GameConstant.GAME_HEIGHT;
    }

    // 礼物砖道具功能：对相对应的 GiftBrick 的同一行和同一列所有存活砖块血量-1
    public static void triggerGiftSkill(Brick giftBrick, List<Brick> allBricks, ScoreManager scoreManager, AbyssBrickGame game) {
        double targetX = giftBrick.getX();
        double targetY = giftBrick.getY();

        ((GiftBrick) giftBrick).markTriggered();

        for (Brick b : allBricks) {
            // 已经碎掉的不处理
            if (!b.isAlive()) {
                continue;
            }
            // 跳过自身
            if (b == giftBrick) {
                continue;
            }
            // 判断同一行 或 同一列
            boolean sameRow = Math.abs(b.getY() - targetY) < 2;
            boolean sameCol = Math.abs(b.getX() - targetX) < 2;

            if (sameRow || sameCol) {
                b.isHit();
                // 如果连锁击杀，立即计分
                if (!b.isAlive()) {
                    scoreManager.addScoreForBrick(b);
                    game.decrementAliveBrickCount();
                }
            }
        }
    }
}

