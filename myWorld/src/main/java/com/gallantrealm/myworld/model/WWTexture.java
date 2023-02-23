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

	public String name;
	public float scaleX = 1.0f;
	public float scaleY = 1.0f;
	public float rotation;
	public float offsetX;
	public float offsetY;
	public float velocityX;
	public float velocityY;
	public float aMomentum;
	public long refreshInterval;
	public boolean pixelated;
	
	public WWTexture() {
	}
	
	public WWTexture(String name) {
		this.name = name;
	}
	
	public WWTexture(String name, float scaleX, float scaleY) {
		this.name = name;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}
	
	public WWTexture(String name, float scaleX, float scaleY, float rotation) {
		this.name = name;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.rotation = rotation;
	}
	
	public WWTexture(String name, float scaleX, float scaleY, float rotation, float offsetX, float offsetY) {
		this.name = name;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.rotation = rotation;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public WWTexture(String name, float scaleX, float scaleY, float rotation, float offsetX, float offsetY, boolean pixelated) {
		this.name = name;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.rotation = rotation;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.pixelated = pixelated;
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
	
	public boolean isPixelated() {
		return pixelated;
	}
	public void setPixelated(boolean pixelate) {
		this.pixelated = pixelate;
	}

	public WWTexture subMap(float totalWidth, float totalHeight, float width, float height,  float offsetX, float offsetY, float rotation) {
		return new WWTexture(name, totalWidth / width, totalHeight / height, rotation, offsetX / width, offsetY / height, pixelated);
	}

	public WWTexture subMap(float totalWidth, float totalHeight, float width, float height,  float offsetX, float offsetY) {
		return subMap(totalWidth, totalHeight, width, height, offsetX, offsetY, 0);
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
		os.writeBoolean(isPixelated());
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
		setPixelated(is.readBoolean());
	}

	@Override
	public String toString() {
		return "Texture<"+getName()+" "+getScaleX()+" "+getScaleY()+" "+" "+getRotation()+" "+getOffsetX()+" "+getOffsetY()+" "+ isPixelated()+">";
	}

}
