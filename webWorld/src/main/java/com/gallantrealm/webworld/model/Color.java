package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWColor;

public class Color extends WWColor {
	private static final long serialVersionUID = 1L;
	
	public static final Color RED = new Color(0xFF0000);
	public static final Color GREEN = new Color(0x00FF00);
	public static final Color BLUE = new Color(0x0000FF);
	
	public Color() {
		super();
	}
	
	public Color(int rgb) {
		super(rgb);
	}

	public Color(long rgb) {
		super((int)rgb);
	}

	public Color(float red, float green, float blue) {
		super(red, green, blue);
	}

}
