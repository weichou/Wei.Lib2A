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

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.WeakHashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hobby.wei.c.L;
import hobby.wei.c.anno.inject.Injector;
import hobby.wei.c.anno.proguard.KeepC$$e;

/**
 * 子类如果作为内部类，必须是static的。如：private static.
 * 注意：对于一个用于Adapter的需要实现主题切换的ViewHolder，应该使用一个具体的Adapter实现类，并把ViewHolder作为private static的内部类。
 * 注意：不能让构造方法的参数View使用泛型，否则无法通过反射来实例化。
 *
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@SuppressWarnings("unchecked")
@KeepC$$e
public abstract class AbsViewHolder<DATA, OBJ> {
	private static final WeakHashMap<View, AbsViewHolder<?, ?>> refMap = new WeakHashMap<View, AbsViewHolder<?, ?>>();

	/**创建ViewHolder。如果跟View相关的ViewHolder已经存在，则不会创建新的。（不开放本方法，防止导致使用混乱，应该使用bindView来同时绑定数据）**/
	private static <T extends AbsViewHolder<?, OBJ>, OBJ> T get(View view, Class<T> clazzOfT, OBJ...args) {
		T viewHolder = get(view);
		if(viewHolder == null) {
			try {
	            Constructor<T> constructor = clazzOfT.getConstructor(View.class);
	            constructor.setAccessible(true);
	            viewHolder = constructor.newInstance(view);
	            constructor.setAccessible(false);
				viewHolder.init(args);
				refMap.put(view, viewHolder);
				view.setTag(viewHolder);
			} catch (Exception e) {
				L.e(AbsViewHolder.class, e);
			}
		}
		return viewHolder;
	}

	/**从缓存或view.tag里面获取，可能为空**/
	public static <T extends AbsViewHolder<?, ?>> T get(View view) {
		T holder = (T)refMap.get(view);
		if(holder == null) {
			Object tag = view.getTag();
			if(tag != null && tag instanceof AbsViewHolder) holder = (T)tag;
		}
		return holder;
	}

	/**仅适用于ListViewAdapter的getView()方法，不可作为他用**/
	public static <T extends AbsViewHolder<DATA, OBJ>, DATA, OBJ, V extends View> V getAndBindView(int position, View convertView, ViewGroup parent, LayoutInflater inflater, Class<T> clazzOfT, DATA data, OBJ... args) {
		if(convertView == null) convertView = makeView(clazzOfT, inflater, parent);
		bindView(position, convertView, clazzOfT, data, args);
		return (V)convertView;
	}

	public static <T extends AbsViewHolder<?, ?>, V extends View> V makeView(Class<T> clazzOfT, LayoutInflater inflater, ViewGroup parent) {
		return (V)inflater.inflate(layoutID(inflater.getContext(), clazzOfT), parent, false);
	}

	public static <T extends AbsViewHolder<DATA, OBJ>, DATA, OBJ> T bindView(int position, View view, Class<T> clazzOfT, DATA data, OBJ... args) {
		T vHolder = get(view, clazzOfT, args);
		bindView(vHolder, position, data);
		return vHolder;
	}

	public static <T extends AbsViewHolder<DATA, OBJ>, DATA, OBJ> void bindView(T vHolder, int position, DATA data) {
		vHolder.bindInner(position, data);
	}

	public static <T extends AbsViewHolder<?, ?>> int layoutID(Context context, Class<T> clazzOfT) {
		return Injector.layoutID(context, clazzOfT);
	}

	public AbsViewHolder(View view) {
		this(view, true);
	}

	public AbsViewHolder(View view, boolean inject) {
		viewRef = new WeakReference<View>(view);
		if(inject) Injector.inject(this, view, AbsViewHolder.class);
	}

	protected final void bindInner(int position, DATA data) {
        this.position = position;
		this.data = data;
		bind(position, data);
	}

	protected abstract void init(OBJ...args);
	protected abstract void bind(int position, DATA data);

	public final <V extends View> V getView() {
		return (V)viewRef.get();
	}

	public final int getPosition() {
		return position;
	}

	public final DATA getData() {
		return data;
	}

	private WeakReference<View> viewRef;
	private DATA data;
    private int position;
}
