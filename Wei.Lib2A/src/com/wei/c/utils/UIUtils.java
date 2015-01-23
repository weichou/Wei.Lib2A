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

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class UIUtils {
	public static int dip2pix(Context context, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public static int sp2pix(Context context, int sp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
	}

	public static int getLocationCenterX(View view) {
		int[] arrLocation = new int[2];
		view.getLocationInWindow(arrLocation);
		return view.getMeasuredWidth() / 2 + arrLocation[0];
	}

	public static void hideView(View view) {
		if (view != null) view.setVisibility(View.GONE);
	}

	public static void showView(View view) {
		if (view != null) view.setVisibility(View.VISIBLE);
	}

	public static void toggleView(View view) {
		if (view != null) {
			if (view.getVisibility() == View.VISIBLE)
				view.setVisibility(View.GONE);
			else
				view.setVisibility(View.VISIBLE);
		}
	}

	public static void invisibleView(View view) {
		if (view != null) view.setVisibility(View.INVISIBLE);
	}

	public static void setScreenOnFlag(Activity activity) {
		setScreenOnFlag(activity.getWindow());
	}

	public static void setScreenOnFlag(Window window) {
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public static void setText(Activity activity, int viewId, String value) {
		((TextView) activity.findViewById(viewId)).setText(value);
	}

	public static void setWindowBrightness(Activity activity, float screenBrightness) {
		setWindowBrightness(activity.getWindow(), screenBrightness);
	}

	public static void setWindowBrightness(Window window, float screenBrightness) {
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.screenBrightness = screenBrightness;
		window.setAttributes(layoutParams);
	}
}
