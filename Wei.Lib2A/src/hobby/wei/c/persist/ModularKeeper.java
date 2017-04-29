/*
 * Copyright (C) 2017-present, Wei.Chou(weichou2010@gmail.com)
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

package hobby.wei.c.persist;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import hobby.wei.c.framework.AbsApp;
import hobby.wei.c.tools.ICache;

import static hobby.wei.c.utils.Assist.requireNonEmpty;
import static hobby.wei.c.utils.Assist.requireNonEquals;

public class ModularKeeper extends Keeper.Wrapper {
    private static final String KEEPER_XML = "modular";
    private static final String KEEPER_META = "modular-meta";
    private static final String KEY_META = "meta";

    private static final ICache<Tuple, ModularKeeper> sCache = new ICache.Impl.SyncGet<>(5,
            new ICache.Delegate<Tuple, ModularKeeper>() {
                @Override
                public ModularKeeper load(Tuple key) {
                    final String module = key.clear ? key.module + "_c" : key.module;
                    ensureModule2Meta(key.userId, module, key.clear);
                    return key.creator.create(getModule(key.userId, module));
                }

                @Override
                public boolean update(Tuple key, ModularKeeper value) {
                    return true;
                }
            });

    public interface Creator<K extends ModularKeeper> {
        K create(Keeper.Builder builder);
    }

    private static class Tuple<K extends ModularKeeper> {
        final String userId;
        final String module;
        final boolean clear;

        final Creator<K> creator;

        public Tuple(String userId, String module, boolean clear, Creator<K> creator) {
            this.userId = requireNonEmpty(userId);
            this.module = requireNonEmpty(module);
            this.clear = clear;
            this.creator = creator;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Tuple) {
                final Tuple tuple = (Tuple) o;
                return tuple.userId.equals(userId)
                        && tuple.module.equals(module)
                        && (tuple.clear == clear);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 41 * (userId.hashCode() + (41 * (module.hashCode() + (clear ? 1 : 0))));
        }

        @Override
        public String toString() {
            return "Tuple{" +
                    "userId='" + userId + '\'' +
                    ", module='" + module + '\'' +
                    ", clear=" + clear +
                    '}';
        }
    }

    /**
     * 取得与参数指定的module关联的本对象。
     * <p>
     * 注意：虽然本对象里的方法可以在任何module被调用，但是请确保仅调用与参数module相关的方法，
     * 否则会造成混乱。因此，不建议将方法写在本类里面。
     * <p>
     * 不过也有在不同module下写同一个flag的需求，反正自己应该理清楚需求和存储关系。
     *
     * @param userId
     * @param module
     * @param clear  在特定情况下（退出之后或登录之前）是否清除该数据。
     * @return
     */
    public static <K extends ModularKeeper> K get(String userId, String module, boolean clear, Creator<K> creator) {
        return (K) sCache.get(new Tuple<>(userId, module, clear, creator));
    }

    private static Keeper.Builder getModule(String userId, String module) {
        return get(AbsApp.get().getApplicationContext(), KEEPER_XML + "-" + module)
                .withUser(userId)
                .multiProcess();
    }

    private static void ensureModule2Meta(String userId, String module, boolean clear) {
        final Keeper meta = getMeta(userId);
        final Set<String> set = meta.getSharedPreferences().getStringSet(KEY_META,
                new HashSet<String>()/*后面有add()操作*/);
        if (!set.contains(module)) {
            set.add(module);
            meta.edit().putStringSet(KEY_META, set).apply();
        }
        if (clear) {
            meta.keepBoolean(requireNonEquals(module, KEY_META), true);
        }
    }

    private static Keeper getMeta(String userId) {
        return get(AbsApp.get().getApplicationContext(), KEEPER_META)
                .withUser(userId)
                .multiProcess()
                .ok();
    }

    public static void clear(String userId) {
        final Keeper meta = getMeta(userId);
        final Set<String> set = meta.getSharedPreferences().getStringSet(KEY_META, Collections.<String>emptySet());
        boolean b = false;
        for (String module : new HashSet<>(set)) {
            if (meta.contains(module)) {
                if (!b) b = true;
                getModule(userId, module).ok().edit().clear().apply();
                set.remove(module);
            }
        }
        if (b) meta.edit().putStringSet(KEY_META, set).apply();
    }
}
