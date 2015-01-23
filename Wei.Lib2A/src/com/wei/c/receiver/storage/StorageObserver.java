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
import android.content.Intent;
import android.view.KeyEvent;

import com.wei.c.phone.Storage.SdCard;
import com.wei.c.receiver.Observer;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class StorageObserver implements Observer {
	/*friendly*/ void onChanged(Context context, Intent intent, String action, String path, SdCard sdcard) {
		if(action.equals(Intent.ACTION_MEDIA_BUTTON)) {
			onMediaButton((KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT));
		}else if(action.equals(Intent.ACTION_MEDIA_UNMOUNTABLE)) {
			onMediaUnMountable(sdcard);
		}else if(action.equals(Intent.ACTION_MEDIA_NOFS)) {
			onMediaNoFS(sdcard);
		}else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
			onMediaMounted(sdcard, intent.getBooleanExtra("read-only", false));
		}else if(action.equals(Intent.ACTION_MEDIA_CHECKING)) {
			onMediaChecking(sdcard);
		}else if(action.equals(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)) {
			onMediaScannerScanFile(path);
		}else if(action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
			onMediaScannerStarted(path);
		}else if(action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
			onMediaScannerFinished(path);
		}else if(action.equals(Intent.ACTION_MEDIA_SHARED)) {
			onMediaShared(sdcard);
		}else if(action.equals(Intent.ACTION_MEDIA_EJECT)) {
			onMediaEject(sdcard);
		}else if(action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
			onMediaUnmounted(sdcard);
		}else if(action.equals(Intent.ACTION_MEDIA_REMOVED)) {
			onMediaRemoved(sdcard);
		}else if(action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
			onMediaBadRemoval(sdcard);
		}
	}
	
	/**按下"MediaButton"按键时发出的广播,假如有"MediaButton"按键的话(硬件按键)，
	 * Intent.EXTRA_KEY_EVENT携带了这个KeyEvent对象**/
	protected void onMediaButton(KeyEvent ev) {}
	/**已经插入，但是不能挂载**/
	protected void onMediaUnMountable(SdCard sdcard) {}
	/**对象为空白或正在使用不受支持的文件系统，未格式化**/
	protected void onMediaNoFS(SdCard sdcard) {}
	/**扩展介质被插入，而且已经被挂载。intent包含一个名为"read-only"的boolean extra表明挂载点是否只读**/
	protected void onMediaMounted(SdCard sdcard, boolean readOnly) {}
	/**正在磁盘检查**/
	protected void onMediaChecking(SdCard sdcard) {}
	/**请求媒体扫描器扫描存储介质，以将媒体文件信息放入数据库**/
	protected void onMediaScannerScanFile(String filePath) {}
	/**媒体扫描器开始扫描文件目录**/
	protected void onMediaScannerStarted(String dirPath) {}
	/**媒体扫描器完成扫描文件目录**/
	protected void onMediaScannerFinished(String dirPath) {}
	/**由于通过USB存储共享导致无法挂载，挂载点路径可参见参数Intent.mData**/
	protected void onMediaShared(SdCard sdcard) {}
	/**用户希望弹出外部存储介质，收到此广播之后应该立即关闭正在读写的文件；
	 * 要弹出的卡的路径在intent.getData()里面包含了，可以先判断是不是正在读写的文件的那个卡**/
	protected void onMediaEject(SdCard sdcard) {}
	/**已经卸载但未拔出**/
	protected void onMediaUnmounted(SdCard sdcard) {}
	/**已经卸载并拔出**/
	protected void onMediaRemoved(SdCard sdcard) {}
	/**未卸载直接拔出**/
	protected void onMediaBadRemoval(SdCard sdcard) {}
}
