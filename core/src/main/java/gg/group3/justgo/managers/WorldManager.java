package gg.group3.justgo.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import gg.group3.justgo.GameLevel;
import gg.group3.justgo.entities.Entity;
import gg.group3.justgo.entities.utils.ArrayUtils;
import gg.group3.justgo.math.Vector2Int;
import gg.group3.justgo.utils.MathGen;

public class WorldManager {
    private final GameLevel level;
    private final Entity player;
    private Array<Entity> doors;
    private Array<Entity> enemies;
    private final WorldEventListener listener;
    private final VisibilityManager visibilityManager;

    public WorldManager(String levelPath, Texture atlas, WorldEventListener listener) {
        this.level = new GameLevel(levelPath);
        this.visibilityManager = new VisibilityManager(level.getWidth(), level.getHeight());
        this.listener = listener;
        this.doors = new Array<>();
        this.enemies = new Array<>();

        this.player = new Entity(
            new TextureRegion(atlas, 0, 0, 16, 16),
            level.getPlayerPosition().x,
            level.getPlayerPosition().y
        ).withCollisionCallback((parent, other) -> {
            // Generate problem and notify the listener (UI)
            MathGen problem = MathGen.generateBasicArithmetic(10);
            listener.onQuestionTriggered(other, problem);
        });
        this.player.setHealth(5);

        initializeEntities(atlas);

        // Perform initial calculation so the player isn't in the dark at start
        this.visibilityManager.update(level.getPlayerPosition(), level, doors);
    }

    private void initializeEntities(Texture atlas) {
        // Initialize Doors with collision logic
        for (Vector2Int doorPos : level.getDoorPositions()) {
            doors.add(
                new Entity(new TextureRegion(atlas, 16, 32, 16, 16), doorPos.x, doorPos.y)
                    .withCollisionCallback((parent, other) -> {
                        // Generate problem and notify the listener (UI)
                        MathGen problem = MathGen.generateBasicArithmetic(10);
                        listener.onQuestionTriggered(parent, problem);
                    })
            );
        }

        // Initialize Enemies with collision logic
        for (GameLevel.EnemyData enemyData : level.getEnemies()) {
            int atlX = enemyData.type.atlasX;
            int atlY = enemyData.type.atlasY;
            TextureRegion region = new TextureRegion(atlas, atlX * 16, atlY * 16, 16, 16);
            enemies.add(new Entity(region, enemyData.position.x, enemyData.position.y)
                .withCollisionCallback((parent, other) -> {
                    MathGen problem = MathGen.generateBasicArithmetic(30);
                    listener.onQuestionTriggered(parent, problem);
                })
            );
        }
    }

    // THE CORE TURN LOGIC
    public void processTurn(int dirX, int dirY) {
        Array<Entity> allCollidables = ArrayUtils.combineArrays(doors, enemies);

        // 1. Attempt Player Move
        boolean playerMoved = player.move(dirX, dirY, level, allCollidables);

        // 2. If player successfully moved (spent a turn), update enemies
        if (playerMoved) {
            visibilityManager.update(player.getPos(), level, doors);
            allCollidables.add(player);
            updateEnemies(allCollidables);
        }
    }

    private void updateEnemies(Array<Entity> allCollidables) {
        for (Entity enemy : enemies) {
            if (enemy.getHealth() <= 0) continue;

            // Simple Vision Check
            if (enemy.isVisibleTo(player, level, doors)) {
                enemy.moveTowards(player, level, allCollidables);
            }
        }
    }

    // Updates animations (interpolations)
    public void update(float dt) {
        player.update(dt);
        for (Entity enemy : enemies) enemy.update(dt);
        for (Entity door : doors) door.update(dt);
    }

    public void dispose() {
        level.dispose();
        visibilityManager.dispose();
    }

    // Getters for the Renderer
    public Entity getPlayer() { return player; }
    public Array<Entity> getEnemies() { return enemies; }
    public Array<Entity> getDoors() { return doors; }
    public GameLevel getLevel() { return level; }
    public VisibilityManager getVisibilityManager() { return visibilityManager; }
}
