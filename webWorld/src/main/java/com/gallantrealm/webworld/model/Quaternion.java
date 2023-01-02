package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWQuaternion;

public class Quaternion extends WWQuaternion {
	private static final long serialVersionUID = 1L;

	public Quaternion() {
		super();
	}

	public Quaternion(float pitch, float roll, float yaw) {
		super(pitch, roll, yaw);
	}

}
