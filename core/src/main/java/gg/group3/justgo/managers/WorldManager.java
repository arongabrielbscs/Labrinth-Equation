package gg.group3.justgo.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import gg.group3.justgo.GameLevel;
import gg.group3.justgo.entities.Entity;
import gg.group3.justgo.entities.SpikeEntity;
import gg.group3.justgo.entities.utils.ArrayUtils;
import gg.group3.justgo.math.Vector2Int;
import gg.group3.justgo.utils.MathGen;

public class WorldManager {
    private final GameLevel level;
    private final Entity player;
    private Array<Entity> doors;
    private Array<Entity> enemies;
    private Array<Entity> items;
    private Entity boss;
    private Array<SpikeEntity> spikes;
    private final WorldEventListener listener;
    private final VisibilityManager visibilityManager;
    private final int currentLevelIndex;

    public WorldManager(String levelPath, Texture atlas, WorldEventListener listener, int levelIndex) {
        this.level = new GameLevel(levelPath);
        this.visibilityManager = new VisibilityManager(level.getWidth(), level.getHeight());
        this.listener = listener;
        this.doors = new Array<>();
        this.enemies = new Array<>();
        this.items = new Array<>();
        this.spikes = new Array<>();
        this.currentLevelIndex = levelIndex;

        this.player = new Entity(
            new TextureRegion(atlas, 0, 0, 16, 16),
            level.getPlayerPosition().x,
            level.getPlayerPosition().y
        ).withCollisionCallback((parent, other) -> {
            // Generate problem and notify the listener (UI)
            boolean isBoss = other.isBoss();
            MathGen problem = MathGen.getForLevel(currentLevelIndex, isBoss);
            listener.onQuestionTriggered(other, problem);
        })
        .health(5);

        if (level.getBossData() != null) {
            GameLevel.EnemyData data = level.getBossData();
            int atlX = data.type.atlasX;
            int atlY = data.type.atlasY;
            int size = data.type.size;
            this.boss = new Entity(
                new TextureRegion(atlas, atlX * 16, atlY * 16, size, size),
                data.position.x,
                data.position.y
            ).withCollisionCallback((parent, other) -> {
                boolean isBoss = parent.isBoss(); // 'parent' is the enemy here
                MathGen problem = MathGen.getForLevel(currentLevelIndex, isBoss);
                listener.onQuestionTriggered(parent, problem);
            })
            .health(data.type.maxHp)
            .asEnemy(data.type, true);
        }

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
                    boolean isBoss = parent.isBoss(); // 'parent' is the enemy here
                    MathGen problem = MathGen.getForLevel(currentLevelIndex, isBoss);
                    listener.onQuestionTriggered(parent, problem);
                })
                .health(enemyData.type.maxHp)
                .asEnemy(enemyData.type)
            );
        }

        // Initialize Items
        for (GameLevel.ItemData itemData : level.getItems()) {
            TextureRegion region = new TextureRegion(atlas, itemData.type.atlasX * 16, itemData.type.atlasY * 16, 16, 16);

            Entity item = new Entity(region, itemData.position.x, itemData.position.y)
                .asItem(itemData.type);

            // --- THE PICKUP LOGIC ---
            item.withCollisionCallback((parent, other) -> {
                // 'parent' is the Item, 'other' is the Player
                GameLevel.ItemType type = parent.getItemType();

                if (type == GameLevel.ItemType.HealthPotion) {
                    other.heal(type.value);
                    Gdx.app.log("Pickup", "Healed! HP is now: " + other.getHealth());
                }
                else if (type == GameLevel.ItemType.Dagger) {
                    other.increaseDamage(type.value);
                    Gdx.app.log("Pickup", "Damage Up! Now deals: " + other.getDamageValue());
                }

                // Remove the item from the world
                parent.setHealth(0);
            });

            items.add(item); // Add to a new 'items' array in WorldManager
        }

        // INITIALIZE SPIKES
        for (Vector2Int pos : level.getSpikePositions()) {
            spikes.add(new SpikeEntity(atlas, pos.x, pos.y));
        }
    }

    // THE CORE TURN LOGIC
    public void processTurn(int dirX, int dirY) {
        Array<Entity> playerCollisions = ArrayUtils.combineArrays(doors, enemies, items);
        if (boss != null && boss.getHealth() > 0) {
            playerCollisions.add(boss); // Add Boss to collisions
        }

        // 1. Attempt Player Move
        boolean playerMoved = player.move(dirX, dirY, level, playerCollisions);


        // 3. UPDATE SPIKES (Cycle: Off -> Priming -> Active)
        for (SpikeEntity spike : spikes) {
            spike.advanceState();
        }
        checkForSpikeTrap();

        // 2. If player successfully moved (spent a turn), update enemies
        if (playerMoved) {
            Array<Entity> enemyCollisions = ArrayUtils.combineArrays(doors, enemies);
            visibilityManager.update(player.getPos(), level, doors);

            enemyCollisions.add(player);
            updateEnemies(enemyCollisions);
            updateBoss(enemyCollisions);
        }
    }

    private void checkForSpikeTrap() {
        for (SpikeEntity spike : spikes) {
            // Check if player is on the same tile AND spike is active
            if (spike.getPos().equals(player.getPos()) && spike.isActive()) {
                // Trigger the Question Screen!
                listener.onQuestionTriggered(spike, spike.getTrapProblem());

                SoundManager.getInstance().playSound("splat");

                // Optional: If you want the spike to turn off immediately after triggering:
                // spike.resetState();
                return; // Trigger only one trap at a time
            }
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

    private void updateBoss(Array<Entity> allCollidables) {
        if (boss == null || boss.getHealth() <= 0) return;

        // 1. Check Priming Logic
        // If this returns false, the boss is "charging" and skips movement
        if (!boss.processBossTurn()) {
            return;
        }

        // 2. Move (The boss is huge, but moveTowards handles the center position)
        if (boss.isVisibleTo(player, level, doors)) {
            boss.moveTowards(player, level, allCollidables, 4);
        }
    }

    // Updates animations (interpolations)
    public void update(float dt) {
        player.update(dt);
        if (boss != null) boss.update(dt);
        for (Entity enemy : enemies) enemy.update(dt);
        for (Entity door : doors) door.update(dt);
    }

    public void dispose() {
        level.dispose();
        visibilityManager.dispose();
    }

    // Getters for the Renderer
    public Entity getPlayer() { return player; }
    public Entity getBoss() { return boss; }
    public Array<Entity> getEnemies() { return enemies; }
    public Array<Entity> getItems() { return items; }
    public Array<Entity> getDoors() { return doors; }
    public Array<SpikeEntity> getSpikes() { return spikes; }
    public GameLevel getLevel() { return level; }
    public VisibilityManager getVisibilityManager() { return visibilityManager; }
}
