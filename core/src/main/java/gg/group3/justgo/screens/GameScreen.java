package gg.group3.justgo.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import gg.group3.justgo.JustGo;
import gg.group3.justgo.entities.Entity;
import gg.group3.justgo.entities.SpikeEntity;
import gg.group3.justgo.managers.SoundManager;
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

    // --- TRANSITION SYSTEM ---
    private final Stage transitionStage;
    private final Image blackOverlay;
    private boolean isTransitioning = false;
    private final Texture blackTexture;

    // Boss Battle Logic
    private int questionsQueue = 0;

    // Level Management
    private int currentLevelIndex = 1;
    private final int MAX_LEVELS = 3;

    public GameScreen(JustGo game) {
        this.game = game;

        TextureRegion heartRegion = new TextureRegion(game.atlas, 0, 144, 16, 16);
        TextureRegion daggerRegion = new TextureRegion(game.atlas, 0, 160, 16, 16);

        // --- SETUP TRANSITION OVERLAY ---
        // Create 1x1 black texture
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackTexture = new Texture(pixmap);
        pixmap.dispose();

        transitionStage = new Stage(new FitViewport(320, 180));
        blackOverlay = new Image(blackTexture);
        blackOverlay.setFillParent(true);
        blackOverlay.getColor().a = 0f; // Start invisible
        transitionStage.addActor(blackOverlay);

        // --- SETUP UI ---
        questionScreen = new QuestionScreen(createAnswerListener(), heartRegion);
        hud = new HUD(game.batch, heartRegion, daggerRegion);

        loadLevel(currentLevelIndex);
    }

    // --- LOGIC METHODS ---

    private void loadLevel(int levelIndex) {
        Gdx.app.log("GameScreen", "Loading Level " + levelIndex);
        if (worldManager != null) worldManager.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();

        String mapPath = "levels/level" + levelIndex + ".tmx";
        worldManager = new WorldManager(mapPath, game.atlas, createWorldListener(), levelIndex);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(worldManager.getLevel().getRawLevel());

        questionScreen.hide();
        questionsQueue = 0;

        // Ensure player is unlocked after reload
        isTransitioning = false;
    }

    private void triggerRetry() {
        if (isTransitioning) return;
        isTransitioning = true;

        // Fade In (3s) -> Reload -> Fade Out (1s)
        blackOverlay.addAction(Actions.sequence(
            Actions.fadeIn(3.0f),
            Actions.run(() -> loadLevel(currentLevelIndex)),
            Actions.fadeOut(1.0f),
            Actions.run(() -> isTransitioning = false)
        ));
    }

    private QuestionScreen.Answered createAnswerListener() {
        return new QuestionScreen.Answered() {
            @Override
            public void onCorrect(Entity enemy) {
                SoundManager.getInstance().playSound("correct");

                int damageDealt = worldManager.getPlayer().getDamageValue();
                enemy.damage(damageDealt);

                if (enemy.getHealth() <= 0) {
                    if(enemy.isEnemy()) {
                        SoundManager.getInstance().playSound("kill");
                    } else if (! (enemy instanceof SpikeEntity)) {
                        SoundManager.getInstance().playSound("doorOpening");
                    }
                } else {
                    if (enemy.isEnemy()) {
                        SoundManager.getInstance().playSound("hit");
                    }
                }
                handleBattleFlow(enemy);
            }

            @Override
            public void onWrong(Entity enemy) {
                    SoundManager.getInstance().playSound("wrong");
                if (enemy.isEnemy()) {
                    SoundManager.getInstance().playSound("hit");
                }

                enemy.heal(1);
                int dmg = enemy.isBoss() ? 2 : 1;
                worldManager.getPlayer().damage(dmg);

                handleBattleFlow(enemy);
            }

            @Override
            public void onCancel() { }
        };
    }

    private WorldEventListener createWorldListener() {
        return new WorldEventListener() {
            @Override
            public void onQuestionTriggered(Entity target, MathGen problem) {
                if (target.isBoss()) questionsQueue = 3;
                else questionsQueue = 1;
                showQuestionUI(target, problem);
            }

            @Override
            public void onGameOver() {
                // Trigger the fade instead of instant reload
                triggerRetry();
            }
        };
    }

    private void handleBattleFlow(Entity enemy) {
        questionsQueue--;

        // 1. Entity Died
        if (enemy.getHealth() <= 0) {
            questionScreen.hide();
            questionsQueue = 0;

            if (enemy.isBoss()) {
                Gdx.app.log("Game", "BOSS DEFEATED!");
                currentLevelIndex++;
                if (currentLevelIndex > MAX_LEVELS) {
                    Gdx.app.log("Game", "VICTORY!");
                    currentLevelIndex = 1;
                }
                loadLevel(currentLevelIndex);
            }
            return;
        }

        // 2. Player Died
        if (worldManager.getPlayer().getHealth() <= 0) {
            questionScreen.hide();
            createWorldListener().onGameOver();
            return;
        }

        // 3. Continue Battle
        if (questionsQueue > 0) {
            MathGen nextProblem = MathGen.getForLevel(currentLevelIndex, enemy.isBoss());
            showQuestionUI(enemy, nextProblem);
        } else {
            questionScreen.hide();
        }
    }

    private void showQuestionUI(Entity target, MathGen problem) {
        questionScreen.setQuestion(
            problem.getQuestion(), problem.getAnswer(), problem.getOptions(),
            target, worldManager.getPlayer().getHealth(), worldManager.getPlayer().getDamageValue()
        );
        questionScreen.show();
    }

    // --- GAME LOOP ---

    private void update(float dt) {
        // Block input if transitioning OR question screen is up
        if (questionScreen.isVisible() || isTransitioning) {
            transitionStage.act(dt); // Keep fading
            return;
        }

        int dirX = 0, dirY = 0;
        if (InputUtils.isKeysJustPressed(Input.Keys.A, Input.Keys.LEFT)) dirX -= 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.D, Input.Keys.RIGHT)) dirX += 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.W, Input.Keys.UP)) dirY += 1;
        if (InputUtils.isKeysJustPressed(Input.Keys.S, Input.Keys.DOWN)) dirY -= 1;

        if (dirX != 0 || dirY != 0) {
            worldManager.processTurn(dirX, dirY);
        }

        game.viewport.getCamera().position.x = worldManager.getPlayer().getX();
        game.viewport.getCamera().position.y = worldManager.getPlayer().getY();

        worldManager.update(dt);
        transitionStage.act(dt);
    }

    private void draw() {
        ScreenUtils.clear(0, 0, 0, 1f);
        game.viewport.apply();
        tiledMapRenderer.setView((OrthographicCamera) game.viewport.getCamera());
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        tiledMapRenderer.render();

        game.batch.begin();
        for (Entity spike : worldManager.getSpikes()) spike.draw(game.batch);
        for (Entity door : worldManager.getDoors()) if (door.getHealth() > 0) door.draw(game.batch);
        for (Entity items : worldManager.getItems()) if (items.getHealth() > 0) items.draw(game.batch);
        for (Entity enemy : worldManager.getEnemies()) if (enemy.getHealth() > 0) enemy.draw(game.batch);

        if (worldManager.getBoss() != null && worldManager.getBoss().getHealth() > 0) {
            worldManager.getBoss().draw(game.batch);
        }
        worldManager.getPlayer().draw(game.batch);
        game.batch.end();

        worldManager.getVisibilityManager().render(16, 16, game.viewport.getCamera());

        hud.update(worldManager.getPlayer().getHealth(), worldManager.getPlayer().getDamageValue());
        hud.draw();

        // Draw Fade Overlay on top
        transitionStage.draw();
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
        transitionStage.getViewport().update(width, height, true);
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        questionScreen.dispose();
        tiledMapRenderer.dispose();
        worldManager.dispose();
        hud.dispose();
        transitionStage.dispose();
        blackTexture.dispose();
    }
}
