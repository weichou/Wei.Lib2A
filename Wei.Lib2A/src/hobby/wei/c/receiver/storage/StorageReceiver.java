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

package hobby.wei.c.receiver.storage;

import android.content.Intent;
import android.content.IntentFilter;

import hobby.wei.c.receiver.AbsBroadcastReceiver;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class StorageReceiver extends AbsBroadcastReceiver {

	@SuppressWarnings("unchecked")
	@Override
	protected StorageObservable newObservable() {
		return new StorageObservable();
	}

	@Override
	protected IntentFilter newFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
		filter.addAction(Intent.ACTION_MEDIA_NOFS);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_CHECKING);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addAction(Intent.ACTION_MEDIA_SHARED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		filter.addDataScheme("file");
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);	//提高广播接受优先级
		return filter;
	}

	public static void registerObserver(StorageObserver observer) {
		registerObserver(observer, StorageReceiver.class);
	}

	public static void unregisterObserver(StorageObserver observer) {
		unregisterObserver(observer, StorageReceiver.class);
	}

	public static void unregisterAll() {
		unregisterAll(StorageReceiver.class);
	}
}
