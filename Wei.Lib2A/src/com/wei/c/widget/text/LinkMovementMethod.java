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

import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class LinkMovementMethod extends ScrollingMovementMethod {
	private static final int CLICK = 1;
	private static final int UP = 2;
	private static final int DOWN = 3;

	private LinkSpan mLink = null;
	private boolean mOutOfBounds = true, mHasMovedOut = false, mIntercepted = false;

	@Override
	public boolean onKeyDown(TextView widget, Spannable buffer,
			int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			if (event.getRepeatCount() == 0) {
				if (action(CLICK, widget, buffer)) {
					return true;
				}
			}
		}

		return super.onKeyDown(widget, buffer, keyCode, event);
	}

	@Override
	protected boolean up(TextView widget, Spannable buffer) {
		if (action(UP, widget, buffer)) {
			return true;
		}

		return super.up(widget, buffer);
	}

	@Override
	protected boolean down(TextView widget, Spannable buffer) {
		if (action(DOWN, widget, buffer)) {
			return true;
		}

		return super.down(widget, buffer);
	}

	@Override
	protected boolean left(TextView widget, Spannable buffer) {
		if (action(UP, widget, buffer)) {
			return true;
		}

		return super.left(widget, buffer);
	}

	@Override
	protected boolean right(TextView widget, Spannable buffer) {
		if (action(DOWN, widget, buffer)) {
			return true;
		}

		return super.right(widget, buffer);
	}

	private boolean action(int what, TextView widget, Spannable buffer) {
		Layout layout = widget.getLayout();

		int padding = widget.getTotalPaddingTop() +
				widget.getTotalPaddingBottom();
		int areatop = widget.getScrollY();
		int areabot = areatop + widget.getHeight() - padding;

		int linetop = layout.getLineForVertical(areatop);
		int linebot = layout.getLineForVertical(areabot);

		int first = layout.getLineStart(linetop);
		int last = layout.getLineEnd(linebot);

		ClickableSpan[] candidates = buffer.getSpans(first, last, ClickableSpan.class);

		int a = Selection.getSelectionStart(buffer);
		int b = Selection.getSelectionEnd(buffer);

		int selStart = Math.min(a, b);
		int selEnd = Math.max(a, b);

		if (selStart < 0) {
			if (buffer.getSpanStart(FROM_BELOW) >= 0) {
				selStart = selEnd = buffer.length();
			}
		}

		if (selStart > last)
			selStart = selEnd = Integer.MAX_VALUE;
		if (selEnd < first)
			selStart = selEnd = -1;

		switch (what) {
		case CLICK:
			if (selStart == selEnd) {
				return false;
			}

			ClickableSpan[] link = buffer.getSpans(selStart, selEnd, ClickableSpan.class);

			if (link.length != 1)
				return false;

			link[0].onClick(widget);
			break;

		case UP:
			int beststart, bestend;

			beststart = -1;
			bestend = -1;

			for (int i = 0; i < candidates.length; i++) {
				int end = buffer.getSpanEnd(candidates[i]);

				if (end < selEnd || selStart == selEnd) {
					if (end > bestend) {
						beststart = buffer.getSpanStart(candidates[i]);
						bestend = end;
					}
				}
			}

			if (beststart >= 0) {
				Selection.setSelection(buffer, bestend, beststart);
				return true;
			}

			break;

		case DOWN:
			beststart = Integer.MAX_VALUE;
			bestend = Integer.MAX_VALUE;

			for (int i = 0; i < candidates.length; i++) {
				int start = buffer.getSpanStart(candidates[i]);

				if (start > selStart || selStart == selEnd) {
					if (start < beststart) {
						beststart = start;
						bestend = buffer.getSpanEnd(candidates[i]);
					}
				}
			}

			if (bestend < Integer.MAX_VALUE) {
				Selection.setSelection(buffer, beststart, bestend);
				return true;
			}

			break;
		}

		return false;
	}

	public boolean onKeyUp(TextView widget, Spannable buffer,
			int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer,
			MotionEvent event) {
		int action = event.getAction();

		if (action == MotionEvent.ACTION_DOWN) {
			/*
			 * 通知上一个启用了颜色变化的超链接取颜色变化权限，由于状态更新有延迟，因此
			 * 不能直接在ACTION_UP或ACTION_CANCEL事件里取消颜色变化权限，否则
			 * 当快速点击超链接的时候将看不到任何颜色的变化
			 */
			mOutOfBounds = true;
			dispatchLinkSpanEvent(widget, event);
			mLink = null;
		}
		
		if(!mIntercepted) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);
			//获取触摸位置的超链接，如果有只会存在一个，因此后面使用了link[0]
			LinkSpan[] link = buffer.getSpans(off, off, LinkSpan.class);

			if (link.length != 0) {	//在触摸位置存在一个超链接，但不一定是ACTION_DOWN事件时的超链接
				/*
				 * 只有当超链接接收到了ACTION_DOWN事件才能够让LinkSpan接收到后续事件。
				 * 如果LinkSpan接收到ACTION_DOWN事件后手指移出了该超链接区域，依然需要接收后续事件作出处理。
				 * 是否超过界限由参数outOfBounds标记。
				 */

				/*
				 * 在ACTION_MOVE事件中可能移动到另一个链接上去了，也可能先移出本链接再移进来
				 */
				mOutOfBounds = mLink != link[0];

				if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer,
							buffer.getSpanStart(link[0]),
							buffer.getSpanEnd(link[0]));
					mLink = link[0];
					mOutOfBounds = false;
					mHasMovedOut = false;
					dispatchLinkSpanEvent(widget, event);
					return true;
				}else if (action == MotionEvent.ACTION_UP) {
					dispatchLinkSpanEvent(widget, event);
					if(!mOutOfBounds && !mHasMovedOut) mLink.onClick(widget);
					return true;
				}else {
					dispatchLinkSpanEvent(widget, event);
				}
			}else {
				if (action == MotionEvent.ACTION_DOWN) {
					Selection.removeSelection(buffer);
					mIntercepted = true;
					return true;
				}

				mOutOfBounds = true;
				mHasMovedOut = true;
				dispatchLinkSpanEvent(widget, event);

				if (action == MotionEvent.ACTION_UP) {
					Selection.removeSelection(buffer);
					return true;
				}
			}
		}
		if (action == MotionEvent.ACTION_UP ||
				action == MotionEvent.ACTION_CANCEL) {
			mIntercepted = false;
		}

		return super.onTouchEvent(widget, buffer, event);
	}

	private void dispatchLinkSpanEvent(TextView widget, MotionEvent event) {
		if (mLink != null) {
			mLink.onTouchEvent(widget, event, mOutOfBounds, mHasMovedOut);
		}
	}

	public void initialize(TextView widget, Spannable text) {
		Selection.removeSelection(text);
		text.removeSpan(FROM_BELOW);
	}

	public void onTakeFocus(TextView view, Spannable text, int dir) {
		Selection.removeSelection(text);

		if ((dir & View.FOCUS_BACKWARD) != 0) {
			text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT);
		} else {
			text.removeSpan(FROM_BELOW);
		}
	}

	public static MovementMethod getInstance() {
		if (sInstance == null)
			sInstance = new LinkMovementMethod();

		return sInstance;
	}

	private static LinkMovementMethod sInstance;
	private static Object FROM_BELOW = new NoCopySpan.Concrete();
}
