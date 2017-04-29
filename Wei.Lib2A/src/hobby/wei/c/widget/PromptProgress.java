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

package hobby.wei.c.widget;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import hobby.wei.c.lib.R;
import hobby.wei.c.utils.IdGetter;
//import com.weic.easycache.widget.text.LinkMovementMethod;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class PromptProgress {
	public static final String ID_LAYOUT		= "lib_wei_c_prompt_progress";
	public static final String ID_PROGRESS		= "lib_wei_c_prompt_progressbar";
	public static final String ID_IMAGE			= "lib_wei_c_prompt_imageview";
	public static final String ID_TEXT			= "lib_wei_c_prompt_text";
	public static final String ID_PERCENT		= "lib_wei_c_prompt_percent";

	private static WeakReference<Activity> actyRef;

	private static Handler mHandler;
	private static Thread mUiThread;

	private static Activity getActivity() {
		Activity acty = null;
		if(actyRef != null) acty = actyRef.get();
		return acty;
	}

	/**
	 * @param activity		需要靠它来创建一个Handler
	 * @param text			要显示的字符串
	 * @param progress		进度。是以1000为分母的分子数
	 * @param iconResId		显示一个图像的资源id
	 */
	public synchronized static void show(Activity activity, CharSequence text, int progress, int iconResId) {
		if(getActivity() != activity) {
			disposeInner(false);
			actyRef = new WeakReference<Activity>(activity);
		}
		MainAction action = MainAction.getInstance();
		if(mHandler != null) mHandler.removeCallbacks(action);

		action.set(activity, text, progress, iconResId);

		if(mHandler == null) {
			getHandlerAndPostActions(activity, action);
		}else {
			if(mUiThread != Thread.currentThread()) {
				mHandler.post(action);
			}else {
				action.run();
			}
		}
	}

	public synchronized static void remove() {
		if(mHandler != null) mHandler.removeCallbacks(MainAction.get());
		MyDialog dialog = MyDialog.get();
		if(dialog != null) dialog.dismiss();
	}

	private static void disposeInner(boolean remove) {
		if(remove) remove();
		mHandler = null;
		mUiThread = null;
	}

	public synchronized static void dispose() {
		disposeInner(true);
	}

	private static void getHandlerAndPostActions(Activity activity, MainAction action) {
		if(!activity.isFinishing()) {
			HandlerAction handAction = HandlerAction.getInstance();
			handAction.set(action);
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

		private void set(MainAction action) {
			this.action = action;
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
			action = null;
		}
	}

	private static class MainAction implements Runnable {
		private static WeakReference<MainAction> ref;
		private static WeakReference<Context> conRef;

		private CharSequence text;
		private int progress;
		private int iconResId;

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

		private void set(Context context, CharSequence text, int progress, int iconResId) {
			conRef = new WeakReference<Context>(context);
			this.text = text;
			this.progress = progress;
			this.iconResId = iconResId;
		}

		@Override
		public void run() {
			if(conRef == null) return;
			Context context = conRef.get();
			if(context == null) return;
			MyDialog dialog = MyDialog.getInstance(context);
			dialog.setCancelable(false);

			dialog.setText(text);
			dialog.setProgress(progress);
			dialog.setIcon(iconResId);

			dialog.show();
		}
	}

	private static class MyDialog extends Dialog {
		private static WeakReference<MyDialog> ref;
		private static WeakReference<Context> conRef;

		ProgressBar mProgressBar;
		int mProgress;

		TextView mTextView, mPercent;
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

		public void setProgress(int progress) {
			mProgress = progress;
			if(mProgressBar != null) {
				mProgressBar.setMax(1000);
				mProgressBar.setProgress(progress);
			}
			if(mPercent != null) {
				mPercent.setText(progress/10.0f+"%");
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

			setContentView(IdGetter.getLayoutId(getContext(), ID_LAYOUT));
			mProgressBar = (ProgressBar)findViewById(IdGetter.getIdId(getContext(), ID_PROGRESS));
			mImageView = (ImageView)findViewById(IdGetter.getIdId(getContext(), ID_IMAGE));
			mTextView = (TextView)findViewById(IdGetter.getIdId(getContext(), ID_TEXT));
			mPercent = (TextView)findViewById(IdGetter.getIdId(getContext(), ID_PERCENT));

			//如果不设置则第一次弹出该Prompt后，点击超链接无响应，但是设置了又会影响长文本省略号的位置，这里不要超链接
			//setMovementMethod(LinkMovementMethod.getInstance());

			setText(mText);
			setProgress(mProgress);
			setIcon(mIconResId);

			WindowManager.LayoutParams l = getWindow().getAttributes();
			//去掉背景变暗
			//l.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			//背景变暗的程度，注意跟透明度相反
			l.dimAmount = 0.4f;
		}

		@Override
		public void cancel() {
			mProgressBar = null;
			mImageView = null;
			mTextView = null;
			mPercent = null;

			super.cancel();
		}
	}
}
