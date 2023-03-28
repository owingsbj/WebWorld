package com.gallantrealm.myworld.android.renderer;

import com.gallantrealm.myworld.client.renderer.IRendering;

public abstract class GLRendering implements IRendering {

	public abstract void snap(long worldTime);
	
	public abstract void draw(Shader shader, float[] viewMatrix, long worldTime, int drawType, boolean transparency, int lod);

	public abstract void updateRendering();
}
