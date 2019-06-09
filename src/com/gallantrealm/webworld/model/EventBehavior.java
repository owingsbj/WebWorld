package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWEntity;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

public class EventBehavior extends WWBehavior {
	private static final long serialVersionUID = 1L;

	public Function onTouch;
	public Function onPress;
	public Function onDrag;
	public Function onRelease;
	public Function onCollide;
	public Function onSlide;
	public Function onStopSlide;
	public Function onTimer;
	
	@Override
	public boolean touchEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onTouch != null) {
			World world = (World)object.world;
			Scriptable scriptableToucher;
			if (toucher != null) {
				scriptableToucher = Context.toObject(toucher, world.scope);
			} else {
				scriptableToucher = null;
			}
			world.callFunction(onTouch, object, new Object[] {scriptableToucher, side, x, y});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean pressEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onPress != null) {
			World world = (World)object.world;
			Scriptable scriptableToucher;
			if (toucher != null) {
				scriptableToucher = Context.toObject(toucher, world.scope);
			} else {
				scriptableToucher = null;
			}
			world.callFunction(onPress, object, new Object[] {scriptableToucher, side, x, y});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean dragEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onDrag != null) {
			World world = (World)object.world;
			Scriptable scriptableToucher;
			if (toucher != null) {
				scriptableToucher = Context.toObject(toucher, world.scope);
			} else {
				scriptableToucher = null;
			}
			world.callFunction(onDrag, object, new Object[] {scriptableToucher, side, x, y});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean releaseEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onRelease != null) {
			World world = (World)object.world;
			Scriptable scriptableToucher;
			if (toucher != null) {
				scriptableToucher = Context.toObject(toucher, world.scope);
			} else {
				scriptableToucher = null;
			}
			world.callFunction(onRelease, object, new Object[] {scriptableToucher, side, x, y});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onCollide != null) {
			World world = (World)object.world;
			Scriptable scriptableNearObject = Context.toObject(nearObject, world.scope);
			world.callFunction(onCollide, object, new Object[] {scriptableNearObject, proximity});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onSlide != null) {
			World world = (World)object.world;
			Scriptable scriptableNearObject = Context.toObject(nearObject, world.scope);
			world.callFunction(onSlide, object, new Object[] {scriptableNearObject, proximity});
			return true;
		}
		return false;
	}
	
	@Override
	public boolean stopSlideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onStopSlide != null) {
			World world = (World)object.world;
			Scriptable scriptableNearObject = Context.toObject(nearObject, world.scope);
			world.callFunction(onStopSlide, object, new Object[] {scriptableNearObject, proximity});
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

}
