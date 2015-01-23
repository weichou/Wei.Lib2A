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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import android.app.ActivityManager;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.wei.c.L;
import com.wei.c.framework.user.IUser;
import com.wei.c.framework.user.IUserConfig;
import com.wei.c.helper.SharedPrefHelper;
import com.wei.c.utils.CrashHandler;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@SuppressWarnings("unchecked")
public abstract class AbsApp extends Application {
	static {
		CrashHandler.startCaughtAllException(false, true);
	}

	private static Application instance;
	private final Map<String, Object> mStaticInstances = new HashMap<String, Object>();
	private final static WeakHashMap<Looper, Handler> sHandlerRefMap = new WeakHashMap<Looper, Handler>();

	/*package*/ Config mConfig;
	/*package*/ IUser<?> mUser;
	/*package*/ IUserConfig<?> mUserConfig;
	/*package*/ boolean mHasLogout = false;
	/*package*/ Runnable mLoginedRun;

	private boolean mFirstLaunch = true;
	private boolean mExiting = false;

	public static final <A extends AbsApp> A get() {
		return (A)instance;
	}

	public AbsApp() {
		instance = this;
	}

	public void cacheSingleInstance(Object obj) {
		mStaticInstances.put(obj.getClass().getName(), obj);
	}

	public <O> O getSingleInstance(Class<O> clazz) {
		return (O)mStaticInstances.get(clazz.getName());
	}

	public void removeSingleInstance(Class<?> clazz) {
		mStaticInstances.remove(clazz.getName());
	}

	public String withPackageNamePrefix(String name) {
		return getPackageName() + "." + name;
	}

	public Handler getMainHandler() {
		return getHandler(getMainLooper());
	}

	public static Handler getHandler(Looper looper) {
		if(looper == null) throw new RuntimeException("参数不能为空");
		Handler handler = sHandlerRefMap.get(looper);
		if(handler == null) {
			handler = new Handler(looper);
			sHandlerRefMap.put(looper, handler);
		}
		return handler;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		//if(getConfig() == null) throw new NullPointerException("getConfig() 返回值不能为null。请不要返回Config.get()");

		ensureInfos();
	}

	//protected abstract Config getConfig();

	/**
	 * 退出应用事件回调
	 * @return true表示kill当前App以及其所有后台进程（需要加上权限：android.permission.KILL_BACKGROUND_PROCESSES），false则不kill.
	 */
	protected boolean onExit() {
		return false;
	}

	private void ensureInfos() {
		mFirstLaunch = SharedPrefHelper.AppS.getFirstLaunch(this);
		if(mFirstLaunch) SharedPrefHelper.AppS.clearFirstLaunch(this);
	}

	//////////////////////////////////////////////////////////////////////////////////////////

	/**退出应用。如果希望在退出之后本App的所有进程也关闭，则需要加上权限：android.permission.KILL_BACKGROUND_PROCESSES**/
	public void exit() {
		mExiting = true;
		getMainHandler().post(new Runnable() {
			@Override
			public void run() {
				finishActivities();
			}
		});
	}

	public boolean isFirstLaunch() {
		return mFirstLaunch;
	}

	public boolean isExiting() {
		return mExiting;
	}

	//没有意义，已经退出了的话，没人会调用本方法
	/*public boolean isExited() {
		return isActivitieStackEmpty();
	}*/

	/*package*/ void onActivityCreate(AbsActivity acty) {
		mActivitieStack.push(new WeakReference<AbsActivity>(acty));
	}

	/*package*/ boolean onActivityDestroy(AbsActivity acty) {
		cleanStackOrDelete(acty);
		if (mFirstLaunch && isActivitieStackEmpty()) {
			mFirstLaunch = false;
		}
		if (mExiting) finishActivities();
		boolean exit = isCurrentTheLastActivityToExit(acty);
		if (exit) getMainHandler().post(new Runnable() {
			@Override
			public void run() {
				doExit();
			}
		});
		return exit;
	}

	/*package*/ void doExit() {
		if (onExit() && checkCallingOrSelfPermission(android.Manifest.permission.KILL_BACKGROUND_PROCESSES) == PackageManager.PERMISSION_GRANTED) {
			L.w(AbsApp.class, "@@@@@@@@@@@@@@@@@@---程序关闭---killBackgroundProcesses");
			//只会对后台进程起作用，当本App最后一个Activity.onDestroy()的时候也会起作用，并且是立即起作用，即本语句后面的语句将不会执行。
			((ActivityManager) getSystemService(ACTIVITY_SERVICE)).killBackgroundProcesses(getPackageName());
			L.w(AbsApp.class, "@@@@@@@@@@@@@@@@@@---程序关闭---走不到这里来");
		}
		mExiting = false;
		L.w(AbsApp.class, "@@@@@@@@@@@@@@@@@@---程序关闭---没有killBackgroundProcesses");
	}

	/**
	 * 关闭activity. 只可在onActivityDestroy()的内部调用，否则返回值会不准确。
	 * @return true 表示已经全部关闭完，false 表示还没有关闭完。
	 */
	private boolean finishActivities() {
		WeakReference<? extends AbsActivity> actyRef;
		AbsActivity refActy;
		while (mActivitieStack.size() > 0) {
			actyRef = mActivitieStack.peek();
			refActy = actyRef.get();
			if (refActy == null) {
				mActivitieStack.remove(actyRef);
			} else {
				if (refActy.isFinishing()) {	//isFinishing表示是否调用过finish()
					//调用过finish()，则等待在onActivityDestroy()移除（都在UI线程，理论上不会有问题），
					//这里移除会导致同时进行多个finish()，并且size()不准确。
					//mActivitieStack.remove(actyRef);
				} else {
					refActy.finish();	//（现在情况不一样了-->）不能直接finish(), 否则被系统销毁的Activity重建之后可能不会被finish。
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * 只可在onActivityDestroy()的内部调用，否则返回值会不准确。
	 * @return 当前是不是最后一个正在关闭的Activity。
	 */
	private boolean isCurrentTheLastActivityToExit(AbsActivity acty) {
		return isActivitieStackEmpty();
	}

	private boolean isActivitieStackEmpty() {
		cleanStackOrDelete(null);
		return mActivitieStack.isEmpty();
	}

	private void cleanStackOrDelete(AbsActivity acty) {
		WeakReference<? extends AbsActivity> actyRef;
		AbsActivity refActy;
		for (int i = 0; i < mActivitieStack.size(); i++) {
			actyRef = mActivitieStack.get(i);
			refActy = actyRef.get();
			if (refActy == null || refActy == acty) {
				mActivitieStack.remove(actyRef);
				i--;
			}
		}
	}

	/**由于无论是进入新的Activity还是返回到旧的Activity，将要显示的页面B总是先创建，将要放入后台或销毁的页面A
	 * 总是在B.onCreate()之后执行A.onDestroy()<u>（即使处于后台由于内存不足而要被系统销毁的，理论上也会执行onDestroy()，
	 * 即使不执行，这里的软引用也会cover这种情况，对于高优先级App需要内存时，不会执行A.onDestroy()而直接killProcess，
	 * 这种情况不考虑，因为进程已经kill了，一切都没了）</u>，因此只要有Activity还要显示，本变量的元素个数总大于0，
	 * 即<h1>mActivitieStack.size()在App退出之前总是大于0，为0即认为退出。</h1><br/>
	 * 这里关于退出的定义：
	 * 认为只要有Activity在显示，就表示没有退出；当没有要显示的了，则表示App退出了。
	 * 关于没有要显示的了，是指在系统Task返回栈里面没有Activity记录了，正在显示的Activity-->finish()-->onDestroy()了。
	 * 而那些没有finish()但被系统直接onDestroy()的，Task返回栈里的记录仍然存在，只是内存里的实例对象被销毁，由于执行了onDestroy()，
	 * 本变量会删除一条记录，size()减少（即使系统没有执行onDestroy()，同时本变量会丢失一个key），
	 * 但是当返回时，会根据Task返回栈记录重建Activity，本变量会增加记录，在执行返回操作的B.onDestroy()之前。
	 */
	//private WeakHashMap<AbsActivity, Object> mActivities = new WeakHashMap<AbsActivity, Object>();
	private Stack<WeakReference<? extends AbsActivity>> mActivitieStack = new Stack<WeakReference<? extends AbsActivity>>();
}
