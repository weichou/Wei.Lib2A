package hobby.wei.c.persist;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static hobby.wei.c.utils.Assist.requireNonEmpty;
import static hobby.wei.c.utils.Assist.requireNotNull;

/**
 * @author Wei.Chou(weichou2010@gmail.com)
 * @version 1.0, 02/12/2015;
 *          1.1, 04/01/2017, 增加withUser().
 */
public class Keeper {
    protected static final String KEEP_XML_DEF = "keeper";
    private static final Map<String, WeakReference<Keeper>> sName2KeeperMap = new HashMap<>();

    private static Keeper get(Context context, String keepXml, boolean multiProcess) {
        final String keepXmlLC = requireNonEmpty(keepXml).toLowerCase();
        WeakReference<Keeper> ref = sName2KeeperMap.get(keepXmlLC);
        Keeper instance = ref == null ? null : ref.get();
        if (instance == null) {
            synchronized (Keeper.class) {
                ref = sName2KeeperMap.get(keepXmlLC);
                instance = ref == null ? null : ref.get();
                if (instance == null) {
                    instance = new Keeper(context, keepXml, multiProcess);
                    sName2KeeperMap.put(keepXmlLC, new WeakReference<>(instance));
                }
            }
        }
        return instance;
    }

    private final String mKeepXml;
    private final SharedPreferences mSPref;

    private Keeper(Context context, String keepXml, boolean multiProcess) {
        mKeepXml = keepXml;
        mSPref = multiProcess ? SPrefHelper.multiProcess().getSPref(context, mKeepXml) :
                SPrefHelper.def().getSPref(context, mKeepXml);
    }

    public String getKeepXml() {
        return mKeepXml;
    }

    public SharedPreferences getSharedPreferences() {
        return mSPref;
    }

    public Editor edit() {
        return mSPref.edit();
    }

    public static Builder.User get(Context context, String keepXml) {
        return new Builder.User(context, keepXml);
    }

    /**
     * 用法示例：
     * <pre><code>
     * public class XxxKeeper extends Keeper.Wrapper {
     *      private static final String KEEPER_XML = "xxx-state";
     *
     *      public static XxxKeeper get(String userId) {
     *          return Keeper.get(AbsApp.get().getApplicationContext(), KEEPER_XML).withUser(userId).bind(new XxxKeeper());
     *      }
     *
     *      public saveXxx(String key, String value) {
     *          get.keepString(key, value);
     *      }
     * }
     * </code></pre>
     */
    public static class Wrapper {
        public static Builder.User get(Context context, String keepXml) {
            return Keeper.get(context, keepXml);
        }

        private Keeper mKeeper;

        protected Wrapper() {
        }

        <T extends Wrapper> T bind(Keeper keeper) {
            mKeeper = keeper;
            return (T) this;
        }

        public Keeper get() {
            return requireNotNull(mKeeper, "请确保之前调用了Keeper.get(Context, String).bind(Wrapper)而不是ok()");
        }
    }

    public static class Builder {
        private final Context mContext;
        String mKeepXml;
        boolean mMultiProcess = false;

        private Builder(Context context, String keepXml) {
            mContext = requireNotNull(context);
            mKeepXml = requireNonEmpty(keepXml);
        }

        public Keeper ok() {
            return get(mContext, mKeepXml, mMultiProcess);
        }

        public <T extends Wrapper> T bind(T wrapper) {
            return wrapper.bind(ok());
        }

        public static class Multiper extends Builder {
            private Multiper(Context context, String keepXml) {
                super(context, keepXml);
            }

            public Builder multiProcess() {
                mMultiProcess = true;
                return this;
            }
        }

        public static class Localer extends Multiper {
            private Localer(Context context, String keepXml) {
                super(context, keepXml);
            }

            /**
             * 获取根据地区语言不同而隔离的{@link Keeper}实例。
             */
            // 返回{@link Builder}类型是为了避免再次出现本法。这里非常重要。
            public Multiper withLocale() {
                mKeepXml += "-" + Locale.getDefault().toString();
                return this;
            }
        }

        public static class User extends Localer {
            User(Context context, String keepXml) {
                super(context, keepXml);
            }

            /**
             * 获取根据UserId不同而隔离的{@link Keeper}实例。
             */
            // 返回{@link Localer}类型是为了避免再次出现本法。这里非常重要。
            public Localer withUser(String userId) {
                mKeepXml += "-" + requireNotNull(userId);
                return this;
            }
        }
    }

    public Keeper keepInt(String key, int value) {
        edit().putInt(requireNonEmpty(key), value).apply();
        return this;
    }

    public int readInt(String key) {
        return readInt(key, -1);
    }

    public int readInt(String key, int defaultValue) {
        return mSPref.getInt(requireNonEmpty(key), defaultValue);
    }

    public Keeper keepBoolean(String key, boolean value) {
        edit().putBoolean(requireNonEmpty(key), value).apply();
        return this;
    }

    public boolean readBoolean(String key) {
        return readBoolean(key, false);
    }

    public boolean readBoolean(String key, boolean defaultValue) {
        return mSPref.getBoolean(requireNonEmpty(key), defaultValue);
    }

    public Keeper keepFloat(String key, float value) {
        edit().putFloat(requireNonEmpty(key), value).apply();
        return this;
    }

    public float readFloat(String key) {
        return readFloat(key, -1);
    }

    public float readFloat(String key, float defaultValue) {
        return mSPref.getFloat(requireNonEmpty(key), defaultValue);
    }

    public Keeper keepLong(String key, long value) {
        edit().putLong(requireNonEmpty(key), value).apply();
        return this;
    }

    public long readLong(String key) {
        return readLong(key, -1);
    }

    public long readLong(String key, long defaultValue) {
        return mSPref.getLong(requireNonEmpty(key), defaultValue);
    }

    public Keeper keepString(String key, String value) {
        edit().putString(requireNonEmpty(key), value).apply();
        return this;
    }

    public String readString(String key) {
        return readString(key, null);
    }

    public String readString(String key, String defaultValue) {
        return mSPref.getString(requireNonEmpty(key), defaultValue);
    }

    public Keeper remove(String key) {
        edit().remove(requireNonEmpty(key)).apply();
        return this;
    }

    public boolean contains(String key) {
        return mSPref.contains(requireNonEmpty(key));
    }
}
