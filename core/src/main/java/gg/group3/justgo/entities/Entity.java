package gg.group3.justgo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import gg.group3.justgo.GameLevel;
import gg.group3.justgo.managers.SoundManager;
import gg.group3.justgo.math.Vector2Int;

public class Entity extends Sprite {
    public interface CollisionCallback {
        void collided(Entity parent, Entity other);
    }

    private final Vector2Int pos;
    private final Vector2Int targetPos;
    private int health = 1;
    private CollisionCallback collisionCallback = null;

    private static final float POS_TRANSITION_TIME = 0.3f;
    private final Interpolation posTransition = Interpolation.swingOut;
    private float transitionElapsed = 0f;

    private boolean isWiggling = false;
    private float wiggleElapsed = 0f;
    private final Vector2Int wiggleStart;
    private final Vector2Int wiggleTarget;
    private GameLevel.EnemyType enemyType = null;

    private static final float WIGGLE_TIME = 0.15f;
    private static final float WIGGLE_DISTANCE = 0.3f;

    // --- BOSS MECHANICS ---
    private boolean isBoss = false;
    private int primeCounter = 0;
    private static final int TURNS_TO_PRIME = 5;

    // ITEMS
    private int damageValue = 1; // Default damage is 1
    private GameLevel.ItemType itemType = null;

    public Entity(TextureRegion region, int posX, int posY) {
        super(region);
        this.pos = new Vector2Int(posX, posY);
        this.targetPos = new Vector2Int(posX, posY);
        this.wiggleStart = new Vector2Int();
        this.wiggleTarget = new Vector2Int();
        setPosition(pos.x * 16, pos.y * 16);
    }

    /**
     * Move's the entity
     * @param dx x value of how much does the entity move
     * @param dy y value of how much does the entity move
     * @param level to know where is the wall or a floor etc.
     * @param collidables any entities that are solids and immovable
     * @return returns `true` if the player move succeeded, or if dx and dy != 0
     */
    public boolean move(int dx, int dy, GameLevel level, Array<Entity> collidables) {
        if (dx == 0 && dy == 0) return false;

        // Snap to grid if not moving
        if (!pos.equals(targetPos)) {
            pos.set(targetPos);
            isWiggling = false;
        }

        Vector2Int newTargetPos = targetPos.cpy().add(dx, dy);

        // 1. WALL COLLISION (Check every tile this entity would occupy)
        // If I am a 4x4 Boss, I need to check all 16 tiles I'm stepping onto, not just the top-left.
        int myW = getTileWidth();
        int myH = getTileHeight();

        for (int ox = 0; ox < myW; ox++) {
            for (int oy = 0; oy < myH; oy++) {
                if (level.isCollidable(newTargetPos.x + ox, newTargetPos.y + oy)) {
                    startWiggle(dx, dy);
                    return false; // Hit a wall
                }
            }
        }

        // 2. ENTITY COLLISION (AABB Intersection)
        for (Entity e : collidables) {
            if (e.getHealth() <= 0) continue;
            if (e == this) continue;

            // Get the OTHER entity's bounds
            // We use e.getTargetPos() to collide with where they are GOING,
            // otherwise we might walk through them if we move on the same turn.
            int otherX = e.getTargetPos().x;
            int otherY = e.getTargetPos().y;
            int otherW = e.getTileWidth();
            int otherH = e.getTileHeight();

            // My bounds at the new position
            int myX = newTargetPos.x;
            int myY = newTargetPos.y;

            // CHECK INTERSECTION (AABB)
            // Logic: If (My Left < Other Right) AND (My Right > Other Left) ...
            if (myX < otherX + otherW && myX + myW > otherX &&
                myY < otherY + otherH && myY + myH > otherY) {

                // COLLISION DETECTED!
                startWiggle(dx, dy);

                // Trigger the callback (This starts the Math Question)
                if (e.collisionCallback != null) {
                    e.collisionCallback.collided(e, this);
                }

                return false;
            }
        }

        // 3. Movement Allowed
        targetPos.set(newTargetPos);
        transitionElapsed = 0f;
        if (dx != 0) {
            setFlip(dx <= 0, false);
        }
        SoundManager.getInstance().playSound("step", 1.1f);
        return true;
    }

    // Helper to get width in TILES (e.g., Boss = 4, Player = 1)
    public int getTileWidth() {
        return (int) (getWidth() / 16);
    }

    public int getTileHeight() {
        return (int) (getHeight() / 16);
    }

    /**
     * Overloaded move method that accepts a Vector2Int for direction
     * @param direction Vector2Int representing the movement direction
     * @param level to know where is the wall or a floor etc.
     * @param collidables any entities that are solids and immovable
     * @return returns `true` if the player move succeeded, or if direction is not zero
     */
    public boolean move(Vector2Int direction, GameLevel level, Array<Entity> collidables) {
        return move(direction.x, direction.y, level, collidables);
    }

    /**
     * Checks if this entity has a clear line of sight to another entity (e.g., Player).
     * This uses a grid-traversal algorithm (like a simplified Bresenham) to check
     * all tiles between the two entities for collidable walls OR other collidable entities.
     *
     * @param other The target entity (e.g., the player).
     * @param level The GameLevel to check for collidable tiles (walls).
     * @param collidables A list of entities that can block line of sight (excluding this and other).
     * @return true if there is a clear line of sight, false otherwise.
     */
    public boolean isVisibleTo(Entity other, GameLevel level, Array<Entity> collidables) {
        // Start and end positions
        int x0 = pos.x;
        int y0 = pos.y;
        int x1 = other.pos.x;
        int y1 = other.pos.y;

        // Trivial case: Same position
        if (x0 == x1 && y0 == y1) return true;

        // Simplified Bresenham's-like algorithm for grid traversal
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = (dx > dy ? dx : -dy) / 2;
        int e2;

        int currentX = x0;
        int currentY = y0;

        while (true) {
            // Check for collision at the *intermediate* tile.
            // We skip the starting tile (x0, y0) and the ending tile (x1, y1).
            if (currentX != x0 || currentY != y0) {

                // 1. Check for wall/level collision at the current tile
                if (level.isCollidable(currentX, currentY)) {
                    return false; // Obstruction found (Wall)
                }

                // 2. Check for other collidable entities at the current tile
                //    We only check intermediate tiles. We already excluded the start/end entity.
                for (Entity e : collidables) {
                    // Ignore dead entities
                    if (e.getHealth() <= 0) continue;

                    // Ignore the 'other' entity if it happens to be in the collidables list
                    if (e == other) continue;

                    // Ignore the 'this' entity if it happens to be in the collidables list
                    if (e == this) continue;

                    // Check if the entity is at the current position
                    if (e.getPos().x == currentX && e.getPos().y == currentY) {
                        return false; // Obstruction found (Other Entity)
                    }
                }
            }

            if (currentX == x1 && currentY == y1) break; // Reached the target

            e2 = err;
            if (e2 > -dx) {
                err -= dy;
                currentX += sx;
            }
            if (e2 < dy) {
                err += dx;
                currentY += sy;
            }
        }

        return true; // No obstructions found between the two positions
    }

    /**
     * Attempts to move the entity one tile closer to the target entity,
     * prioritizing movement that reduces the Manhattan distance.
     *
     * @param target      The entity to move towards (e.g., the player).
     * @param level       The game level for wall checks.
     * @param collidables All entities that can block the move.
     */
    public void moveTowards(Entity target, GameLevel level, Array<Entity> collidables, int speed) {
        // Loop for the number of steps allowed by speed
        for (int i = 0; i < speed; i++) {

            // 1. Check if we already arrived
            if (this.getPos().equals(target.getPos())) return;

            // 2. RE-CALCULATE logic relative to the current position (it changes every loop!)
            int dx = target.getPosX() - this.getPosX();
            int dy = target.getPosY() - this.getPosY();
            int stepX = Integer.compare(dx, 0);
            int stepY = Integer.compare(dy, 0);
            int absDx = Math.abs(dx);
            int absDy = Math.abs(dy);

            Vector2Int[] potentialMoves = new Vector2Int[2];

            // Prioritize axis
            if (absDx > absDy) {
                potentialMoves[0] = new Vector2Int(stepX, 0);
                potentialMoves[1] = new Vector2Int(0, stepY);
            } else {
                potentialMoves[0] = new Vector2Int(0, stepY);
                potentialMoves[1] = new Vector2Int(stepX, 0);
            }

            // 3. Attempt the move for this specific step
            boolean movedThisStep = false;
            for (Vector2Int direction : potentialMoves) {
                if (direction.x == 0 && direction.y == 0) continue;

                // Try to move 1 tile
                if (move(direction.x, direction.y, level, collidables)) {
                    movedThisStep = true;
                    break; // Break inner loop (directions), continue outer loop (speed)
                }
            }

            // 4. If we were blocked on all sides, stop trying to use the rest of our speed
            if (!movedThisStep) {
                break;
            }
        }
    }

    public void moveTowards(Entity target, GameLevel level, Array<Entity> collidables) {
        moveTowards(target, level, collidables, 1);
    }

    public void update(float dt) {
        // Handle wiggle animation first (takes priority)
        if (isWiggling) {
            updateWiggle(dt);
            return;
        }

        if (pos.equals(targetPos)) return;
        transitionElapsed += dt;
        float progress = Math.min(1f, transitionElapsed / POS_TRANSITION_TIME);
        float interpolatedProgress = posTransition.apply(progress);

        // Update Sprite position directly
        float currentX = pos.x + (targetPos.x - pos.x) * interpolatedProgress;
        float currentY = pos.y + (targetPos.y - pos.y) * interpolatedProgress;

        // World -> Pixels
        setPosition(currentX * 16, currentY * 16);

        // Check if the transition is complete
        if (progress >= 1f) {
            pos.set(targetPos);
            transitionElapsed = 0f;
            setPosition(pos.x * 16, pos.y * 16);
        }
    }

    private void startWiggle(int dx, int dy) {
        isWiggling = true;
        wiggleElapsed = 0f;
        wiggleStart.set(targetPos);

        // Use the full movement direction for wiggle
        wiggleTarget.set(targetPos).add(dx, dy);

        if (dx != 0) {
            setFlip(dx <= 0, false);
        }
    }

    private void updateWiggle(float dt) {
        wiggleElapsed += dt;
        float progress = Math.min(1f, wiggleElapsed / WIGGLE_TIME);

        // Simple sine wave that oscillates and diminishes over time
        float wiggleIntensity = (1f - progress); // Fade out over time
        float wiggleOffset = (float)Math.sin(progress * Math.PI * 2) * WIGGLE_DISTANCE * wiggleIntensity;

        // Apply the wiggle in the direction we tried to move
        float currentX = wiggleStart.x + (wiggleTarget.x - wiggleStart.x) * wiggleOffset;
        float currentY = wiggleStart.y + (wiggleTarget.y - wiggleStart.y) * wiggleOffset;

        setPosition(currentX * 16, currentY * 16);

        if (progress >= 1f) {
            isWiggling = false;
            wiggleElapsed = 0f;
            setPosition(pos.x * 16, pos.y * 16);
        }
    }

    // Getter methods using Vector2Int
    public Vector2Int getPos() {
        return pos.cpy();
    }

    public Vector2Int getTargetPos() {
        return targetPos.cpy();
    }

    public int getPosX() {
        return pos.x;
    }

    public int getPosY() {
        return pos.y;
    }

    public float getWorldPosX() {
        return (float)targetPos.x * getWidth();
    }

    public float getWorldPosY() {
        return (float)targetPos.y * getHeight();
    }

    /**
     * Logic for the Boss Turn.
     * @return true if the entity should move/act, false if it is busy (priming).
     */
    public boolean processBossTurn() {
        // Normal enemies always act
        if (!isBoss) return true;

        if (primeCounter < TURNS_TO_PRIME) {
            primeCounter++;
            Gdx.app.log("Boss", "Priming... " + primeCounter + "/" + TURNS_TO_PRIME);
            // Return false to tell the Manager "Don't move me yet!"
            return false;
        } else {
            // Priming done! Reset for the next cycle and allow movement.
            primeCounter = 0;
            return true;
        }
    }

    /**
     * Get the current world position as a Vector2
     * @return Vector2 representing the current world position in pixels
     */
    public Vector2 getWorldPos() {
        return new Vector2(getWorldPosX(), getWorldPosY());
    }

    /**
     * Calculate the distance to another entity
     * @param other The other entity
     * @return The distance in tiles
     */
    public float distanceTo(Entity other) {
        return pos.dst(other.pos);
    }

    /**
     * Calculate the Manhattan distance to another entity (useful for pathfinding)
     * @param other The other entity
     * @return The Manhattan distance in tiles
     */
    public int manhattanDistanceTo(Entity other) {
        return pos.manhattanDistance(other.pos);
    }

    /**
     * Check if this entity is adjacent to another entity
     * @param other The other entity
     * @return true if the entities are adjacent (distance of 1 tile)
     */
    public boolean isAdjacentTo(Entity other) {
        return manhattanDistanceTo(other) == 1;
    }

    /**
     * Get the direction vector to another entity
     * @param other The other entity
     * @return Vector2Int representing the direction (will be normalized to unit directions)
     */
    public Vector2Int getDirectionTo(Entity other) {
        Vector2Int direction = other.pos.cpy().sub(pos);
        // Normalize to unit directions for grid-based movement
        if (direction.x != 0) direction.x = direction.x > 0 ? 1 : -1;
        if (direction.y != 0) direction.y = direction.y > 0 ? 1 : -1;
        return direction;
    }

    /**
     * Set the entity's position directly (useful for teleporting)
     * @param newPos The new position
     */
    public void setPos(Vector2Int newPos) {
        pos.set(newPos);
        targetPos.set(newPos);
        setPosition(pos.x * 16, pos.y * 16);
        transitionElapsed = 0f;
        isWiggling = false;
    }

    /**
     * Set the entity's position directly (useful for teleporting)
     * @param x The new x position
     * @param y The new y position
     */
    public void setPos(int x, int y) {
        setPos(new Vector2Int(x, y));
    }

    public Entity withCollisionCallback(CollisionCallback collisionCallback) {
        this.collisionCallback = collisionCallback;
        return this;
    }

    public Entity asEnemy(GameLevel.EnemyType enemyType, boolean isBoss) {
        this.enemyType = enemyType;
        this.isBoss = isBoss;
        return this;
    }

    public Entity asEnemy(GameLevel.EnemyType enemyType) {
        this.enemyType = enemyType;
        return this;
    }

    public Entity health(int health) {
        this.health = health;
        return this;
    }

    // Call this to turn an entity into an item
    public Entity asItem(GameLevel.ItemType type) {
        this.itemType = type;
        return this;
    }

    public boolean isItem() { return itemType != null; }
    public GameLevel.ItemType getItemType() { return itemType; }

    public int getDamageValue() { return damageValue; }
    public void increaseDamage(int amount) { this.damageValue += amount; }

    public void damage(int damage) {
        this.health -= damage;
    }

    public void heal(int heal) {
        this.health += heal;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return health;
    }

    public boolean isEnemy() {
        return enemyType != null;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public GameLevel.EnemyType getEnemyType() {
        return enemyType;
    }
}

