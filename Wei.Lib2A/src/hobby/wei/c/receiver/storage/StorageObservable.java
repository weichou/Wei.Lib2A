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

import android.content.Context;
import android.content.Intent;

import hobby.wei.c.phone.Storage;
import hobby.wei.c.phone.Storage.SdCard;
import hobby.wei.c.receiver.AbsRcvrObservable;
import hobby.wei.c.receiver.storage.StorageObservable.Data;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class StorageObservable extends AbsRcvrObservable<StorageObserver, Data> {
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
    protected Data onParseData(Tuple tuple) {
        return new Data(tuple.context, tuple.intent);
    }

    @Override
    protected void onNotifyChange(StorageObserver observer, Data data) {
        observer.onChanged(data.context, data.intent, data.action, data.path, data.sdcard);
    }
}
