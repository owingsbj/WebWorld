package com.gallantrealm.webworld.model;

import java.io.Serializable;
import java.util.HashMap;

import com.gallantrealm.myworld.client.model.ClientModel;
import com.gallantrealm.myworld.model.WWEntity;

public class AvatarCustomization extends WWEntity {
	private static final long serialVersionUID = 1L;
	
	public static AvatarCustomization createOrRestore(String avatarName) {
		AvatarCustomization avatarCustomization = (AvatarCustomization)ClientModel.getClientModel().loadObject(avatarName + ".customization");
		if (avatarCustomization == null) {
			avatarCustomization = new AvatarCustomization(avatarName);
		}
		return avatarCustomization;
	}
	
	String avatarName;

	public AvatarCustomization(String avatarName) {
		this.avatarName = avatarName;
	}
	
	/** Overridden to also serialize and save */
	public void setCustomProperty(String key, Serializable value) {
		super.setCustomProperty(key, value);
		ClientModel.getClientModel().saveObject(this, avatarName+".customization");
	}

}
