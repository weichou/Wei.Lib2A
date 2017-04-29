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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class ReflectUtils {
	public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClazz = clazz.getSuperclass();
			if (superClazz != null) {
				return getField(superClazz, fieldName);
			}
			throw e;
		}
	}

	public static List<Field> getFields(Class<?> clazz, Class<?> stopSearch) {
		List<Field> method = new ArrayList<Field>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			method.add(field);
		}
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && (stopSearch == null || stopSearch.isAssignableFrom(superClazz))) {
			method.addAll(getFields(superClazz, stopSearch));
		}
		return method;
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		try {
			return clazz.getDeclaredMethod(methodName, parameterTypes);
		} catch (NoSuchMethodException e) {
			Class<?> superClazz = clazz.getSuperclass();
			if (superClazz != null) {
				return getMethod(superClazz, methodName, parameterTypes);
			}
			throw e;
		}
	}

	public static List<Method> getMethods(Class<?> clazz, Class<?> stopSearch) {
		List<Method> list = new ArrayList<Method>();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			list.add(method);
		}
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && (stopSearch == null || stopSearch.isAssignableFrom(superClazz))) {
			list.addAll(getMethods(superClazz, stopSearch));
		}
		return list;
	}

	public static Object getFieldValue(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = getField(object.getClass(), fieldName);
		field.setAccessible(true);
		return field.get(object);
	}

	/**获取基本类型变量值，对于复合类型将返回null**/
	public static final String getBasicFieldValue(Field field, Object object) {
		String value = null;
		try {
			Class<?> fieldType = field.getType();
			if(fieldType.isPrimitive()
					|| String.class.isAssignableFrom(fieldType)
					|| Byte.class.isAssignableFrom(fieldType)
					|| Character.class.isAssignableFrom(fieldType)
					|| Integer.class.isAssignableFrom(fieldType)
					|| Long.class.isAssignableFrom(fieldType)
					|| BigInteger.class.isAssignableFrom(fieldType)
					|| Float.class.isAssignableFrom(fieldType)
					|| Double.class.isAssignableFrom(fieldType)
					|| BigDecimal.class.isAssignableFrom(fieldType)) {
				/*if(!field.isAccessible()) {	//不论是否public的都返回false
						field.setAccessible(true);
					}*/
				field.setAccessible(true);	//取消存取检查
				Object valueObj = field.get(object);
				field.setAccessible(false);	//启用存取检查
				value = valueObj==null ? null : valueObj.toString();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return value;
	}
}
