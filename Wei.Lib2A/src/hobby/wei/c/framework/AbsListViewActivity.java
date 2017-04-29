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

package hobby.wei.c.framework;

import java.util.List;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import hobby.wei.c.adapter.AbsAdapter;
import hobby.wei.c.anno.inject.Injector;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class AbsListViewActivity<V extends AbsListView, D, A extends AbsAdapter<D>> extends AbsActivity {
    private V mListView;
    private A mAdapter;

    public void updateListData(List<D> data) {
        getAdapter().setDataSource(data);
    }

    @SuppressWarnings("unchecked")
    protected V getListView() {
        if (mListView == null) {
            mListView = (V) findViewById(listViewId());
        }
        return mListView;
    }

    protected A getAdapter() {
        if (mAdapter == null) {
            mAdapter = newAdapter();
            //不要重复调用setAdapter(), 否则会滚动到开头，而最好的办法就是在创建的时候set
            ((AdapterView<ListAdapter>) getListView()).setAdapter(mAdapter);
        }
        return mAdapter;
    }

    private int listViewId() {
        return Injector.listViewID(this, getClass());
    }

    protected abstract A newAdapter();
}
