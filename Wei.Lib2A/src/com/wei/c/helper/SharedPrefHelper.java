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

package com.wei.c.helper;

import android.content.Context;

import com.wei.c.framework.AbsApp;
import com.wei.c.framework.UserHelper;
import com.wei.c.phone.Device;
import com.wei.c.utils.SPref;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class SharedPrefHelper {

	public static class AppS {
		public static boolean getFirstLaunch(Context context) {
			return SPref.getSPref(context, AbsApp.class).getBoolean("first_launch", true);
		}

		public static void clearFirstLaunch(Context context) {
			SPref.edit(context, AbsApp.class).putBoolean("first_launch", false).commit();
		}
	}

	public static class DeviceS {
		public static String getUniqueId(Context context) {
			return SPref.getSPref(context, Device.class).getString("unique_id", null);
		}

		public static void saveUniqueId(Context context, String value) {
			SPref.edit(context, Device.class).putString("unique_id", value).commit();
		}
	}

	public static class UserHelperS {
		public static void saveToken(Context context, String value) {
			SPref.saveAsFile(context, UserHelper.class, "token", value);
		}

		public static String getToken(Context context) {
			return SPref.getFromFile(context, UserHelper.class, "token");
		}

		public static void saveAccountJson(Context context, String value) {
			SPref.saveAsFile(context, UserHelper.class, "account", value);
		}

		//区分开的目的，解决跨进程之间共享同一个sharedpref文件时不同步导致值丢失的问题
		public static String getAccountJson(Context context) {
			return SPref.getFromFile(context, UserHelper.class, "account");
		}

		public static void saveAuthorityFlag(Context context, boolean value) {
			SPref.saveBooleanAsFile(context, UserHelper.class, "authority_flag", value);
		}

		public static boolean getIsAuthorizeSuccess(Context context) {
			return SPref.getBooleanFromFile(context, UserHelper.class, "authority_flag");
		}
	}
}
