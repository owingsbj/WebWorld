package com.gallantrealm.webworld.model;

import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWMatrix;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWQuaternion;
import com.gallantrealm.myworld.model.WWVector;

public class Behavior extends WWAnimation {

	public Behavior() {
	}

	@Override
	public void getAnimatedPosition(WWObject object, WWVector position, long time) {
	}

	@Override
	public void getAnimatedRotation(WWObject object, WWQuaternion rotation, long time) {
	}

	@Override
	public void preAnimateModelMatrix(WWObject object, WWMatrix modelMatrix, long time) {
	}

	@Override
	public void postAnimateModelMatrix(WWObject object, WWMatrix modelMatrix, long time) {
	}

}
