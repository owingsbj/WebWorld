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
	public String textureURL;
	public float textureScaleX = 1.0f;
	public float textureScaleY = 1.0f;
	public float textureRotation;
	public float textureOffsetX = 0.5f;
	public float textureOffsetY = 0.5f;
	public float textureVelocityX;
	public float textureVelocityY;
	public float textureAMomentum;
	public long textureRefreshInterval;
	public boolean texturePixelated;
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
		os.writeString(textureURL);
		os.writeFloat(textureScaleX);
		os.writeFloat(textureScaleY);
		os.writeFloat(textureRotation);
		os.writeFloat(textureOffsetX);
		os.writeFloat(textureOffsetY);
		os.writeFloat(textureVelocityX);
		os.writeFloat(textureVelocityY);
		os.writeFloat(textureAMomentum);
		os.writeLong(textureRefreshInterval);
		os.writeBoolean(texturePixelated);
	}

	@Override
	public void receive(DataInputStreamX is) throws IOException {
		color = (WWColor)is.readKnownObject(WWColor.class);
		transparency = is.readFloat();
		shininess = is.readFloat();
		fullBright = is.readBoolean();
		textureURL = is.readString();
		textureScaleX = is.readFloat();
		textureScaleY = is.readFloat();
		textureRotation = is.readFloat();
		textureOffsetX = is.readFloat();
		textureOffsetY = is.readFloat();
		textureVelocityX = is.readFloat();
		textureVelocityY = is.readFloat();
		textureAMomentum = is.readFloat();
		textureRefreshInterval = is.readLong();
		texturePixelated = is.readBoolean();
	}

	public WWColor getColor() {
		return color;
	}

	public void setColor(WWColor color) {
		this.color = color;
	}

	public WWTexture getTexture() {
		WWTexture texture = new WWTexture();
		texture.setName(textureURL);
		texture.setScaleX(textureScaleX);
		texture.setScaleY(textureScaleY);
		texture.setRotation(textureRotation);
		texture.setOffsetX(textureOffsetX - 0.5f);
		texture.setOffsetY(textureOffsetY - 0.5f);
		texture.setVelocityX(textureVelocityX);
		texture.setVelocityY(textureVelocityY);
		texture.setaMomentum(textureAMomentum);
		texture.setRefreshInterval(textureRefreshInterval);
		texture.setPixelated(texturePixelated);
		return texture;
	}

	public void setTexture(WWTexture texture) {
		textureURL = texture.getName();
		textureScaleX = texture.getScaleX();
		textureScaleY = texture.getScaleY();
		textureRotation = texture.getRotation();
		textureOffsetX = texture.getOffsetX() + 0.5f;
		textureOffsetY = texture.getOffsetY() + 0.5f;
		textureVelocityX = texture.getVelocityX();
		textureVelocityY = texture.getVelocityY();
		textureAMomentum = texture.getaMomentum();
		textureRefreshInterval = texture.getRefreshInterval();
		texturePixelated = texture.isPixelated();
	}

}
