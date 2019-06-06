package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Function;
import com.gallantrealm.myworld.model.WWMesh;

public class Mesh extends WWMesh {
	private static final long serialVersionUID = 1L;
	
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
