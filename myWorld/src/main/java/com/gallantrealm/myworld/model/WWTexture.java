package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;
import com.gallantrealm.myworld.communication.Sendable;

import java.io.IOException;
import java.io.Serializable;

/**
 * A collection of all properties related to a texture.  For ease in setting and manipuating textures.
 */
public class WWTexture implements Serializable, Cloneable, Sendable {
	private static final long serialVersionUID = 1L;

	private static WWTexture defaultTexture;
	static {
		defaultTexture = new WWTexture();
		defaultTexture.isDefault = true;
	}

	public static WWTexture getDefaultTexture() {
		return defaultTexture;
	}

	public boolean isDefault;
	public float red = 1.0f;
	public float green = 1.0f;
	public float blue = 1.0f;
	public float transparency;
	public float shininess = 0.0f;
	public boolean fullBright;
	public String url;
	public float scaleX = 1.0f;
	public float scaleY = 1.0f;
	public float rotation;
	public float offsetX;
	public float offsetY;
	public float velocityX;
	public float velocityY;
	public float aMomentum;
	public long refreshInterval;
	public boolean alphaTest;
	public boolean pixelate;

	public WWTexture() {
	}
	
	public WWTexture(String url) {
		this.url = url;
	}
	
	public WWTexture(String url, float scaleX, float scaleY) {
		this.url = url;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}
	
	public WWTexture(String url, float scaleX, float scaleY, float rotation) {
		this.url = url;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.rotation = rotation;
	}
	
	public WWTexture(String url, float scaleX, float scaleY, float rotation, float offsetX, float offsetY) {
		this.url = url;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.rotation = rotation;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public WWTexture(String url, float scaleX, float scaleY, float rotation, float offsetX, float offsetY, boolean pixelate) {
		this.url = url;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.rotation = rotation;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.pixelate = pixelate;
	}
	
	public String getName() {
		return url;
	}
	public void setName(String name) {
		this.url = name;
	}
	
	public float getScaleX() {
		return scaleX;
	}
	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}
	
	public float getScaleY() {
		return scaleY;
	}
	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}
	
	public float getRotation() {
		return rotation;
	}
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
	public float getOffsetX() {
		return offsetX;
	}
	public void setOffsetX(float offsetX) {
		this.offsetX = offsetX;
	}
	
	public float getOffsetY() {
		return offsetY;
	}
	public void setOffsetY(float offsetY) {
		this.offsetY = offsetY;
	}
	
	public float getVelocityX() {
		return velocityX;
	}
	public void setVelocityX(float velocityX) {
		this.velocityX = velocityX;
	}
	
	public float getVelocityY() {
		return velocityY;
	}
	public void setVelocityY(float velocityY) {
		this.velocityY = velocityY;
	}
	
	public float getaMomentum() {
		return aMomentum;
	}
	public void setaMomentum(float aMomentum) {
		this.aMomentum = aMomentum;
	}
	
	public long getRefreshInterval() {
		return refreshInterval;
	}
	public void setRefreshInterval(long refreshInterval) {
		this.refreshInterval = refreshInterval;
	}
	
	public boolean isPixelate() {
		return pixelate;
	}
	public void setPixelate(boolean pixelate) {
		this.pixelate = pixelate;
	}

	public void setColor(WWColor color) {
		this.red = color.getRed();
		this.green = color.getGreen();
		this.blue = color.getBlue();
	}

	public WWColor getColor() {
		return new WWColor(red, green, blue);
	}
	
	@Override
	public Object clone() {
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
		os.writeFloat(transparency);
		os.writeFloat(shininess);
		os.writeBoolean(fullBright);
		os.writeString(url);
		os.writeFloat(scaleX);
		os.writeFloat(scaleY);
		os.writeFloat(rotation);
		os.writeFloat(offsetX);
		os.writeFloat(offsetY);
		os.writeFloat(velocityX);
		os.writeFloat(velocityY);
		os.writeFloat(aMomentum);
		os.writeLong(refreshInterval);
		os.writeBoolean(alphaTest);
		os.writeBoolean(pixelate);
	}

	@Override
	public void receive(DataInputStreamX is) throws IOException {
		red = is.readFloat();
		green = is.readFloat();
		blue = is.readFloat();
		transparency = is.readFloat();
		shininess = is.readFloat();
		fullBright = is.readBoolean();
		url = is.readString();
		scaleX = is.readFloat();
		scaleY = is.readFloat();
		rotation = is.readFloat();
		offsetX = is.readFloat();
		offsetY = is.readFloat();
		velocityX = is.readFloat();
		velocityY = is.readFloat();
		aMomentum = is.readFloat();
		refreshInterval = is.readLong();
		alphaTest = is.readBoolean();
		pixelate = is.readBoolean();
	}

}
