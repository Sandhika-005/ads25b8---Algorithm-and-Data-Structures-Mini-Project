import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioPlayer {
    private Clip backgroundClip;
    private long backgroundPosition = 0; // Menyimpan posisi frame saat dijeda
    private Map<String, Clip> effectClips = new HashMap<>();
    private boolean isMuted = false;

    // --- MAPPING AUDIO BARU DARI PENGGUNA ---
    private static final Map<String, String> AUDIO_FILES = Map.of(
            "background", "audio/Elevator Music Vanoss Gaming Background Music HD [GAlR8iu0y1c].wav",
            "rollDice", "audio/Dice - SOUND EFFECT - [o-1U19vao78].wav",
            "win", "audio/Winner Announcement Sound Effect.wav",
            "gameOver", "audio/tamatlah-sudah.wav",
            "prime", "audio/cihuyy-wielino-ino_kO92s4H.wav",
            "star", "audio/ih-takotnyee.wav",
            "connection", "audio/oh-my-god-bro-oh-hell-nah-man.wav",
            "score", "audio/Score - Sound Effect for editing [2cUbCkL75AM].wav"
    );
    // ----------------------------------------

    public AudioPlayer() {
        loadClip("background", true);

        for (Map.Entry<String, String> entry : AUDIO_FILES.entrySet()) {
            if (!entry.getKey().equals("background")) {
                loadClip(entry.getKey(), false);
            }
        }
    }

    private void loadClip(String key, boolean isBackground) {
        String filePath = AUDIO_FILES.get(key);
        if (filePath == null) return;

        try {
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("Audio file not found for " + key + " (Expected path: " + filePath + ")");
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            if (isBackground) {
                this.backgroundClip = clip;
            } else {
                this.effectClips.put(key, clip);
            }

        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            System.err.println("Error loading audio for " + key + ": " + e.getMessage());
        }
    }

    // --- Background Music Controls ---
    public void playBackgroundMusic() {
        if (backgroundClip != null && !isMuted) { // HANYA CHECK isMuted di sini
            // Resume dari posisi terakhir
            backgroundClip.setMicrosecondPosition(backgroundPosition);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // Method untuk jeda dan menyimpan posisi
    public void pauseBackgroundMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundPosition = backgroundClip.getMicrosecondPosition();
            backgroundClip.stop();
        }
    }

    // Dipanggil saat game berakhir
    public void stopBackgroundMusic() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundPosition = 0; // Reset total
        }
    }

    /**
     * Mengganti status Mute/Unmute BGM.
     */
    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            pauseBackgroundMusic();
        } else {
            playBackgroundMusic();
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    // --- Sound Effect Control (Tidak terpengaruh oleh isMuted) ---
    /**
     * Memainkan SFX dan menjeda BGM (jika BGM sedang berjalan). BGM akan dilanjutkan otomatis saat SFX selesai.
     */
    public void playEffect(String key) {
        Clip clip = effectClips.get(key);
        if (clip != null) {

            // JEDA BACKGROUND MUSIC (terlepas dari status isMuted)
            pauseBackgroundMusic();

            clip.stop();
            clip.setFramePosition(0);
            clip.start();

            // Tambahkan listener untuk melanjutkan BGM
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.removeLineListener(this);
                        // LANJUTKAN BACKGROUND MUSIC (akan check status isMuted di playBackgroundMusic)
                        playBackgroundMusic();
                    }
                }
            });
        }
    }

    /**
     * Memainkan SFX tanpa menjeda BGM.
     */
    public void playEffectImmediately(String key) {
        Clip clip = effectClips.get(key);
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void close() {
        if (backgroundClip != null) {
            backgroundClip.close();
        }
        for (Clip clip : effectClips.values()) {
            clip.close();
        }
    }
}