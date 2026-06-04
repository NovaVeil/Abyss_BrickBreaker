# Abyss_BrickBreaker

大一下java课程设计

在\src\main\resources\中添加项目各类bgm音频时注意把格式改为WAV或MP3

目前有基本项目结构和基础类代码：

AbyssBrickGame (Model - 游戏核心逻辑)
职责：
1.管理所有游戏实体（挡板、小球、砖块）
2.处理碰撞检测调度（调用 CollisionDetector）
3.管理游戏状态（运行/结束、关卡、生命值）
4.提供 getter 方法给 View 访问
5.管理 ScoreManager 和 LevelManager
❌ 不包含：渲染逻辑、用户输入处理
依赖关系：
→ 依赖 Baffle、Ball、Brick（游戏实体）
→ 依赖 CollisionDetector（碰撞检测工具）
→ 依赖 ScoreManager（计分管理）
→ 依赖 LevelManager（关卡管理）
← 被 GameView 调用

GameView (View + Controller - 显示和交互)
职责：
1.负责画面渲染（绘制背景、砖块、挡板、小球、UI）
2.处理用户输入（键盘方向键、鼠标移动、点击）
3.调用 Model 的方法（game.update()、game.restart()）
4.从 Model 获取数据并显示（getBaffle()、getBallList()等）
5.管理游戏菜单和对话框（暂停、重新开始、退出）
❌ 不包含：游戏逻辑计算、碰撞检测
依赖关系：
→ 依赖 AbyssBrickGame（Model层）
→ 依赖 LevelManager（获取关卡样式）
→ 依赖 JavaFX UI 组件
← 被 Main 类启动

CollisionDetector (Utility - 工具类)
职责：
1.提供静态碰撞检测方法（球碰挡板、球碰砖块）
2.判断小球是否掉出底部
3.触发礼物砖块技能（整行整列扣血）
4.纯工具类，无状态，无私有字段
❌ 不包含：业务逻辑、游戏状态管理
依赖关系：
→ 依赖 Ball、Baffle、Brick（几何计算）
→ 依赖 GameConstant（边界常量）
→ 依赖 ScoreManager（礼物技能计分）
← 被 AbyssBrickGame 调用

Baffle (Entity - 游戏实体)
职责：
1.封装挡板属性（位置、尺寸、速度、颜色）
2.提供移动方法（左右移动、鼠标定位）
3.根据关卡调整宽度（难度递增）
4.根据关卡生成不同颜色
❌ 不包含：碰撞检测逻辑、渲染逻辑
依赖关系：
→ 依赖 GameConstant（默认常量）
→ 依赖 JavaFX Color（颜色）
← 被 AbyssBrickGame 管理
← 被 GameView 读取用于渲染

Ball (Entity - 游戏实体)
职责：
1.封装小球属性（位置、半径、速度向量、颜色）
2.实现小球移动逻辑
3.处理墙壁碰撞反弹（水平、竖直）
4.预留砖块击中计数功能（bricksHitcount）
❌ 不包含：碰撞检测（由 CollisionDetector 负责）
依赖关系：
→ 依赖 GameConstant（默认常量）
→ 依赖 JavaFX Color（颜色）
← 被 AbyssBrickGame 管理
← 被 CollisionDetector 修改运动状态
← 被 GameView 读取用于渲染

Brick (Abstract Entity - 抽象实体基类)
职责：
1.定义砖块通用属性（位置、尺寸、生命值）
2.提供通用方法（isHit、isAlive）
3.作为所有砖块类型的父类
❌ 不直接实例化（抽象类）
依赖关系：
→ 依赖 GameConstant（砖块尺寸）
← 被 NormalBrick、HardBrick、GiftBrick 继承
← 被 AbyssBrickGame 管理
← 被 CollisionDetector 检测碰撞

NormalBrick (Concrete Entity - 具体实体)
职责：
1.普通砖块实现（1点生命值）
2.继承 Brick 基类所有功能
❌ 无额外特殊功能
依赖关系：
→ 继承 Brick
← 被 LevelManager 创建

HardBrick (Concrete Entity - 具体实体)
职责：
1.硬砖块实现（2点生命值）
2.需要多次击打才能摧毁
❌ 无额外特殊功能
依赖关系：
→ 继承 Brick
← 被 LevelManager 创建

GiftBrick (Concrete Entity - 具体实体)
职责：
1.礼物砖块实现（3点生命值）
2.重写 isHit() 方法（只有存活时才能扣血）
3.提供礼物触发判断（isTiggerGift）
4.被击碎时触发整行整列扣血技能
依赖关系：
→ 继承 Brick
→ 被 CollisionDetector 检测礼物触发
← 被 LevelManager 创建

ScoreManager (Service - 服务类)
职责：
1.管理游戏分数（使用 JavaFX Property）
2.管理连击系统（combo、倍率）
3.根据砖块类型计算得分
4.支持关卡切换时的分数累加
❌ 不包含：UI 显示逻辑
依赖关系：
→ 依赖 JavaFX Property（响应式数据）
→ 依赖 Brick 类型（区分得分）
← 被 AbyssBrickGame 管理
← 被 CollisionDetector 调用加分
← 被 GameView 读取用于显示

LevelManager (Service - 服务类)
职责：
1.管理关卡主题样式（背景色、球色、砖块色）
2.生成关卡砖块布局（概率分布）
3.根据关卡调整难度（行数递增、砖块类型比例）
4.提供关卡主题名称
❌ 不包含：关卡切换逻辑（由 AbyssBrickGame 控制）
依赖关系：
→ 依赖 GameConstant（砖块尺寸）
→ 依赖 NormalBrick、HardBrick、GiftBrick
→ 依赖 JavaFX Color（主题颜色）
← 被 AbyssBrickGame 调用
← 被 GameView 读取样式信息

GameConstant (Configuration - 配置类)
职责：
1.定义游戏窗口尺寸常量
2.定义挡板默认属性（宽、高、速度）
3.定义小球默认属性（半径、速度）
4.定义砖块默认属性（宽、高）
❌ 无方法，纯常量定义
依赖关系：
→ 依赖 JavaFX Color（未使用的导入）
← 被所有游戏实体类引用

Main (Application Entry - 应用入口)
职责：
1.JavaFX 应用程序入口
2.创建并启动 GameView
3.管理 Stage 生命周期
❌ 不包含：任何游戏逻辑
依赖关系：
→ 依赖 GameView
→ 依赖 JavaFX Application
