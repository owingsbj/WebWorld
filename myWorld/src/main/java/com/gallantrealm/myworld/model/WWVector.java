package com.gallantrealm.myworld.model;

import java.io.IOException;
import java.io.Serializable;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import com.gallantrealm.myworld.communication.Sendable;

/**
 * This class provides a simple fixed vector (starts at the origin). It is used for position, size, velocity, angular velocity, acceleration, torque.
 * It is NOT used for rotation -- see WWQuaternion for that.
 */
public class WWVector implements Cloneable, Serializable, Sendable {
	static final long serialVersionUID = 1L;

	public static final float TORADIAN = 0.0174532925f;
	public static final float TODEGREES = 57.29577866f;

	public static final WWVector ZERO_VECTOR = new WWVector(0, 0, 0);

	public float x;
	public float y;
	public float z;

	public WWVector() {
	}

	public WWVector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public WWVector(WWVector v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WWVector)) {
			return false;
		}
		WWVector v = (WWVector) obj;
		return x == v.x && y == v.y && z == v.z;
	}

	@Override
	public final String toString() {
		//String formattedx = String.format("%+.4f", x);
		//String formattedy = String.format("%+.4f", y);
		//String formattedz = String.format("%+.4f", z);
		//return "<" + formattedx + "," + formattedy + "," + formattedz + ">";
		return "<" + x + ", " + y + ", " + z + ">";
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

	/**
	 * Determine the distance between this vector and another, assuming the vectors represent points in threespace.
	 */
	public final float distanceFrom(WWVector v) {
		float dx = v.x - x;
		float dy = v.y - y;
		float dz = v.z - z;
		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public final WWVector subtract(WWVector v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}

	public final WWVector subtract(float vx, float vy, float vz) {
		x -= vx;
		y -= vy;
		z -= vz;
		return this;
	}

	/**
	 * Cross product another vector with this one.
	 */
	public final WWVector cross(WWVector v) {
		float i = y * v.z - z * v.y;
		float j = z * v.x - x * v.z;
		float k = x * v.y - y * v.x;
		x = i;
		y = j;
		z = k;
		return this;
	}

	public final WWVector cross(float vx, float vy, float vz) {
		float i = y * vz - z * vy;
		float j = z * vx - x * vz;
		float k = x * vy - y * vx;
		x = i;
		y = j;
		z = k;
		return this;
	}

	public final float dot(WWVector v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/**
	 * Get the pan needed to reach the point.
	 */
	public final float getPan() {
		return (float) Math.toDegrees(Math.atan2(x, y));
	}

	/**
	 * Get the tilt needed to reach the point.
	 */
	public final float getTilt() {
		return (float) Math.toDegrees(Math.atan2(z, Math.sqrt(x * x + y * y)));
	}

	/**
	 * Returns the reflection of the vector, given a vector normal to the mirror of reflection.
	 */
	public final WWVector getReflection(WWVector mirror) {
		WWVector mirrorNormal = mirror.clone();
		mirrorNormal.normalize();
		float length = this.length();
		WWVector normal = this.clone();
		normal.normalize();
		WWVector reflection = new WWVector((normal.x + 2 * mirrorNormal.x), (normal.y + 2 * mirrorNormal.y), (normal.z + 2 * mirrorNormal.z));
		reflection.normalize();
		reflection.scale(length);
		return reflection;
	}

	/**
	 * Normalizes this vector.
	 */
	public final WWVector normalize() {
//		if (free) {
//			throw new RuntimeException("Attempting to use an already freed vector");
//		}
		float length = length();
		if (length <= 0.000001) {
			x = 0;
			y = 0;
			z = 0;
			return this;
		}
		x = x / length;
		y = y / length;
		z = z / length;
		return this;
	}

	/**
	 * Returns the distance from the origin of the point represented by the vector.
	 */
	public final float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Add the value of another vector to this vector.
	 */
	public final WWVector add(WWVector vector) {
		x += vector.x;
		y += vector.y;
		z += vector.z;
		return this;
	}

	public final WWVector add(float vx, float vy, float vz) {
		x += vx;
		y += vy;
		z += vz;
		return this;
	}

	/**
	 * Averages another vector into this vector.
	 */
	public final WWVector avg(WWVector vector) {
		x = (x + vector.x) / 2;
		y = (y + vector.y) / 2;
		z = (z + vector.z) / 2;
		return this;
	}

	public final WWVector scale(float s) {
		x *= s;
		y *= s;
		z *= s;
		return this;
	}

	public final WWVector scale(float sx, float sy, float sz) {
		x *= sx;
		y *= sy;
		z *= sz;
		return this;
	}
	
	public final WWVector scale(WWVector v) {
		x *= v.x;
		y *= v.y;
		z *= v.z;
		return this;
	}

	public final WWVector negate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public final WWVector set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public final WWVector setX(float x) {
		this.x = x;
		return this;
	}

	public final WWVector setY(float y) {
		this.y = y;
		return this;
	}

	public final WWVector setZ(float z) {
		this.z = z;
		return this;
	}

	@Override
	public final WWVector clone() {
		return new WWVector(x, y, z);
	}

	public final void copyInto(WWVector v) {
		v.x = x;
		v.y = y;
		v.z = z;
	}

	public final WWVector zero() {
		x = 0;
		y = 0;
		z = 0;
		return this;
	}

	public final boolean isZero() {
		return x == 0 && y == 0 && z == 0;
	}

	public final boolean isLongerThan(WWVector v) {
		return x * x + y * y + z * z > v.x * v.x + v.y * v.y + v.z * v.z;
	}

	/**
	 * Reduce the length of the vector by the amount specified.
	 */
	public final WWVector reduce(float reduction) {
		float length = length();
		if (length == 0.0f) {
			return this;
		}
		float newLength = FastMath.max(0.0f, length - reduction);
		normalize();
		x = x * newLength;
		y = y * newLength;
		z = z * newLength;
		return this;
	}

	/**
	 * Average-in another vector by the percentage amount. (Using 0.5 as the amount gives an even average.)
	 */
	public final WWVector average(WWVector v, float amount) {
		x = x * (1.0f - amount) + v.x * amount;
		y = y * (1.0f - amount) + v.y * amount;
		z = z * (1.0f - amount) + v.z * amount;
		return this;
	}

	public final WWVector average(WWVector v, WWVector amount) {
		x = x * (1.0f - amount.x) + v.x * amount.x;
		y = y * (1.0f - amount.y) + v.y * amount.y;
		z = z * (1.0f - amount.z) + v.z * amount.z;
		return this;
	}

	public final WWVector rotate(WWQuaternion q) {
		q.rotateVector(this);
		return this;
	}

	public final WWVector antirotate(WWQuaternion q) {
		q.clone().invert().rotateVector(this);
		return this;
	}

	public final void send(DataOutputStreamX os) throws IOException {
//		if (free) {
//			throw new RuntimeException("Attempting to use an already freed vector");
//		}
		os.writeFloat(x);
		os.writeFloat(y);
		os.writeFloat(z);
	}

	public final void receive(DataInputStreamX is) throws IOException {
//		if (free) {
//			throw new RuntimeException("Attempting to use an already freed vector");
//		}
		x = is.readFloat();
		y = is.readFloat();
		z = is.readFloat();
	}

}
