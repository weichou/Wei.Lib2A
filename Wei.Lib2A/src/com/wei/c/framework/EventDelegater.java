/*
 * Copyright (C) 2014 Wei Chou (weichou2010@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wei.c.framework;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class EventDelegater {
	private final static String KEY_BUNDLE_EVENT				= AbsApp.get().withPackageNamePrefix(EventDelegater.class.getName());

	public static enum PeriodMode {
		PAUSE_RESUME, START_STOP;
	}

	private final Map<String, EventSession> mLocalBroadcastEvent = new HashMap<String, EventSession>();
	private final Map<String, EventSession> mGlobalBroadcastEvent = new HashMap<String, EventSession>();

	private Context mContext;
	private boolean mStarted = false;
	private boolean mResumed = false;

	/*package*/ EventDelegater(Context context) {
		mContext = context;
	}

	/*package*/ void onStart() {
		mStarted = true;
		registerReceiver(PeriodMode.START_STOP);
	}

	/**{@link android.app.Fragment#onActivityCreated(Bundle) Fragment}会用到：旋转屏幕的时候，Activity会重建，但是Fragment不会。**/
	/*package*/ void onActivityCreated(Context context) {
		unregisterReceiver(PeriodMode.PAUSE_RESUME);
		unregisterReceiver(PeriodMode.START_STOP);
		mContext = context;
		//以下代码是安全的，不会导致在不恰当的时候注册
		registerReceiver(PeriodMode.START_STOP);
		registerReceiver(PeriodMode.PAUSE_RESUME);
	}

	/*package*/ void onResume() {
		mResumed = true;
		registerReceiver(PeriodMode.PAUSE_RESUME);
	}

	/*package*/ void onPause() {
		mResumed = false;
		unregisterReceiver(PeriodMode.PAUSE_RESUME);
	}

	/*package*/ void onStop() {
		mStarted = false;
		unregisterReceiver(PeriodMode.START_STOP);
	}

	public static void sendLocalEvent(Context context, String eventName, Bundle data) {
		Intent intent = new Intent(AbsApp.get().withPackageNamePrefix(eventName));
		if (data != null) intent.putExtra(KEY_BUNDLE_EVENT, data);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public static void sendGlobalEvent(Context context, String eventName, Bundle data) {
		Intent intent = new Intent(AbsApp.get().withPackageNamePrefix(eventName));
		if (data != null) intent.putExtra(KEY_BUNDLE_EVENT, data);
		context.sendBroadcast(intent);
	}

	public void hostingLocalEventReceiver(String eventName, PeriodMode periodMode, EventReceiver receiver) {
		EventSession session = new EventSession(true, eventName, periodMode, receiver);
		EventSession session1 = mLocalBroadcastEvent.put(eventName, session);
		if (session1 != null) unregisterReceiver(session1);
		registerReceiver(session);
	}

	public void hostingGlobalEventReceiver(String eventName, PeriodMode periodMode, EventReceiver receiver) {
		EventSession session = new EventSession(false, eventName, periodMode, receiver);
		EventSession session1 = mGlobalBroadcastEvent.put(eventName, session);
		if (session1 != null) unregisterReceiver(session1);
		registerReceiver(session);
	}

	public void unhostingLocalEventReceiver(String eventName) {
		EventSession session = mLocalBroadcastEvent.remove(eventName);
		if (session != null) unregisterReceiver(session);
	}

	public void unhostingGlobalEventReceiver(String eventName) {
		EventSession session = mGlobalBroadcastEvent.remove(eventName);
		if (session != null) unregisterReceiver(session);
	}

	private void registerReceiver(PeriodMode periodMode) {
		Collection<EventSession> sessions = mLocalBroadcastEvent.values();
		for (EventSession session : sessions) {
			if (session.mPeriodMode == periodMode) {
				registerReceiver(session);
			}
		}
		sessions = mGlobalBroadcastEvent.values();
		for (EventSession session : sessions) {
			if (session.mPeriodMode == periodMode) {
				registerReceiver(session);
			}
		}
	}

	private void registerReceiver(EventSession session) {
		if (session.mPeriodMode == PeriodMode.START_STOP && mStarted
				|| session.mPeriodMode == PeriodMode.PAUSE_RESUME && mResumed) {
			session.makeBroadcastReceiver();
			session.makeIntentFilter();
			try {	//避免在重复注册的时候导致异常
				if (session.mLocal) {
					LocalBroadcastManager.getInstance(mContext).registerReceiver(session.mBroadcastReceiver, session.mIntentFilter);
				} else {
					mContext.registerReceiver(session.mBroadcastReceiver, session.mIntentFilter);
				}
			} catch(Exception e) {}
		}
	}

	private void unregisterReceiver(PeriodMode periodMode) {
		Collection<EventSession> sessions = mLocalBroadcastEvent.values();
		for (EventSession session : sessions) {
			if (session.mPeriodMode == periodMode) {
				unregisterReceiver(session);
			}
		}
		sessions = mGlobalBroadcastEvent.values();
		for (EventSession session : sessions) {
			if (session.mPeriodMode == periodMode) {
				unregisterReceiver(session);
			}
		}
	}

	private void unregisterReceiver(EventSession session) {
		if (session.mBroadcastReceiver != null) {
			try {	//避免在重复取消注册的时候导致异常
				if (session.mLocal) {
					LocalBroadcastManager.getInstance(mContext).unregisterReceiver(session.mBroadcastReceiver);
				} else {
					mContext.unregisterReceiver(session.mBroadcastReceiver);
				}
			} catch(Exception e) {}
			session.mBroadcastReceiver = null;
			session.mIntentFilter = null;
		}
	}

	private static class EventSession {
		private final boolean mLocal;
		private final PeriodMode mPeriodMode;
		private final String mEventName;
		private final EventReceiver mReceiver;
		private BroadcastReceiver mBroadcastReceiver;
		private IntentFilter mIntentFilter;
		public EventSession(boolean local, String eventName, PeriodMode periodMode, EventReceiver receiver) {
			mLocal = local;
			mPeriodMode = periodMode;
			mEventName = eventName;
			mReceiver = receiver;
		}

		public void makeBroadcastReceiver() {
			if (mBroadcastReceiver == null) {
				mBroadcastReceiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						mReceiver.onEvent(intent.getBundleExtra(KEY_BUNDLE_EVENT));
					}
				};
			}
		}

		public void makeIntentFilter() {
			if (mIntentFilter == null) {
				mIntentFilter = new IntentFilter(AbsApp.get().withPackageNamePrefix(mEventName));
			}
		}

		@Override
		public int hashCode() {
			return mEventName.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return mEventName.equals(((EventSession)o).mEventName);
		}
	}

	public static interface EventReceiver {
		void onEvent(Bundle data);
	}
}
