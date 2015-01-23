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

package com.wei.c.data.abs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Json序列化和反序列化的Gson封装。静态变量会被忽略掉，不受影响。
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class AbsJson<T> implements IJson<T> {
	public static final String DATE_FORMAT			= "yyyy-MM-dd HH:mm:ss:SSS";
	private static final GsonBuilder sGsonBuilder	= new GsonBuilder()
	.excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
	.enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
	.serializeNulls()	//null值也进行输出
	.disableHtmlEscaping()	//取消unicode及等号的转义
	.setDateFormat(DATE_FORMAT)	//时间转化为特定格式
	//.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)	//会把字段首字母大写
	;

	public static <D> D fromJsonWithAllFields(String json, Class<D> clazz) {
		return new Gson().fromJson(json, clazz);
	}

	public static <D> D fromJsonWithAllFields(String json, TypeToken<D> type) {
		return new Gson().fromJson(json, type.getType());
	}

	public static String toJsonWithAllFields(Object o) {
		return new Gson().toJson(o);
	}

	public static <D> D fromJsonWithExposeAnnoFields(String json, Class<D> clazz) {
		return sGsonBuilder.create().fromJson(json, clazz);
	}

	public static <D> D fromJsonWithExposeAnnoFields(String json, TypeToken<D> type) {
		return sGsonBuilder.create().fromJson(json, type.getType());
	}

	public static String toJsonWithExposeAnnoFields(Object o) {
		return sGsonBuilder.create().toJson(o);
	}
}
