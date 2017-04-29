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

package hobby.wei.c.file;

import java.text.DecimalFormat;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class FsSize {
	public static final byte B		= 0;
	public static final byte KB		= 1;
	public static final byte MB		= 2;
	public static final byte GB		= 3;
	public static final byte DEF		= 4;

	private static DecimalFormat formatter;

	private final long value;
	private final boolean lowerCase;
	private final boolean singleChar;
	private final boolean limitMode;

	private byte defUnit = -1;
	private double defSize = -1;

	/**@param sizeInByte	单位是Byte**/
	public FsSize(long sizeInByte) {
		this(sizeInByte, false, false, true);
	}

	public FsSize(long sizeInByte, boolean lowerCase, boolean singleChar, boolean limitMode) {
		value = sizeInByte;
		this.lowerCase = lowerCase;
		this.singleChar = singleChar;
		this.limitMode = limitMode;
	}

	public long toByte() {
		return value;
	}
	public double toKB() {
		return formatNum(KB);
	}
	public double toMB() {
		return formatNum(MB);
	}
	public double toGB() {
		return formatNum(GB);
	}
	public double toDef() {
		if(!isDefParsed()) format(DEF);
		return defSize;
	}

	public byte getDefUnit() {
		if(!isDefParsed()) format(DEF);
		return defUnit;
	}

	@Override
	public String toString() {
		return format(DEF);
	}
	public String toByteString() {
		return format(B);
	}
	public String toKBString() {
		return format(KB);
	}
	public String toMBString() {
		return format(MB);
	}
	public String toGBString() {
		return format(GB);
	}
	public String toString(byte unit) {
		checkParamUnit(unit);
		return format(unit);
	}
	public String toStringWithDefUnitOf(FsSize fs) {
		return format(fs.getDefUnit());
	}

	private double formatNum(byte unit) {
		double size = 0;
		switch(unit) {
		case B:
			size = value;
			break;
		case KB:
			size = value/1024d;
			break;
		case MB:
			size = value/1024d/1024;
			break;
		case GB:
			size = value/1024d/1024/1024;
			break;
		}
		return size;
	}

	private String format(byte unit) {
		switch(unit) {
		case DEF:
			if(!isDefParsed()) {
				defSize = value;
				defUnit = B;
				if(defSize > 1024) {
					defSize = defSize/1024;
					defUnit = KB;
					if(defSize > 1024) {
						defSize = defSize/1024;
						defUnit = MB;
						if(defSize > 1024) {
							defSize = defSize/1024;
							defUnit = GB;
						}
					}
				}
			}
			return trimLimit(getFormatter().format(defSize)) + unitToString(defUnit);
		default:
			return trimLimit(getFormatter().format(formatNum(unit))) + unitToString(unit);
		}
	}

	public String unitToString(byte unit) {
		//checkParamUnit(unit);
		switch(unit) {
		case B:  return lowerCase ? singleChar ? "b" : "b" : singleChar ? "B" : "B";
		case KB: return lowerCase ? singleChar ? "k" : "kb" : singleChar ? "K" : "KB";
		case MB: return lowerCase ? singleChar ? "m" : "mb" : singleChar ? "M" : "MB";
		case GB: return lowerCase ? singleChar ? "g" : "gb" : singleChar ? "G" : "GB";
		default: return null;
		}
	}

	public static DecimalFormat getFormatter() {
		if(formatter == null) {
			formatter = new DecimalFormat();
			formatter.setGroupingSize(3);	//将整数部分按三位分节
			formatter.setMaximumFractionDigits(3);	//最多三位小数
		}
		return formatter;
	}

	private String trimLimit(String formatted) {
		if(limitMode) {
			int index = formatted.indexOf('.');
			if(index <= 1) {
				//...
			}else if(index == 2) {
				if(formatted.length() - index - 1 > 2) {
					formatted = formatted.substring(0, index + 3);
				}
			}else if(index == 3) {
				if(formatted.length() - index - 1 > 1) {
					formatted = formatted.substring(0, index + 2);
				}
			}else {
				formatted = formatted.substring(0, index);
			}
		}
		return formatted;
	}

	private boolean isDefParsed() {
		return defUnit >= 0 && defSize >= 0;
	}

	private static void checkParamUnit(byte unit) {
		if(unit < B || unit > DEF) throw new IllegalArgumentException("参数unit不正确，请参见常量，如KB");
	}
}
