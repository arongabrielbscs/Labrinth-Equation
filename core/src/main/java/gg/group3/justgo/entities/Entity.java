package gg.group3.justgo.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import gg.group3.justgo.GameLevel;
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
    private static final float WIGGLE_TIME = 0.15f;
    private static final float WIGGLE_DISTANCE = 0.3f;

    public Entity(TextureRegion region, int posX, int posY) {
        super(region);
        this.pos = new Vector2Int(posX, posY);
        this.targetPos = new Vector2Int(posX, posY);
        this.wiggleStart = new Vector2Int();
        this.wiggleTarget = new Vector2Int();
        setPosition(pos.x * getWidth(), pos.y * getHeight());
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

        // If we're already moving, snap to current target and start new movement
        if (!pos.equals(targetPos)) {
            pos.set(targetPos);
            isWiggling = false;
        }

        Vector2Int newTargetPos = targetPos.cpy().add(dx, dy);

        if (level.isCollidable(newTargetPos.x, newTargetPos.y)) {
            startWiggle(dx, dy);
            return false;
        }

        for (Entity e : collidables) {
            if (e.getHealth() <= 0) continue;
            if (e == this) continue;
            if (e.getPos().equals(newTargetPos)) {
                startWiggle(dx, dy);
                if (e.collisionCallback != null) e.collisionCallback.collided(e,this);
                return false;
            }
        }

        targetPos.set(newTargetPos);

        // Reset interpolation
        transitionElapsed = 0f;
        if (dx != 0) {
            setFlip(dx <= 0, false);
        }
        return true;
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
    public void moveTowards(Entity target, GameLevel level, Array<Entity> collidables) {
        if (this.equals(target)) return;

        // Get the difference in position (delta)
        int dx = target.getPosX() - this.getPosX();
        int dy = target.getPosY() - this.getPosY();

        // Determine the step magnitude (+1 or -1) for each axis
        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        // Get the absolute distance remaining on each axis
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        // Create an array of potential moves (up to 2: Horizontal, Vertical)
        Vector2Int potentialMoves[] = new Vector2Int[2];

        // Prioritize the axis with the largest distance remaining (greedy approach)
        if (absDx > absDy) {
            // 1. Horizontal move (e.g., (1, 0) or (-1, 0))
            potentialMoves[0] = new Vector2Int(stepX, 0);
            // 2. Vertical move (e.g., (0, 1) or (0, -1))
            potentialMoves[1] = new Vector2Int(0, stepY);
        } else { // absDy >= absDx (prioritize Y or equal preference)
            // 1. Vertical move
            potentialMoves[0] = new Vector2Int(0, stepY);
            // 2. Horizontal move
            potentialMoves[1] = new Vector2Int(stepX, 0);
        }

        // Edge Case: If the target is exactly one step away diagonally (e.g., (1, 1))
        // we only test the top priority move (e.g., (1, 0) then (0, 1)).
        // This maintains the "single direction" constraint.

        // Attempt the moves in order of priority:
        for (Vector2Int direction : potentialMoves) {
            // Skip zero moves (e.g., if we are already aligned on an axis)
            if (direction.x == 0 && direction.y == 0) continue;

            // Try to move. The 'move' method handles collision and wall checks.
            if (move(direction.x, direction.y, level, collidables)) {
                return; // Move successful
            }
        }

        // If no single-axis move succeeded, no movement possible this turn.
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
        setPosition(currentX * getWidth(), currentY * getHeight());

        // Check if the transition is complete
        if (progress >= 1f) {
            pos.set(targetPos);
            transitionElapsed = 0f;
            setPosition(pos.x * getWidth(), pos.y * getHeight());
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

        setPosition(currentX * getWidth(), currentY * getHeight());

        if (progress >= 1f) {
            isWiggling = false;
            wiggleElapsed = 0f;
            setPosition(pos.x * getWidth(), pos.y * getHeight());
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
        setPosition(pos.x * getWidth(), pos.y * getHeight());
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

    public Entity health(int health) {
        this.health = health;
        return this;
    }

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
}

