package com.gallantrealm.myworld.model;

import android.opengl.Matrix;

import java.util.Arrays;

/**
 * An implementation of a 4x4 model matrix.  The internal float array, m, is compatible with OpenGL
 * and is in column-major order:
 * <pre>
 *  m[0] m[4] m[8] m[12]
 *  m[1] m[5] m[9] m[13]
 *  m[2] m[6] m[10] m[14]
 *  m[3] m[7] m[11] m[15]
 *  </pre>
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
	
	public WWMatrix clone() {
		return new WWMatrix(Arrays.copyOf(m, 16));
	}

	public void copyInto(float[] m) {
		System.arraycopy(this.m, 0, m, 0, 16);
	}

	public void move(float x, float y, float z) {
		for (int i=0 ; i<4 ; i++) {
			m[12 + i] += m[i] * x + m[4 + i] * z + m[8 + i] * y;
		}
	}

	public void move(WWVector v) {
		move(v.x, v.y, v.z);
	}

	public void antirotate(WWQuaternion q) {
		float[] qm = new float[16];
		q.clone().invert().toMatrix(qm);
		Matrix.multiplyMM(m, 0, m, 0, qm, 0);
	}

	public void rotate(WWQuaternion q) {
		float[] qm = new float[16];
		q.toMatrix(qm);
		Matrix.multiplyMM(m, 0, m, 0, qm, 0);
	}

	public void scale(float x, float y, float z) {
		for (int i=0 ; i<4 ; i++) {
			m[i] *= x;
			m[4 + i] *= z;
			m[8 + i] *= y;
		}
	}

	public void scale(WWVector v) {
		scale(v.x, v.y, v.z);
	}

	public void shear(WWVector v) {
		// todo
	}

	public void taper(WWVector v) {
		// todo
	}

}
