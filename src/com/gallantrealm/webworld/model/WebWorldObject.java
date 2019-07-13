package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Function;

public interface WebWorldObject {
	public void setOnTouch(Function f);
	public Function getOnTouch();
	public void setOnPress(Function f);
	public Function getOnPress();
	public void setOnDrag(Function f);
	public Function getOnDrag();
	public void setOnRelease(Function f);
	public Function getOnRelease();
	public void setOnCollide(Function f);
	public Function getOnCollide();
	public void setOnSlide(Function f);
	public Function getOnSlide();
	public void setOnStopSlide(Function f);
	public Function getOnStopSlide();
	public void setTimer(int millis);
	public int getTimer();
	public void setOnTimer(Function f);
	public Function getOnTimer();
	public void animate(String type);
	public void animate(String type, float speed, float range);
	public void stopAnimation();
}
