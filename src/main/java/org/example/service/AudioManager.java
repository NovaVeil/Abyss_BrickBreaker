package org.example.service;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * 音效管理器
 * 负责加载和播放所有的游戏音效
 */
public class AudioManager {

    // 单例模式：确保整个游戏只有一个音效管理器实例
    private static AudioManager instance;

    // 各种音效的 AudioClip 对象
    private MediaPlayer bgmPlayer;
    private AudioClip hitClip;

    /**
     * 私有构造方法，防止外部直接 new
     * 在这里加载所有音效资源
     */
    private AudioManager() {
        loadSounds();
    }

    /**
     * 获取单例实例
     *
     * @return AudioManager 的唯一实例
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * 从 resources 文件夹加载音效文件
     */
    private void loadSounds() {
        try {
            URL bgmUrl = getClass().getResource("/audios/bgm.wav");
            URL hitUrl = getClass().getResource("/audios/hit.mp3");

            if (bgmUrl != null) {
                Media media = new Media(bgmUrl.toString());
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgmPlayer.setVolume(0.3);
            } else {
                throw new RuntimeException("错误：找不到背景音乐文件 bgm.mp3");
            }

            if (hitUrl != null) {
                hitClip = new AudioClip(hitUrl.toString());
                hitClip.setVolume(0.6);
            } else {
                throw new RuntimeException("错误：找不到撞击音效文件 hit.wav");
            }

        } catch (Exception e) {
            System.err.println("错误：加载音效文件失败" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 对外提供的播放方法 ====================

    /**
     * 播放背景音乐
     */
    public void playBGM() {
        if (bgmPlayer != null && bgmPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            bgmPlayer.play();
        }
    }

    /**
     * 停止背景音乐
     */
    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }
    }

    /**
     * 播放撞击音效（砖块或挡板）
     */
    public void playHitSound() {
        if (hitClip != null) {
            hitClip.play();
        }
    }


    public void dispose() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }
        instance = null;
    }

}