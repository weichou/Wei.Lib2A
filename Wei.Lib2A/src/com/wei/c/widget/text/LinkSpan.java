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

package com.wei.c.widget.text;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.provider.Browser;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class LinkSpan extends ClickableSpan {
	private static final String LOG_TAG = "LinkSpan";
	private static final boolean LOG = false;
	//[.]点字符放在括号里只能匹配其本身，等同于\\.，若单独放在外面则匹配除\n之外所有字符
	final String mUrlRegex = "^(http://).+";
	
	private ColorStateList mLinkColor;
	private boolean mCanHighLight = false, mRefreshed;
	private String mUrl;

	public LinkSpan(ColorStateList linkColor, String url) {
		mLinkColor = linkColor;
		mUrl = url;
	}

	public String getURL() {
		return mUrl;
	}

	@Override
	public void onClick(View widget) {
		if(mUrl != null && mUrl.trim().matches(mUrlRegex)) {
			Uri uri = Uri.parse(getURL());
			Context context = widget.getContext();
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
			context.startActivity(intent);
		}
	}

	/**
	 * @param widget		当前正在触发事件的TextView。注意：必然是TextView
	 * @param event			事件对象
	 * @param outOfBounds	当前事件的触摸位置是否超出本超链接的区域
	 * @param hasMovedOut	是否曾经出界(可能先移动出界后又移动回来了)
	 */
	public void onTouchEvent(TextView widget, MotionEvent event, boolean outOfBounds, boolean hasMovedOut) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(LOG) Log.i(LOG_TAG, "ACTION_DOWN");
			/*
			 * 注意：ACTION_DOWN会触发在两种状态：
			 * 1、本Span所在链接被点击；
			 * 2、上次点击了本Span所在链接。现在通知本Span不要改变颜色。接下来可能会触发其他位置的链接，
			 * 也可能依然触发本链接，也可能不触发任何链接。
			 * 由于状态更新有延迟，因此
			 * 不能直接在ACTION_UP或ACTION_CANCEL事件里取消颜色变化权限，否则
			 * 当快速点击超链接的时候将看不到任何颜色的变化。
			 * 因此做了这种设计。
			 */
			
			if(outOfBounds) {
				mCanHighLight = false;
			}else {
				mCanHighLight = true;
				mRefreshed = false;
				/*
				 * 注意TextView新颜色也是调用了invalidate()，由于只有当TextView的
				 * 至少一个颜色是selector时，才会刷新颜色，详见TextView.updateTextColors()，
				 * 否则不会刷新，为安全起见，这里直接调用。
				 * 
				 * 但是仍然无法更新最后的状态，即通常会停留在高亮状态而无法还原。
				 * 虽然mTextPaint.drawableState可以更新（是在onDraw()方法里调用的
				 * mTextPaint.drawableState = getDrawableState();），但是没能够再次调用重绘。
				 */
				//widget.invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(LOG) Log.i(LOG_TAG, "ACTION_MOVE");
			if(!mRefreshed && outOfBounds) {
				mCanHighLight = false;
				widget.refreshDrawableState();
				/*
				 * 在弹出窗口中竟然会不刷新，得进行以下调用才起作用。
				 * 弹出窗口中的问题比较多。
				 */
				widget.invalidate();
				mRefreshed = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(LOG) Log.i(LOG_TAG, "ACTION_UP");
			break;
		case MotionEvent.ACTION_CANCEL:
			if(LOG) Log.i(LOG_TAG, "ACTION_CANCEL");
			break;
		}
	}

	/**
	 * 不要试图屏蔽某种触摸状态下对颜色的更新，否则颜色不会保留之前的状态而是更新为本Span外层的Span的状态。
	 * <br/>
	 * 注意外层的ScrollView对TextView的重绘刷新有延迟，会导致链接的点击效果稍有迟钝。
	 */
	@Override
	public void updateDrawState(TextPaint ds) {
		/*
		 * 注意ds.linkColor是会改变的，详见TextView.updateTextColors()方法片段如下：
		 * if (mLinkTextColor != null) {
            color = mLinkTextColor.getColorForState(getDrawableState(), 0);
            if (color != mTextPaint.linkColor) {
                mTextPaint.linkColor = color;
                inval = true;
            }
        }
		 */
		if(LOG) Log.i(LOG_TAG, "updateDrawState-----");
		if(mLinkColor != null) {
			if(mCanHighLight) {
				ds.setColor(mLinkColor.getColorForState(ds.drawableState, ds.linkColor));
			}else {
				//第一个参数传入任意值都可以获取默认的颜色
				ds.setColor(mLinkColor.getColorForState(null, ds.linkColor));
			}
		}else {
			ds.setColor(ds.linkColor);
		}
		ds.setUnderlineText(true);
	}
}
