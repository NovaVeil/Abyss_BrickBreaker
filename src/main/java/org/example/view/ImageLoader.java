package org.example.view;

import javafx.scene.image.Image;
import org.example.model.ThemeType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {

    // ===== 开始界面背景 =====
    public static final Image BG_MENU = loadImage("/images/bg_menu.png");

    // ===== 砖块材质 =====
    public static final Image BRICK_NORMAL_IMG = loadImage("/images/brick_normal.png");
    public static final Image BRICK_HARD_IMG = loadImage("/images/brick_hard.png");
    public static final Image BRICK_GIFT_IMG = loadImage("/images/brick_gift.png");
    public static final Image BRICK_TRIANGLE_IMG = loadImage("/images/brick_triangle.png");

    // ===== 主题背景池 =====
    private static final Map<ThemeType, Image> THEME_BACKGROUNDS = new HashMap<>();

    static {
        THEME_BACKGROUNDS.put(ThemeType.THEME_1, loadImage("/images/bg_theme1.png"));
        THEME_BACKGROUNDS.put(ThemeType.THEME_2, loadImage("/images/bg_theme2.png"));
        THEME_BACKGROUNDS.put(ThemeType.THEME_3, loadImage("/images/bg_theme3.png"));
        THEME_BACKGROUNDS.put(ThemeType.THEME_4, loadImage("/images/bg_theme4.png"));
        THEME_BACKGROUNDS.put(ThemeType.THEME_5, loadImage("/images/bg_theme5.png"));
    }

    /**
     * 根据关卡号获取主题
     */
    public static ThemeType getThemeByLevel(int level) {
        int index = Math.max(1, (level - 1) % 5 + 1);
        return ThemeType.valueOf("THEME_" + index);
    }

    /**
     * 根据主题获取背景图
     */
    public static Image getBackgroundByTheme(ThemeType theme) {
        return THEME_BACKGROUNDS.get(theme);
    }

    private static Image loadImage(String path) {
        try (InputStream is = ImageLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("找不到图片：" + path);
                return null;
            }
            return new Image(is);
        } catch (Exception e) {
            System.err.println("加载图片失败：" + path);
            return null;
        }
    }
}