package gg.group3.justgo.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import gg.group3.justgo.utils.MathGen;

public class SpikeEntity extends Entity{
    public enum State {
        OFF,
        PRIMING,
        ACTIVE
    }

    private State state;
    private final TextureRegion regionOff;
    private final TextureRegion regionPriming;
    private final TextureRegion regionActive;

    public SpikeEntity(Texture atlas, int x, int y) {
        super(new TextureRegion(atlas, 0, 64, 16, 16), x, y);

        this.regionOff = new TextureRegion(atlas, 0, 64, 16, 16);
        this.regionPriming = new TextureRegion(atlas, 16, 64, 16, 16);
        this.regionActive = new TextureRegion(atlas, 32, 64, 16, 16);

        this.state = State.OFF;
        this.setHealth(0); // Spikes are invincible/don't have hearts
    }

    public void advanceState() {
        switch (state) {
            case OFF:
                state = State.PRIMING;
                setRegion(regionPriming);
                break;
            case PRIMING:
                state = State.ACTIVE;
                setRegion(regionActive);
                break;
            case ACTIVE:
                state = State.OFF;
                setRegion(regionOff);
                break;
        }
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    // Helper to get the math problem for this trap
    public MathGen getTrapProblem() {
        return MathGen.generateBasicArithmetic(10);
    }
}
