package gg.group3.justgo.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HUD implements Disposable {
    public final Stage stage;

    private final Table rootTable;
    private final Table heartTable;
    private final Label damageLabel;

    private final TextureRegion heartTexture;

    private int lastKnownHealth = -1;
    private int lastKnownDamage = -1;

    public HUD(SpriteBatch batch, TextureRegion heartTexture, TextureRegion daggerTexture) {
        this.heartTexture = heartTexture;

        stage = new Stage(new ScreenViewport(), batch);

        rootTable = new Table();
        rootTable.top().left();
        rootTable.setFillParent(true);
        rootTable.pad(10);

        // 1. HEART ROW
        heartTable = new Table();
        rootTable.add(heartTable).left().row();

        // 2. DAMAGE ROW (Icon + Number)
        Table damageTable = new Table();
        Image swordIcon = new Image(daggerTexture);

        // Simple default font style
        Label.LabelStyle style = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        damageLabel = new Label("1", style);

        damageTable.add(swordIcon).size(24, 24).padRight(5);
        damageTable.add(damageLabel);

        rootTable.add(damageTable).left().padTop(5);

        stage.addActor(rootTable);
    }

    public void update(int playerHealth, int playerDamage) {
        // Update Hearts
        if (playerHealth != lastKnownHealth) {
            lastKnownHealth = playerHealth;
            heartTable.clearChildren();
            for (int i = 0; i < playerHealth; i++) {
                heartTable.add(new Image(heartTexture)).width(32).height(32).padRight(4);
            }
        }

        // Update Damage Text
        if (playerDamage != lastKnownDamage) {
            lastKnownDamage = playerDamage;
            damageLabel.setText("DMG: " + playerDamage);
        }
    }

    public void draw() {
        stage.draw();
        stage.act();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
