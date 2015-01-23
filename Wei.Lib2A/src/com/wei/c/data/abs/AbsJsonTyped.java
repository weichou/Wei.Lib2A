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

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.reflect.TypeToken;
import com.wei.c.L;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class AbsJsonTyped<T extends AbsJsonTyped<T>> extends AbsJson<T> {
	private String absDataBelongToType;	//子类变量名不能与本变量名重复，否则gson反序列化会报错，这里取复杂一点防止重复

	protected boolean parseType(JSONObject json) {
		String typeKey = typeKey();
		if (json.has(typeKey)) {
			try {
				absDataBelongToType = json.getString(typeKey);
				return absDataBelongToType != null;
			} catch (JSONException e) {
				L.e(this, e);
			}
		}
		return false;
	}

	@Override
	public boolean isBelongToMe(JSONObject json) {
		return parseType(json) && equalsTypeValue();
	}

	private boolean equalsTypeValue() {
		for (String t : typeValues()) {
			if (t.equals(absDataBelongToType)) return true;
		}
		return false;
	}

	@Override
	public T fromJson(String json) {
		return fromJsonWithAllFields(json, getTypeToken());
	}

	@Override
	public String toJson() {
		return toJsonWithAllFields(this);
	}

	protected abstract String typeKey();
	protected abstract String[] typeValues();
	protected abstract TypeToken<T> getTypeToken();
}
