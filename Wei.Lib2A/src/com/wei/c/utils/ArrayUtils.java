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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@SuppressWarnings("unchecked")
public class ArrayUtils {

	public static <T> T[] addElement(T[] array, T element) {
		Class<?> ct = array.getClass().getComponentType();
		T[] newArray = (T[])Array.newInstance(ct, array.length + 1);
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[newArray.length-1] = element;
		return newArray;
	}

	/*并不能正常工作
	 public static <T> T[] toArray(Collection<T> collect) {
		if(collect == null) return (T[])new Object[0];
		return (T[])collect.toArray();
	}*/

	public static <T> ArrayList<T> toArrayList(T[] array) {
		ArrayList<T> list = new ArrayList<T>();
		if(array != null) {
			for(T t : array) {
				list.add(t);
			}
		}
		return list;
	}

	public static <T> ArrayList<T> toArrayList(Collection<T> collect) {
		ArrayList<T> list = new ArrayList<T>();
		if(collect != null) {
			for(T t : collect) {
				list.add(t);
			}
		}
		return list;
	}

	public static long[] copy(long[] array) {
		if(array == null) return new long[0];
		long[] newArray = new long[array.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	public static int[] copy(int[] array) {
		if(array == null) return new int[0];
		int[] newArray = new int[array.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	public static short[] copy(short[] array) {
		if(array == null) return new short[0];
		short[] newArray = new short[array.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	public static byte[] copy(byte[] array) {
		if(array == null) return new byte[0];
		byte[] newArray = new byte[array.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	public static double[] copy(double[] array) {
		if(array == null) return new double[0];
		double[] newArray = new double[array.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	public static float[] copy(float[] array) {
		if(array == null) return new float[0];
		float[] newArray = new float[array.length];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}

	public static long[] toPrimitive(Long[] array) {
		if(array == null) return new long[0];
		long[] newArray = new long[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].longValue();
		}
		return newArray;
	}

	public static int[] toPrimitive(Integer[] array) {
		if(array == null) return new int[0];
		int[] newArray = new int[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].intValue();
		}
		return newArray;
	}

	public static short[] toPrimitive(Short[] array) {
		if(array == null) return new short[0];
		short[] newArray = new short[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].shortValue();
		}
		return newArray;
	}

	public static byte[] toPrimitive(Byte[] array) {
		if(array == null) return new byte[0];
		byte[] newArray = new byte[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].byteValue();
		}
		return newArray;
	}

	public static double[] toPrimitive(Double[] array) {
		if(array == null) return new double[0];
		double[] newArray = new double[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].doubleValue();
		}
		return newArray;
	}

	public static float[] toPrimitive(Float[] array) {
		if(array == null) return new float[0];
		float[] newArray = new float[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].floatValue();
		}
		return newArray;
	}

	public static Long[] toObject(long[] array) {
		if(array == null) return new Long[0];
		Long[] newArray = new Long[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = Long.valueOf(array[i]);
		}
		return newArray;
	}

	public static Integer[] toObject(int[] array) {
		if(array == null) return new Integer[0];
		Integer[] newArray = new Integer[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = Integer.valueOf(array[i]);
		}
		return newArray;
	}

	public static Short[] toObject(short[] array) {
		if(array == null) return new Short[0];
		Short[] newArray = new Short[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = Short.valueOf(array[i]);
		}
		return newArray;
	}

	public static Byte[] toObject(byte[] array) {
		if(array == null) return new Byte[0];
		Byte[] newArray = new Byte[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = Byte.valueOf(array[i]);
		}
		return newArray;
	}

	public static Double[] toObject(double[] array) {
		if(array == null) return new Double[0];
		Double[] newArray = new Double[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = Double.valueOf(array[i]);
		}
		return newArray;
	}

	public static Float[] toObject(float[] array) {
		if(array == null) return new Float[0];
		Float[] newArray = new Float[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = Float.valueOf(array[i]);
		}
		return newArray;
	}

	public static String toNumbers(byte[] bytes, String seperator) {
		return toNumbers(bytes, 0, bytes.length, seperator);
	}

	public static String toHexs(byte[] bytes, String seperator) {
		return toHexs(bytes, 0, bytes.length, seperator);
	}

	public static String toNumbers(byte[] bytes, int offset, int count, String seperator) {
		StringBuilder s = new StringBuilder();
		int end = offset + count;
		for (int i = offset; i < end; i++) {
			if(i > offset) s.append(seperator);
			s.append(Integer.valueOf(bytes[i]));
		}
		return s.toString();
	}

	public static String toHexs(byte[] bytes, int offset, int count, String seperator) {
		StringBuilder s = new StringBuilder();
		int end = offset + count;
		for (int i = offset; i < end; i++) {
			if(i > offset) s.append(seperator);
			s.append(Integer.toHexString(bytes[i]));
		}
		return s.toString();
	}

	public static <T> String join(T[] array, String seperator) {
		return join(array, 0, array.length, seperator);
	}

	public static <T> String join(T[] array, int offset, int count, String seperator) {
		StringBuilder s = new StringBuilder();
		int end = offset + count;
		for (int i = offset; i < end; i++) {
			if(i > offset) s.append(seperator);
			s.append(array[i]);
		}
		return s.toString();
	}
}
