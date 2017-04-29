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

import java.text.DecimalFormat;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class NumberUtils {
	public static String formatFloat(double number, int group, int maxFrac, int minFrac) {
		return getFloatFormatter(group, maxFrac, minFrac).format(number);
	}

	public static DecimalFormat getFloatFormatter(int group, int maxFrac, int minFrac) {
		DecimalFormat formatter = new DecimalFormat();
		formatter.setGroupingSize(group);	//将整数部分按group位分节
		formatter.setMaximumFractionDigits(maxFrac);	//最多maxFrac位小数
		formatter.setMinimumFractionDigits(minFrac);	//最少minFrac位小数
		return formatter;
	}

	public static int lengthOfInt(int i) {
		return Integer.toString(i).length();
	}

	public static int lengthOfFloat(double d, int maxFrac, int minFrac) {
		final String s = formatFloat(d, Integer.MAX_VALUE, maxFrac, minFrac);
		return s.indexOf('.') > 0 ? s.length() - 1 : s.length();
	}
}
