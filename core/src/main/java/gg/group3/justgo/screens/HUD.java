package gg.group3.justgo.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HUD implements Disposable {
    public final Stage stage;

    private final Table heartTable;
    private final TextureRegion heartTexture;

    private int lastKnownHealth = -1;

    public HUD(SpriteBatch batch, TextureRegion heartTexture) {
        this.heartTexture = heartTexture;

        // Use ScreenViewport for UI so it stays the same size regardless of window zoom
        stage = new Stage(new ScreenViewport(), batch);

        // Create the layout table
        heartTable = new Table();
        heartTable.top().left(); // Anchor to Top-Left
        heartTable.setFillParent(true); // Make table size of the screen
        heartTable.pad(10); // Add some padding from the screen edge

        stage.addActor(heartTable);
    }

    public void update(int playerHealth) {
        // Optimization: Don't redraw if health hasn't changed
        if (playerHealth == lastKnownHealth) return;

        lastKnownHealth = playerHealth;
        heartTable.clearChildren(); // Clear old hearts

        // Add a heart image for every HP point
        for (int i = 0; i < playerHealth; i++) {
            Image heartImage = new Image(heartTexture);

            // Optional: Scale up the hearts if your pixel art is tiny (16x16)
            // Scene2D images scale visually, so we set the size in the cell
            heartTable.add(heartImage)
                .width(32).height(32) // Render at 32x32 size
                .padRight(4);         // Gap between hearts
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

    }
}
