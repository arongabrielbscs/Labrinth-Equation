package gg.group3.justgo.math;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.NumberUtils;

/** A 2D vector with integer components. Implements the Vector interface for compatibility
 * with libGDX vector operations while maintaining integer precision for tile-based games.
 * @author Your Name */
public class Vector2Int implements Vector<Vector2Int> {
    public int x, y;

    /** Constructs a vector at (0,0) */
    public Vector2Int() {
        this.x = 0;
        this.y = 0;
    }

    /** Constructs a vector with the given components
     * @param x The x component
     * @param y The y component */
    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Constructs a vector from the given vector
     * @param v The vector */
    public Vector2Int(Vector2Int v) {
        this.x = v.x;
        this.y = v.y;
    }

    /** Constructs a vector from a Vector2, rounding the components
     * @param v The Vector2 */
    public Vector2Int(Vector2 v) {
        this.x = Math.round(v.x);
        this.y = Math.round(v.y);
    }

    /** Sets the components of this vector
     * @param x The x component
     * @param y The y component
     * @return This vector for chaining */
    public Vector2Int set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public Vector2Int cpy() {
        return new Vector2Int(this.x, this.y);
    }

    @Override
    public float len() {
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public float len2() {
        return x * x + y * y;
    }

    @Override
    public Vector2Int limit(float limit) {
        return limit2(limit * limit);
    }

    @Override
    public Vector2Int limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            float scalar = (float) Math.sqrt(limit2 / len2);
            this.x = Math.round(this.x * scalar);
            this.y = Math.round(this.y * scalar);
        }
        return this;
    }

    @Override
    public Vector2Int setLength(float len) {
        return setLength2(len * len);
    }

    @Override
    public Vector2Int setLength2(float len2) {
        float oldLen2 = len2();
        if (oldLen2 == 0 || oldLen2 == len2) return this;
        float scalar = (float) Math.sqrt(len2 / oldLen2);
        this.x = Math.round(this.x * scalar);
        this.y = Math.round(this.y * scalar);
        return this;
    }

    @Override
    public Vector2Int clamp(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return setLength2(max2);
        float min2 = min * min;
        if (len2 < min2) return setLength2(min2);
        return this;
    }

    @Override
    public Vector2Int set(Vector2Int v) {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    @Override
    public Vector2Int sub(Vector2Int v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    /** Subtracts the given values from this vector
     * @param x The x value
     * @param y The y value
     * @return This vector for chaining */
    public Vector2Int sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    @Override
    public Vector2Int nor() {
        float len = len();
        if (len != 0) {
            this.x = Math.round(this.x / len);
            this.y = Math.round(this.y / len);
        }
        return this;
    }

    @Override
    public Vector2Int add(Vector2Int v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    /** Adds the given values to this vector
     * @param x The x value
     * @param y The y value
     * @return This vector for chaining */
    public Vector2Int add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    @Override
    public float dot(Vector2Int v) {
        return this.x * v.x + this.y * v.y;
    }

    @Override
    public Vector2Int scl(float scalar) {
        this.x = Math.round(this.x * scalar);
        this.y = Math.round(this.y * scalar);
        return this;
    }

    /** Scales this vector by the given value
     * @param scalar The scalar value
     * @return This vector for chaining */
    public Vector2Int scl(int scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    /** Scales this vector by the given values
     * @param x The x scalar
     * @param y The y scalar
     * @return This vector for chaining */
    public Vector2Int scl(int x, int y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    @Override
    public Vector2Int scl(Vector2Int v) {
        this.x *= v.x;
        this.y *= v.y;
        return this;
    }

    @Override
    public float dst(Vector2Int v) {
        final int x_d = v.x - this.x;
        final int y_d = v.y - this.y;
        return (float) Math.sqrt(x_d * x_d + y_d * y_d);
    }

    /** Returns the distance between this and the given point
     * @param x The x coordinate of the other point
     * @param y The y coordinate of the other point
     * @return The distance */
    public float dst(int x, int y) {
        final int x_d = x - this.x;
        final int y_d = y - this.y;
        return (float) Math.sqrt(x_d * x_d + y_d * y_d);
    }

    @Override
    public float dst2(Vector2Int v) {
        final int x_d = v.x - this.x;
        final int y_d = v.y - this.y;
        return x_d * x_d + y_d * y_d;
    }

    /** Returns the squared distance between this and the given point
     * @param x The x coordinate of the other point
     * @param y The y coordinate of the other point
     * @return The squared distance */
    public float dst2(int x, int y) {
        final int x_d = x - this.x;
        final int y_d = y - this.y;
        return x_d * x_d + y_d * y_d;
    }

    @Override
    public Vector2Int lerp(Vector2Int target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = Math.round((this.x * invAlpha) + (target.x * alpha));
        this.y = Math.round((this.y * invAlpha) + (target.y * alpha));
        return this;
    }

    @Override
    public Vector2Int interpolate(Vector2Int target, float alpha, Interpolation interpolator) {
        return lerp(target, interpolator.apply(alpha));
    }

    @Override
    public Vector2Int setToRandomDirection() {
        float theta = MathUtils.random(0f, MathUtils.PI2);
        this.x = Math.round(MathUtils.cos(theta));
        this.y = Math.round(MathUtils.sin(theta));
        return this;
    }

    @Override
    public boolean isUnit() {
        return isUnit(0.000000001f);
    }

    @Override
    public boolean isUnit(final float margin) {
        return Math.abs(len2() - 1f) < margin;
    }

    @Override
    public boolean isZero() {
        return x == 0 && y == 0;
    }

    @Override
    public boolean isZero(final float margin) {
        return len2() < margin;
    }

    @Override
    public boolean isOnLine(Vector2Int other, float epsilon) {
        return Math.abs(this.x * other.y - this.y * other.x) <= epsilon;
    }

    @Override
    public boolean isOnLine(Vector2Int other) {
        return this.x * other.y == this.y * other.x;
    }

    @Override
    public boolean isCollinear(Vector2Int other, float epsilon) {
        return isOnLine(other, epsilon) && hasSameDirection(other);
    }

    @Override
    public boolean isCollinear(Vector2Int other) {
        return isOnLine(other) && hasSameDirection(other);
    }

    @Override
    public boolean isCollinearOpposite(Vector2Int other, float epsilon) {
        return isOnLine(other, epsilon) && hasOppositeDirection(other);
    }

    @Override
    public boolean isCollinearOpposite(Vector2Int other) {
        return isOnLine(other) && hasOppositeDirection(other);
    }

    @Override
    public boolean isPerpendicular(Vector2Int other) {
        return MathUtils.isZero(dot(other));
    }

    @Override
    public boolean isPerpendicular(Vector2Int other, float epsilon) {
        return MathUtils.isZero(dot(other), epsilon);
    }

    @Override
    public boolean hasSameDirection(Vector2Int other) {
        return dot(other) > 0;
    }

    @Override
    public boolean hasOppositeDirection(Vector2Int other) {
        return dot(other) < 0;
    }

    @Override
    public boolean epsilonEquals(Vector2Int other, float epsilon) {
        if (other == null) return false;
        if (Math.abs(other.x - x) > epsilon) return false;
        if (Math.abs(other.y - y) > epsilon) return false;
        return true;
    }

    /** Compares this vector with the other vector for exact equality.
     * @param other The other vector
     * @return Whether the vectors are exactly equal */
    public boolean equals(Vector2Int other) {
        if (other == null) return false;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public Vector2Int mulAdd(Vector2Int v, float scalar) {
        this.x += Math.round(v.x * scalar);
        this.y += Math.round(v.y * scalar);
        return this;
    }

    @Override
    public Vector2Int mulAdd(Vector2Int v, Vector2Int mulVec) {
        this.x += v.x * mulVec.x;
        this.y += v.y * mulVec.y;
        return this;
    }

    @Override
    public Vector2Int setZero() {
        this.x = 0;
        this.y = 0;
        return this;
    }

    /** @return the angle in radians of this vector (point) relative to the x-axis. Angles are towards the positive y-axis
     *         (typically counter-clockwise) and between 0 and 2pi. */
    public float angle() {
        float angle = (float) Math.atan2(y, x);
        if (angle < 0) angle += MathUtils.PI2;
        return angle;
    }

    /** @return the angle in radians of this vector (point) relative to the given vector. Angles are towards the positive y-axis
     *         (typically counter-clockwise.) between -PI and PI */
    public float angleRad(Vector2Int reference) {
        return (float) Math.atan2(this.x * reference.y - this.y * reference.x, this.x * reference.x + this.y * reference.y);
    }

    /** Rotates the Vector2Int by 90 degrees in the specified direction, where >= 0 is counter-clockwise and < 0 is clockwise. */
    public Vector2Int rotate90(int dir) {
        int x = this.x;
        if (dir >= 0) {
            this.x = -this.y;
            this.y = x;
        } else {
            this.x = this.y;
            this.y = -x;
        }
        return this;
    }

    /** Converts this Vector2Int to a Vector2
     * @return A new Vector2 with the same components as this vector */
    public Vector2 toVector2() {
        return new Vector2(this.x, this.y);
    }

    /** Returns the Manhattan distance (taxicab distance) between this and the other vector
     * @param other The other vector
     * @return The Manhattan distance */
    public int manhattanDistance(Vector2Int other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /** Returns the Manhattan distance (taxicab distance) between this and the given point
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The Manhattan distance */
    public int manhattanDistance(int x, int y) {
        return Math.abs(this.x - x) + Math.abs(this.y - y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vector2Int other = (Vector2Int) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + NumberUtils.floatToIntBits(x);
        result = prime * result + NumberUtils.floatToIntBits(y);
        return result;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
