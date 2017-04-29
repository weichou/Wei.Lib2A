/*
 * Copyright (C) 2014-present, Wei Chou (weichou2010@gmail.com)
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

package hobby.wei.c.receiver.net;

import android.content.IntentFilter;
import android.net.ConnectivityManager;

import hobby.wei.c.receiver.AbsBroadcastReceiver;

/**
 * action android:name="android.net.conn.CONNECTIVITY_CHANGE"
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class NetConnectionReceiver extends AbsBroadcastReceiver {

	@SuppressWarnings("unchecked")
	@Override
	protected NetObservable newObservable() {
		return new NetObservable();
	}

	@Override
	protected IntentFilter newFilter() {
		return new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
	}

	public static void registerObserver(NetObserver observer) {
		registerObserver(observer, NetConnectionReceiver.class);
	}

	public static void unregisterObserver(NetObserver observer) {
		unregisterObserver(observer, NetConnectionReceiver.class);
	}

	public static void unregisterAll() {
		unregisterAll(NetConnectionReceiver.class);
	}
}
