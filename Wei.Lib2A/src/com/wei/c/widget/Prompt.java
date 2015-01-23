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

package com.wei.c.widget;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wei.c.lib.R;
import com.wei.c.utils.IdGetter;
import com.wei.c.widget.text.LinkMovementMethod;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class Prompt {
	public static final String ID_LAYOUT			= "lib_wei_c_prompt";
	public static final String ID_PROGRESS			= "lib_wei_c_prompt_progressbar";
	public static final String ID_IMAGE				= "lib_wei_c_prompt_imageview";
	public static final String ID_TEXT				= "lib_wei_c_prompt_text";

	private static WeakReference<Activity> actyRef;

	private static Handler mHandler;
	private static Thread mUiThread;
	private static OnCancelListener mOnCancelListener;
	private static OnDismissListener mOnDismissListener;

	private static Activity getActivity() {
		Activity acty = null;
		if(actyRef != null) acty = actyRef.get();
		return acty;
	}

	/**
	 * @param activity		需要靠它来创建一个Handler
	 * @param text			要显示的字符串
	 * @param cancelable	点击返回键时是否清除本Prompt的显示
	 * @param processVisble	是否显示旋转进度，可用来显示一个提示或警告，而不是进度
	 * @param iconResId		显示一个图像的资源id
	 */
	public synchronized static void show(final Activity activity, CharSequence text,
			boolean cancelable, boolean processVisble, int iconResId) {
		if(getActivity() != activity) {
			disposeInner(false);
			actyRef = new WeakReference<Activity>(activity);
		}
		MainAction action = MainAction.getInstance();
		if(mHandler != null) mHandler.removeCallbacks(action);

		action.set(activity, text, iconResId, processVisble, cancelable);

		if(mHandler == null) {
			getHandlerAndPostActions(activity, action, null);
		}else {
			if(mUiThread != Thread.currentThread()) {
				mHandler.post(action);
			}else {
				action.run();
			}
		}
	}

	/**
	 * 显示一个提示或警告，点击返回键可取消
	 */
	public static void showMessage(Activity activity, CharSequence text, int iconResId) {
		show(activity, text, true, false, iconResId);
	}

	public synchronized static void showMessage(Activity activity, CharSequence text, int iconResId, long delayMillis) {
		show(activity, text, true, false, iconResId);
		removeDelayed(activity, delayMillis);
	}

	public synchronized static void showProgress(Activity activity, CharSequence text, boolean cancelable) {
		show(activity, text, cancelable, true, 0);
	}

	/**手动点击返回键取消弹出窗口，会发送该事件；接着会发送OnDismissListener事件**/
	public synchronized static void setOnCancelListener(OnCancelListener l) {
		if(mOnCancelListener == l) return;
		final OnCancelListener oldListnener = mOnCancelListener;
		mOnCancelListener = l;
		if(oldListnener != null) {
			final MyDialog dialog = MyDialog.get();
			if(dialog != null) {
				if(mUiThread != Thread.currentThread()) {
					if(mHandler != null) mHandler.post(new Runnable() {
						@Override
						public void run() {
							oldListnener.onCancel(dialog);
						}
					});
				}else {
					oldListnener.onCancel(dialog);
				}
			}
		}
	}

	/**弹出窗口消失会发送该事件**/
	public synchronized static void setOnDismissListener(OnDismissListener l) {
		if(mOnDismissListener == l) return;
		final OnDismissListener oldListnener = mOnDismissListener;
		mOnDismissListener = l;
		if(oldListnener != null) {
			final MyDialog dialog = MyDialog.get();
			if(dialog != null) {
				if(mUiThread != Thread.currentThread()) {
					if(mHandler != null) mHandler.post(new Runnable() {
						@Override
						public void run() {
							oldListnener.onDismiss(dialog);
						}
					});
				}else {
					oldListnener.onDismiss(dialog);
				}
			}
		}
	}

	public synchronized static void remove() {
		if(mHandler != null) {
			mHandler.removeCallbacks(MainAction.get());
			mHandler.removeCallbacks(DelayedAction.get());
		}
		MyDialog dialog = MyDialog.get();
		if(dialog != null) dialog.dismiss();
	}

	private static void disposeInner(boolean remove) {
		if(remove) remove();
		mHandler = null;
		mUiThread = null;
		//不触发事件
		mOnCancelListener = null;
		mOnDismissListener = null;
	}

	public synchronized static void dispose() {
		disposeInner(true);
	}

	/**
	 * 在延迟一段时间之后自动调用remove()
	 * @param delayMillis 延迟时间，单位毫秒
	 */
	public synchronized static void removeDelayed(Activity activity, long delayMillis) {
		DelayedAction dAction = DelayedAction.getInstance();
		dAction.delayMillis = delayMillis;
		if(mHandler == null) {
			getHandlerAndPostActions(activity, null, dAction);
		}else {
			mHandler.postDelayed(dAction, delayMillis);
		}
	}

	private static void getHandlerAndPostActions(Activity activity, MainAction action, DelayedAction delAction) {
		if(!activity.isFinishing()) {
			HandlerAction handAction = HandlerAction.getInstance();
			handAction.set(action, delAction);
			activity.runOnUiThread(handAction);
		}
	}

	private static class HandlerAction implements Runnable {
		private static WeakReference<HandlerAction> ref;

		private static HandlerAction getInstance() {
			HandlerAction action = null;
			if(ref != null) action = ref.get();
			if(action == null) {
				action = new HandlerAction();
				ref = new WeakReference<HandlerAction>(action);
			}
			return action;
		}

		private MainAction action;
		private DelayedAction delAction;

		private void set(MainAction action, DelayedAction delAction) {
			this.action = action;
			this.delAction = delAction;
		}

		@Override
		public void run() {
			/*可能在activity第二次post本对象Runnable的时候，第一次post的还没执行，
			 * 等执行第二个Runnable的时候，mHandler已经有值了。
			 */
			if(mHandler == null) {
				mHandler = new Handler();
				mUiThread = Thread.currentThread();
			}
			if(action != null) {
				mHandler.post(action);
			}
			if(delAction != null && delAction.delayMillis > 0) {
				mHandler.postDelayed(delAction, delAction.delayMillis);
			}
			action = null;
			delAction = null;
		}
	}

	private static class MainAction implements Runnable {
		private static WeakReference<MainAction> ref;
		private static WeakReference<Context> conRef;

		private CharSequence text;
		private int iconResId;
		private boolean processVisble;
		private boolean cancelable;

		private static MainAction get() {
			MainAction action = null;
			if(ref != null) action = ref.get();
			return action;
		}

		private static MainAction getInstance() {
			MainAction action = get();
			if(action == null) {
				action = new MainAction();
				ref = new WeakReference<MainAction>(action);
			}
			return action;
		}

		private void set(Context context, CharSequence text, int iconResId, boolean processVisble, boolean cancelable) {
			conRef = new WeakReference<Context>(context);
			this.text = text;
			this.iconResId = iconResId;
			this.processVisble = processVisble;
			this.cancelable = cancelable;
		}

		@Override
		public void run() {
			if(conRef == null) return;
			Context context = conRef.get();
			if(context == null) return;
			MyDialog dialog = MyDialog.getInstance(context);
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if(mOnCancelListener != null) {
						mOnCancelListener.onCancel(dialog);
						mOnCancelListener = null;
					}
				}
			});
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(mOnDismissListener != null) {
						mOnDismissListener.onDismiss(dialog);
						mOnDismissListener = null;
					}
				}
			});
			dialog.setCancelable(cancelable);
			dialog.setText(text);
			dialog.setProcessVisble(processVisble);
			dialog.setIcon(iconResId);

			dialog.show();
		}
	}

	private static class DelayedAction implements Runnable {
		private static WeakReference<DelayedAction> ref;

		private static DelayedAction get() {
			DelayedAction action = null;
			if(ref != null) action = ref.get();
			return action;
		}

		private static DelayedAction getInstance() {
			DelayedAction action = get();
			if(action == null) {
				action = new DelayedAction();
				ref = new WeakReference<DelayedAction>(action);
			}
			return action;
		}

		private long delayMillis = 0;

		@Override
		public void run() {
			delayMillis = 0;
			remove();
		}
	}

	private static class MyDialog extends Dialog {
		private static WeakReference<MyDialog> ref;
		private static WeakReference<Context> conRef;
		//不一定是ProgressBar，可能是ImageView
		View mProgressBar;
		boolean mProcessVisble;

		TextView mTextView;
		CharSequence mText;

		ImageView mImageView;
		int mIconResId;
		Drawable mIconDrawable;

		private static MyDialog get() {
			MyDialog dialog = null;
			if(ref != null) dialog = ref.get();
			return dialog;
		}

		private synchronized static MyDialog getInstance(Context context) {
			MyDialog dialog = get();
			if(dialog == null || conRef.get() != context) {
				conRef = new WeakReference<Context>(context);
				dialog = new MyDialog(context);
				ref = new WeakReference<MyDialog>(dialog);
			}
			return dialog;
		}

		public MyDialog(Context context) {
			super(context, R.style.Theme_Wei_C_Dialog_Alert);
		}

		public void setText(CharSequence text) {
			mText = text;
			if(mTextView != null) {
				if(mText == null || mText.toString().equals("")) {
					mTextView.setVisibility(View.GONE);
				}else {
					mTextView.setVisibility(View.VISIBLE);
				}
				mTextView.setText(mText);
			}
		}

		public void setProcessVisble(boolean b) {
			mProcessVisble = b;
			if(mProgressBar != null) {
				if(mProcessVisble) {
					mProgressBar.setVisibility(View.VISIBLE);
				}else {
					mProgressBar.setVisibility(View.GONE);
				}
			}
		}

		public void setIcon(int resId) {
			boolean newIcon = mIconResId != resId;

			mIconResId = resId;

			if(newIcon && mIconResId>0) {
				mIconDrawable = getContext().getResources().getDrawable(mIconResId);
			}

			if(mImageView != null) {
				if(mIconResId>0) {
					if(mIconDrawable != null) {
						mImageView.setImageDrawable(mIconDrawable);
						mImageView.setVisibility(View.VISIBLE);
					}else {
						mImageView.setVisibility(View.GONE);
					}
				}else {
					mImageView.setVisibility(View.GONE);
				}
			}
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		protected void onStart() {
			super.onStart();

			/* 放在这里重复创建ContentView的原因是因为ProgressBar若不重新创建，
			 * 在再次显示的时候会不旋转。
			 */
			setContentView(IdGetter.getLayoutId(getContext(), ID_LAYOUT));
			mProgressBar = findViewById(IdGetter.getIdId(getContext(), ID_PROGRESS));
			mImageView = (ImageView)findViewById(IdGetter.getIdId(getContext(), ID_IMAGE));
			mTextView = (TextView)findViewById(IdGetter.getIdId(getContext(), ID_TEXT));

			//如果不设置则第一次弹出该Prompt后，点击超链接无响应。不过会影响长文本省略号的位置
			mTextView.setMovementMethod(LinkMovementMethod.getInstance());

			setProcessVisble(mProcessVisble);
			setIcon(mIconResId);
			setText(mText);
			startLoadingBar();

			WindowManager.LayoutParams l = getWindow().getAttributes();
			//去掉背景变暗
			//l.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			//背景变暗的程度，注意跟透明度相反
			l.dimAmount = 0.4f;
		}

		private void startLoadingBar() {
			Drawable d = mProgressBar.getBackground();
			if(d instanceof AnimationDrawable) {
				progressDrawable = (AnimationDrawable)d;
			}else {
				// 注意这里，如果你的图片控件用的是setImageResource ,你这里应该使用getDrawable();
				if(mProgressBar instanceof ImageView)
					d = ((ImageView)mProgressBar).getDrawable();
				if(d instanceof AnimationDrawable) {
					progressDrawable = (AnimationDrawable)d;
				}
			}
			if(progressDrawable != null) mProgressBar.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
		}

		private AnimationDrawable progressDrawable;

		private OnPreDrawListener preDrawListener = new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				progressDrawable.start();
				return true; // 必须要有这个true返回
			}
		};

		@Override
		public void cancel() {
			mProgressBar = null;
			mImageView = null;
			mTextView = null;

			super.cancel();
		}
	}
}
