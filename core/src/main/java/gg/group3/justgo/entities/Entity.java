package gg.group3.justgo.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import gg.group3.justgo.GameLevel;

public class Entity extends Sprite {
    private int posX, posY;
    private int targetPosX, targetPosY;

    private static final float POS_TRANSITION_TIME = 0.3f;
    private final Interpolation posTransition = Interpolation.swingOut;
    private float transitionElapsed = 0f;

    private boolean isWiggling = false;
    private float wiggleElapsed = 0f;
    private int wiggleStartX, wiggleStartY;
    private int wiggleTargetX, wiggleTargetY;
    private static final float WIGGLE_TIME = 0.15f;
    private static final float WIGGLE_DISTANCE = 0.3f;

    public Entity(TextureRegion region, int posX, int posY) {
        super(region);
        this.posX = posX;
        this.posY = posY;
        targetPosX = posX;
        targetPosY = posY;
        setPosition(posX * getWidth(), posY * getHeight());
    }

    /**
     * Move's the entity
     * @param dx x value of how much does the entity move
     * @param dy y value of how much does the entity move
     * @param level to know where is the wall or a floor etc.
     * @return returns `true` if the player move succeeded, or if dx and dy != 0
     */
    public boolean move(int dx, int dy, GameLevel level) {
        if (dx == 0 && dy == 0) return false;

        // If we're already moving, snap to current target and start new movement
        if (posX != targetPosX || posY != targetPosY) {
            posX = targetPosX;
            posY = targetPosY;
            isWiggling = false;
        }

        int newTargetPosX = targetPosX + dx;
        int newTargetPosY = targetPosY + dy;

        if (level.isCollidable(newTargetPosX, newTargetPosY)) {
            startWiggle(dx, dy);
            return false;
        }

        targetPosX = newTargetPosX;
        targetPosY = newTargetPosY;

        Gdx.app.log("Entity Move", String.format("Moved to position: %d, %d", targetPosX, targetPosY));

        // Reset interpolation
        transitionElapsed = 0f;
        if (dx != 0) {
            setFlip(dx <= 0, false);
        }
        return true;
    }

    public void update(float dt) {
        // Handle wiggle animation first (takes priority)
        if (isWiggling) {
            updateWiggle(dt);
            return;
        }

        if (posX == targetPosX && posY == targetPosY) return;
        transitionElapsed += dt;
        float progress = Math.min(1f, transitionElapsed / POS_TRANSITION_TIME);
        float interpolatedProgress = posTransition.apply(progress);

        // Update Sprite position directly
        float currentX = posX + (targetPosX - posX) * interpolatedProgress;
        float currentY = posY + (targetPosY - posY) * interpolatedProgress;

        // World -> Pixels
        setPosition(currentX * getWidth(), currentY * getHeight());

        // Check if the transition is complete
        if (progress >= 1f) {
            posX = targetPosX;
            posY = targetPosY;
            transitionElapsed = 0f;
            setPosition(posX * getWidth(), posY * getHeight());
        }
    }

    private void startWiggle(int dx, int dy) {
        isWiggling = true;
        wiggleElapsed = 0f;
        wiggleStartX = targetPosX;
        wiggleStartY = targetPosY;

        // Use the full movement direction for wiggle
        wiggleTargetX = targetPosX + dx;
        wiggleTargetY = targetPosY + dy;

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
        float currentX = wiggleStartX + (wiggleTargetX - wiggleStartX) * wiggleOffset;
        float currentY = wiggleStartY + (wiggleTargetY - wiggleStartY) * wiggleOffset;

        setPosition(currentX * getWidth(), currentY * getHeight());

        if (progress >= 1f) {
            isWiggling = false;
            wiggleElapsed = 0f;
            setPosition(posX * getWidth(), posY * getHeight());
        }
    }


    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public float getWorldPosX() { return (float)targetPosX * getWidth(); }

    public float getWorldPosY() { return (float)targetPosY * getHeight(); }
}
