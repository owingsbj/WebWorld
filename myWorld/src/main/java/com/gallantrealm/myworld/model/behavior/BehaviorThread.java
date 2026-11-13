package com.gallantrealm.myworld.model.behavior;

import java.util.ArrayList;
import com.gallantrealm.myworld.model.WWEntity;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWWorld;
import com.gallantrealm.myworld.model.WWWorld.ActionParams;

public class BehaviorThread extends Thread {

	private static class BehaviorRequest {
		public String event; // the act
		public WWObject object; // the object being acted upon
		public WWEntity agent; // the user or object doing the action
		public Object params; // more details on the act
	}

	private static class BehaviorThreadRequestPool {
		private final ArrayList<BehaviorRequest> pool = new ArrayList<BehaviorRequest>();

		public BehaviorRequest obtain(String event, WWObject object, WWEntity agent, Object params) {
			BehaviorRequest request;
			if (!pool.isEmpty()) {
				request = pool.remove(pool.size() - 1);
			} else {
				request = new BehaviorRequest();
			}
			request.event = event;
			request.object = object;
			request.agent = agent;
			request.params = params;
			return request;
		}

		public void recycle(BehaviorRequest request) {
			if (request != null) {
				request.event = null;
				request.object = null;
				request.agent = null;
				request.params = null;
				pool.add(request);
			}
		}
	}

	private final WWWorld world;
	private final ArrayList<BehaviorRequest> requestQueue;
	private final BehaviorThreadRequestPool requestPool;
	private final Object queueSemaphore;
	public boolean safeStop;

	public BehaviorThread(WWWorld world) {
		setName("BehaviorThread");
		this.world = world;
		requestQueue = new ArrayList<BehaviorRequest>();
		requestPool = new BehaviorThreadRequestPool();
		queueSemaphore = new Object();
		setDaemon(true);
	}

	public void queue(String event, WWObject object, WWEntity user, Object params) {
		// If a request already exists for this on the queue, then update the request with the new params,
		// otherwise queue a new request
		synchronized (queueSemaphore) {
			int size = requestQueue.size();
			for (int i = 0; i < size; i++) {
				BehaviorRequest queuedRequest = requestQueue.get(i);
				if (queuedRequest.event.equals(event) && queuedRequest.object == object && queuedRequest.agent == user) {
					queuedRequest.params = params;
					return;
				}
			}
			BehaviorRequest request = requestPool.obtain(event, object, user, params);
			requestQueue.add(request);
			queueSemaphore.notify();
		}
	}

	@Override
	public void run() {
		BehaviorRequest request = null;
		while (!safeStop) {
			try {
				synchronized (queueSemaphore) {
					if (request != null) {
						requestPool.recycle(request);
						request = null;
					}
					if (requestQueue.isEmpty()) {
						queueSemaphore.wait();
					}
					if (!requestQueue.isEmpty()) {
						request = requestQueue.remove(0);
					}
				}
				// TODO add timer to force interruption if event runs too long
				if (request != null) {
					if (request.object != null) {
						request.object.invokeBehavior(request.event, request.agent, request.params);
					} else {
						world.invokeAction(request.event, request.agent, (ActionParams) request.params);
					}
				}
				//Thread.sleep(25); // throttle speed
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				e.printStackTrace();
				// TODO send a message to the user if known to let them know the behavior failed
			}
		}
	}

}
