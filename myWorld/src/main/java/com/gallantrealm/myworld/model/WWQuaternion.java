package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import com.gallantrealm.myworld.communication.Sendable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Used for representing rotation. A quaternion has four values that allow the rotation of an object to be represented
 * in a fashion suitable for arithmetic with other rotations, avoiding the 'gimbal lock' problem.
 */
public class WWQuaternion implements Cloneable, Serializable, Sendable {
    private static final long serialVersionUID = 1L;

    private float w = 1.0f;
    private float x;
    private float y;
    private float z;

    public WWQuaternion() {
    }

    public WWQuaternion(WWQuaternion q) {
        w = q.w;
        x = q.x;
        y = q.y;
        z = q.z;
    }

    /**
     * Constructor taking euler values (in degrees). The values
     * are applied pitch first, then roll, then yaw.
     */
    public WWQuaternion(float pitch, float roll, float yaw) {
        set(pitch, roll, yaw);
    }

    public final WWQuaternion set(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
        normalize();
        return this;
    }

    public final WWQuaternion set(float pitch, float roll, float yaw) {
        pitch = FastMath.toRadians(pitch);
        roll = FastMath.toRadians(roll);
        yaw = FastMath.toRadians(yaw);

        float angle;
        angle = pitch * 0.5f;
        float sinX = FastMath.sin(angle);
        float cosX = FastMath.cos(angle);
        angle = roll * 0.5f;
        float sinY = FastMath.sin(angle);
        float cosY = FastMath.cos(angle);
        angle = yaw * 0.5f;
        float sinZ = FastMath.sin(angle);
        float cosZ = FastMath.cos(angle);

        float sinXcosYcosZ = sinX * cosY * cosZ;
        float sinXsinYsinZ = sinX * sinY * sinZ;
        float sinXcosYsinZ = sinX * cosY * sinZ;
        float sinXsinYcosZ = sinX * sinY * cosZ;
        float cosXcosYcosZ = cosX * cosY * cosZ;
        float cosXsinYsinZ = cosX * sinY * sinZ;
        float cosXcosYsinZ = cosX * cosY * sinZ;
        float cosXsinYcosZ = cosX * sinY * cosZ;

        w = cosXcosYcosZ - sinXsinYsinZ;
        x = sinXcosYcosZ + cosXsinYsinZ;
        y = cosXsinYcosZ - sinXcosYsinZ;
        z = cosXcosYsinZ + sinXsinYcosZ;

        normalize();
        return this;
    }

    /**
     * Constructor taking an angle (in degrees) and x, y, z coordinates
     * of a vector that the angle is rotated around (using the right-hand-rule).
     */
    public WWQuaternion(float angle, float x, float y, float z) {
        angle = FastMath.toRadians(angle);
        float mag = (float)Math.sqrt(x * x + y * y + z * z);
        if (mag < 1e-6f) {
            this.w = 1.0f;
            this.x = 0.0f;
            this.y = 0.0f;
            this.z = 0.0f;
            return;
        }
        x /= mag;
        y /= mag;
        z /= mag;
        float halfAngle = 0.5f * angle;
        float sin = FastMath.sin(halfAngle);
        this.w = FastMath.cos(halfAngle);
        this.x = sin * x;
        this.y = sin * y;
        this.z = sin * z;
    }

    public final float getW() {
        return w;
    }

    public final float getX() {
        return x;
    }

    public final float getY() {
        return y;
    }

    public final float getZ() {
        return z;
    }

    public final void setW(float w) {
        this.w = w;
    }

    public final void setX(float x) {
        this.x = x;
    }

    public final void setY(float y) {
        this.y = y;
    }

    public final void setZ(float z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WWQuaternion)) {
            return false;
        }
        WWQuaternion q = (WWQuaternion) obj;
        return w == q.w && x == q.x && y == q.y && z == q.z;
    }

    /**
     * Spin rotation on the x axis.
     */
    public WWQuaternion pitch(float pitch) {
        WWQuaternion temp = new WWQuaternion(pitch, 0, 0);
        rotate(temp);
        return this;
    }

    /**
     * Spin rotation on the y axis.
     */
    public WWQuaternion roll(float roll) {
        WWQuaternion temp = new WWQuaternion(0, roll, 0);
        rotate(temp);
        return this;
    }

    /**
     * Spin rotation on the z axis.
     */
    public WWQuaternion yaw(float yaw) {
        WWQuaternion temp = new WWQuaternion(0, 0, yaw);
        rotate(temp);
        return this;
    }

    /**
     * Adjusts the rotation by angle with the center of rotation according to axis.
     */
    public WWQuaternion spin(float angle, float x, float y, float z) {
        WWQuaternion temp = new WWQuaternion(angle, x, y, z);
        rotate(temp);
        return this;
    }

    /**
     * Returns rotation along the x axis
     */
    public final float getPitch() {
        float sqw = w * w;
        float sqx = x * x;
        float sqy = y * y;
        float sqz = z * z;
        float pitch = FastMath.atan2(2 * x * w - 2 * z * y, -sqx + sqz - sqy + sqw);
        return FastMath.toDegrees(pitch);
    }

    /**
     * Returns rotation along the y axis
     */
    public final float getRoll() {
        float sqw = w * w;
        float sqx = x * x;
        float sqy = y * y;
        float sqz = z * z;
        float val = 2 * (x * z + y * w) / (sqx + sqz + sqy + sqw);
        val = Math.max(-1.0f, Math.min(1.0f, val));
        float roll = (float)Math.asin(val);
        return FastMath.toDegrees(roll);
    }

    /**
     * Returns rotation along the z axis
     */
    public final float getYaw() {
        float sqw = w * w;
        float sqx = x * x;
        float sqy = y * y;
        float sqz = z * z;
        float yaw = FastMath.atan2(2 * z * w - 2 * x * y, sqx - sqz - sqy + sqw);
        return FastMath.toDegrees(yaw);
    }

    private final void normalize() {
        float norm = (float) Math.sqrt(w * w + x * x + y * y + z * z);
        if (norm < 1e-6f) {
            w = 1.0f;
            x = 0.0f;
            y = 0.0f;
            z = 0.0f;
            return;
        }
        float n = 1.0f / norm;
        w *= n;
        x *= n;
        y *= n;
        z *= n;
    }

    @Override
    public final WWQuaternion clone() {
        WWQuaternion q = new WWQuaternion();
        q.w = w;
        q.x = x;
        q.y = y;
        q.z = z;
        return q;
    }

    public final void copyInto(WWQuaternion q) {
        q.w = w;
        q.x = x;
        q.y = y;
        q.z = z;
    }

    // return the quaternion conjugate
    public final WWQuaternion conjugate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /**
     * Adds the rotation effects of quaternion q to this quaternion
     * in such a way that it is as if this quaternion had initially
     * been turned by q.
     * (This is the same as quaternion multiplication where
     * the parameter q is the left-hand-side.)
     */
    public final WWQuaternion prerotate(WWQuaternion q) {
        float tw = q.w * this.w - q.x * this.x - q.y * this.y - q.z * this.z;
        float tx = q.w * this.x + q.x * this.w + q.y * this.z - q.z * this.y;
        float ty = q.w * this.y - q.x * this.z + q.y * this.w + q.z * this.x;
        float tz = q.w * this.z + q.x * this.y - q.y * this.x + q.z * this.w;
        w = tw;
        x = tx;
        y = ty;
        z = tz;
        return this;
    }

    /**
     * Adds the rotation effects of quaternion q to this quaternion.
     * (This is the same as quaternion multiplication where
     * the parameter q is the right-hand-side.)
     */
    public final WWQuaternion rotate(WWQuaternion q) {
        WWQuaternion r = this;
        float tw = r.w * q.w - r.x * q.x - r.y * q.y - r.z * q.z;
        float tx = r.w * q.x + r.x * q.w + r.y * q.z - r.z * q.y;
        float ty = r.w * q.y - r.x * q.z + r.y * q.w + r.z * q.x;
        float tz = r.w * q.z + r.x * q.y - r.y * q.x + r.z * q.w;
        w = tw;
        x = tx;
        y = ty;
        z = tz;
        return this;
    }

    /**
     * Converts the rotation into an identical rotation
     * in the opposing direction.
     */
    public final WWQuaternion invert() {
        float normSq = w * w + x * x + y * y + z * z;
        if (normSq < 1e-6f) {
            w = 1.0f;
            x = 0.0f;
            y = 0.0f;
            z = 0.0f;
            return this;
        }
        w = w / normSq;
        x = -x / normSq;
        y = -y / normSq;
        z = -z / normSq;
        return this;
    }

    /**
     * A rotation using the inverse of q. This undoes
     * the rotation performed by rotate(q).
     */
    public final WWQuaternion antirotate(WWQuaternion q) {
        WWQuaternion qi = q.clone().invert();
        rotate(qi);
        return this;
    }

    /**
     * Modifies the vector, rotating it by the rotation in this quaternion.
     */
    public final void rotateVector(WWVector v) {
        if (v.isZero()) {
            return;
        }
        // Standard 3D vector rotation by quaternion v' = v + w*t + q_v x t, where t = 2 * (q_v x v)
        float tx = 2.0f * (y * v.z - z * v.y);
        float ty = 2.0f * (z * v.x - x * v.z);
        float tz = 2.0f * (x * v.y - y * v.x);

        v.x += w * tx + (y * tz - z * ty);
        v.y += w * ty + (z * tx - x * tz);
        v.z += w * tz + (x * ty - y * tx);
    }

    public final void antirotateVector(WWVector v) {
        WWQuaternion q = this.clone().invert();
        q.rotateVector(v);
    }

    /**
     * Interpolates quickly between the current instance and q using
     * nlerp, and stores the result in the current instance.
     *
     * @param q the desired value when blend=1 (not null, unaffected)
     * @param blend the fractional change amount
     */
    public final void nlerp(WWQuaternion q, float blend) {
        float dot = w * q.w + x * q.x + y * q.y + z * q.z;
        float blendI = 1.0f - blend;
        if (dot < 0.0f) {
            x = blendI * x - blend * q.x;
            y = blendI * y - blend * q.y;
            z = blendI * z - blend * q.z;
            w = blendI * w - blend * q.w;
        } else {
            x = blendI * x + blend * q.x;
            y = blendI * y + blend * q.y;
            z = blendI * z + blend * q.z;
            w = blendI * w + blend * q.w;
        }
        normalize();
    }

    public String toString() {
        return "<" + w + ", " + x + ", " + y + ", " + z + ">";
    }

    @Override
    public void send(DataOutputStreamX os) throws IOException {
        os.writeFloat(w);
        os.writeFloat(x);
        os.writeFloat(y);
        os.writeFloat(z);
    }

    @Override
    public void receive(DataInputStreamX is) throws IOException {
        w = is.readFloat();
        x = is.readFloat();
        y = is.readFloat();
        z = is.readFloat();
    }

    public final void toMatrix(float[] mat) {
        float xx = x * x;
        float xy = x * y;
        float xz = x * z;
        float xw = x * w;

        float yy = y * y;
        float yz = y * z;
        float yw = y * w;

        float zz = z * z;
        float zw = z * w;

        // OpenGL column-major matrix converting World space (X, Y=forward, Z=up) to OpenGL space (X, Y=up, Z=depth)
        mat[0] = 1.0f - 2.0f * (yy + zz);
        mat[1] = 2.0f * (xz - yw);
        mat[2] = 2.0f * (xy + zw);
        mat[3] = 0.0f;

        mat[4] = 2.0f * (xz + yw);
        mat[5] = 1.0f - 2.0f * (xx + yy);
        mat[6] = 2.0f * (yz - xw);
        mat[7] = 0.0f;

        mat[8] = 2.0f * (xy - zw);
        mat[9] = 2.0f * (yz + xw);
        mat[10] = 1.0f - 2.0f * (xx + zz);
        mat[11] = 0.0f;

        mat[12] = 0.0f;
        mat[13] = 0.0f;
        mat[14] = 0.0f;
        mat[15] = 1.0f;
    }

}
