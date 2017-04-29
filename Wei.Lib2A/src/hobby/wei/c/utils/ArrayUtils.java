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

package hobby.wei.c.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@SuppressWarnings("unchecked")
public class ArrayUtils {
    public static <T> T[] addElement(T[] array, T element) {
        Class<?> ct = array.getClass().getComponentType();
        T[] newArray = (T[]) Array.newInstance(ct, array.length + 1);
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

	/*并不能正常工作
     public static <T> T[] toArray(Collection<T> collect) {
		if(collect == null) return (T[])new Object[0];
		return (T[])collect.toArray();
	}*/

    public static <T> List<T> toList(T[] array) {
        return Arrays.asList(array);
    }

    public static <T> ArrayList<T> toArrayList(T[] array) {
        final List<T> list = Arrays.asList(array);
        if (list instanceof ArrayList) {
            return (ArrayList<T>) list;
        }
        return new ArrayList<>(list);
    }

    public static <T> ArrayList<T> toArrayList(Collection<T> collect) {
        return new ArrayList<>(collect);
    }

    public static long[] copy(long[] array) {
        if (array == null) return new long[0];
        long[] newArray = new long[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static int[] copy(int[] array) {
        if (array == null) return new int[0];
        int[] newArray = new int[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static short[] copy(short[] array) {
        if (array == null) return new short[0];
        short[] newArray = new short[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static byte[] copy(byte[] array) {
        if (array == null) return new byte[0];
        byte[] newArray = new byte[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static double[] copy(double[] array) {
        if (array == null) return new double[0];
        double[] newArray = new double[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static float[] copy(float[] array) {
        if (array == null) return new float[0];
        float[] newArray = new float[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static long[] toPrimitive(Long[] array) {
        if (array == null) return new long[0];
        long[] newArray = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static int[] toPrimitive(Integer[] array) {
        if (array == null) return new int[0];
        int[] newArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static short[] toPrimitive(Short[] array) {
        if (array == null) return new short[0];
        short[] newArray = new short[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static byte[] toPrimitive(Byte[] array) {
        if (array == null) return new byte[0];
        byte[] newArray = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static double[] toPrimitive(Double[] array) {
        if (array == null) return new double[0];
        double[] newArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static float[] toPrimitive(Float[] array) {
        if (array == null) return new float[0];
        float[] newArray = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static Long[] toObject(long[] array) {
        if (array == null) return new Long[0];
        Long[] newArray = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static Integer[] toObject(int[] array) {
        if (array == null) return new Integer[0];
        Integer[] newArray = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static Short[] toObject(short[] array) {
        if (array == null) return new Short[0];
        Short[] newArray = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static Byte[] toObject(byte[] array) {
        if (array == null) return new Byte[0];
        Byte[] newArray = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static Double[] toObject(double[] array) {
        if (array == null) return new Double[0];
        Double[] newArray = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static Float[] toObject(float[] array) {
        if (array == null) return new Float[0];
        Float[] newArray = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }

    public static String toNumbers(byte[] bytes, String separator) {
        return toNumbers(bytes, 0, bytes.length, separator);
    }

    public static String toHexs(byte[] bytes, String separator) {
        return toHexs(bytes, 0, bytes.length, separator);
    }

    public static String toNumbers(byte[] bytes, int offset, int count, String separator) {
        StringBuilder s = new StringBuilder();
        int end = offset + count;
        for (int i = offset; i < end; i++) {
            if (i > offset) s.append(separator);
            s.append(bytes[i]);
        }
        return s.toString();
    }

    public static String toHexs(byte[] bytes, int offset, int count, String separator) {
        StringBuilder s = new StringBuilder();
        int end = offset + count;
        for (int i = offset; i < end; i++) {
            if (i > offset) s.append(separator);
            s.append(Integer.toHexString(bytes[i]));
        }
        return s.toString();
    }

    public static <T> String join(T[] array, String separator) {
        return join(array, 0, array.length, separator);
    }

    public static <T> String join(T[] array, int offset, int count, String separator) {
        StringBuilder s = new StringBuilder();
        int end = offset + count;
        for (int i = offset; i < end; i++) {
            if (i > offset) s.append(separator);
            s.append(array[i]);
        }
        return s.toString();
    }

    public static <T> String[] toUpperCase(T[] array, Locale locale, boolean trim) {
        String[] newArray = null;
        if (array != null) {
            if (array instanceof String[]) {
                newArray = (String[]) array;
            } else {
                newArray = new String[array.length];
            }
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    newArray[i] = null;
                } else {
                    newArray[i] = array[i].toString();
                    if (trim) newArray[i] = newArray[i].trim();
                    newArray[i] = newArray[i].toUpperCase(locale);
                }
            }
        }
        return newArray;
    }

    public static <T> String[] toLowerCase(T[] array, Locale locale, boolean trim) {
        String[] newArray = null;
        if (array != null) {
            if (array instanceof String[]) {
                newArray = (String[]) array;
            } else {
                newArray = new String[array.length];
            }
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    newArray[i] = null;
                } else {
                    newArray[i] = array[i].toString();
                    if (trim) newArray[i] = newArray[i].trim();
                    newArray[i] = newArray[i].toLowerCase(locale);
                }
            }
        }
        return newArray;
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length <= 0;
    }

    public static <T extends Collection> boolean isEmpty(T collection) {
        return collection == null || collection.size() <= 0;
    }

    public static <T> boolean contains(T elem, T[] array) {
        if (array != null && array.length > 0) {
            for (T ds : array) {
                if (elem.equals(ds)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> boolean contains(T elem, Collection<T> collection) {
        if (collection != null && collection.size() > 0) {
            for (T ds : collection) {
                if (elem.equals(ds)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String[] toPathArray(File[] files) {
        if (files != null && files.length > 0) {
            String[] paths = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                paths[i] = files[i].getPath();
            }
            return paths;
        }
        return null;
    }
}
