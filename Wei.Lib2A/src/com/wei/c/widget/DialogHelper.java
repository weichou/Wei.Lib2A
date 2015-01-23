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

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wei.c.Debug;
import com.wei.c.lib.R;
import com.wei.c.utils.IdGetter;
import com.wei.c.widget.text.LinkSpan;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class DialogHelper {
	public static final String LOG_TAG	= "DialogHelper";
	public static final boolean LOG	= Debug.LOG;

	public static final String ID_LAYOUT				= "lib_wei_c_dialog";
	public static final String ID_STR_TITLE			= "lib_wei_c_dialog_title";
	public static final String ID_STR_CONTENT			= "lib_wei_c_dialog_content";
	public static final String ID_BTN_POSITIVE			= "lib_wei_c_dialog_btn_positive";
	public static final String ID_BTN_NEUTRAL			= "lib_wei_c_dialog_btn_neutral";
	public static final String ID_BTN_NEGATIVE			= "lib_wei_c_dialog_btn_negative";
	public static final String ID_BG_BTN_ONLY			= "lib_wei_c_dialog_bg_selector_button_only";

	public static void showAlertDialog(Context context, String title, int iconId, String msg, String positiveText, String neutralText,
			String negativeText, final Runnable doPositive, final Runnable doNeutral, final Runnable doNegative) {
		createDialogWithView(context, title, iconId, msg, positiveText, neutralText, negativeText, doPositive, doNeutral, doNegative).show();
	}

	public static void showSystemDialog(Context context, String title, int iconId, String msg, String positiveText, String neutralText,
			String negativeText, final Runnable doPositive, final Runnable doNeutral, final Runnable doNegative) {
		//getApplicationContext()	至关重要，如果没有这句，当在程序中弹出本对话框之后，再切换到主屏幕的时候将没法操作主屏幕（空白且被盖住）
		context = context.getApplicationContext();
		DialogViewCreator creator = new DialogViewCreator(context);
		final WindowManager wManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		final View view = creator.get();

		creator.setTitle(title);
		creator.setIcon(iconId);
		creator.setMessage(msg);
		if(positiveText!=null || doNegative!=null) creator.setPositiveButton(positiveText, new Runnable() {
			@Override
			public void run() {
				wManager.removeView(view);
				if(doPositive != null) doPositive.run();
			}
		});
		if(neutralText!=null || doNeutral!=null) creator.setNeutralButton(neutralText, new Runnable() {
			@Override
			public void run() {
				wManager.removeView(view);
				if(doNeutral != null) doNeutral.run();
			}
		});
		if(negativeText!=null || doNegative!=null) creator.setNegativeButton(negativeText, new Runnable() {
			@Override
			public void run() {
				wManager.removeView(view);
				if(doNegative != null) doNegative.run();
			}
		});

		LayoutParams p = new LayoutParams();
		p.format = PixelFormat.TRANSLUCENT;
		/* FLAG_NOT_FOCUSABLE默认会开启FLAG_NOT_TOUCH_MODAL，意味着后面的视图可以接收事件。FLAG_DIM_BEHIND使背景变暗
		 * 当不设置FLAG_NOT_FOCUSABLE或者FLAG_NOT_TOUCH_MODAL的时候，事件全都会被该View接收，即使该view布局不是全屏。
		 */
		p.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		//LayoutParams.FLAG_DIM_BEHIND;
		//wmParams.dimAmount = 0.4f;
		p.type = LayoutParams.TYPE_SYSTEM_ALERT;
		computePosition(context, p, creator.getMargins());
		wManager.addView(view, p);
	}

	private static Dialog createDialogWithView(Context context, String title, int iconId, String msg, String positiveText, String neutralText,
			String negativeText, final Runnable doPositive, final Runnable doNeutral, final Runnable doNegative) {

		final Dialog dialog = new Dialog(context, R.style.Theme_Wei_C_Dialog_Alert);
		DialogViewCreator creator = new DialogViewCreator(context);
		creator.setTitle(title);
		creator.setIcon(iconId);
		creator.setMessage(msg);
		if(positiveText!=null || doNegative!=null) creator.setPositiveButton(positiveText, new Runnable() {
			@Override
			public void run() {
				dialog.cancel();
				if(doPositive != null) doPositive.run();
			}
		});
		if(neutralText!=null || doNeutral!=null) creator.setNeutralButton(neutralText, new Runnable() {
			@Override
			public void run() {
				dialog.cancel();
				if(doNeutral != null) doNeutral.run();
			}
		});
		if(negativeText!=null || doNegative!=null) creator.setNegativeButton(negativeText, new Runnable() {
			@Override
			public void run() {
				dialog.cancel();
				if(doNegative != null) doNegative.run();
			}
		});
		dialog.setContentView(creator.get());
		LayoutParams p = dialog.getWindow().getAttributes();
		computePosition(context, p, creator.getMargins());
		dialog.getWindow().setAttributes(p);
		return dialog;
	}

	private static class DialogViewCreator {
		Context mContext;
		View mView;
		TextView mTitle;
		TextView mMsg;
		Button mPositive;
		Button mNeutral;
		Button mNegative;
		int mMargins;

		public DialogViewCreator(Context context) {
			mContext = context;
			LayoutInflater factory = LayoutInflater.from(context);
			mView = factory.inflate(IdGetter.getLayoutId(mContext, ID_LAYOUT), new LinearLayout(context), false);
			mTitle = (TextView)mView.findViewById(IdGetter.getIdId(mContext, ID_STR_TITLE));
			mMsg = (TextView)mView.findViewById(IdGetter.getIdId(mContext, ID_STR_CONTENT));
			mPositive = (Button)mView.findViewById(IdGetter.getIdId(mContext, ID_BTN_POSITIVE));
			mNeutral = (Button)mView.findViewById(IdGetter.getIdId(mContext, ID_BTN_NEUTRAL));
			mNegative = (Button)mView.findViewById(IdGetter.getIdId(mContext, ID_BTN_NEGATIVE));

			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mView.getLayoutParams();
			mMargins = lp!=null ? lp.leftMargin : 30;

			mPositive.setVisibility(View.GONE);
			mNeutral.setVisibility(View.GONE);
			mNegative.setVisibility(View.GONE);
			
			/* 注意一定要使用：
			   TextView.setMovementMethod(LinkMovementMethod.getInstance());
			      并且一定要去掉：
			   android:autoLink="all"
			      并且只有当TextView的至少一个颜色或者drawable是selector时，才会刷新颜色，详见TextView.updateTextColors()
			      否则超链接点击不起作用。
			      在某些情况下，比如弹出窗口，如果没有指定文本的默认颜色，则setMovementMethod只后整个文本的颜色也会像超链接一样变。
			 */
			mMsg.setMovementMethod(LinkMovementMethod.getInstance());
		}

		public void setIcon(int iconId) {
			if(iconId > 0x7f000000) mTitle.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
		}

		public void setTitle(CharSequence text) {
			mTitle.setText(text);
		}

		public void setMessage(CharSequence text) {
			if(text instanceof String) {
				//把所有的超链接URLSpan替换成LinkSpan
				text = ((String) text).replace("\n", "<br>").replace("  ", "&#160;&#160;").replace("	", "\t");
				SpannableStringBuilder spanText = new SpannableStringBuilder(Html.fromHtml((String)text));
				URLSpan[] urlSpan = spanText.getSpans(0, spanText.length(), URLSpan.class);
				for(URLSpan uSpan : urlSpan) {
					final int start = spanText.getSpanStart(uSpan);
					final int end = spanText.getSpanEnd(uSpan);
					final String url = uSpan.getURL();
					LinkSpan linkSpan = new LinkSpan(mMsg.getLinkTextColors(), url);
					spanText.removeSpan(uSpan);
					spanText.setSpan(linkSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				text = spanText;
			}
			
			mMsg.setText(text);
		}

		public void setPositiveButton(CharSequence positiveText, final Runnable doPositive) {
			if(positiveText!=null || doPositive!=null) {
				mPositive.setVisibility(View.VISIBLE);
				mPositive.setText(positiveText);
				mPositive.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(doPositive != null) doPositive.run();
					}
				});
			}else {
				mPositive.setVisibility(View.GONE);
			}
		}

		public void setNeutralButton(CharSequence neutralText, final Runnable doNeutral) {
			if(neutralText!=null || doNeutral!=null) {
				mNeutral.setVisibility(View.VISIBLE);
				mNeutral.setText(neutralText);
				mNeutral.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(doNeutral != null) doNeutral.run();
					}
				});
			}else {
				mNeutral.setVisibility(View.GONE);
			}
		}

		public void setNegativeButton(CharSequence negativeText, final Runnable doNegative) {
			if(negativeText!=null || doNegative!=null) {
				mNegative.setVisibility(View.VISIBLE);
				mNegative.setText(negativeText);
				mNegative.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(doNegative != null) doNegative.run();
					}
				});
			}else {
				mNegative.setVisibility(View.GONE);
			}
			updateButtonStytle();
		}

		private void updateButtonStytle() {
			//只有一个按钮
			if(mPositive.getVisibility()==View.GONE
					&& mNeutral.getVisibility()==View.GONE
					&& mNegative.getVisibility()!=View.GONE) {
				mNegative.setBackgroundResource(IdGetter.getDrawableId(mContext, ID_BG_BTN_ONLY));
			}
		}

		public int getMargins() {
			return mMargins;
		}

		public View get() {
			return mView;
		}
	}

	private static void computePosition(Context context, LayoutParams p, int margins) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		if(dm.widthPixels > dm.heightPixels) {	//横屏
			p.width = (int)(dm.widthPixels*.75f);
		}else {
			p.width = dm.widthPixels - margins*2;
		}
		p.height = LayoutParams.WRAP_CONTENT;
		p.gravity = Gravity.CENTER;
	}
}
