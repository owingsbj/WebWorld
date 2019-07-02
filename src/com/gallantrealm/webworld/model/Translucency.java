package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Function;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWTranslucency;

public class Translucency extends WWTranslucency {
	private static final long serialVersionUID = 1L;
	
	public WWColor getTranslucencyMask() {
		return new WWColor(super.getInsideColor());
	}
	
	public void setTranslucencyMask(WWColor insideColor) {
		super.setInsideColor(insideColor.color & 0xFFFFFF);
		super.setInsideTransparency((insideColor.color >> 24) & 0xFF);
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
	
	public Function getOnTouch() {
		return getEventBehavior().onTouch;
	}
	
	public void setOnPress(Function f) {
		getEventBehavior().onPress = f;
	}
	
	public Function getOnPress() {
		return getEventBehavior().onPress;
	}

	public void setOnDrag(Function f) {
		getEventBehavior().onDrag = f;
	}
	
	public Function getOnDrag() {
		return getEventBehavior().onDrag;
	}

	public void setOnRelease(Function f) {
		getEventBehavior().onRelease = f;
	}
	
	public Function getOnRelease() {
		return getEventBehavior().onRelease;
	}

	public void setOnCollide(Function f) {
		getEventBehavior().onCollide = f;
	}
	
	public Function getOnCollide() {
		return getEventBehavior().onCollide;
	}

	public void setOnSlide(Function f) {
		getEventBehavior().onSlide = f;
	}
	
	public Function getOnSlide() {
		return getEventBehavior().onSlide;
	}

	public void setOnStopSlide(Function f) {
		getEventBehavior().onStopSlide = f;
	}
	
	public Function getOnStopSlide() {
		return getEventBehavior().onStopSlide;
	}

	public void setTimer(int millis) {
		getEventBehavior().setTimer(millis);
	}
	
	public int getTimer() {
		return getEventBehavior().getTimer();
	}

	public void setOnTimer(Function f) {
		getEventBehavior().onTimer = f;
	}
	
	public Function getOnTimer() {
		return getEventBehavior().onTimer;
	}

	public void setAnimations(Animation[] animations) {
		getEventBehavior().animations = animations;
	}

	public Animation[] getAnimations() {
		return getEventBehavior().animations;
	}
	
}
