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

package com.wei.c.receiver.storage;

import android.content.Context;
import android.content.Intent;

import com.wei.c.phone.Storage;
import com.wei.c.phone.Storage.SdCard;
import com.wei.c.receiver.CntObservable;
import com.wei.c.receiver.storage.StorageObservable.Data;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class StorageObservable extends CntObservable<StorageObserver, Data> {
	static class Data {
		private Context context;
		private Intent intent;
		private String action;
		private String path;
		private SdCard sdcard;

		public Data(Context context, Intent intent) {
			this.context = context;
			this.intent = intent;
			action = intent.getAction();
			path = intent.getData() != null ? intent.getData().getPath() : null;
			sdcard = Storage.getSdCardByFilePath(path);
		}
	}

	@Override
	protected Data onParserData(Context context, Intent intent) {
		return new Data(context, intent);
	}

	@Override
	protected void onChange(StorageObserver observer, Data data) {
		observer.onChanged(data.context, data.intent, data.action, data.path, data.sdcard);
	}
}
