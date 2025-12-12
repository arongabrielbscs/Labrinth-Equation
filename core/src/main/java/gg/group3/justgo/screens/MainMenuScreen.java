package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import gg.group3.justgo.JustGo;
import gg.group3.justgo.managers.SoundManager;

public class MainMenuScreen implements Screen {
    private final JustGo game;
    private final Stage stage;
    private final Skin skin;
    private final Texture titleTexture;
    private final TextureRegion backgroundTexture; // NEW: For the floor background

    public MainMenuScreen(JustGo game) {
        this.game = game;
        stage = new Stage(new FitViewport(320, 180)); // Keep retro res

        // 1. Load Assets
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/question-screen.atlas"));
        skin = new Skin(Gdx.files.internal("ui/menu.skin"), atlas);
        titleTexture = new Texture(Gdx.files.internal("images/title.png"));

        // Grab a floor tile from your main atlas for the background
        // (Assuming floor is at 16,0 based on your Tiled setup, change if needed)
        backgroundTexture = new TextureRegion(game.atlas, 32, 48, 16, 16);

        // 2. FIX THE BUTTONS (The "NinePatch" Magic)
        // This stops the buttons from looking stretched and blurry
        fixButtonStyles(atlas);

        setupUI();
    }

    private void fixButtonStyles(TextureAtlas atlas) {
        // Create a "NinePatch" which allows the button to stretch cleanly
        // Arguments: Region, left, right, top, bottom (pixels to NOT stretch)
        NinePatch patchUp = new NinePatch(atlas.findRegion("button-green"), 6, 6, 6, 6);
        NinePatch patchDown = new NinePatch(atlas.findRegion("button-green-pressed"), 6, 6, 6, 6);
        NinePatch patchQuit = new NinePatch(atlas.findRegion("button-red"), 6, 6, 6, 6);

        // Apply these clean patches to the skin
        skin.get("default", TextButton.TextButtonStyle.class).up = new NinePatchDrawable(patchUp);
        skin.get("default", TextButton.TextButtonStyle.class).down = new NinePatchDrawable(patchDown);
        skin.get("quit", TextButton.TextButtonStyle.class).up = new NinePatchDrawable(patchQuit);

        // Shrink the font slightly to match the 320x180 resolution
        skin.getFont("ui-font").getData().setScale(0.5f);
    }

    private void setupUI() {
        Table table = new Table();
        table.setFillParent(true);

        // --- TITLE ---
        Image titleImage = new Image(titleTexture);
        // Let the title breathe a bit
        table.add(titleImage).width(200).height(80).padTop(10).padBottom(15).row();

        // --- PLAY ---
        TextButton playBtn = new TextButton("PLAY", skin);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundManager.getInstance().playSound("click");
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        // Make buttons smaller and closer together
        table.add(playBtn).width(90).height(20).padBottom(4).row();

        // --- CREDITS ---
        TextButton creditsBtn = new TextButton("CREDITS", skin);
        creditsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // PLAY CLICK SOUND
                gg.group3.justgo.managers.SoundManager.getInstance().playSound("click");

                // SWITCH TO CREDITS
                game.setScreen(new CreditsScreen(game));
                dispose();
            }
        });
        table.add(creditsBtn).width(90).height(20).padBottom(4).row();

        // --- QUIT ---
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
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.setProjectionMatrix(stage.getCamera().combined);

        // 1. SET COLOR TO DARK GRAY (Dim the lights!)
        game.batch.setColor(0.3f, 0.3f, 0.3f, 1f);

        // 2. Draw the Tiling Background
        for (int x = 0; x < 320; x += 16) {
            for (int y = 0; y < 180; y += 16) {
                game.batch.draw(backgroundTexture, x, y);
            }
        }

        // 3. RESET COLOR TO WHITE (So buttons don't look dark)
        game.batch.setColor(1, 1, 1, 1);

        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    // ... Standard show/resize/dispose methods ...
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
