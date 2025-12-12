package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import gg.group3.justgo.JustGo;

public class CreditsScreen implements Screen {
    private final JustGo game;
    private final Stage stage;
    private final Skin skin;

    public CreditsScreen(JustGo game) {
        this.game = game;

        // 1. UPDATE VIEWPORT: 640x360
        stage = new Stage(new FitViewport(640, 360));

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/question-screen.atlas"));
        skin = new Skin(Gdx.files.internal("ui/menu.skin"), atlas);

        skin.getFont("ui-font").getData().setScale(0.5f);
        skin.getFont("title-font").getData().setScale(0.5f);

        fixScrollPaneStyle(atlas);
        setupUI();
    }

    private void fixScrollPaneStyle(TextureAtlas atlas) {
        ScrollPane.ScrollPaneStyle style = skin.get(ScrollPane.ScrollPaneStyle.class);

        // We still need the manual fix, but we can make the bar thicker for 360p
        NinePatch knobPatch = new NinePatch(atlas.findRegion("button-green"), 2, 2, 2, 2);
        NinePatch scrollPatch = new NinePatch(atlas.findRegion("textfield-background"), 2, 2, 2, 2);

        style.vScrollKnob = new NinePatchDrawable(knobPatch);
        style.vScrollKnob.setMinWidth(20); // Doubled from 10 to 20

        style.vScroll = new NinePatchDrawable(scrollPatch);
        style.vScroll.setMinWidth(20); // Doubled from 10 to 20
    }

    private void setupUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        // Title
        Label titleLabel = new Label("CREDITS", skin, "title");
        titleLabel.setAlignment(Align.center);
        rootTable.add(titleLabel).padTop(20).padBottom(20).row(); // Increased padding

        // Content
        Table contentTable = new Table();
        contentTable.top();

        addSection(contentTable, "ART", "Carl Cipriano");
        addSection(contentTable, "SOUND & MUSIC", "Pixabay.com");
        addSection(contentTable, "PROGRAMMERS", "Aron Ogayon\nAllain Kumar\nJanvher Sarmiento");
        addSection(contentTable, "LEVEL DESIGNERS", "Aron Ogayon\nAllain Kumar\nJanvher Sarmiento");

        // ScrollPane
        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);

        // 3. DOUBLE THE SIZE
        // Was 300x100 -> Now 600x200
        rootTable.add(scrollPane).width(600).height(200).padBottom(20).row();

        // Back Button
        TextButton backBtn = new TextButton("BACK", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gg.group3.justgo.managers.SoundManager.getInstance().playSound("click");
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        // 4. DOUBLE THE BUTTON SIZE
        // Was 80x20 -> Now 160x40
        rootTable.add(backBtn).width(160).height(40).padBottom(20);

        stage.addActor(rootTable);
    }

    private void addSection(Table table, String header, String names) {
        Label headerLabel = new Label(header, skin, "default");
        headerLabel.setColor(0.2f, 1f, 0.2f, 1f);
        headerLabel.setAlignment(Align.center);

        Label namesLabel = new Label(names, skin, "default");
        namesLabel.setAlignment(Align.center);

        // Doubled padding here too
        table.add(headerLabel).padTop(20).row();
        table.add(namesLabel).padTop(5).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
