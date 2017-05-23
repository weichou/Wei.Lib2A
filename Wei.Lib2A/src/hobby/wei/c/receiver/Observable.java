/*
 * Copyright (C) 2017-present, Wei Chou (weichou2010@gmail.com)
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

package hobby.wei.c.receiver;

import java.util.concurrent.CopyOnWriteArraySet;

import hobby.wei.c.utils.Assist;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class Observable<O extends Observer, PARSE, DATA> {
    private final CopyOnWriteArraySet<O> mObservers = new CopyOnWriteArraySet<>();

    public void registerObserver(O observer) {
        mObservers.add(Assist.requireNonNull(observer));
    }

    public void unregisterObserver(O observer) {
        mObservers.remove(Assist.requireNonNull(observer));
    }

    public void unregisterAll() {
        mObservers.clear();
    }

    public int countObservers() {
        return mObservers.size();
    }

    void notifyChanged(PARSE parse) {
        final DATA data = onParseData(parse);
        for (O obs : mObservers) {
            onNotifyChange(obs, data);
        }
    }

    protected abstract DATA onParseData(PARSE parse);

    protected abstract void onNotifyChange(O observer, DATA data);
}
