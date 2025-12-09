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
import gg.group3.justgo.entities.utils.ArrayUtils;
import gg.group3.justgo.managers.WorldEventListener;
import gg.group3.justgo.managers.WorldManager;
import gg.group3.justgo.math.Vector2Int;
import gg.group3.justgo.utils.InputUtils;
import gg.group3.justgo.utils.MathGen;


public class GameScreen implements Screen {
    private final JustGo game;
    private final OrthogonalTiledMapRenderer tiledMapRenderer;
    private final QuestionScreen questionScreen;
    private final HUD hud;

    private final WorldManager worldManager;

    public GameScreen(JustGo game) {
        this.game = game;
        questionScreen = new QuestionScreen(new QuestionScreen.Answered() {
            @Override
            public void onCorrect(Entity whoQuestionedThePlayer) {
                // TODO Maybe make sure that we remove it on where we actually put it, instead of... this
                whoQuestionedThePlayer.damage(1);
            }

            @Override
            public void onWrong(Entity whoQuestionedThePlayer) {
                whoQuestionedThePlayer.heal(1);
                worldManager.getPlayer().damage(1);
                Gdx.app.log("Question Screen", "Wrong Answer!!!");
            }

            @Override
            public void onCancel() {
            }
        });

        worldManager = new WorldManager("levels/testlevel.tmx", game.atlas, new WorldEventListener() {
            @Override
            public void onQuestionTriggered(Entity target, MathGen problem) {
                // The Manager says a collision happened -> The Screen shows the UI
                questionScreen.setQuestion(problem.getQuestion(), problem.getAnswer(), target);
                questionScreen.show();
            }

            @Override
            public void onGameOver() {
                // Handle Game over logic
            }
        });

        tiledMapRenderer = new OrthogonalTiledMapRenderer(worldManager.getLevel().getRawLevel());

        TextureRegion heartRegion = new TextureRegion(game.atlas, 0, 144, 16, 16);
        hud = new HUD(game.batch, heartRegion);
    }

    private void update(float dt) {
        // 1. Input Handling
        int dirX = 0, dirY = 0;
        if (InputUtils.isKeysJustPressed(Input.Keys.A, Input.Keys.LEFT)) dirX -= 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.D, Input.Keys.RIGHT)) dirX += 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.W, Input.Keys.UP)) dirY += 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.S, Input.Keys.DOWN)) dirY -= 1;

        // 2. Delegate Logic to Manager
        if (dirX != 0 || dirY != 0) {
            worldManager.processTurn(dirX, dirY);
        }
        game.viewport.getCamera().position.x = worldManager.getPlayer().getX();
        game.viewport.getCamera().position.y = worldManager.getPlayer().getY();

        // 3. Update Animations
        worldManager.update(dt);
    }

    private void draw() {
        ScreenUtils.clear(0, 0, 0, 1f);
        game.viewport.apply();
        tiledMapRenderer.setView((OrthographicCamera) game.viewport.getCamera());
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        tiledMapRenderer.render();
        worldManager.getPlayer().draw(game.batch);
        for (Entity door : worldManager.getDoors()) {
            if (door.getHealth() <= 0) continue;
            door.draw(game.batch);
        }
        for (Entity enemy : worldManager.getEnemies()) {
            if (enemy.getHealth() <= 0) continue;
            enemy.draw(game.batch);
        }

        game.batch.end();

        worldManager.getVisibilityManager().render(16, 16, game.viewport.getCamera());

        hud.update(worldManager.getPlayer().getHealth());
        hud.draw();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        draw();
        if (!questionScreen.isVisible()) {
            update(delta);
        } else {
            questionScreen.draw();
            questionScreen.act();
        }
    }

    @Override
    public void resize(int width, int height) {
        questionScreen.getViewport().update(width, height, true);
        hud.resize(width, height);
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
        questionScreen.dispose();
        tiledMapRenderer.dispose();
        worldManager.dispose();
        hud.dispose();
    }
}
