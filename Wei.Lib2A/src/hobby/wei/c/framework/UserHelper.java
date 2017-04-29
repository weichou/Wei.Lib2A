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

package hobby.wei.c.framework;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import hobby.wei.c.Const;
import hobby.wei.c.L;
import hobby.wei.c.framework.user.IUser;
import hobby.wei.c.framework.user.IUserConfig;
import hobby.wei.c.used.UsedKeeper;
import hobby.wei.c.utils.IdGetter;
import hobby.wei.c.widget.DialogHelper;

/**
 * 所有使用本库的App都可以调用主程序的登录页面
 *
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class UserHelper {
    public static final String LOG_TAG = "User";

    public static final String ID_STR_ASK_LOGIN = "lib_wei_c_login_ask_if_goto_login";

    @SuppressWarnings("unchecked")
    public static <U extends IUser<?>> U getUser() {
        return (U) AbsApp.get().mUser;
    }

    @SuppressWarnings("unchecked")
    public static <U extends IUserConfig<?>> U getUserConfig() {
        return (U) AbsApp.get().mUserConfig;
    }

    public static boolean isLogined() {
        return isLoginedInner(getUser());
    }

    private static boolean isLoginedInner(IUser<?> user) {
        return user != null && user.isLogined();
    }

    public static String getToken(Context context) {
        IUser<?> user = getUser();
        return isLoginedInner(user) ? user.getToken() : UsedKeeper.UserHelperS.getToken();
    }

    public static void saveToken(Context context, String token) {
        UsedKeeper.UserHelperS.saveToken(token);
    }

    private static void saveTokenInner(Context context) {
        IUser<?> user = getUser();
        if (isLoginedInner(user)) saveToken(context, user.getToken());
    }

    public static String getAccountJson(Context context) {
        IUser<?> user = getUser();
        return isLoginedInner(user) ? user.toJson() : UsedKeeper.UserHelperS.getAccountJson();
    }

    public static void saveAccountJson(Context context, String accountJson) {
        UsedKeeper.UserHelperS.saveAccountJson(accountJson);
    }

    private static void saveAccountJsonInner(Context context) {
        IUser<?> user = getUser();
        if (isLoginedInner(user)) saveAccountJson(context, user.toJson());
    }

    public static void saveAuthorityFlag(Context context, boolean success) {
        UsedKeeper.UserHelperS.saveAuthorityFlag(success);
    }

    public static boolean isAuthorizeSuccess(Context context) {
        return UsedKeeper.UserHelperS.getIsAuthorizeSuccess();
    }

    public static boolean checkLogin(final AbsDataActivity activity, boolean gotoLogin, boolean noDialog) {
        return checkLoginToDo(activity, gotoLogin, noDialog, null);
    }

    public static boolean checkLoginToDo(final AbsDataActivity activity, boolean gotoLogin, boolean noDialog, final Runnable loginedRun) {
        if (isLogined()) {
            if (loginedRun != null) loginedRun.run();
            return true;
        }
        //没有登录则重新登录（全部子项目都调用com.edu24ol程序包中的 LoginActivity）
        if (gotoLogin) {
            if (noDialog) {
                login(activity);
            } else {
                DialogHelper.showAlertDialog(activity, "登录", 0,
                        activity.getString(IdGetter.getStringId(activity, ID_STR_ASK_LOGIN)),
                        "登录", null, "取消", new Runnable() {
                            @Override
                            public void run() {
                                AbsApp.get().mLoginedRun = loginedRun;
                                login(activity);
                            }
                        }, null, null);
            }
        }
        return false;
    }

    public static void login(AbsDataActivity activity) {
        //进入一个新的页面输入用户名密码，因此不用携带任何参数
        Intent intent = new Intent(Const.ACTION_LOGIN);
        //activity.setPassUser(false);
        activity.startActivityForResult(intent, Const.REQUEST_CODE_LOGIN);
    }

    public static void autoLogin(AbsDataActivity activity) {
        activity.startActivityForResult(new Intent(Const.ACTION_AUTO_LOGIN), Const.REQUEST_CODE_LOGIN);
    }

    public static void logout(AbsDataActivity activity) {
        //应该调用一个不显示的Activity，或者一个对话框式的Activity
        Intent intent = new Intent(Const.ACTION_LOGOUT);
        //需要携带参数
        intent.putExtra(Const.EXTRA_LOGOUT, packForLogout());
        activity.startActivityForResult(intent, Const.REQUEST_CODE_LOGOUT);
    }

    public static boolean hasLogout() {
        return AbsApp.get().mHasLogout;
    }

    static void onActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        L.d(LOG_TAG, "onActivityResult-----------");
        if (resultCode == Const.RESULT_CODE_USER) {
            //activity.setPassUser(true);
            switch (requestCode) {
                case Const.REQUEST_CODE_LOGIN:
                    unpack(data == null ? null : data.getBundleExtra(Const.EXTRA_LOGIN));
                    if (isLogined()) {
                        saveAccountJsonInner(context);
                        saveTokenInner(context);
                        if (AbsApp.get().mLoginedRun != null) {
                            AbsApp.get().mLoginedRun.run();
                        }
                    }
                    AbsApp.get().mLoginedRun = null;
                    break;
                case Const.REQUEST_CODE_LOGOUT:
                    unpack(data == null ? null : data.getBundleExtra(Const.EXTRA_LOGOUT));
                    AbsApp.get().mHasLogout = true;
                    break;
            }
        }
    }

    public static void invalidate() {
        L.d(LOG_TAG, "invalidate-----------");
        IUser<?> user = getUser();
        IUserConfig<?> userConf = getUserConfig();
        if (user != null) AbsApp.get().mUser = user.invalidate();
        if (userConf != null) AbsApp.get().mUserConfig = userConf.invalidate();
    }

    private static Bundle packForLogout() {
        L.d(LOG_TAG, "packForLogout-----------");
        if (mLogout == null) {
            mLogout = new Bundle();
        } else {
            mLogout.clear();
        }
        mLogout.putParcelable(Const.KEY_USER, getUser());
        return mLogout;
    }

    static Bundle pack(Bundle bundle) {
        L.d(LOG_TAG, "pack-----------");
        if (bundle == null) bundle = new Bundle();
        bundle.putParcelable(Const.KEY_USER, getUser());
        bundle.putParcelable(Const.KEY_USER_CONF, getUserConfig());
        return bundle;
    }

    static Bundle packAndClear(Bundle bundle) {
        L.d(LOG_TAG, "packAndClear-----------");
        bundle = pack(bundle);
        AbsApp.get().mUser = null;
        AbsApp.get().mUserConfig = null;
        return bundle;
    }

    static void unpack(Bundle bundle) {
        L.d(LOG_TAG, "unpack-----------");
        IUser<?> user = getUser();
        IUserConfig<?> userConf = getUserConfig();
        if (bundle == null) {
            if (user != null) AbsApp.get().mUser = user.logout();
            if (userConf != null) AbsApp.get().mUserConfig = userConf.logout();
        } else {
            AbsApp.get().mUser = bundle.getParcelable(Const.KEY_USER);
            AbsApp.get().mUserConfig = bundle.getParcelable(Const.KEY_USER_CONF);
        }
    }

    private static Bundle mLogout;
}
