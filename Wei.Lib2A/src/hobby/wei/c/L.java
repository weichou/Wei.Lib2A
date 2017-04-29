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

package hobby.wei.c;

import android.util.Log;

import hobby.wei.c.anno.proguard.Burden;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class L {
    private static final String sPrefix = "hobby.wei.c-";

    @Burden
    private static String TAG(Object o) {
        if (o instanceof String) return sPrefix + o;
        if (o instanceof Class) return sPrefix + ((Class<?>) o).getSimpleName();
        return sPrefix + o.getClass().getSimpleName();
    }

    @Burden
    public static S s(String s) {
        return S.obtain(s);
    }

    public static class S {
        private static final Object sPoolSync = new Object();
        private static final int MAX_POOL_SIZE = 50;
        private static S sPool;
        private static int sPoolSize;
        private S next;

        private String s;

        private S() {
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof S) {
                S so = (S) o;
                return so.s == null && s == null ||
                        so.s != null && so.s.equals(s);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return s == null ? "".hashCode() : s.hashCode();
        }

        public static S obtain(String s) {
            S so = obtain();
            so.s = s;
            return so;
        }

        private static S obtain() {
            synchronized (sPoolSync) {
                if (sPool != null) {
                    S sp = sPool;
                    sPool = sp.next;
                    sp.next = null;
                    sPoolSize--;
                    return sp;
                }
            }
            return new S();
        }

        private void recycle() {
            s = null;
            synchronized (sPoolSync) {
                if (sPoolSize < MAX_POOL_SIZE) {
                    next = sPool;
                    sPool = this;
                    sPoolSize++;
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            recycle();
            super.finalize();
        }
    }

    @Burden
    private static void checkArgs(Object... args) {
        if (args != null && args.length > 0) {
            for (Object o : args) {
                if (o instanceof String) throw new IllegalArgumentException("请不要使用String作为参数，" +
                        "以防使用常量字符串，在数组里无法被混淆优化掉。常量请拼接到String类型的那个参数一起，" +
                        "如果为变量，请使用L.s(s)方法包装。");
            }
        }
    }

    @Burden
    public static void i(Object o, String s, Object... args) {
        checkArgs(args);
        Log.i(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args));
    }

    @Burden
    public static void i(Object o, Throwable e, String s, Object... args) {
        checkArgs(args);
        Log.i(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args), e);
    }

    @Burden
    public static void i(Object o, Throwable e) {
        i(o, e, null);
    }

    @Burden
    public static void d(Object o, String s, Object... args) {
        checkArgs(args);
        Log.d(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args));
    }

    @Burden
    public static void d(Object o, Throwable e, String s, Object... args) {
        checkArgs(args);
        Log.d(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args), e);
    }

    @Burden
    public static void d(Object o, Throwable e) {
        d(o, e, null);
    }

    @Burden
    public static void w(Object o, String s, Object... args) {
        checkArgs(args);
        Log.w(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args));
    }

    @Burden
    public static void w(Object o, Throwable e, String s, Object... args) {
        checkArgs(args);
        Log.w(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args), e);
    }

    @Burden
    public static void w(Object o, Throwable e) {
        w(o, e, null);
    }

    public static void e(Object o, String s, Object... args) {
        checkArgs(args);
        Log.e(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args));
        //发送错误统计数据
    }

    public static void e(Object o, Throwable e, String s, Object... args) {
        checkArgs(args);
        Log.e(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args), e);
        //发送错误统计数据
    }

    public static void e(Object o, Throwable e) {
        e(o, e, null);
    }

    public static void v(Object o, String s, Object... args) {
        checkArgs(args);
        Log.v(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args));
        //发送错误统计数据
    }

    public static void v(Object o, Throwable e, String s, Object... args) {
        checkArgs(args);
        Log.v(TAG(o), (args == null || args.length == 0) ? String.valueOf(s) : String.format(s, args), e);
        //发送错误统计数据
    }

    public static void v(Object o, Throwable e) {
        v(o, e, null);
    }
}