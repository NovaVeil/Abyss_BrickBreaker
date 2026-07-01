package org.example.model;

import javafx.scene.paint.Color;
import java.util.Random;

/**
 * 胜利粒子类
 * 当玩家通过一关时，屏幕上飞出的彩色粒子（星星、方块、圆片、彩纸）
 * 每个粒子都有自己的位置、速度、颜色、形状和透明度
 * 通过物理模拟（重力）实现抛物线运动，并逐渐淡出消失
 */
public class VictoryParticle {
    // ==================== 粒子属性 ====================
    private double x, y;              // 粒子在屏幕上的坐标位置
    private double vx, vy;            // 粒子的速度分量：vx水平方向，vy垂直方向（负值=向上）
    private double size;              // 粒子的大小（4~12像素，随机生成）
    private Color color;              // 粒子的颜色（从6种庆祝色中随机选取）
    private double alpha;             // 透明度：1.0=完全不透明，0.0=完全透明（不可见）
    private double rotation;          // 当前旋转角度（0~360度）
    private double rotationSpeed;     // 旋转速度（正=顺时针，负=逆时针，单位：度/秒）
    private ParticleType type;        // 粒子的形状类型（星星/方块/圆/彩纸）

    /**
     * 粒子形状枚举
     * 定义了4种可能的粒子外观
     */
    public enum ParticleType {
        STAR,      // 五角星
        SQUARE,    // 正方形
        CIRCLE,    // 圆形
        CONFETTI   // 彩纸屑
    }

    // ==================== 静态常量（所有粒子共享） ====================

    // 随机数生成器，static表示所有粒子共用同一个，避免每个粒子都创建一个
    private static final Random random = new Random();

    // 庆祝颜色数组，粒子创建时从中随机选一种颜色
    private static final Color[] CELEBRATION_COLORS = {
        Color.web("#FFD93D"),  // 金黄色
        Color.web("#FF6B9D"),  // 粉红色
        Color.web("#00FF88"),  // 薄荷绿
        Color.web("#4A90E2"),  // 天蓝色
        Color.web("#FFA500"),  // 橙色
        Color.web("#FF1493")   // 深粉色
    };

    // ==================== 构造方法 ====================

    /**
     * 创建一个新粒子
     * @param x 起始X坐标（通常由外部指定，如屏幕底部随机位置）
     * @param y 起始Y坐标
     * 其他属性（速度、大小、颜色、形状等）全部随机生成
     */
    public VictoryParticle(double x, double y) {
        this.x = x;
        this.y = y;
        // 水平速度：(0~1 - 0.5) * 200 = -100~+100，正=向右，负=向左
        this.vx = (random.nextDouble() - 0.5) * 200;
        // 垂直速度：0~1 * -300 - 100 = -100~-400，一定是负值（向上飞）
        this.vy = random.nextDouble() * -300 - 100;
        // 大小：0~1 * 8 + 4 = 4~12像素
        this.size = random.nextDouble() * 8 + 4;
        // 颜色：从6种庆祝色中随机选一种
        this.color = CELEBRATION_COLORS[random.nextInt(CELEBRATION_COLORS.length)];
        // 透明度：初始为1.0（完全不透明），之后逐渐减小到0（消失）
        this.alpha = 1.0;
        // 初始旋转角度：0~360度随机
        this.rotation = random.nextDouble() * 360;
        // 旋转速度：(0~1 - 0.5) * 360 = -180~+180度/秒
        this.rotationSpeed = (random.nextDouble() - 0.5) * 360;
        // 形状：从4种类型中随机选一种
        this.type = ParticleType.values()[random.nextInt(ParticleType.values().length)];
    }

    // ==================== 物理更新 ====================

    /**
     * 每帧更新粒子的状态（位置、旋转、透明度）
     * @param deltaTime 上一帧到这一帧经过的时间（秒），用于让动画速度不受帧率影响
     */
    public void update(double deltaTime) {
        x += vx * deltaTime;              // 水平位置 = 原位置 + 水平速度 × 时间
        y += vy * deltaTime;              // 垂直位置 = 原位置 + 垂直速度 × 时间
        vy += 150 * deltaTime;            // 重力加速度：每帧给垂直速度增加150，模拟重力下拉效果
        rotation += rotationSpeed * deltaTime;  // 旋转角度 = 原角度 + 旋转速度 × 时间
        alpha -= 0.3 * deltaTime;         // 透明度递减，约3.3秒后完全消失（alpha=0）

        if (alpha < 0) alpha = 0;         // 透明度不能为负数
    }

    // ==================== 渲染绘制 ====================

    /**
     * 把粒子画到屏幕上
     * @param gc JavaFX的画笔对象（GraphicsContext）
     */
    public void render(javafx.scene.canvas.GraphicsContext gc) {
        gc.save();                        // 保存画布当前状态（位置、旋转等），画完后恢复
        gc.setGlobalAlpha(alpha);         // 设置画笔透明度，让粒子看起来在慢慢消失
        gc.setFill(color);               // 设置填充颜色
        gc.translate(x, y);              // 把画笔移动到粒子所在位置（后续绘制都以这个位置为基准）
        gc.rotate(rotation);             // 旋转画布，让粒子看起来在翻滚

        // 根据形状类型分别绘制不同的图形
        switch (type) {
            case STAR:
                drawStar(gc);            // 画五角星
                break;
            case SQUARE:
                // 画正方形：从中心向四周扩展（所以坐标是 -size/2）
                gc.fillRect(-size/2, -size/2, size, size);
                break;
            case CIRCLE:
                // 画圆形：fillOval参数是(左上角x, 左上角y, 宽, 高)
                gc.fillOval(-size/2, -size/2, size, size);
                break;
            case CONFETTI:
                // 画彩纸屑：扁长条形
                gc.fillRect(-size/2, -size/4, size, size/2);
                break;
        }

        gc.restore();                     // 恢复画布状态，避免影响其他粒子的绘制
    }

    /**
     * 画五角星
     * 原理：五角星有10个顶点（5个外尖角 + 5个内凹角），交替排列
     * 用三角函数 cos/sin 计算每个顶点的坐标，然后连线填充
     * @param gc 画笔对象
     */
    private void drawStar(javafx.scene.canvas.GraphicsContext gc) {
        double innerRadius = size / 2;    // 内半径（凹进去的部分到中心的距离）
        double outerRadius = size;        // 外半径（尖角到中心的距离）

        gc.beginPath();                   // 开始一条新的路径
        for (int i = 0; i < 5; i++) {    // 循环5次，画5个角
            // 计算外顶点（尖角）的坐标
            // i * 72 = 每个角间隔72度（360°/5），-18是初始偏移角度
            double angle = Math.toRadians(i * 72 - 18);  // 角度转弧度
            double x1 = Math.cos(angle) * outerRadius;    // x = cos(角度) × 半径
            double y1 = Math.sin(angle) * outerRadius;    // y = sin(角度) × 半径

            // 计算内顶点（凹角）的坐标，偏移18度
            angle = Math.toRadians(i * 72 + 18);
            double x2 = Math.cos(angle) * innerRadius;
            double y2 = Math.sin(angle) * innerRadius;

            if (i == 0) {
                gc.moveTo(x1, y1);       // 第一个点：移动画笔到此处（不画线）
            } else {
                gc.lineTo(x1, y1);       // 后续点：画直线到此处
            }
            gc.lineTo(x2, y2);           // 再画到内顶点
        }
        gc.closePath();                   // 闭合路径（自动连回起点）
        gc.fill();                        // 用设置好的颜色填充整个星形
    }

    // ==================== 状态判断 ====================

    /**
     * 判断粒子是否已经"死亡"（完全透明不可见）
     * @return true=已死亡，应该从列表中移除
     */
    public boolean isDead() {
        return alpha <= 0;               // 透明度降到0或以下 → 粒子不可见 → 死亡
    }
}
