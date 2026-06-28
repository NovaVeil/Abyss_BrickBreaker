package org.example.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ScoreFile {

    private static final String FILE_NAME = "./src/main/java/org/example/data/score.txt";

    public static int loadHighScore() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                return 0;
            }

            String[] lines = Files.readString(Paths.get(FILE_NAME)).trim().split("\n");
            if (lines.length == 0 || lines[0].trim().isEmpty()) {
                return 0;
            }

            return Integer.parseInt(lines[0].trim());

        } catch (Exception e) {
            System.err.println("读取最高分失败：" + e.getMessage());
            return 0;
        }
    }

    public static boolean saveHighScore(int newScore) {
        int highScore = loadHighScore();

        if (newScore > highScore) {
            int maxLevel = loadMaxUnlockedLevel();
            Map<Integer, Integer> levelScores = loadLevelScores();
            saveAll(newScore, maxLevel, levelScores);
            return true;
        }
        return false;
    }

    public static int loadMaxUnlockedLevel() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                return 1;
            }

            String[] lines = Files.readString(Paths.get(FILE_NAME)).trim().split("\n");
            if (lines.length < 2 || lines[1].trim().isEmpty()) {
                return 1;
            }

            return Integer.parseInt(lines[1].trim());

        } catch (Exception e) {
            System.err.println("读取最高解锁关卡失败：" + e.getMessage());
            return 1;
        }
    }

    public static void saveMaxUnlockedLevel(int level) {
        int highScore = loadHighScore();
        int currentMax = loadMaxUnlockedLevel();
        if (level > currentMax) {
            Map<Integer, Integer> levelScores = loadLevelScores();
            saveAll(highScore, level, levelScores);
        }
    }

    public static Map<Integer, Integer> loadLevelScores() {
        Map<Integer, Integer> levelScores = new HashMap<>();
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) {
                return levelScores;
            }

            String[] lines = Files.readString(Paths.get(FILE_NAME)).trim().split("\n");
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    levelScores.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                }
            }

        } catch (Exception e) {
            System.err.println("读取关卡分数失败：" + e.getMessage());
        }
        return levelScores;
    }

    public static void saveLevelScore(int level, int score, int maxUnlockedLevel) {
        Map<Integer, Integer> levelScores = loadLevelScores();
        levelScores.put(level, score);
        int highScore = loadHighScore();
        saveAll(highScore, maxUnlockedLevel, levelScores);
    }

    public static boolean saveAll(int highScore, int maxUnlockedLevel, Map<Integer, Integer> levelScores) {
        File file = new File(FILE_NAME);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(highScore + "\n");
            writer.write(maxUnlockedLevel + "\n");
            for (Map.Entry<Integer, Integer> entry : levelScores.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
            writer.flush();
            return true;
        } catch (IOException e) {
            System.err.println("保存存档失败：" + e.getMessage());
            return false;
        }
    }
}