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

package com.wei.c.receiver.net;

import android.content.Context;
import android.content.Intent;

import com.wei.c.phone.Network;
import com.wei.c.phone.Network.State;
import com.wei.c.phone.Network.Type;
import com.wei.c.receiver.CntObservable;
import com.wei.c.receiver.net.NetObservable.Data;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class NetObservable extends CntObservable<NetObserver, Data> {
	static class Data {
		Type type;
		State state;

		public Data(Context context) {
			type = Network.getNetworkType(context);
			state = Network.getNetworkState(context);
		}
	}

	@Override
	protected Data onParserData(Context context, Intent intent) {
		return new Data(context);
	}

	@Override
	protected void onChange(NetObserver observer, Data data) {
		observer.onChanged(data.type, data.state);
	}
}
