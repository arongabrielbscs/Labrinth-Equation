package gg.group3.justgo.managers;

import gg.group3.justgo.entities.Entity;
import gg.group3.justgo.utils.MathGen;

public interface WorldEventListener {
    // When the player hits an enemy/door, trigger the UI
    void onQuestionTriggered(Entity target, MathGen problem);

    // When the player dies
    void onGameOver();
}
