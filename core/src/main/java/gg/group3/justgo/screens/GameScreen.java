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
import gg.group3.justgo.math.Vector2Int;
import gg.group3.justgo.utils.InputUtils;
import gg.group3.justgo.utils.MathGen;


public class GameScreen implements Screen {
    private final JustGo game;
    private final Entity player;
    private final Array<Entity> doors;
    private final Array<Entity> enemies;

    private final GameLevel level;
    private final OrthogonalTiledMapRenderer tiledMapRenderer;

    private final QuestionScreen questionScreen;

    public GameScreen(JustGo game) {
        this.game = game;
        level = new GameLevel("levels/testlevel.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(level.getRawLevel());
        questionScreen = new QuestionScreen(new QuestionScreen.Answered() {
            @Override
            public void onCorrect(Entity whoQuestionedThePlayer) {
                // TODO Maybe make sure that we remove it on where we actually put it, instead of... this
                whoQuestionedThePlayer.damage(1);
                Gdx.app.log("Question Screen", String.format("Correct Answer!!! QH: %d", whoQuestionedThePlayer.getHealth()));
            }

            @Override
            public void onWrong(Entity whoQuestionedThePlayer) {
                whoQuestionedThePlayer.heal(1);
                Gdx.app.log("Question Screen", "Wrong Answer!!!");
            }

            @Override
            public void onCancel() {
            }
        });

        player = new Entity(
            new TextureRegion(game.atlas, 0, 0, 16, 16),
            level.getPlayerPosition().x,
            level.getPlayerPosition().y
        );

        doors = new Array<>();
        for (Vector2Int doorPos : level.getDoorPositions()) {
            doors.add(
                new Entity(new TextureRegion(game.atlas, 16, 32, 16, 16), doorPos.x, doorPos.y)
                    .withCollisionCallback((parent, other) -> {
                        Gdx.app.log("Entity Screen", "View the Screen");
                        MathGen question = MathGen.generateBasicArithmetic(10);
                        questionScreen.setQuestion(question.getQuestion(), question.getAnswer(), parent);
                        questionScreen.show();
                    })
            );
        }

        enemies = new Array<>();
        for (GameLevel.EnemyData enemyData : level.getEnemies()) {
            TextureRegion region = new TextureRegion(game.atlas, 3 * 16, 16, 16, 16);
            enemies.add(new Entity(region, enemyData.position.x, enemyData.position.y)
                .withCollisionCallback(((parent, other) -> {
                    Gdx.app.log("Entity Screen", "View the Screen");
                    MathGen question = MathGen.generateBasicArithmetic(30);
                    questionScreen.setQuestion(question.getQuestion(), question.getAnswer(), parent);
                    questionScreen.show();
                }))
            );
        }
    }

    private void update(float dt) {
        int dirX = 0, dirY = 0;
        if (InputUtils.isKeysJustPressed(Input.Keys.A, Input.Keys.LEFT)) dirX -= 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.D, Input.Keys.RIGHT)) dirX += 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.W, Input.Keys.UP)) dirY += 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.S, Input.Keys.DOWN)) dirY -= 1;

        if (player.move(dirX, dirY, level, ArrayUtils.combineArrays(doors, enemies))) {
            // TODO End Turn
            System.out.println("Player Successfully moved");

            // Move the Enemies
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
            if (door.getHealth() <= 0) continue;
            door.draw(game.batch);
        }
        for (Entity enemy : enemies) {
            if (enemy.getHealth() <= 0) continue;
            enemy.draw(game.batch);
        }

        game.batch.end();
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
