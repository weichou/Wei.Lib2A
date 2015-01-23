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

package com.wei.c.receiver.storage;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.Toast;

import com.wei.c.phone.Storage.SdCard;
import com.wei.c.utils.IdGetter;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class ToastStorageObserver extends StorageObserver {
	private Context mContext;

	public ToastStorageObserver(Context context) {
		mContext = context;
	}

	private void showToastInner(String str) {
		Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
	}

	protected void showToast(String idStr) {
		showToastInner(mContext.getString(IdGetter.getStringId(mContext, idStr)));
	}

	protected void onMediaButton(KeyEvent ev) {
		showToast(ID_STR_MEDIA_BUTTON);
	}

	protected void onMediaUnMountable(SdCard sdcard) {
		showToast(ID_STR_MEDIA_UNMOUNTABLE);
	}

	protected void onMediaNoFS(SdCard sdcard) {
		showToast(ID_STR_MEDIA_NOFS);
	}

	protected void onMediaMounted(SdCard sdcard, boolean readOnly) {
		showToast(ID_STR_MEDIA_MOUNTED);
	}

	protected void onMediaChecking(SdCard sdcard) {
		showToast(ID_STR_MEDIA_CHECKING);
	}

	protected void onMediaScannerScanFile(String filePath) {
		showToast(ID_STR_MEDIA_SCANNER_SCAN_FILE);
	}

	protected void onMediaScannerStarted(String dirPath) {
		showToast(ID_STR_MEDIA_SCANNER_STARTED);
	}

	protected void onMediaScannerFinished(String dirPath) {
		showToast(ID_STR_MEDIA_SCANNER_FINISHED);
	}

	protected void onMediaShared(SdCard sdcard) {
		showToast(ID_STR_MEDIA_SHARED);
	}

	protected void onMediaEject(SdCard sdcard) {
		showToast(ID_STR_MEDIA_EJECT);
	}

	protected void onMediaUnmounted(SdCard sdcard) {
		showToast(ID_STR_MEDIA_UNMOUNTED);
	}

	protected void onMediaRemoved(SdCard sdcard) {
		showToast(ID_STR_MEDIA_REMOVED);
	}

	protected void onMediaBadRemoval(SdCard sdcard) {
		showToast(ID_STR_MEDIA_BAD_REMOVAL);
	}

	public static final String ID_STR_MEDIA_BUTTON              = "lib_wei_c_receiver_string_media_btn";
	public static final String ID_STR_MEDIA_UNMOUNTABLE         = "lib_wei_c_receiver_string_media_unmountable";
	public static final String ID_STR_MEDIA_NOFS                = "lib_wei_c_receiver_string_media_nofs";
	public static final String ID_STR_MEDIA_MOUNTED             = "lib_wei_c_receiver_string_media_mounted";
	public static final String ID_STR_MEDIA_CHECKING            = "lib_wei_c_receiver_string_media_checking";
	public static final String ID_STR_MEDIA_SCANNER_SCAN_FILE   = "lib_wei_c_receiver_string_media_scan_file";
	public static final String ID_STR_MEDIA_SCANNER_STARTED     = "lib_wei_c_receiver_string_media_scan_started";
	public static final String ID_STR_MEDIA_SCANNER_FINISHED    = "lib_wei_c_receiver_string_media_scan_finished";
	public static final String ID_STR_MEDIA_SHARED              = "lib_wei_c_receiver_string_media_shared";
	public static final String ID_STR_MEDIA_EJECT               = "lib_wei_c_receiver_string_media_eject";
	public static final String ID_STR_MEDIA_UNMOUNTED           = "lib_wei_c_receiver_string_media_unmounted";
	public static final String ID_STR_MEDIA_REMOVED             = "lib_wei_c_receiver_string_media_removed";
	public static final String ID_STR_MEDIA_BAD_REMOVAL         = "lib_wei_c_receiver_string_media_bad_removal";
}