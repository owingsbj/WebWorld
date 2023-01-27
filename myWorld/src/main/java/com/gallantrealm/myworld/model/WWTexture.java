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

	private String name;
	private float scaleX = 1.0f;
	private float scaleY = 1.0f;
	private float rotation;
	private float offsetX;
	private float offsetY;
	private float velocityX;
	private float velocityY;
	private float aMomentum;
	private long refreshInterval;
	private boolean pixelate;
	
	public WWTexture() {
	}
	
	public WWTexture(String url) {
		this.setName(url);
	}
	
	public WWTexture(String url, float scaleX, float scaleY) {
		this.setName(url);
		this.setScaleX(scaleX);
		this.setScaleY(scaleY);
	}
	
	public WWTexture(String url, float scaleX, float scaleY, float rotation) {
		this.setName(url);
		this.setScaleX(scaleX);
		this.setScaleY(scaleY);
		this.setRotation(rotation);
	}
	
	public WWTexture(String url, float scaleX, float scaleY, float rotation, float offsetX, float offsetY) {
		this.setName(url);
		this.setScaleX(scaleX);
		this.setScaleY(scaleY);
		this.setRotation(rotation);
		this.setOffsetX(offsetX);
		this.setOffsetY(offsetY);
	}
	
	public WWTexture(String url, float scaleX, float scaleY, float rotation, float offsetX, float offsetY, boolean pixelate) {
		this.setName(url);
		this.setScaleX(scaleX);
		this.setScaleY(scaleY);
		this.setRotation(rotation);
		this.setOffsetX(offsetX);
		this.setOffsetY(offsetY);
		this.setPixelate(pixelate);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
		os.writeString(name);
		os.writeFloat(scaleX);
		os.writeFloat(scaleY);
		os.writeFloat(rotation);
		os.writeFloat(offsetX);
		os.writeFloat(offsetY);
		os.writeFloat(velocityX);
		os.writeFloat(velocityY);
		os.writeFloat(aMomentum);
		os.writeLong(refreshInterval);
		os.writeBoolean(isPixelate());
	}

	@Override
	public void receive(DataInputStreamX is) throws IOException {
		name = is.readString();
		scaleX = is.readFloat();
		scaleY = is.readFloat();
		rotation = is.readFloat();
		offsetX = is.readFloat();
		offsetY = is.readFloat();
		velocityX = is.readFloat();
		velocityY = is.readFloat();
		aMomentum = is.readFloat();
		refreshInterval = is.readLong();
		setPixelate(is.readBoolean());
	}
}
