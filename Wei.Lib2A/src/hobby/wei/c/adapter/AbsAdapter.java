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

package hobby.wei.c.adapter;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class AbsAdapter<T> extends BaseAdapter {
	public final List<T> EMPTY = Collections.emptyList();
	private List<T> mData;
	private LayoutInflater mInflater;

	public AbsAdapter(Context context) {
		this(context, null);
	}

	public AbsAdapter(Context context, List<T> data) {
		mInflater = LayoutInflater.from(context);
		mData = data == null ? EMPTY : data;
	}

	public void setDataSource(List<T> data) {
		mData = data == null ? EMPTY : data;
		notifyDataSetChanged();
	}

	public List<T> getData() {
		return mData;
	}

	protected LayoutInflater getInflater() {
		return mInflater;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public T getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
