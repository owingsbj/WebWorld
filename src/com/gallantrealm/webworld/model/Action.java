package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Function;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.model.WWAction;

public class Action extends WWAction {
	private static final long serialVersionUID = 1L;

	private String name;
	private String image;
	private Function onStart;
	private Function onRepeat;
	private Function onStop;

	public Action(String name) {
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImage() {
		return image;
	}

	public void setOnStart(Function onStart) {
		this.onStart = onStart;
	}

	public Function getOnStart() {
		return onStart;
	}

	public void setOnRepeat(Function onRepeat) {
		this.onRepeat = onRepeat;
	}

	public Function getOnRepeat() {
		return onRepeat;
	}

	public void setOnStop(Function onStop) {
		this.onStop = onStop;
	}

	public Function getOnStop() {
		return onStop;
	}

	@Override
	public void start(float x, float y) {
		if (onStart != null) {
			((World) AndroidClientModel.getClientModel().world).callFunction(onStart, this, new Object[] { x, y });
		}
	}

	@Override
	public void repeat(float x, float y) {
		if (onRepeat != null) {
			((World) AndroidClientModel.getClientModel().world).callFunction(onRepeat,  this, new Object[] {x, y});
		}
	}

	@Override
	public void stop() {
		if (onStop != null) {
			((World) AndroidClientModel.getClientModel().world).callFunction(onStop,  this, new Object[] {});
		}
	}

}
