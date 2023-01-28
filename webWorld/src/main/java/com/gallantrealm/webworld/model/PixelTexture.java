package com.gallantrealm.webworld.model;

public class PixelTexture extends Texture {

	public PixelTexture(String name, int totalWidth, int totalHeight, int width, int height, float rotation,  int offsetX, int offsetY) {
		super(name, totalWidth / (float)width, totalHeight / (float)height, rotation, width / (float)offsetX, height / (float)offsetY, true);
	}
}
