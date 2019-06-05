package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Function;
import com.gallantrealm.myworld.model.WWBox;

public class Box extends WWBox {
	private static final long serialVersionUID = 1L;
	
	private EventBehavior eventBehavior;
	
	private EventBehavior getEventBehavior() {
		if (eventBehavior == null) {
			eventBehavior = new EventBehavior();
		}
		return eventBehavior;
	}
	
	public void setOnTouch(Function f) {
		getEventBehavior().onTouch = f;
	}

}
