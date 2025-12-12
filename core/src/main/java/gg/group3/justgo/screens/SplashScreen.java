package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import gg.group3.justgo.JustGo;

public class SplashScreen implements Screen {
    private final JustGo game;
    private final Stage stage;
    private final Texture splashTexture;
    private final Image splashImage;

    public SplashScreen(JustGo game) {
        this.game = game;

        // Use FitViewport to ensure the logo isn't stretched oddly
        stage = new Stage(new FitViewport(320, 180), game.batch);

        // 1. Load the JPG
        splashTexture = new Texture(Gdx.files.internal("images/splash.jpg"));

        // 2. Setup the Image Actor
        splashImage = new Image(splashTexture);

        // Size it nicely (adjust if your logo needs specific dimensions)
        // Here we make it fit the height of the screen (180px) and scale width to match ratio
        // Or just center it if you resized the file externally.
        splashImage.setSize(320, 180); // Square logo
        splashImage.setPosition(
            (stage.getWidth() - splashImage.getWidth()) / 2,
            (stage.getHeight() - splashImage.getHeight()) / 2
        );

        // Start completely transparent so we can fade in
        splashImage.getColor().a = 0f;

        stage.addActor(splashImage);
    }

    @Override
    public void show() {
        // --- THE ANIMATION SEQUENCE ---
        splashImage.addAction(Actions.sequence(
            Actions.fadeIn(1.5f),        // Fade in over 1.5 seconds
            Actions.delay(2f),           // Wait for 2 seconds
            Actions.fadeOut(1.5f),       // Fade out over 1.5 seconds
            Actions.run(new Runnable() { // When done, switch screens
                @Override
                public void run() {
                    game.setScreen(new MainMenuScreen(game));
                    dispose(); // Cleanup this screen
                }
            })
        ));
    }

    @Override
    public void render(float delta) {
        // Black Background
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        splashTexture.dispose();
    }
}
