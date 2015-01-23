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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.wei.c.L;
import com.wei.c.phone.Storage;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class CrashHandler {
	private static boolean sHandleOutOfMemoryError = false;
	private static boolean sHandleNullPointerException = false;

	private static final UncaughtExceptionHandler sUncaughtExceptionHandler = new UncaughtExceptionHandler();

	public static void makeDumpHprofData() {
		try {
			String path = Storage.CARD_DEF.path + "/currentdump" + getDateFormat().format(new Date()) + ".hprof";
			android.os.Debug.dumpHprofData(path);
			L.i(CrashHandler.class, "生成内存快照："+path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**拦截所有线程的所有异常。作该操作的主要原因：如果想在某个特定的时刻对某些线程进行拦截，很有可能无法在设置完handler的时刻立即拦截到。因此通常进行全局的拦截**/
	public static void startCaughtAllException(boolean currentThread, boolean handle) {
		CatchOutOfMemoryError.start(currentThread, handle);
		CatchNullPointerException.start(currentThread, handle);
	}

	public static class CatchOutOfMemoryError {
		public static void start(boolean currentThread, boolean handle) {
			sHandleOutOfMemoryError = handle;
			setDefaultHandler(currentThread);
		}
	}

	public static class CatchNullPointerException {
		public static void start(boolean currentThread, boolean handle) {
			sHandleNullPointerException = handle;
			setDefaultHandler(currentThread);
		}
	}

	public static void setDefaultHandler(boolean currentThread) {
		if(currentThread) {
			Thread.currentThread().setUncaughtExceptionHandler(sUncaughtExceptionHandler);
		}else {
			Thread.setDefaultUncaughtExceptionHandler(sUncaughtExceptionHandler);
		}
	}

	private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread thread, Throwable e) {
			Class<?> clazz = e.getClass();
			L.e(CrashHandler.class, "拦截异常："+clazz.getName()+"，线程："+thread.getName(), e);
			if(sHandleOutOfMemoryError && clazz.equals(OutOfMemoryError.class)) {
				makeDumpHprofData();
			}else if(sHandleNullPointerException && clazz.equals(NullPointerException.class)) {

			}
			//android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(2);
		}
	}

	private static SimpleDateFormat dateFormat;
	private static SimpleDateFormat getDateFormat() {
		if(dateFormat == null) dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
		return dateFormat;
	}
}