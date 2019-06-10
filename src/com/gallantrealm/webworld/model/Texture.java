package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWTexture;

public class Texture extends WWTexture {
	private static final long serialVersionUID = 1L;

	public Texture() {
	}

	public Texture(String url) {
		super(url);
	}

	public Texture(String url, float scaleX, float scaleY) {
		super(url, scaleX, scaleY);
	}

	public Texture(String url, float scaleX, float scaleY, float offsetX, float offsetY) {
		super(url, scaleX, scaleY, offsetX, offsetY);
	}

	public Texture(String url, float scaleX, float scaleY, float offsetX, float offsetY, float rotation) {
		super(url, scaleX, scaleY, offsetX, offsetY, rotation);
	}

}
