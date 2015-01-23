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

package com.wei.c.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

import com.wei.c.file.FileVersioned;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class SPref {

	public static void saveAsFile(Context context, String fileName, String content) {
		FileVersioned.saveAsFileInDefaultDir(context, fileName, content);
	}

	public static void saveAsFile(Context context, Object o, String key, String content) {
		saveAsFile(context, fileName(o, key), content);
	}

	public static void saveAsFile(Context context, Class<?> c, String key, String content) {
		saveAsFile(context, fileName(c, key), content);
	}

	public static String getFromFile(Context context, String fileName) {
		return FileVersioned.getStringFromFileInDefaultDir(context, fileName);
	}

	public static String getFromFile(Context context, Object o, String key) {
		return getFromFile(context, fileName(o, key));
	}

	public static String getFromFile(Context context, Class<?> c, String key) {
		return getFromFile(context, fileName(c, key));
	}

	public static void saveBooleanAsFile(Context context, String fileName, boolean content) {
		FileVersioned.saveAsFileInDefaultDir(context, fileName, content ? sTrue : sFalse);
	}

	public static void saveBooleanAsFile(Context context, Object o, String key, boolean content) {
		saveBooleanAsFile(context, fileName(o, key), content);
	}

	public static void saveBooleanAsFile(Context context, Class<?> c, String key, boolean content) {
		saveBooleanAsFile(context, fileName(c, key), content);
	}

	public static boolean getBooleanFromFile(Context context, String fileName) {
		String s = FileVersioned.getStringFromFileInDefaultDir(context, fileName);
		return s != null && s.equals(sTrue);
	}

	public static boolean getBooleanFromFile(Context context, Object o, String key) {
		return getBooleanFromFile(context, fileName(o, key));
	}

	public static boolean getBooleanFromFile(Context context, Class<?> c, String key) {
		return getBooleanFromFile(context, fileName(c, key));
	}

	public static Editor edit(Context context, String fileName) {
		return getSPref(context, fileName).edit();
	}

	public static SharedPreferences getSPref(Context context, String fileName) {
		return context.getSharedPreferences(fileName, getMode());
	}

	public static Editor edit(Context context, Object o) {
		return getSPref(context, o).edit();
	}

	public static SharedPreferences getSPref(Context context, Object o) {
		return getSPref(context, fileName(o));
	}

	public static Editor edit(Context context, Class<?> c) {
		return getSPref(context, c).edit();
	}

	public static SharedPreferences getSPref(Context context, Class<?> c) {
		return getSPref(context, fileName(c));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static int getMode() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Context.MODE_MULTI_PROCESS : Context.MODE_PRIVATE;
	}

	private static String fileName(Object o) {
		if(o instanceof String) return (String)o;
		return replace$(fileName(o.getClass()));
	}

	private static String fileName(Class<?> c) {
		return replace$(c.getName());
	}

	private static String fileName(String s, String key) {
		return replace$(s + (key == null ? "" : "." + key));
	}

	private static String fileName(Object o, String key) {
		return fileName(fileName(o), key);
	}

	private static String fileName(Class<?> c, String key) {
		return fileName(fileName(c), key);
	}

	private static String replace$(String s) {
		return s.replaceAll("\\$", ".");	//不然会被解释为正则的的末尾字符，然后在末尾加上.字符
	}

	private static final String sTrue		= "1";
	private static final String sFalse		= "0";
}
