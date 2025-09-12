package gg.group3.justgo.utils;

import com.badlogic.gdx.Gdx;

public class InputUtils {
    public static boolean isKeysJustPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyJustPressed(key)) return true;
        }

        return false;
    }

    public static boolean isKeysPressed(int... keys) {
        for (int key : keys) {
            if (Gdx.input.isKeyPressed(key)) return true;
        }

        return false;
    }
}
