package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import gg.group3.justgo.JustGo;
import gg.group3.justgo.entities.Entity;
import gg.group3.justgo.managers.WorldEventListener;
import gg.group3.justgo.managers.WorldManager;
import gg.group3.justgo.utils.InputUtils;
import gg.group3.justgo.utils.MathGen;


public class GameScreen implements Screen {
    private final JustGo game;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private final QuestionScreen questionScreen;
    private final HUD hud;

    private WorldManager worldManager;

    // NEW: Counter for the boss battle
    private int questionsQueue = 0;

    // Level Management
    private int currentLevelIndex = 1;
    private final int MAX_LEVELS = 3;

    public GameScreen(JustGo game) {
        this.game = game;

        TextureRegion heartRegion = new TextureRegion(game.atlas, 0, 144, 16, 16);
        questionScreen = new QuestionScreen(new QuestionScreen.Answered() {
            @Override
            public void onCorrect(Entity enemy) {
                enemy.damage(1);
                handleBattleFlow(enemy); // Use new helper
            }

            @Override
            public void onWrong(Entity enemy) {
                enemy.heal(1);
                // Boss deals more damage?
                int dmg = enemy.isBoss() ? 2 : 1;
                worldManager.getPlayer().damage(dmg);

                handleBattleFlow(enemy); // Use new helper
            }

            @Override
            public void onCancel() { }
        }, heartRegion);

//        worldManager = new WorldManager("levels/testlevel.tmx", game.atlas, new WorldEventListener() {
//            @Override
//            public void onQuestionTriggered(Entity target, MathGen problem) {
//                if (target.isBoss()) {
//                    questionsQueue = 3;
//                } else {
//                    questionsQueue = 1;
//                }
//
//                showQuestionUI(target, problem);
//            }
//
//            @Override
//            public void onGameOver() {
//                // Handle Game over logic
//            }
//        });
//
//        tiledMapRenderer = new OrthogonalTiledMapRenderer(worldManager.getLevel().getRawLevel());

        hud = new HUD(game.batch, heartRegion);
        loadLevel(currentLevelIndex);
    }

    private void showQuestionUI(Entity target, MathGen problem) {
        // The Manager says a collision happened -> The Screen shows the UI
        questionScreen.setQuestion(
            problem.getQuestion(),
            problem.getAnswer(),
            problem.getOptions(), // <--- Pass the generated options
            target,
            worldManager.getPlayer().getHealth()
        );
        questionScreen.show();
    }

    private void loadLevel(int levelIndex) {
        Gdx.app.log("GameScreen", "Loading Level " + levelIndex);
        if (worldManager != null) worldManager.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();

        String mapPath = "levels/level" + levelIndex + ".tmx";

        // FIX: Pass 'levelIndex' to the new constructor
        worldManager = new WorldManager(mapPath, game.atlas, createWorldListener(), levelIndex);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(worldManager.getLevel().getRawLevel());
        questionScreen.hide();
        questionsQueue = 0;
    }

    private void handleBattleFlow(Entity enemy) {
        questionsQueue--;

        // 1. CHECK IF BATTLE ENDED (Entity Died)
        if (enemy.getHealth() <= 0) {
            questionScreen.hide();
            questionsQueue = 0;

            // --- ENDGAME CHECK: DID WE KILL THE BOSS? ---
            if (enemy.isBoss()) {
                Gdx.app.log("Game", "BOSS DEFEATED! Proceeding to next level...");

                // Advance to next level
                currentLevelIndex++;

                if (currentLevelIndex > MAX_LEVELS) {
                    Gdx.app.log("Game", "YOU WIN THE GAME! Looping back to Level 1.");
                    currentLevelIndex = 1; // Or go to Main Menu
                }

                loadLevel(currentLevelIndex);
            }
            return;
        }

        // 2. CHECK IF PLAYER DIED
        if (worldManager.getPlayer().getHealth() <= 0) {
            questionScreen.hide();
            // Trigger Game Over Logic via Listener
            createWorldListener().onGameOver();
            return;
        }

        // 3. CONTINUE BATTLE (Queue not empty)
        if (questionsQueue > 0) {
            int difficulty = enemy.isBoss() ? 100 : 10;
            MathGen nextProblem = MathGen.generateBasicArithmetic(difficulty);
            showQuestionUI(enemy, nextProblem);
        } else {
            questionScreen.hide();
        }
    }

    private WorldEventListener createWorldListener() {
        return new WorldEventListener() {
            @Override
            public void onQuestionTriggered(Entity target, MathGen problem) {
                if (target.isBoss()) {
                    questionsQueue = 3;
                } else {
                    questionsQueue = 1;
                }
                showQuestionUI(target, problem);
            }

            @Override
            public void onGameOver() {
                // Handle Player Death (Restart Level?)
                Gdx.app.log("Game", "Player Died - Restarting Level");
                loadLevel(currentLevelIndex);
            }
        };
    }

    private void update(float dt) {
        if (questionScreen.isVisible())
            return;
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
        for (Entity spike : worldManager.getSpikes()) {
            spike.draw(game.batch);
        }
        worldManager.getBoss().draw(game.batch);
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
        update(delta);
        questionScreen.draw();
        questionScreen.act();
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
