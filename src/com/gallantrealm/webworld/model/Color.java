package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWColor;

public class Color extends WWColor {
	private static final long serialVersionUID = 1L;

	public Color(int rgb) {
		super(rgb);
	}

	public Color(float red, float green, float blue) {
		super(red, green, blue);
	}

}
