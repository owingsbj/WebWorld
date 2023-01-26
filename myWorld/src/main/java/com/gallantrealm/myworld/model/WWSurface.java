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
public final class WWSurface implements Serializable, Cloneable, Sendable {
	static final long serialVersionUID = 1L;

	private static WWSurface defaultSurface;

	public static WWSurface getDefaultSurface() {
		if (defaultSurface == null) {
			defaultSurface = new WWSurface();
			defaultSurface.isDefault = true;
		}
		return defaultSurface;
	}

	public float red = 1.0f;
	public float green = 1.0f;
	public float blue = 1.0f;
	private float transparency;
	private float shininess = 0.0f;
	private boolean fullBright;
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
	public boolean bumpMap;  // keep, for compatibility..
	private boolean alphaTest;
	public boolean isDefault;
	private boolean pixelate;

	public WWSurface() {
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
		os.writeFloat(red);
		os.writeFloat(green);
		os.writeFloat(blue);
		os.writeFloat(getTransparency());
		os.writeFloat(getShininess());
		os.writeBoolean(isFullBright());
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
		os.writeBoolean(isAlphaTest());
		os.writeBoolean(isPixelate());
	}

	@Override
	public void receive(DataInputStreamX is) throws IOException {
		red = is.readFloat();
		green = is.readFloat();
		blue = is.readFloat();
		setTransparency(is.readFloat());
		setShininess(is.readFloat());
		setFullBright(is.readBoolean());
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
		setAlphaTest(is.readBoolean());
		setPixelate(is.readBoolean());
	}

	public float getTransparency() {
		return transparency;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	public float getShininess() {
		return shininess;
	}

	public void setShininess(float shininess) {
		this.shininess = shininess;
	}

	public boolean isFullBright() {
		return fullBright;
	}

	public void setFullBright(boolean fullBright) {
		this.fullBright = fullBright;
	}

	public boolean isAlphaTest() {
		return alphaTest;
	}

	public void setAlphaTest(boolean alphaTest) {
		this.alphaTest = alphaTest;
	}

	public boolean isPixelate() {
		return pixelate;
	}

	public void setPixelate(boolean pixelate) {
		this.pixelate = pixelate;
	}

	public WWColor getColor() {
		return new WWColor(red, green, blue);
	}

	public void setColor(WWColor color) {
		red = color.getRed();
		green = color.getGreen();
		blue = color.getBlue();
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
		texture.setPixelate(pixelate);
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
		pixelate = texture.isPixelate();
	}

}
