package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.model.WWTexture;

public class Texture extends WWTexture {
	private static final long serialVersionUID = 1L;

	public Texture() {
	}

	public Texture(String url) {
		super(worldPrefixUrl(url));
	}

	public Texture(String url, float scaleX, float scaleY) {
		super(worldPrefixUrl(url), scaleX, scaleY);
	}

	public Texture(String url, float scaleX, float scaleY, float rotation) {
		super(worldPrefixUrl(url), scaleX, scaleY, rotation);
	}

	public Texture(String url, float scaleX, float scaleY, float rotation, float offsetX, float offsetY) {
		super(worldPrefixUrl(url), scaleX, scaleY, rotation, offsetX, offsetY);
	}

	private static String worldPrefixUrl(String url) {
		if (url != null) {
			if (!url.contains(":") && url.contains(".")) { // a file in the world or avatar
				if (World.runningAvatarScript) {
					String avatarName = AndroidClientModel.getClientModel().getAvatarName();
					return "http://gallantrealm.com/webworld/avatars/" + avatarName + "/" + url;
				} else {
					String worldName = AndroidClientModel.getClientModel().getWorldName();
					return "http://gallantrealm.com/webworld/worlds/" + worldName + "/" + url;
				}
			}
		}
		return url;
	}

	@Override
	public void setName(String name) {
		super.setName(worldPrefixUrl(name));
	}

}
