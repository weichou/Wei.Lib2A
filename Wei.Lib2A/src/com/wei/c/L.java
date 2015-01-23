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

package com.wei.c;

import android.util.Log;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class L {
	private static final String sPrefix = "wei.c-";
	private static String contextName(Object o) {
		if (o instanceof String) return sPrefix + (String)o;
		if (o instanceof Class) return sPrefix + ((Class<?>)o).getSimpleName();
		return sPrefix + o.getClass().getSimpleName();
	}

	public static void i(Object o, String s) {
		if(Debug.LOG) Log.i(contextName(o), s);
	}

	public static void i(Object o, String s, Throwable e) {
		if(Debug.LOG) Log.i(contextName(o), s, e);
	}

	public static void i(Object o, Throwable e) {
		i(o, "", e);
	}

	public static void d(Object o, String s) {
		if(Debug.LOG) Log.d(contextName(o), s);
	}

	public static void d(Object o, String s, Throwable e) {
		if(Debug.LOG) Log.d(contextName(o), "", e);
	}

	public static void d(Object o, Throwable e) {
		d(o, "", e);
	}

	public static void e(Object o, String s) {
		if(Debug.LOG) Log.e(contextName(o), s);
		//发送错误统计数据
	}

	public static void e(Object o, String s, Throwable e) {
		if(Debug.LOG) Log.e(contextName(o), s, e);
		//发送错误统计数据
	}

	public static void e(Object o, Throwable e) {
		e(o, "", e);
	}

	public static void w(Object o, String s) {
		if(Debug.LOG) Log.w(contextName(o), s);
	}

	public static void w(Object o, String s, Throwable e) {
		if(Debug.LOG) Log.w(contextName(o), s, e);
	}

	public static void w(Object o, Throwable e) {
		w(o, "", e);
	}
}