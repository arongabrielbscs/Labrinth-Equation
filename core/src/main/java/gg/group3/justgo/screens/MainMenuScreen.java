package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import gg.group3.justgo.JustGo;

public class MainMenuScreen implements Screen {
    private final JustGo game;
    private final Stage stage;
    private final Skin skin;
    private final Texture titleTexture;

    // We don't need to store backgroundTexture here anymore,
    // we pass it to the Actor.

    public MainMenuScreen(JustGo game) {
        this.game = game;
        stage = new Stage(new FitViewport(320, 180));

        // 1. Load Assets
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/question-screen.atlas"));
        skin = new Skin(Gdx.files.internal("ui/menu.skin"), atlas);
        titleTexture = new Texture(Gdx.files.internal("images/title.png"));

        TextureRegion backgroundTexture = new TextureRegion(game.atlas, 32, 48, 16, 16);

        fixButtonStyles(atlas);

        // 2. SETUP BACKGROUND ACTOR (Fixes the crash)
        // Add this BEFORE setupUI() so it sits behind the buttons
        stage.addActor(new BackgroundActor(backgroundTexture));

        setupUI();
    }

    private void fixButtonStyles(TextureAtlas atlas) {
        NinePatch patchUp = new NinePatch(atlas.findRegion("button-green"), 4, 4, 4, 4);
        NinePatch patchDown = new NinePatch(atlas.findRegion("button-green-pressed"), 4, 4, 4, 4);
        NinePatch patchQuit = new NinePatch(atlas.findRegion("button-red"), 4, 4, 4, 4);

        skin.get("default", TextButton.TextButtonStyle.class).up = new NinePatchDrawable(patchUp);
        skin.get("default", TextButton.TextButtonStyle.class).down = new NinePatchDrawable(patchDown);
        skin.get("quit", TextButton.TextButtonStyle.class).up = new NinePatchDrawable(patchQuit);

        skin.getFont("ui-font").getData().setScale(0.5f);
    }

    private void setupUI() {
        Table table = new Table();
        table.setFillParent(true);

        Image titleImage = new Image(titleTexture);
        table.add(titleImage).width(200).height(80).padTop(10).padBottom(15).row();

        TextButton playBtn = new TextButton("PLAY", skin);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Play Click Sound
                gg.group3.justgo.managers.SoundManager.getInstance().playSound("click");
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        table.add(playBtn).width(90).height(20).padBottom(4).row();

        TextButton creditsBtn = new TextButton("CREDITS", skin);
        creditsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gg.group3.justgo.managers.SoundManager.getInstance().playSound("click");
                game.setScreen(new CreditsScreen(game));
                dispose();
            }
        });
        table.add(creditsBtn).width(90).height(20).padBottom(4).row();

        TextButton quitBtn = new TextButton("QUIT", skin, "quit");
        quitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        table.add(quitBtn).width(90).height(20).padBottom(10).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // REMOVED: Manual game.batch calls.
        // The Stage now handles everything (Background + UI) safely.
        stage.act(delta);
        stage.draw();
    }

    // --- INNER CLASS FOR BACKGROUND ---
    private class BackgroundActor extends Actor {
        private final TextureRegion region;

        public BackgroundActor(TextureRegion region) {
            this.region = region;
            // Make this actor fill the entire stage
            setSize(320, 180);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            // 1. Dim the lights (Dark Gray)
            batch.setColor(0.3f, 0.3f, 0.3f, 1f * parentAlpha);

            // 2. Draw Tiling Background
            for (int x = 0; x < 320; x += 16) {
                for (int y = 0; y < 180; y += 16) {
                    batch.draw(region, x, y);
                }
            }

            // 3. Reset Color for the next actors (Buttons)
            batch.setColor(1, 1, 1, 1);
        }
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
        titleTexture.dispose();
    }
}
