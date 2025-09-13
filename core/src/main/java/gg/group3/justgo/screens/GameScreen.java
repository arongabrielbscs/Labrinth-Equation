package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import gg.group3.justgo.GameLevel;
import gg.group3.justgo.JustGo;
import gg.group3.justgo.entities.Entity;
import gg.group3.justgo.math.Vector2Int;
import gg.group3.justgo.utils.InputUtils;


public class GameScreen implements Screen {
    private final JustGo game;
    private final Entity player;
    private final Array<Entity> doors;

    private final GameLevel level;
    private final OrthogonalTiledMapRenderer tiledMapRenderer;

    public GameScreen(JustGo game) {
        this.game = game;
        level = new GameLevel("levels/testlevel.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(level.getRawLevel());
        player = new Entity(
            new TextureRegion(game.atlas, 0, 0, 16, 16),
            level.getPlayerPosition().x,
            level.getPlayerPosition().y
        );

        Array<Vector2Int> doorPositions = level.getDoorPositions();
        doors = new Array<>(doorPositions.size);
        for (Vector2Int doorPos : doorPositions) {
            doors.add(
                new Entity(new TextureRegion(game.atlas, 16, 32, 16, 16), doorPos.x, doorPos.y)
                    .withCollisionCallback((parent, other) -> {
                        Gdx.app.log("Entity Screen", "View the Screen");
                    })
            );
        }
    }

    private void update(float dt) {
        int dirX = 0, dirY = 0;
        if (InputUtils.isKeysJustPressed(Input.Keys.A, Input.Keys.LEFT)) dirX -= 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.D, Input.Keys.RIGHT)) dirX += 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.W, Input.Keys.UP)) dirY += 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.S, Input.Keys.DOWN)) dirY -= 1;

        if (player.move(dirX, dirY, level, doors)) {
            // TODO End Turn
            System.out.println("Player Successfully moved");
        }
        game.viewport.getCamera().position.x = player.getX();
        game.viewport.getCamera().position.y = player.getY();
        player.update(dt);
    }

    private void draw() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        game.viewport.apply();
        tiledMapRenderer.setView((OrthographicCamera) game.viewport.getCamera());
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        tiledMapRenderer.render();
        player.draw(game.batch);
        for (Entity door : doors) {
            door.draw(game.batch);
        }

        game.batch.end();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        level.dispose();
    }
}
