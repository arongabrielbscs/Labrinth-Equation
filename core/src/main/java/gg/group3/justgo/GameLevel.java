package gg.group3.justgo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import gg.group3.justgo.math.Vector2Int;

public class GameLevel {
    private final boolean[][] collidables;
    private Vector2Int playerPosition;
    private final Array<Vector2Int> doorPositions;
    private final Array<Vector2Int> spikePositions;
    private final Array<EnemyData> enemies;
    private final TiledMap rawLevel;
    private final int width, height;

    public enum EnemyType {
        Beanling(3, 1, 1),
        Beanite(2, 1, 2);

        // Fields to store the data
        public final int atlasX;
        public final int atlasY;
        public final int maxHp;

        // Constructor for the Enum
        EnemyType(int x, int y, int hp) {
            this.atlasX = x;
            this.atlasY = y;
            this.maxHp = hp;
        }
    }

    public static class EnemyData {
        public final Vector2Int position;
        public final EnemyType type;

        public EnemyData(Vector2Int position, EnemyType type) {
            this.position = position;
            this.type = type;
        }
    }


    public GameLevel(String path) {
        rawLevel = new TmxMapLoader().load(path);

        // Initialize the Entities
        MapLayer entityLayer = rawLevel.getLayers().get("Entities");
        doorPositions = new Array<>();
        spikePositions = new Array<>();
        enemies = new Array<>();
        for (MapObject obj : entityLayer.getObjects()) {
            float x = obj.getProperties().get("x", float.class);
            float y = obj.getProperties().get("y", float.class);
            float w = obj.getProperties().get("width", float.class);
            float h = obj.getProperties().get("height", float.class);

            final Vector2Int position = new Vector2Int((int)(x/w), (int)(y/h));
            switch (obj.getName()) {
                case "Player":
                    playerPosition = position;
                    break;
                case "Door":
                    doorPositions.add(position);
                    break;
                case "Spike":
                    spikePositions.add(position);
                    break;
                case "Beanling":
                    enemies.add(new EnemyData(position, EnemyType.Beanling));
                    break;
                case "Beanite":
                    enemies.add(new EnemyData(position, EnemyType.Beanite));
                    break;
            }
        }

        // Initialize the collidables
        width = rawLevel.getProperties().get("width", int.class);
        height = rawLevel.getProperties().get("height", int.class);
        TiledMapTileLayer tileLayer = (TiledMapTileLayer)rawLevel.getLayers().get("Tiles");
        collidables = new boolean[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);

                if (cell != null && cell.getTile() != null) {
                    TiledMapTile tile = cell.getTile();
                    assert tile.getProperties().containsKey("collidable") : "A tile that has no properties 'collidables' is not mine";
                    if (tile.getProperties().get("collidable", boolean.class)) {
                        collidables[x][y] = true;
                    }
                }
            }
        }
    }

    public boolean isCollidable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            Gdx.app.error("GameLevel", "Out of bounds: (" + x + ", " + y + ") - Map size: " + width + "x" + height);
            return true; // Consider out-of-bounds as collidable for safety
        }

        return collidables[x][y];
    }

    public void dispose() {
        rawLevel.dispose();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean[][] getCollidables() {
        return collidables;
    }

    public TiledMap getRawLevel() {
        return rawLevel;
    }

    public Vector2Int getPlayerPosition() { return playerPosition; }

    public Array<Vector2Int> getDoorPositions() {
        return doorPositions;
    }

    public Array<Vector2Int> getSpikePositions() {
        return spikePositions;
    }

    public Array<EnemyData> getEnemies() {
        return enemies;
    }
}
