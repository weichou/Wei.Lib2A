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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import hobby.wei.c.anno.proguard.KeepMp$e;
import hobby.wei.c.anno.proguard.KeepVp$e;

/**
 * Json序列化和反序列化的Gson封装。静态变量会被忽略掉，不受影响。
 *
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 * @version 1.1，28/07/2016 增加对时间时区的反序列化支持。
 */
@KeepVp$e
@KeepMp$e
public abstract class AbsJson<T> implements IJson<T> {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";    //1970-01-01 00:00:00.000+0000
    private static final String DATE_FORMAT_v1_0 = "yyyy-MM-dd HH:mm:ss:SSS";

    private static final GsonBuilder sGsonBuilderNormal = newGsonBuilder();
    private static final GsonBuilder sGsonBuilderExpose = newGsonBuilder().excludeFieldsWithoutExposeAnnotation();

    public static GsonBuilder newGsonBuilder() {
        return new GsonBuilder()
                //.excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
                .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
                .serializeNulls()    //null值也进行输出
                .disableHtmlEscaping()    //取消unicode及等号的转义
                .setDateFormat(DATE_FORMAT)    //时间转化为特定格式
                // .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)	//会把字段首字母大写
                .registerTypeAdapter(Date.class, new DateSeri())
                .registerTypeAdapter(Calendar.class, new CalendarSeri())
                ;
    }

    public static class DateSeri extends TimeSeri<Date> {
        @Override
        public JsonElement serialize(Date time, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(serialize(time));
        }

        @Override
        public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return deserialize(json.getAsString());
        }
    }

    public static class CalendarSeri extends TimeSeri<Calendar> {
        @Override
        public JsonElement serialize(Calendar time, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(serialize(time.getTime()));
        }

        @Override
        public Calendar deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(deserialize(json.getAsString()).getTime());
            return calendar;
        }
    }

    public static abstract class TimeSeri<T> implements JsonSerializer<T>, JsonDeserializer<T> {
        /**
         * 关于时区的若干解释：
         * <p/>
         * CST仅仅指美国中部时间。
         * 美国横跨西五区至西十区，共六个时区。每个时区对应一个标准时间，从东向西分别为东部时间(EST)(西五区时间)、
         * 中部时间(CST)(西六区时间)、山地时间(MST)(西七区时间)、太平洋时间(西部时间)(PST)(西八区时间)、
         * 阿拉斯加时间(AKST)(西九区时间)和夏威夷时间(HST)(西十区时间)，按照“东早西晚”的规律，各递减一小时；
         * <p/>
         * DST「夏日节约时间」(Daylight Saving Time)，英国称「夏令时」(Summer Time)；
         * <p/>
         * GMT「格林威治标准时间」(Greenwich Mean Time)，时区划分的由来，从1970年开始；
         * <p/>
         * UTC「协调世界时」(Universal Time Coordinated)，与太阳保持精确同步，有闰秒的概念（其误差值必须保持在0.9秒以内，
         * 若大于0.9秒则由位于巴黎的国际地球自转事务中央局发布闰秒），从1900年开始。
         *
         * @param time 必须是符合{@link #DATE_FORMAT}或{@link #DATE_FORMAT_v1_0}这种格式的时间字符串。当然v1.0会返回0时区。
         * @return 时区
         */
        public static TimeZone parseTimeZone(String time) {
            return TimeZone.getTimeZone("GMT" + time.substring(DATE_FORMAT.length() - 1));
        }

        public static SimpleDateFormat formatter() {
            //序列化成这种结构（符号跟Locale有关）：1970-01-01 00:00:00.000+0000
            return new SimpleDateFormat(DATE_FORMAT.substring(0, DATE_FORMAT.length() - 1), Locale.US);
        }

        public static SimpleDateFormat formatter_v1_0() {
            return new SimpleDateFormat(DATE_FORMAT_v1_0, Locale.US);
        }

        public static String serialize(Date date) {
            final SimpleDateFormat sdf = formatter();
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date);
        }

        public static Date deserialize(String time) throws JsonParseException {
            if (time.length() == DATE_FORMAT.length()) {
                final SimpleDateFormat sdf = formatter();
                sdf.setTimeZone(parseTimeZone(time));
                try {
                    return sdf.parse(time);
                } catch (ParseException e) {
                    throw new JsonParseException(e);
                }
            } else if (time.length() == DATE_FORMAT_v1_0.length()) { // 兼容v1.0
                final SimpleDateFormat sdf = formatter_v1_0();
                sdf.setTimeZone(TimeZone.getDefault());
                try {
                    return sdf.parse(time);
                } catch (ParseException e) {
                    throw new JsonParseException(e);
                }
            }
            throw new JsonParseException(String.format("日期格式不符合规则：%s, %s", DATE_FORMAT, time));
        }
    }

    public static <D> D fromJsonWithAllFields(String json, Class<D> clazz) {
        return sGsonBuilderNormal.create().fromJson(json, clazz);
    }

    public static <D> D fromJsonWithAllFields(String json, TypeToken<D> type) {
        return sGsonBuilderNormal.create().fromJson(json, type.getType());
    }

    public static String toJsonWithAllFields(Object o) {
        return sGsonBuilderNormal.create().toJson(o);
    }

    public static <D> D fromJsonWithExposeAnnoFields(String json, Class<D> clazz) {
        return sGsonBuilderExpose.create().fromJson(json, clazz);
    }

    public static <D> D fromJsonWithExposeAnnoFields(String json, TypeToken<D> type) {
        return sGsonBuilderExpose.create().fromJson(json, type.getType());
    }

    public static String toJsonWithExposeAnnoFields(Object o) {
        return sGsonBuilderExpose.create().toJson(o);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
