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

package hobby.wei.c.used;

import hobby.wei.c.framework.AbsApp;
import hobby.wei.c.persist.Keeper;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class UsedKeeper {
    public static class AppS extends Keeper.Wrapper {
        public static Keeper ins() {
            return get(AbsApp.get(), "AbsApp").multiProcess().ok();
        }

        public static Keeper getModule(int moduleId) {
            return get(AbsApp.get(), "AbsApp_module_" + moduleId).multiProcess().ok();
        }

        public static boolean getFirstLaunch(int moduleId) {
            return getModule(moduleId).readBoolean("first_launch", true);
        }

        public static void clearFirstLaunch(int moduleId) {
            getModule(moduleId).keepBoolean("first_launch", false);
        }
    }

    public static class DeviceS extends Keeper.Wrapper {
        public static Keeper ins() {
            return get(AbsApp.get(), "Device").multiProcess().ok();
        }

        public static String getUniqueId() {
            return ins().readString("unique_id");
        }

        public static void saveUniqueId(String value) {
            ins().keepString("unique_id", value);
        }
    }

    public static class UserHelperS extends Keeper.Wrapper {
        public static Keeper ins() {
            return get(AbsApp.get(), "UserHelper").multiProcess().ok();
        }

        public static void saveToken(String value) {
            ins().keepString("token", value);
        }

        public static String getToken() {
            return ins().readString("token");
        }

        public static void saveAccountJson(String value) {
            ins().keepString("account", value);
        }

        public static String getAccountJson() {
            return ins().readString("account");
        }

        public static void saveAuthorityFlag(boolean value) {
            ins().keepBoolean("authority_flag", value);
        }

        public static boolean getIsAuthorizeSuccess() {
            return ins().readBoolean("authority_flag");
        }
    }
}
