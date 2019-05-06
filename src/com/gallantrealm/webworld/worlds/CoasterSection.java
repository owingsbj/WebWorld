package com.gallantrealm.webworld.worlds;

import java.io.Serializable;

public class CoasterSection implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum SectionType {
		STATION, CHAIN, STRAIGHT, LEFT_TURN, RIGHT_TURN, CURVE_UP, CURVE_DOWN
	}

	public SectionType type;
	public float x, y, z;
	public float direction;
	public float slope;
	public int objectCount;

	public CoasterSection(SectionType type, float x, float y, float z, float direction, float slope) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.z = z;
		this.direction = direction;
		this.slope = slope;
	}

}
