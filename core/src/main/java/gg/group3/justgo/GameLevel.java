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
    private EnemyData bossData;
    private final TiledMap rawLevel;
    private final Array<Vector2Int> doorPositions;
    private final Array<Vector2Int> spikePositions;
    private final Array<EnemyData> enemies;
    private final int width, height;

    public enum EnemyType {
        Beanling(3, 1, 1, 1),
        Beanite(2, 1, 2, 2),
        RatFly(0, 1, 3, 2),
        RatGhoul(1, 1, 5, 3),
        GhoulKing(12, 0, 64, 15, 5),
        WillowQueen(12, 4, 64, 16, 5)
        ;

        // Fields to store the data
        public final int atlasX;
        public final int atlasY;
        public final int maxHp;
        public final int damage;
        public final int size;

        // Constructor for the Enum
        EnemyType(int x, int y, int hp, int damage) {
            this.atlasX = x;
            this.atlasY = y;
            this.size = 16;
            this.maxHp = hp;
            this.damage = damage;
        }

        // Constructor for the Enum
        EnemyType(int x, int y, int size, int hp, int damage) {
            this.atlasX = x;
            this.atlasY = y;
            this.size = size;
            this.maxHp = hp;
            this.damage = damage;
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

            Vector2Int position = new Vector2Int((int)(x / 16), (int)(y / 16));

            if (obj.getName() != null) {
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
                    case "RatFly":
                        enemies.add(new EnemyData(position, EnemyType.RatFly));
                        break;
                    case "RatGhoul":
                        enemies.add(new EnemyData(position, EnemyType.RatGhoul));
                        break;
                    case "GhoulKing":
                        bossData = new EnemyData(position, EnemyType.GhoulKing);
                        break;
                    case "WillowQueen":
                        bossData = new EnemyData(position, EnemyType.WillowQueen);
                }
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

    public EnemyData getBossData() {
        return bossData;
    }
}
