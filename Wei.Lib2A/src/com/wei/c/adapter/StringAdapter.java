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

package com.wei.c.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class StringAdapter<T> extends AbsAdapter<T> {
	protected int mItemRes, mTextResId;
	protected int mDropDownItemRes, mDropDownTextId;

	public StringAdapter(Context context, List<T> data, int itemRes, int textResId) {
		super(context, data);
		mItemRes = itemRes;
		mTextResId = textResId;
	}

	public void setDropDownViewResource(int resource, int textId) {  // spinner 
		mDropDownItemRes = resource;
		mDropDownTextId = textId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mItemRes, mTextResId);
	}

	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource, int textId) {
		View view;
		TextView text;
		if (convertView == null) {
			view = getInflater().inflate(resource, parent, false);
		} else {
			view = convertView;
		}
		if (textId <= 0) {
			text = (TextView) view;
		} else {
			text = (TextView) view.findViewById(textId);
		}
		text.setText(getString(getItem(position)));
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent,
				mDropDownItemRes, mDropDownTextId);
	}

	protected abstract String getString(T item);
}
