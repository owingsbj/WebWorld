package com.gallantrealm.myworld.model;

import java.util.Arrays;

/**
 * An implementation of a 4x4 model matrix. The internal float array, m, is compatible with OpenGL
 * and is in column-major order:
 * <pre>
 *  m[0] m[4] m[8] m[12]
 *  m[1] m[5] m[9] m[13]
 *  m[2] m[6] m[10] m[14]
 *  m[3] m[7] m[11] m[15]
 *  </pre>
 * In this project's World coordinate system:
 * <ul>
 *   <li>World X = Right / East &rarr; mapped to OpenGL Column 0 (m[0..3])</li>
 *   <li>World Z = Up / Height &rarr; mapped to OpenGL Column 1 (m[4..7])</li>
 *   <li>World Y = Forward / North &rarr; mapped to OpenGL Column 2 (m[8..11])</li>
 *   <li>Translation &rarr; mapped to OpenGL Column 3 (m[12..15])</li>
 * </ul>
 */
public class WWMatrix implements Cloneable {

	public float[] m;

	public WWMatrix() {
		m = new float[16];
		m[0] = 1.0f;
		m[5] = 1.0f;
		m[10] = 1.0f;
		m[15] = 1.0f;
	}

	public WWMatrix(float[] m) {
		this.m = m;
	}

	@Override
	public WWMatrix clone() {
		return new WWMatrix(Arrays.copyOf(m, 16));
	}

	public void copyInto(float[] m) {
		System.arraycopy(this.m, 0, m, 0, 16);
	}

	/**
	 * Translates the matrix in World space coordinates (x=World X, y=World Y, z=World Z).
	 */
	public void move(float x, float y, float z) {
		for (int i = 0; i < 4; i++) {
			m[12 + i] += m[i] * x + m[4 + i] * z + m[8 + i] * y;
		}
	}

	public void move(WWVector v) {
		move(v.x, v.y, v.z);
	}

	public void antirotate(WWQuaternion q) {
		float[] qm = new float[16];
		q.clone().invert().toMatrix(qm);
		multiplyMM(m, m, qm);
	}

	public void rotate(WWQuaternion q) {
		float[] qm = new float[16];
		q.toMatrix(qm);
		multiplyMM(m, m, qm);
	}

	/**
	 * Scales the matrix in World space coordinates (x=World X, y=World Y, z=World Z).
	 */
	public void scale(float x, float y, float z) {
		for (int i = 0; i < 4; i++) {
			m[i] *= x;
			m[4 + i] *= z;
			m[8 + i] *= y;
		}
	}

	public void scale(WWVector v) {
		scale(v.x, v.y, v.z);
	}

	public void scale(float s) {
		scale(s, s, s);
	}

	/**
	 * Shears the matrix in World space coordinates along World Z (height).
	 * x = shear factor along World X per unit height
	 * y = shear factor along World Y per unit height
	 * z = shear factor along World Z per unit horizontal distance
	 */
	public void shear(float x, float y, float z) {
		for (int i = 0; i < 4; i++) {
			m[4 + i] += x * m[i] + y * m[8 + i];
			if (z != 0.0f) {
				m[i] += z * m[4 + i];
			}
		}
	}

	public void shear(WWVector v) {
		shear(v.x, v.y, v.z);
	}

	public void taper(WWVector v) {
		taper(v.x, v.y, v.z);
	}

	/**
	 * Tapers the matrix in World space coordinates using projective scaling.
	 * @param x taper rate along World Z (height)
	 * @param y taper rate along World Y (forward)
	 * @param z taper rate along World X (right)
	 */
	public void taper(float x, float y, float z) {
		for (int i = 0; i < 4; i++) {
			m[4 + i] += x * m[12 + i];
			if (y != 0.0f) {
				m[8 + i] += y * m[12 + i];
			}
			if (z != 0.0f) {
				m[i] += z * m[12 + i];
			}
		}
	}

	/**
	 * Multiplies two 4x4 column-major matrices: result = a * b.
	 */
	public static void multiplyMM(float[] result, float[] a, float[] b) {
		float b00 = b[0],  b10 = b[1],  b20 = b[2],  b30 = b[3];
		float b01 = b[4],  b11 = b[5],  b21 = b[6],  b31 = b[7];
		float b02 = b[8],  b12 = b[9],  b22 = b[10], b32 = b[11];
		float b03 = b[12], b13 = b[13], b23 = b[14], b33 = b[15];

		float a00 = a[0],  a10 = a[1],  a20 = a[2],  a30 = a[3];
		float a01 = a[4],  a11 = a[5],  a21 = a[6],  a31 = a[7];
		float a02 = a[8],  a12 = a[9],  a22 = a[10], a32 = a[11];
		float a03 = a[12], a13 = a[13], a23 = a[14], a33 = a[15];

		result[0]  = a00 * b00 + a01 * b10 + a02 * b20 + a03 * b30;
		result[1]  = a10 * b00 + a11 * b10 + a12 * b20 + a13 * b30;
		result[2]  = a20 * b00 + a21 * b10 + a22 * b20 + a23 * b30;
		result[3]  = a30 * b00 + a31 * b10 + a32 * b20 + a33 * b30;

		result[4]  = a00 * b01 + a01 * b11 + a02 * b21 + a03 * b31;
		result[5]  = a10 * b01 + a11 * b11 + a12 * b21 + a13 * b31;
		result[6]  = a20 * b01 + a21 * b11 + a22 * b21 + a23 * b31;
		result[7]  = a30 * b01 + a31 * b11 + a32 * b21 + a33 * b31;

		result[8]  = a00 * b02 + a01 * b12 + a02 * b22 + a03 * b32;
		result[9]  = a10 * b02 + a11 * b12 + a12 * b22 + a13 * b32;
		result[10] = a20 * b02 + a21 * b12 + a22 * b22 + a23 * b32;
		result[11] = a30 * b02 + a31 * b12 + a32 * b22 + a33 * b32;

		result[12] = a00 * b03 + a01 * b13 + a02 * b23 + a03 * b33;
		result[13] = a10 * b03 + a11 * b13 + a12 * b23 + a13 * b33;
		result[14] = a20 * b03 + a21 * b13 + a22 * b23 + a23 * b33;
		result[15] = a30 * b03 + a31 * b13 + a32 * b23 + a33 * b33;
	}

}
