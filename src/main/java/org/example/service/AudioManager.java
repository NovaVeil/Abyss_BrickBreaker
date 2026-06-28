package org.example.service;

import javafx.scene.media.AudioClip;

import java.net.URL;

/**
 * 音效管理器
 * 负责加载和播放所有的游戏音效
 */
public class AudioManager {

    // 单例模式：确保整个游戏只有一个音效管理器实例
    private static AudioManager instance;

    // 各种音效的 AudioClip 对象
    private AudioClip bgmClip;
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
        // 使用 getClass().getResource() 来获取 resources 下的文件路径
        try {
            URL bgmUrl = getClass().getResource("/audios/bgm.wav");
            URL hitUrl = getClass().getResource("/audios/hit.mp3");

            if (bgmUrl != null) {
                bgmClip = new AudioClip(bgmUrl.toString());
                bgmClip.setCycleCount(AudioClip.INDEFINITE); // 设置为无限循环
                bgmClip.setVolume(0.3); // 背景音乐音量调低一点
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
        if (bgmClip != null && !bgmClip.isPlaying()) {
            bgmClip.play();
        }
    }

    /**
     * 停止背景音乐
     */
    public void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
        }
    }

    /**
     * 播放撞击音效（砖块或挡板）
     */
    public void playHitSound() {
        if (hitClip != null) {
            // 每次播放都从头开始，适合短促音效
            hitClip.play();
        }
    }


    public void dispose() {
        if (bgmClip != null) {
            bgmClip.stop();
        }
        // 其他音频资源清理...
        instance = null;
    }

}