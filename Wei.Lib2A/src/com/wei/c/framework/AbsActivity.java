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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;

import com.wei.c.Const;
import com.wei.c.anno.Injector;
import com.wei.c.framework.EventDelegater.EventReceiver;
import com.wei.c.framework.EventDelegater.PeriodMode;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class AbsActivity extends FragmentActivity {

	protected static void startMe(Context context, Intent intent) {
		if (!(context instanceof Activity)) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//退出应用，无论是否退出，都应该首先执行上面的代码，确保数据不丢失
		if (data != null) {
			if (data.getBooleanExtra(Const.EXTRA_BACK_CONTINUOUS, false)) {
				backContinuous(resultCode, data == null ? null : new Intent().putExtras(data).setData(data.getData()));
			} else {
				String name = data.getStringExtra(Const.EXTRA_BACK_TO_NAME);
				if (name != null) {
					if (name.equals(getClass().getName())) {
						data.removeExtra(Const.EXTRA_BACK_TO_NAME);
						return;
					}
					backToInner(resultCode, name, data == null ? null : new Intent().putExtras(data).setData(data.getData()));
				} else {
					int count = data.getIntExtra(Const.EXTRA_BACK_TO_COUNT, -1);
					if (--count <= 0) {
						data.removeExtra(Const.EXTRA_BACK_TO_COUNT);
						return;
					}
					backTo(resultCode, count, data == null ? null : new Intent().putExtras(data).setData(data.getData()));
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getApp().onActivityCreate(this);
		Injector.inject(this, this, AbsActivity.class);
	}

	@Override
	protected void onDestroy() {
		if (getApp().onActivityDestroy(this)) {
			onDestroyToExit();
		}
		super.onDestroy();
	}

	/**在{@link #onDestroy()}执行期间被回调，且只有在本次<code>onDestroy()</code>之后会退出应用的情况下才会回调，表示之后就退出应用了。**/
	protected void onDestroyToExit() {}

	@SuppressWarnings("unchecked")
	public <A extends AbsApp> A getApp() {
		return (A)getApplication();
	}

	/*package*/ String uniqueObjectId() {
		return super.toString();	//必须是super，不受子类重写干扰
	}

	public void backContinuous() {
		backContinuous(RESULT_CANCELED, null);
	}

	/**
	 * 给栈中连续的Activity递归地执行finish()操作。注意：本方法需要栈中的Activity可以接收栈顶Activity的返回值才起作用，
	 * 参见{@link Activity#onActivityResult(int, int, Intent) onActivityResult(int, int, Intent)}
	 * 和{@link Activity#startActivityForResult(Intent, int, Bundle) startActivityForResult(Intent, int, Bundle)}。
	 * 注意是连续的，也就是说，栈中的这些Activity自己没有finish()，遇到已经finish()的则中断。
	 * 
	 * @param resultCode 参见{@link AbsActivity#setResult(int, Intent) setResult(int, Intent)}
	 * @param data 参见{@link AbsActivity#setResult(int, Intent) setResult(int, Intent)}
	 */
	public void backContinuous(int resultCode, Intent data) {
		setResult(resultCode, data == null ?
				new Intent().putExtra(Const.EXTRA_BACK_CONTINUOUS, true) :
					data.putExtra(Const.EXTRA_BACK_CONTINUOUS, true));
		finish();
	}

	public void backTo(Class<? extends AbsActivity> activityClass) {
		backTo(RESULT_CANCELED, activityClass, null);
	}

	/**
	 * 返回到Activity栈中指定的类所在的Activity。注意：本方法需要栈中的Activity可以接收栈顶Activity的返回值才起作用，
	 * 参见{@link Activity#onActivityResult(int, int, Intent) onActivityResult(int, int, Intent)}
	 * 和{@link Activity#startActivityForResult(Intent, int, Bundle) startActivityForResult(Intent, int, Bundle)}。
	 * 
	 * @param resultCode 参见{@link AbsActivity#setResult(int, Intent) setResult(int, Intent)}
	 * @param activityClass 要返回到的Activity所对应的类
	 * @param data 参见{@link AbsActivity#setResult(int, Intent) setResult(int, Intent)}
	 */
	public void backTo(int resultCode, Class<? extends AbsActivity> activityClass, Intent data) {
		backToInner(resultCode, activityClass.getName(), data);
	}

	private void backToInner(int resultCode, String actyClassName, Intent data) {
		setResult(resultCode, data == null ?
				new Intent().putExtra(Const.EXTRA_BACK_TO_NAME, actyClassName) :
					data.putExtra(Const.EXTRA_BACK_TO_NAME, actyClassName));
		finish();
	}

	public void backTo(int count) {
		backTo(RESULT_CANCELED, count, null);
	}

	/**
	 * 返回Activity栈中指定数量的Activity。注意：本方法需要栈中的Activity可以接收栈顶Activity的返回值才起作用，
	 * 参见{@link Activity#onActivityResult(int, int, Intent) onActivityResult(int, int, Intent)}
	 * 和{@link Activity#startActivityForResult(Intent, int, Bundle) startActivityForResult(Intent, int, Bundle)}。
	 * 
	 * @param resultCode 参见{@link AbsActivity#setResult(int, Intent) setResult(int, Intent)}
	 * @param count 要返回的Activity栈数量
	 * @param data 参见{@link AbsActivity#setResult(int, Intent) setResult(int, Intent)}
	 */
	public void backTo(int resultCode, int count, Intent data) {
		setResult(resultCode, data == null ?
				new Intent().putExtra(Const.EXTRA_BACK_TO_COUNT, count) :
					data.putExtra(Const.EXTRA_BACK_TO_COUNT, count));
		finish();
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		initClickBlankAreaHandler(((ViewGroup)getWindow().getDecorView()).getChildAt(0));
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		initClickBlankAreaHandler(view);
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		initClickBlankAreaHandler(view);
	}

	protected int[] getClickHideInputMethodViewIds() {
		return null;
	}

	private void initClickBlankAreaHandler(View rootView) {
		rootView.setOnClickListener(mOnClickBlankAreaListener);
		int[] ids = getClickHideInputMethodViewIds();
		if(ids != null) {
			View view;
			for(int id : ids) {
				view = rootView.findViewById(id);
				if(view != null) view.setOnClickListener(mOnClickBlankAreaListener);
			}
		}
	}

	private View.OnClickListener mOnClickBlankAreaListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			hideInputMethod();
		}
	};

	protected void showInputMethod() {
		View focusView = getCurrentFocus();
		if(focusView != null) { //是否存在焦点
			InputMethodManager inputMethodManager = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	protected void hideInputMethod() {
		IBinder windowToken = getWindow().getDecorView().getWindowToken();
		if(windowToken != null) {
			InputMethodManager inputMethodManager = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(
					windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void sendLocalEvent(String eventName, Bundle data) {
		EventDelegater.sendLocalEvent(this, eventName, data);
	}

	public void sendGlobalEvent(String eventName, Bundle data) {
		EventDelegater.sendGlobalEvent(this, eventName, data);
	}

	public void hostingLocalEventReceiver(String eventName, PeriodMode periodMode, EventReceiver receiver) {
		ensureEventDelegaterMade();
		mEventDelegater.hostingLocalEventReceiver(eventName, periodMode, receiver);
	}

	public void hostingGlobalEventReceiver(String eventName, PeriodMode periodMode, EventReceiver receiver) {
		ensureEventDelegaterMade();
		mEventDelegater.hostingGlobalEventReceiver(eventName, periodMode, receiver);
	}

	public void unhostingLocalEventReceiver(String eventName) {
		ensureEventDelegaterMade();
		mEventDelegater.unhostingLocalEventReceiver(eventName);
	}

	public void unhostingGlobalEventReceiver(String eventName) {
		ensureEventDelegaterMade();
		mEventDelegater.unhostingGlobalEventReceiver(eventName);
	}

	private void ensureEventDelegaterMade() {
		if (mEventDelegater == null) mEventDelegater = new EventDelegater(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mEventDelegater != null) mEventDelegater.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mEventDelegater != null) mEventDelegater.onResume();
	}

	@Override
	protected void onPause() {
		if (mEventDelegater != null) mEventDelegater.onPause();
		super.onPause();
	}

	@Override
	protected void onStop() {
		if (mEventDelegater != null) mEventDelegater.onStop();
		super.onStop();
	}

	private EventDelegater mEventDelegater;
}
