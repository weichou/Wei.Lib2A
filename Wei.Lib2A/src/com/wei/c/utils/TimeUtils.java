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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtils {
	public static Date parseDate(String dateString) {
		Date date = null;
		//可以解析这种时间
		//"2013-05-22T09:16:44.871589GMT+08:00"
		//"2013-05-22T09:16:44.871589+0800"
		//但是不可以解析
		//"2013-05-22T09:16:44.871589GMT+0800"
		//"2013-05-22T09:16:44.871589+08:00"(这是.net生成的格式化时间字符串)
		int indexM = dateString.lastIndexOf(':');
		int index_ = dateString.lastIndexOf('+');
		if(index_<=0) index_ = dateString.lastIndexOf('-');
		String format;
		if(index_>19) {
			if(dateString.contains("GMT")) {
				int indexG = dateString.indexOf("GMT");
				int millen = indexG-20;
				if(millen >= 0) {
					format = "yyyy-MM-dd'T'HH:mm:ss.";
					for(int i=0; i<millen; i++) {
						format += "S";
					}
					format += "Z";
				}else {
					throw new RuntimeException("格式不正确："+dateString);
				}
				if(indexM>index_ && index_+5==dateString.length()-1) {
					//正常
				}else if(indexM<index_ && index_+4 == dateString.length()-1) {	//没有冒号的时区
					//去掉GMT
					dateString = dateString.substring(0, indexG) + dateString.substring(indexG+3, dateString.length());
				}else {
					format = "yyyy-MM-dd'T'HH:mm:ss";
					dateString = dateString.substring(0, 19);
				}
			}else {
				int millen = index_-20;
				if(millen >= 0) {
					format = "yyyy-MM-dd'T'HH:mm:ss.";
					for(int i=0; i<millen; i++) {
						format += "S";
					}
					format += "Z";
				}else {
					throw new RuntimeException("格式不正确："+dateString);
				}
				if(indexM>index_ && index_+5==dateString.length()-1) {	//.net字符串，去掉最后一个冒号
					dateString = dateString.substring(0, indexM) + dateString.substring(indexM+1, dateString.length());
				}else if(indexM<index_ && index_+4 == dateString.length()-1) {
					//正常
				}else {
					format = "yyyy-MM-dd'T'HH:mm:ss";
					dateString = dateString.substring(0, 19);
				}
			}
		}else {
			format = "yyyy-MM-dd'T'HH:mm:ss";
			dateString = dateString.substring(0, 19);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			date = sdf.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static String toYMDHM(long timeMillis) {
		return format(timeMillis, "yyyy年MM月dd日HH:mm时", false);
	}

	public static String toYMDH(long timeMillis) {
		return format(timeMillis, "yyyy年MM月dd日HH时", false);
	}

	/**
	 * @param timeMillis
	 * @param format
	 * @param clearTimeZone 清除时区。有时候只是格式化时间数字，不希望把时区换算出来
	 * @return
	 */
	public static String format(long timeMillis, String format, boolean clearTimeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		if (clearTimeZone) sdf.setTimeZone(TimeZone.getTimeZone("GMT+00"));
		return sdf.format(new Date(timeMillis));
	}

	public static int getAge(long birthdayTimeMillis) {
		return Integer.valueOf(format(System.currentTimeMillis() - birthdayTimeMillis, "yyyy", true)) - 1970 + 1;
	}

	public static final String PATTERN_NO_SEP			= "yyyyMMddHHmmss";
	public static final long ZERO_MILLIS_YEAR		= Long.valueOf(format(0, PATTERN_NO_SEP, true));	//19700101000000
	public static final long REACH_YEAR				= 10000000000L;										//00010000000000	//注意不能写成这样，会当做八进制处理
	public static final int REACH_MONTH				= 100000000;										//00000100000000
	public static final int REACH_DAY					= 1000000;											//00000001000000
	public static final int REACH_HOUR				= 10000;											//00000000010000
	public static final int REACH_MINUTE				= 100;												//00000000000100

	public static String getTimeAgo(long timeMillis) {
		return getTimeAgo(timeMillis, "年", "个月", "天", "小时", "分钟", "刚刚", "以前");
	}

	public static String getTimeAgo(long timeMillis, String yearUnit, String monthUnit, String dayUnit, String hourUnit, String minuteUnit, String justNow, String before) {
		long timeYMD = Long.valueOf(format(System.currentTimeMillis() - timeMillis, PATTERN_NO_SEP, true), 10) - ZERO_MILLIS_YEAR;	//加上十进制是防止前面的0被认为是八进制
		System.out.println(timeYMD);
		if (timeYMD >= REACH_YEAR) {
			int year = (int)(timeYMD / REACH_YEAR);
			int month = (int)((timeYMD % REACH_YEAR) / REACH_MONTH);
			return year + yearUnit + (month == 0 ? "" : month + monthUnit) + before;
		} else if (timeYMD >= REACH_MONTH) {
			int month = (int)(timeYMD / REACH_MONTH);
			return month + monthUnit + before;
		} else if (timeYMD >= REACH_DAY) {
			int day = (int)(timeYMD / REACH_DAY);
			return day + dayUnit + before;
		} else if (timeYMD >= REACH_HOUR) {
			int hour = (int)(timeYMD / REACH_HOUR);
			return hour + hourUnit + before;
		} else if (timeYMD >= REACH_MINUTE) {
			int minute = (int)(timeYMD / REACH_MINUTE);
			if (minute >= 5) {
				return minute + minuteUnit + before;
			} else {
				return justNow;
			}
		} else {
			return justNow;
		}
	}

	public static final int DAY_NOON_MORNING				= 0;
	public static final int DAY_NOON_AM					= 1;
	public static final int DAY_NOON_NOON					= 2;
	public static final int DAY_NOON_PM					= 3;
	public static final int DAY_NOON_EVENING				= 4;
	public static final int DAY_NOON_GOOD_NIGHT			= 5;
	public static int getDayNoon() {
		int hour = Integer.valueOf(format(System.currentTimeMillis(), "HHmm", false), 10);
		if (hour >= 2100 && hour <= 2359) {
			return DAY_NOON_GOOD_NIGHT;
		} else if (hour <= 800) {
			return DAY_NOON_MORNING;
		} else if (hour <= 1100) {
			return DAY_NOON_AM;
		} else if (hour <= 1300) {
			return DAY_NOON_NOON;
		} else if (hour <= 1800) {
			return DAY_NOON_PM;
		} else {
			return DAY_NOON_EVENING;
		}
	}

	/**格式化为时分秒。这个由于小时的部分是动态的，到达小时了才显示时部分00:00:00，否则只显示到分00:00。因此直接带了单位时或分**/
	public static String[] toHMS(long timeMillis) {	//不能用format()方法
		timeMillis = timeMillis / 1000;
		int m = (int)(timeMillis / 60);
		int s = (int)(timeMillis % 60);
		int h = (int)(m / 60);
		if(h > 0) m = (int)(m % 60);
		String mm = m < 10 ? "0"+m : ""+m;
		String ss = s < 10 ? "0"+s : ""+s;
		String hh = h < 10 ? "0"+h : ""+h;
		String[] result = new String[2];
		result[0] = (h > 0 ? hh + ":" : "") + mm + ":" + ss;
		result[1] = h > 0 ? "时" : "分";
		return result;
	}

	/**格式化为分秒**/
	public static String toMS(long timeMillis) {	//不能用format()方法，因为分的部分可能超过60
		timeMillis = timeMillis / 1000;
		int m = (int)(timeMillis / 60);
		int s = (int)(timeMillis % 60);
		String mm = m < 10 ? "0"+m : ""+m;
		String ss = s < 10 ? "0"+s : ""+s;
		return mm + ":" + ss;
	}
}
