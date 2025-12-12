package gg.group3.justgo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

import gg.group3.justgo.managers.SoundManager;
import gg.group3.justgo.screens.SplashScreen;

public class JustGo extends Game {
    public FitViewport viewport;
    public SpriteBatch batch;
    public Texture atlas;

    @Override
    public void create() {
        viewport = new FitViewport(320, 180);
        batch = new SpriteBatch();
        atlas = new Texture("images/atlas.png");

        // LOAD SOUNDS (Matching your screenshot exactly)
        SoundManager audio = SoundManager.getInstance();

        // Key Name      // Actual File Name in assets/audio/
        audio.loadSound("step",    "audio/player-move.wav");
        audio.loadSound("hit",     "audio/hit.mp3");
        audio.loadSound("kill",    "audio/boss_hit.mp3"); // Use for boss or death
        audio.loadSound("correct", "audio/on_correct.mp3");
        audio.loadSound("wrong",   "audio/on_wrong.wav");
        audio.loadSound("pickup",  "audio/pickup.mp3");
        audio.loadSound("click",   "audio/select.mp3");   // You have this, let's use it!
        audio.loadSound("splat",   "audio/splat.ogg");

        // Load Music
        audio.playMusic("audio/bg.mp3");

//        setScreen(new GameScreen(this));
        setScreen(new SplashScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();

        super.dispose();
    }
}
