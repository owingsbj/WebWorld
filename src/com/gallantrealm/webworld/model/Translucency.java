package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWTranslucency;

public class Translucency extends WWTranslucency {
	private static final long serialVersionUID = 1L;
	
	public long getTranslucencyMask() {
		return super.getInsideColor();
	}
	
	public void setTranslucencyMask(long insideColor) {
		super.setInsideColor((int)(insideColor & 0xFFFFFF));
		super.setInsideTransparency((insideColor >> 24) & 0xFF);
	}
	
}
