package gg.group3.justgo.entities.utils;

import com.badlogic.gdx.utils.Array;

public class ArrayUtils {
    @SafeVarargs
    public static <T> Array<T> combineArrays(Array<T>... arrays) {
        Array<T> result = new Array<>();
        for (Array<T> array : arrays) {
            result.addAll(array);
        }
        return result;
    }
}
