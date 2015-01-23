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

package com.wei.c.phone;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
<pre>
需要权限：
android.permission.ACCESS_COARSE_LOCATION
android.permission.ACCESS_FINE_LOCATION
android.permission.ACCESS_WIFI_STATE
android.permission.ACCESS_NETWORK_STATE
android.permission.CHANGE_WIFI_STATE
android.permission.INTERNET
打开GPS需要权限：
android.permission.WRITE_SETTINGS
</pre>
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class GPS {

	/**
	 * 判断GPS是否开启。通过GPS卫星定位，定位级别可以精确到街道（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
	 * @param context
	 * @return true 表示开启
	 */
	public static boolean isGpsOpen(Context context) {
		return getLocationManager(context).isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * 判断AGPS是否开启。通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
	 * @param context
	 * @return true 表示开启
	 */
	public static boolean isAgpsOpen(final Context context) {
		return getLocationManager(context).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	/**
	 * 强制帮用户打开GPS
	 * @param context
	 */
	@SuppressLint("InlinedApi")
	public static void openGps(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Settings.System.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
		} else {
			Intent i = new Intent();
			i.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			i.addCategory("android.intent.category.ALTERNATIVE");
			i.setData(Uri.parse("custom:3"));
			try{
				PendingIntent.getBroadcast(context, 0, i, 0).send();
			} catch(CanceledException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 跳转到系统设置页面让用户自己打开GPS
	 * @param context
	 */
	public static void openGpsGraceful(Context context) {
		try {
			context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		} catch (Exception e) {
			context.startActivity(new Intent(Settings.ACTION_SETTINGS));
		}
	}

	private static LocationManager getLocationManager(Context context) {
		return (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	}
}
