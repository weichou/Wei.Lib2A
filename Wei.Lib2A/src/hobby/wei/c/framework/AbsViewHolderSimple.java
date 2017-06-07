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

package hobby.wei.c.framework;

import android.view.View;
import android.view.ViewGroup;

import hobby.wei.c.anno.inject.Injector;

/**
 * @author Wei.Chou
 * @version 1.0, 07/06/2017
 */
public abstract class AbsViewHolderSimple<OBJ> implements Injector.ViewSettable {
    private View mView;

    @SafeVarargs
    public final <T extends AbsViewHolderSimple> T inflate(ViewGroup parent, boolean createLayout, OBJ... args) {
        // 不可以简写成这样：
        // Injector.inject(createLayout ? this : (Object) this, parent, AbsViewHolderSimple.class);
        if (createLayout) Injector.inject(this, parent, AbsViewHolderSimple.class);
        else Injector.inject((Object) this, parent, AbsViewHolderSimple.class);
        init(args);
        return (T) this;
    }

    @Override
    public void onInjectView(View view) {
        mView = view;
    }

    public <V extends View> V getView() {
        return (V) mView;
    }

    protected abstract void init(OBJ... args);
}
