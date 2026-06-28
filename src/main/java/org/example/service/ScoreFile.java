package org.example.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 分数存档管理类
 * 负责读取和保存历史最高分
 */
public class ScoreFile {

    // 存档文件名
    private static final String FILE_NAME = "./src/main/data/score.txt";

    /**
     * 读取历史最高分
     * @return 最高分，若文件不存在则返回 0
     */
    public static int loadHighScore() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                return 0;
            }

            String content = Files.readString(Paths.get(FILE_NAME)).trim();
            if (content.isEmpty()) {
                return 0;
            }

            return Integer.parseInt(content);

        } catch (Exception e) {
            System.err.println("读取最高分失败：" + e.getMessage());
            return 0;
        }
    }

    /**
     * 保存新的最高分（仅当新分数更高时）
     * @param newScore 当前分数
     * @return 是否刷新纪录
     */
    public static boolean saveHighScore(int newScore) {
        int highScore = loadHighScore();

        if (newScore > highScore) {
            try (FileWriter writer = new FileWriter(FILE_NAME)) {
                writer.write(String.valueOf(newScore));
            } catch (IOException e) {
                System.err.println("保存最高分失败：" + e.getMessage());
            }
            return true;
        }
        return false;
    }
}