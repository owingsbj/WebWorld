package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Function;
import com.gallantrealm.myworld.model.WWAnimation;
import com.gallantrealm.myworld.model.WWEntity;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

public class EventBehavior extends WWAnimation {
	private static final long serialVersionUID = 1L;

	public Function onTouch;
	public Function onPress;
	public Function onDrag;
	public Function onRelease;
	public Function onCollide;
	public Function onSlide;
	public Function onStopSlide;
	public Function onTimer;
	public Animation[] animations;
	
	@Override
	public boolean touchEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onTouch != null) {
			World world = (World)object.world;
			world.callFunction(onTouch, object, new Object[] {toucher, side, x, y});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean pressEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onPress != null) {
			World world = (World)object.world;
			world.callFunction(onPress, object, new Object[] {toucher, side, x, y});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean dragEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onDrag != null) {
			World world = (World)object.world;
			world.callFunction(onDrag, object, new Object[] {toucher, side, x, y});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean releaseEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onRelease != null) {
			World world = (World)object.world;
			world.callFunction(onRelease, object, new Object[] {toucher, side, x, y});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onCollide != null) {
			World world = (World)object.world;
			world.callFunction(onCollide, object, new Object[] {nearObject, proximity});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onSlide != null) {
			World world = (World)object.world;
			world.callFunction(onSlide, object, new Object[] {nearObject, proximity});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean stopSlideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onStopSlide != null) {
			World world = (World)object.world;
			world.callFunction(onStopSlide, object, new Object[] {nearObject, proximity});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean timerEvent(WWObject object) {
		if (onTimer != null) {
			World world = (World)object.world;
			world.callFunction(onTimer, object, new Object[] {});
			return true;
		}
		return false;
	}

	@Override
	public void getAnimatedPosition(WWObject object, WWVector position, long time) {
		if (animations != null) {
			for (int i = 0; i < animations.length; i++) {
				animations[i].animatePosition(object, position, time);
			}
		}
	}

	@Override
	public void getAnimatedRotation(WWObject object, WWVector rotation, long time) {
		if (animations != null) {
			for (int i = 0; i < animations.length; i++) {
				animations[i].animateRotation(object, rotation, time);
			}
		}
	}

}
