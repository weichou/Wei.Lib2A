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

package hobby.wei.c.utils;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import hobby.wei.c.L;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class Markets {
	public static void openGooglePlay(Context context) {
		Intent intent = new Intent("com.google.android.finsky.VIEW_MY_DOWNLOADS");
		intent.setComponent(new ComponentName("com.android.vending", "com.android.vending.AssetBrowserActivity"));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static void openAppMarketsAtMyDetails(Activity activity, boolean forResult, int requestCode) {
		String uri = "market://details?id=" + activity.getPackageName();
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		//PackageManager.MATCH_DEFAULT_ONLY用于非Launcher触发的启动但是可以接受data的Intent.CATEGORY_DEFAULT
		List<ResolveInfo> infos = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		//如果没有安装任何应用市场，则startActivity()会报错
		if (infos != null && infos.size() > 0) {
			try {
				if (forResult) {
					//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	//加这句的话，App还没启动，就会收到onActivityResult()
					activity.startActivityForResult(intent, requestCode);
				} else {
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					activity.startActivity(intent);
				}
			} catch (Exception e) {
				L.e(Markets.class, e);
			}
		}
	}

	public static void openAppMarkets(Context context) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory("android.intent.category.APP_MARKET");	//Intent.CATEGORY_APP_MARKET
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
