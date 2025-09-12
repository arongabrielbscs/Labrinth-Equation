package gg.group3.justgo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import gg.group3.justgo.screens.GameScreen;

public class JustGo extends Game {
    public FitViewport viewport;
    public SpriteBatch batch;
    public Texture atlas;

    @Override
    public void create() {
        viewport = new FitViewport(320, 180);
        batch = new SpriteBatch();
        atlas = new Texture("images/atlas.png");

        setScreen(new GameScreen(this));
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
