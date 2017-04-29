/*
 * Copyright (C) 2016-present, Wei.Chou(weichou2010@gmail.com)
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

package hobby.wei.c.utils;

import java.lang.ref.WeakReference;
import java.util.Collection;

import static android.text.TextUtils.isEmpty;
import static hobby.wei.c.utils.ArrayUtils.isEmpty;

/**
 * @author Wei.Chou(weichou2010@gmail.com)
 * @version 1.0, 02/07/2016
 */
public class Assist {
    private static final boolean DEBUG = true;

    public static <T> T getRef(WeakReference<T> ref) {
        return ref == null ? null : ref.get();
    }

    public static float between(float min, float value, float max) {
        return Math.max(min, Math.min(value, max));
    }

    public static int between(int min, int value, int max) {
        return Math.max(min, Math.min(value, max));
    }

    public static void assertx(boolean b) {
        assertx(b, null, false);
    }

    public static void assertf(boolean b) {
        assertx(b, null, true);
    }

    public static void assertx(boolean b, String msg) {
        assertx(b, msg, false);
    }

    public static void assertf(boolean b, String msg) {
        assertx(b, msg, true);
    }

    public static void assertx(boolean b, String msg, boolean force) {
        if ((force || DEBUG) && !b) throw new AssertionError(msg);
    }

    public static <T> T requireEquals(T value, Object o) {
        assertf(value.equals(o));
        return value;
    }

    public static <T> T requireNonEquals(T value, Object o) {
        assertf(!value.equals(o));
        return value;
    }

    public static String requireNonEmpty(final String s) {
        assertf(!isEmpty(s));
        return s;
    }

    public static <T extends Collection> T requireNonEmpty(final T col) {
        assertf(!isEmpty(col));
        return col;
    }

    public static <T> T[] requireNonEmpty(final T[] array) {
        assertf(!isEmpty(array));
        return array;
    }

    public static <T> T requireNotNull(T value) {
        return requireNotNull(value, null);
    }

    public static <T> T requireNotNull(T value, String msg) {
        assertf(value != null, msg);
        return value;
    }

    public static <T extends Collection<?>> T requireNoNullElem(T col) {
        return requireNoNullElem(col, null);
    }

    public static <T extends Collection<?>> T requireNoNullElem(T col, String msg) {
        for (Object t : col) {
            assertf(t != null, msg);
        }
        return col;
    }

    public static void eatExceptions(Runnable work, Runnable onError) {
        try {
            work.run();
        } catch (Exception e) {
            if (onError != null) onError.run();
        }
    }
}
