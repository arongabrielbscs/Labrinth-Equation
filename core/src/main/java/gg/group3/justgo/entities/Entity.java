package gg.group3.justgo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import gg.group3.justgo.GameLevel;
import gg.group3.justgo.math.Vector2Int;

public class Entity extends Sprite {
    private final Vector2Int pos;
    private final Vector2Int targetPos;

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
            if (e.getPos().equals(newTargetPos)) {
                startWiggle(dx, dy);
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
}

