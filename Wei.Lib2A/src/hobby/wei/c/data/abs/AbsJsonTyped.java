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

package hobby.wei.c.data.abs;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class AbsJsonTyped<T extends AbsJsonTyped<T>> extends AbsJson<T> {
	private String parseType(JSONObject json) {
		final String typeKey = typeKey();
		if (json.has(typeKey)) {
			try {
				return json.getString(typeKey);
			} catch (JSONException e) {}
		}
		return null;
	}

	private boolean equalsTypeValue(String type) {
		for (String t : typeValues()) {
			if (t.equals(type)) return true;
		}
		return false;
	}

	@Override
	public boolean isBelongToMe(JSONObject json) {
		final String type = parseType(json);
		return type != null && equalsTypeValue(type);
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
