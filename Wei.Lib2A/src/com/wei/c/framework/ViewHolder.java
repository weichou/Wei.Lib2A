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

package com.wei.c.framework;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wei.c.L;
import com.wei.c.anno.Injector;

/**
 * 子类如果作为内部类，必须是static的。如：private static.
 * 注意：对于一个用于Adapter的需要实现主题切换的ViewHolder，应该使用一个具体的Adapter实现类，并把ViewHolder作为private static的内部类。
 * 注意：不能让构造方法的参数View使用泛型，否则无法通过反射来实例化。
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@SuppressWarnings("unchecked")
public abstract class ViewHolder<DATA, OBJ> {
	private static final WeakHashMap<View, ViewHolder<?, ?>> refMap = new WeakHashMap<View, ViewHolder<?, ?>>();

	/**创建ViewHolder。如果跟View相关的ViewHolder已经存在，则不会创建新的。（不开放本方法，防止导致使用混乱，应该使用bindView来同时绑定数据）**/
	private static <T extends ViewHolder<?, OBJ>, OBJ> T get(View view, Class<T> clazzOfT, OBJ...args) {
		T viewHolder = get(view);
		if(viewHolder == null) {
			try {
				viewHolder = clazzOfT.getConstructor(View.class).newInstance(view);
				viewHolder.init(args);
				refMap.put(view, viewHolder);
				view.setTag(viewHolder);
			} catch (Exception e) {
				L.e(ViewHolder.class, e);
			}
		}
		return viewHolder;
	}

	/**从缓存或view.tag里面获取，可能为空**/
	public static <T extends ViewHolder<?, ?>> T get(View view) {
		T holder = (T)refMap.get(view);
		if(holder == null) {
			Object tag = view.getTag();
			if(tag != null && tag instanceof ViewHolder) holder = (T)tag;
		}
		return holder;
	}

	/**仅适用于ListViewAdapter的getView()方法，不可作为他用**/
	public static <T extends ViewHolder<DATA, OBJ>, DATA, OBJ> View getAndBindView(int position, View convertView, ViewGroup parent, LayoutInflater inflater, Class<T> clazzOfT, DATA data, OBJ... args) {
		if(convertView == null) convertView = makeView(clazzOfT, inflater, parent);
		bindView(position, convertView, clazzOfT, data, args);
		return convertView;
	}

	public static <T extends ViewHolder<?, ?>> View makeView(Class<T> clazzOfT, LayoutInflater inflater, ViewGroup parent) {
		return inflater.inflate(layoutID(clazzOfT), parent, false);
	}

	public static <T extends ViewHolder<DATA, OBJ>, DATA, OBJ> T bindView(int position, View view, Class<T> clazzOfT, DATA data, OBJ... args) {
		T vHolder = get(view, clazzOfT, args);
		bindView(vHolder, position, data);
		return vHolder;
	}

	public static <T extends ViewHolder<DATA, OBJ>, DATA, OBJ> void bindView(T vHolder, int position, DATA data) {
		vHolder.bindInner(position, data);
	}

	public static <T extends ViewHolder<?, ?>> int layoutID(Class<T> clazzOfT) {
		return Injector.layoutID(clazzOfT);
	}

	public ViewHolder(View view) {
		this(view, true);
	}

	public ViewHolder(View view, boolean inject) {
		viewRef = new WeakReference<View>(view);
		if(inject) Injector.inject(this, view, ViewHolder.class);
	}

	private final void bindInner(int position, DATA data) {
		this.data = data;
		bind(position, data);
	}

	protected abstract void init(OBJ...args);
	protected abstract void bind(int position, DATA data);

	public final <V extends View> V getView() {
		return (V)viewRef.get();
	}

	public final DATA getData() {
		return data;
	}

	private WeakReference<View> viewRef;
	private DATA data;
}
