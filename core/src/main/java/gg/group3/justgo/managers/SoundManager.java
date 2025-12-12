package gg.group3.justgo.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;

public class SoundManager implements Disposable {
    private static SoundManager instance;

    // Store sounds in a map so we can reference them by simple names
    private final HashMap<String, Sound> soundEffects;
    private Music currentMusic;

    private SoundManager() {
        soundEffects = new HashMap<>();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Loads a sound effect into memory.
     * @param name The nickname for the sound (e.g., "jump")
     * @param path The file path (e.g., "audio/jump.wav")
     */
    public void loadSound(String name, String path) {
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.log("Audio", "Warning: Sound file not found: " + path);
            return;
        }
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
        soundEffects.put(name, sound);
    }

    /**
     * Loads and plays background music (looping).
     */
    public void playMusic(String path) {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
        }

        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.log("Audio", "Warning: Music file not found: " + path);
            return;
        }

        currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        currentMusic.setLooping(true);
        currentMusic.setVolume(0.5f); // 50% volume
        currentMusic.play();
    }

    public void playSound(String name) {
        playSound(name, 1.0f);
    }

    public void playSound(String name, float pitch) {
        Sound sound = soundEffects.get(name);
        if (sound != null) {
            // Play with (volume, pitch, pan)
            // randomizing pitch slightly (0.9 to 1.1) makes it sound less repetitive!
            float dynamicPitch = pitch == 1.0f ? 0.95f + (float)(Math.random() * 0.1f) : pitch;
            sound.play(1.0f, dynamicPitch, 0f);
        } else {
            // Optional: Log warning if you want to know what's missing
            // Gdx.app.log("Audio", "Sound not found: " + name);
        }
    }

    @Override
    public void dispose() {
        for (Sound s : soundEffects.values()) {
            s.dispose();
        }
        soundEffects.clear();
        if (currentMusic != null) {
            currentMusic.dispose();
        }
    }
}
