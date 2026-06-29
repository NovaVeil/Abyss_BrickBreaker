package org.example.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.example.model.GameMode;

public class ScoreFile {

    private static final String FILE_CAMPAIGN = "./src/main/java/org/example/data/score_campaign.txt";
    private static final String FILE_ENDLESS   = "./src/main/java/org/example/data/score_endless.txt";

    // ✅ 核心：根据模式返回文件名
    private static String getFileName(GameMode mode) {
        if (mode == GameMode.ENDLESS) {
            return FILE_ENDLESS;
        }
        return FILE_CAMPAIGN;
    }

    // ✅ 保存最高分（已正确）
    public static boolean saveHighScore(int newScore, GameMode mode) {
        int highScore = loadHighScore(mode);
        if (newScore > highScore) {
            if (mode == GameMode.CAMPAIGN) {
                Map<Integer, Integer> scores = loadLevelScores();
                int maxLevel = loadMaxUnlockedLevel();
                try {
                    File file = new File(FILE_CAMPAIGN);
                    file.getParentFile().mkdirs();
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(newScore + "\n");
                        writer.write(maxLevel + "\n");
                        for (Map.Entry<Integer, Integer> e : scores.entrySet()) {
                            writer.write(e.getKey() + " " + e.getValue() + "\n");
                        }
                        return true;
                    }
                } catch (IOException e) {
                    System.err.println("保存最高分失败：" + e.getMessage());
                    return false;
                }
            } else {
                try (FileWriter writer = new FileWriter(getFileName(mode))) {
                    writer.write(String.valueOf(newScore));
                    return true;
                } catch (IOException e) {
                    System.err.println("保存最高分失败：" + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    // ✅ 读取最高分（已正确）
    public static int loadHighScore(GameMode mode) {
        try {
            File file = new File(getFileName(mode));
            if (!file.exists()) return 0;

            String[] lines = Files.readString(Paths.get(getFileName(mode))).trim().split("\n");
            if (lines.length == 0 || lines[0].trim().isEmpty()) return 0;
            return Integer.parseInt(lines[0].trim());
        } catch (Exception e) {
            System.err.println("读取最高分失败：" + e.getMessage());
            return 0;
        }
    }

    // ==================== 以下是闯关模式专用（CAMPAIGN ONLY）====================

    public static int loadMaxUnlockedLevel() {
        // 闯关模式固定用 CAMPAIGN 文件
        try {
            File file = new File(FILE_CAMPAIGN);
            if (!file.exists()) return 1;

            String[] lines = Files.readString(Paths.get(FILE_CAMPAIGN)).trim().split("\n");
            if (lines.length < 2 || lines[1].trim().isEmpty()) return 1;

            return Integer.parseInt(lines[1].trim());
        } catch (Exception e) {
            return 1;
        }
    }

    public static void saveMaxUnlockedLevel(int level) {
        int currentMax = loadMaxUnlockedLevel();
        if (level > currentMax) {
            Map<Integer, Integer> scores = loadLevelScores();
            saveAll(level, scores, 0);
        }
    }

    public static Map<Integer, Integer> loadLevelScores() {
        Map<Integer, Integer> scores = new HashMap<>();
        try {
            File file = new File(FILE_CAMPAIGN);
            if (!file.exists()) return scores;

            String[] lines = Files.readString(Paths.get(FILE_CAMPAIGN)).trim().split("\n");
            for (int i = 2; i < lines.length; i++) {
                String[] parts = lines[i].trim().split("\\s+");
                if (parts.length >= 2) {
                    scores.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                }
            }
        } catch (Exception e) {
            System.err.println("读取关卡分数失败：" + e.getMessage());
        }
        return scores;
    }

    public static void saveLevelScore(int level, int score) {
        Map<Integer, Integer> scores = loadLevelScores();
        int oldScore = scores.getOrDefault(level, 0);
        if (score > oldScore) {
            scores.put(level, score);
            saveAll(Math.max(loadMaxUnlockedLevel(), level), scores, score);
        }
    }

    // ✅ 私有方法：只用于闯关模式
    private static boolean saveAll(int maxUnlockedLevel, Map<Integer, Integer> levelScores, int currentScore) {
        File file = new File(FILE_CAMPAIGN);
        file.getParentFile().mkdirs();

        int fileHighScore = loadHighScore(GameMode.CAMPAIGN);
        int highScore = Math.max(fileHighScore, currentScore);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(highScore + "\n");
            writer.write(maxUnlockedLevel + "\n");
            for (Map.Entry<Integer, Integer> e : levelScores.entrySet()) {
                writer.write(e.getKey() + " " + e.getValue() + "\n");
            }
            return true;
        } catch (IOException e) {
            System.err.println("保存闯关存档失败：" + e.getMessage());
            return false;
        }
    }
}