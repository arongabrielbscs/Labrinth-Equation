package gg.group3.justgo.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import gg.group3.justgo.GameLevel;
import gg.group3.justgo.entities.Entity;
import gg.group3.justgo.math.Vector2Int;

public class VisibilityManager {
    // 0 = Unseen (Black), 1 = Explored (Dark), 2 = Visible (Clear)
    public static final int STATE_UNSEEN = 0;
    public static final int STATE_EXPLORED = 1;
    public static final int STATE_VISIBLE = 2;

    private final int width;
    private final int height;
    private final int[][] lightMap; // Stores the state of each
    private final boolean[][] dynamicObstacles;
    private final ShapeRenderer shapeRenderer;

    // How far the player can see
    private final int viewRadius = 8;

    public VisibilityManager(int width, int height) {
        this.width = width;
        this.height = height;
        this.lightMap = new int[width][height];
        this.dynamicObstacles = new boolean[width][height]; // Init helper
        this.shapeRenderer = new ShapeRenderer();
    }

    public void update(Vector2Int playerPos, GameLevel level, Array<Entity> doors) {
        // 1. Reset and Build the Dynamic Obstacle Map
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                dynamicObstacles[x][y] = false;
            }
        }

        // Mark all active (closed) doors as obstacles
        for (Entity door : doors) {
            if (door.getHealth() > 0) { // If health > 0, the door is closed/solid
                // Bounds check just in case
                if (door.getPosX() >= 0 && door.getPosX() < width &&
                    door.getPosY() >= 0 && door.getPosY() < height) {
                    dynamicObstacles[door.getPosX()][door.getPosY()] = true;
                }
            }
        }

        // 2. Demote visible tiles to explored
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (lightMap[x][y] == STATE_VISIBLE) {
                    lightMap[x][y] = STATE_EXPLORED;
                }
            }
        }

        // 3. Cast rays (Standard code)
        for (int x = -viewRadius; x <= viewRadius; x++) {
            for (int y = -viewRadius; y <= viewRadius; y++) {
                if (x*x + y*y <= viewRadius*viewRadius) {
                    castRay(playerPos.x, playerPos.y, playerPos.x + x, playerPos.y + y, level);
                }
            }
        }
    }

    // A simple Bresenham Line Algorithm to check visibility
    private void castRay(int x0, int y0, int x1, int y1, GameLevel level) {
        // ... setup Bresenham variables (dx, dy, sx, sy, err) ...
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            if (x0 < 0 || x0 >= width || y0 < 0 || y0 >= height) break;

            lightMap[x0][y0] = STATE_VISIBLE;

            // FIX: Check BOTH static walls AND dynamic obstacles (doors)
            boolean hitWall = level.isCollidable(x0, y0);
            boolean hitDoor = dynamicObstacles[x0][y0];

            if (hitWall || hitDoor) {
                break;
            }

            if (x0 == x1 && y0 == y1) break;

            // ... standard loop update ...
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public void render(float tileWidth, float tileHeight, Camera camera) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined); // Use the game's camera
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int state = lightMap[x][y];

                if (state == STATE_VISIBLE) {
                    // Draw nothing (transparent)
                    continue;
                } else if (state == STATE_EXPLORED) {
                    // Draw semi-transparent black (Dimmed)
                    shapeRenderer.setColor(0, 0, 0, 0.6f);
                } else {
                    // Draw solid black (Unseen)
                    shapeRenderer.setColor(0, 0, 0, 1f);
                }

                shapeRenderer.rect(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public boolean isVisible(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return lightMap[x][y] == STATE_VISIBLE;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
