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

package com.wei.c;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class Const {
	@Deprecated
	public static final int REQUEST_CODE_NORMAL				= 10000;
	public static final int REQUEST_CODE_LOGIN				= 10001;
	public static final int REQUEST_CODE_LOGOUT				= 10002;
	@Deprecated
	public static final int RESULT_CODE_EXIT				= 99999;
	public static final int RESULT_CODE_USER				= 99998;

	public static final String ACTION_LOGIN					= "com.wei.c.action.LOGIN";
	public static final String ACTION_LOGOUT				= "com.wei.c.action.LOGOUT";
	public static final String ACTION_AUTO_LOGIN			= "com.wei.c.action.AUTO_LOGIN";

	public static final String ACTION_AUTO_START 			= "com.wei.c.action.AUTO.START";
	public static final String ACTION_STOP 					= "com.wei.c.action.STOP";

	public static final String EXTRA_BACK_TO_NAME			= "com.wei.c.extra.back.to.name";
	public static final String EXTRA_BACK_TO_COUNT			= "com.wei.c.extra.back.to.count";
	public static final String EXTRA_BACK_CONTINUOUS		= "com.wei.c.extra.back.continuous";
	public static final String EXTRA_NORMAL					= "com.wei.c.extra.normal";
	public static final String EXTRA_LOGIN					= "com.wei.c.extra.login";
	public static final String EXTRA_LOGOUT					= "com.wei.c.extra.logout";
	public static final String EXTRA_USER_INFO				= "com.wei.c.extra.user.info";

	public static final String KEY_USER						= "com.wei.c.key.user";
	public static final String KEY_USER_CONF				= "com.wei.c.key.user.config";

	public static final String NOTIFY_NET_STATE_CHANGE		= "com.wei.c.notify.net.state.change";

	public static final String KEY_FROM_INIT				= "com.wei.c.key.from.vitamio.initActivity";
	public static final String KEY_MSG						= "com.wei.c.key.message";
	public static final String KEY_PACKAGE					= "com.wei.c.key.package";
	public static final String KEY_CLASS_NAME				= "com.wei.c.key.className";
}
