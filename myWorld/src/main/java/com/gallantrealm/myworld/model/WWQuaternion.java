package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import com.gallantrealm.myworld.communication.Sendable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Used for representing rotation.  A quarternion has four values that allow the rotation of an object to be represented
 * in a fashion suitable for arithmetic with other rotations, avoiding the 'gimble lock' problem.
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
     * Constructor taking euler values (in degrees).  The values
     * are applied pitch first, then roll, then yaw.
     */
    public WWQuaternion(float pitch, float roll, float yaw) {
        set(pitch, roll, yaw);
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
        y = cosXsinYcosZ + sinXcosYsinZ;
        z = cosXcosYsinZ - sinXsinYcosZ;

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
        if (mag < 0.01) {
            return;
        }
        x = x / mag;
        y = y / mag;
        z = z / mag;
        if (angle < 0.01) {
            return;
        }
        float halfAngle = 0.5f * angle;
        float sin = FastMath.sin(halfAngle);
        this.w = FastMath.cos(halfAngle);
        this.x = sin * x;
        this.y = sin * y;
        this.z = sin * z;
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
        float roll = (float)Math.asin(2 * (x * z + y * w) / (sqx + sqz + sqy + sqw));
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
        if (norm == 0) {
            w = 1.0f; // reset back to zero'd quarternion
        }
        float n = (float)(1.0 / Math.sqrt(norm));
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
        w = w;
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

// Note: Add and subtract aren't very useful for quaternions
// that represent rotations, so not enabling them.
//
//    public final WWQuaternion add(WWQuaternion b) {
//        w += b.w;
//        x += b.x;
//        y += b.y;
//        z += b.z;
//        return this;
//    }
//
//    public final WWQuaternion subtract(WWQuaternion b) {
//        w -= b.w;
//        x -= b.x;
//        y -= b.y;
//        z -= b.z;
//        return this;
//    }

    /**
     * Adds the rotation effects of quarternion q to this quaternion
     * in such a way that it is as if this quarternion had initially
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
     * Adds the rotation effects of quarternion q to this quaternion.
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
        float i = w * w + x * x + y * y + z * z;
        w = w / i;
        x = -x / i;
        y = -y / i;
        z = -z / i;
        return this;
    }

    /**
     * A rotation using the inverse of q.  This undoes
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
        float w = this.w, x = this.x, y = this.z, z = this.y;
        float vx = v.x, vy = v.z, vz = v.y;
        v.x = w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x * vx + 2 * y * x * vy + 2 * z * x * vz - z * z * vx - y * y * vx;
        v.z = 2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w * z * vx - z * z * vy + w * w * vy - 2 * x * w * vz - x * x * vy;
        v.y = 2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w * y * vx - y * y * vz + 2 * w * x * vy - x * x * vz + w * w * vz;
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

        // the matrix has y and z switched (opengl convention)
        mat[0] = 1 - 2 * (yy + zz);
        mat[2] = 2 * (xy - zw);
        mat[1] = 2 * (xz + yw);
        mat[3] = 0;

        mat[8] = 2 * (xy + zw);
        mat[10] = 1 - 2 * (xx + zz);
        mat[9] = 2 * (yz - xw);
        mat[11] = 0;

        mat[4] = 2 * (xz - yw);
        mat[6] = 2 * (yz + xw);
        mat[5] = 1 - 2 * (xx + yy);
        mat[7] = 0;

        mat[12] = 0;
        mat[13] = 0;
        mat[14] = 0;
        mat[15] = 1;
    }

}
