package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Function;
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
	
	private EventBehavior eventBehavior;
	
	private EventBehavior getEventBehavior() {
		if (eventBehavior == null) {
			eventBehavior = new EventBehavior();
			addBehavior(eventBehavior);
		}
		return eventBehavior;
	}
	
	public void setOnTouch(Function f) {
		getEventBehavior().onTouch = f;
	}

}
