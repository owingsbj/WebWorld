package com.gallantrealm.myworld.model;

import java.io.IOException;
import java.io.Serializable;
import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import com.gallantrealm.myworld.communication.Sendable;

/**
 * Represents a surface on an object.  A surface has color, texture and several
 * other properties that determine the rendering of the surface.
 */
public final class SideAttributes implements Serializable, Cloneable, Sendable {
	static final long serialVersionUID = 1L;

	public static SideAttributes defaultSurface;

	static {
		defaultSurface = new SideAttributes();
		defaultSurface.isDefault = true;
	}

	public WWColor color = new WWColor();
	public float transparency;
	public float shininess = 0.0f;
	public boolean fullBright;
	public WWTexture texture = new WWTexture();
	public boolean isDefault;

	public SideAttributes() {
	}

	@Override
	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void send(DataOutputStreamX os) throws IOException {
		os.writeKnownObject(color);
		os.writeFloat(transparency);
		os.writeFloat(shininess);
		os.writeBoolean(fullBright);
		os.writeKnownObject(texture);
	}

	@Override
	public void receive(DataInputStreamX is) throws IOException {
		color = (WWColor)is.readKnownObject(WWColor.class);
		transparency = is.readFloat();
		shininess = is.readFloat();
		fullBright = is.readBoolean();
		texture = (WWTexture)is.readKnownObject(WWTexture.class);
	}

	public WWColor getColor() {
		return color;
	}

	public void setColor(WWColor color) {
		this.color = color;
	}

	public WWTexture getTexture() {
		return texture;
	}

	public void setTexture(WWTexture texture) {
		this.texture = texture;
	}

}
