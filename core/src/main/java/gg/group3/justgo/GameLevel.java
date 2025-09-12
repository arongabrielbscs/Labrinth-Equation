package gg.group3.justgo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class GameLevel {
    private final boolean[][] collidables;
    private int startPlayerX, startPlayerY;
    private final TiledMap rawLevel;
    private final int width, height;

    public GameLevel(String path) {
        rawLevel = new TmxMapLoader().load(path);

        // Initialize the Entities
        MapLayer entityLayer = rawLevel.getLayers().get("Entities");
        for (MapObject obj : entityLayer.getObjects()) {
            float x = obj.getProperties().get("x", float.class);
            float y = obj.getProperties().get("y", float.class);
            float w = obj.getProperties().get("width", float.class);
            float h = obj.getProperties().get("height", float.class);

            if (obj.getName().equals("Player")) {
                startPlayerX = (int)(x/w);
                startPlayerY = (int)(y/h);
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

    public int getStartPlayerX() {
        return startPlayerX;
    }

    public int getStartPlayerY() {
        return startPlayerY;
    }
}
