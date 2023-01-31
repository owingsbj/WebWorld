package com.gallantrealm.myworld.model;

import com.gallantrealm.myworld.client.renderer.IRenderer;
import com.gallantrealm.myworld.communication.DataInputStreamX;
import com.gallantrealm.myworld.communication.DataOutputStreamX;

import java.io.IOException;

/**
 * Just like a box, but with bevelled sides
 */
public class WWRoundedBox extends WWBox {

	private boolean roundSides;
	private boolean roundTop;
	private boolean roundBottom;

	public void createRendering(IRenderer renderer, long worldTime) {
		rendering = renderer.createBevelledBoxRendering(this, worldTime);
	}

	@Override
	public void send(DataOutputStreamX os) throws IOException {
		os.writeBoolean(isRoundSides());
		os.writeBoolean(isRoundTop());
		os.writeBoolean(isRoundBottom());
		super.send(os);
	}

	@Override
	public void receive(DataInputStreamX is) throws IOException {
		setRoundSides(is.readBoolean());
		setRoundTop(is.readBoolean());
		setRoundBottom(is.readBoolean());
		super.receive(is);
	}

	public boolean isRoundSides() {
		return roundSides;
	}

	public void setRoundSides(boolean roundSides) {
		this.roundSides = roundSides;
	}

	public boolean isRoundTop() {
		return roundTop;
	}

	public void setRoundTop(boolean roundTop) {
		this.roundTop = roundTop;
	}

	public boolean isRoundBottom() {
		return roundBottom;
	}

	public void setRoundBottom(boolean roundBottom) {
		this.roundBottom = roundBottom;
	}
}
