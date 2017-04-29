/*
 * Copyright (C) 2015-present, Wei Chou (weichou2010@gmail.com)
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
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.DisplayMetrics;

import hobby.wei.c.L;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class WallpaperClipper {
    public static Bitmap clip(Context context, Uri bmpUri, boolean singleWidthMode, boolean currSumsung) throws FileNotFoundException {
        InputStream in = context.getContentResolver().openInputStream(bmpUri);
        return clip(context, BitmapUtils.readImage(in, null), singleWidthMode, currSumsung);
    }

    public static Bitmap clip(Context context, String bmpPath, boolean singleWidthMode, boolean currSumsung) throws FileNotFoundException {
        File file = new File(bmpPath);
        if (!file.exists()) throw new FileNotFoundException();
        return clip(context, BitmapUtils.readImage(file, null), singleWidthMode, currSumsung);
    }

    public static Bitmap clip(Context context, Bitmap src, boolean singleWidthMode, boolean currSumsung) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int bmpWidth = src.getWidth(), bmpHeight = src.getHeight(), screenWidth = dm.widthPixels, screenHeight = dm.heightPixels;
        if (screenWidth > screenHeight) {
            int k = screenWidth;
            screenWidth = screenHeight;
            screenHeight = k;
        }
        Bitmap dest;
        /*原始逻辑
        if (bmpWidth == bmpHeight) {
            if (currSumsung) {          //A.三星-->三星
            } else {
                if (singleWidthMode) {  //B.三星-->非三星*单屏
                } else {                //C.三星-->非三星*滚屏
                }
            }
        } else if (singleWidthMode) {
            if (currSumsung) {          //D.单屏*非三星-->三星
            } else {                    //E.单屏*非三星-->非三星
            }
        } else {
            if (currSumsung) {          //F.滚屏*非三星-->三星
            } else {                    //G.滚屏*非三星-->非三星
            }
        }*/
        //归纳合并化简如下：
        if (currSumsung) {
            if (bmpWidth == bmpHeight) {    //A.三星-->三星
                if (bmpWidth == screenHeight) {
                    dest = src;
                }
            } else {                        //B.非三星-->三星
                //先切成：单屏的长方形/正方形/介于单屏和正方形之间，再加黑色宽度到正方形
                Rect rect = clipToSquareOrAspectRatioBoundsWithNoScaleForSamsung(bmpWidth, bmpHeight, screenWidth, screenHeight);
                L.i(WallpaperClipper.class, "0----%s", L.s(rect.toShortString()));
                dest = Bitmap.createBitmap(src, rect.left, rect.top, rect.width(), rect.height());
                L.i("WallpaperClipper", "dest----0----%s, %s", dest.getWidth(), dest.getHeight());
                if (dest != src) src.recycle();
                src = dest;
                dest = createSquareBitmapFillColor(src, Color.BLACK);   //如果是正方形会直接返回src
                L.i("WallpaperClipper", "dest----1----%s, %s", dest.getWidth(), dest.getHeight());
                if (dest != src) src.recycle();
                src = dest;
            }
            dest = Bitmap.createScaledBitmap(src, screenHeight, screenHeight, true);    //注意这里两个参数都是screenHeight，意味正方形
            L.i("WallpaperClipper", "dest----2----%s, %s", dest.getWidth(), dest.getHeight());
            if (dest != src) src.recycle();
        } else {                            //C.(非)三星-->非三星
            Rect rect = clipToAspectRatioBoundsWithNoScale(bmpWidth, bmpHeight, screenWidth, screenHeight, singleWidthMode);
            L.i("WallpaperClipper", "1----%s", L.s(rect.toShortString()));
            dest = Bitmap.createBitmap(src, rect.left, rect.top, rect.width(), rect.height());
            L.i("WallpaperClipper", "dest----3----%s, %s", dest.getWidth(), dest.getHeight());
            if (dest != src) src.recycle();
            src = dest;
            dest = Bitmap.createScaledBitmap(src, rect.width() * 1.0f / rect.height() > screenWidth * 1.5f / screenHeight ? screenWidth * 2 : screenWidth, screenHeight, true);
            L.i("WallpaperClipper", "dest----4----%s, %s", dest.getWidth(), dest.getHeight());
            if (dest != src) src.recycle();
        }
        return dest;
    }

    public static Rect clipToSquareBoundsWithNoScale(int bmpWidth, int bmpHeight) {
        int left, top, right, bottom;
        int width, height;
        if (bmpWidth > bmpHeight) {
            width = height = bmpHeight;
            left = (bmpWidth - width) / 2;
            top = 0;
        } else {
            width = height = bmpWidth;
            left = 0;
            top = (bmpHeight - height) / 2;
        }
        right = left + width;
        bottom = top + height;
        return new Rect(left, top, right, bottom);
    }

    public static Rect clipToAspectRatioBoundsWithNoScale(int bmpWidth, int bmpHeight, int screenWidth, int screenHeight, boolean singleWidthMode) {
        return getBoundsWithAspectRatio(bmpWidth, bmpHeight, screenWidth * 1.0f / screenHeight, singleWidthMode);
    }

    public static Rect getBoundsWithAspectRatio(int bmpWidth, int bmpHeight, float aspectRatio, boolean singleWidthMode) {
        int left, top, right, bottom;
        float width, height;
        if ((bmpWidth * 1.0f / bmpHeight) > aspectRatio) {  //图片的纵横比大于屏幕的，则要以高为基准剪切宽度
            if (singleWidthMode) {
                width = bmpHeight * aspectRatio;
                height = bmpHeight;
                left = (int)(bmpWidth - width) / 2;
                top = 0;
            } else {
                return getBoundsWithAspectRatio(bmpWidth, bmpHeight, aspectRatio *= 2, true);
            }
        } else {    //连单屏都不够或者只够单屏，那就忽略滚屏，直接为单屏
            width = bmpWidth;
            height = bmpWidth / aspectRatio;
            left = 0;
            top = (int)(bmpHeight - height) / 2;
        }
        right = left + (int)width;
        bottom = top + (int)height;
        return new Rect(left, top, right, bottom);
    }

    public static Rect clipToSquareOrAspectRatioBoundsWithNoScaleForSamsung(int bmpWidth, int bmpHeight, int screenWidth, int screenHeight) {
        int left, top, right, bottom;
        float width, height;
        float aspectRatio = screenWidth * 1.0f / screenHeight;
        if ((bmpWidth * 1.0f / bmpHeight) > aspectRatio) {
            if (bmpWidth > bmpHeight) {
                width = height = bmpHeight; //正方形
                left = (int)(bmpWidth - width) / 2;
                top = 0;
            } else {
                width = bmpWidth;
                height = bmpHeight;
                left = 0;
                top = 0;
            }
        } else {
            width = bmpWidth;
            height = bmpWidth / aspectRatio;
            left = 0;
            top = (int)(bmpHeight - height) / 2;
        }
        right = left + (int)width;
        bottom = top + (int)height;
        return new Rect(left, top, right, bottom);
    }

    public static Bitmap createSquareBitmapFillColor(Bitmap bmp, int color) {
        if (bmp.getWidth() == bmp.getHeight()) return bmp;
        Paint paint = new Paint();
        //设置抗锯齿，三者必须同时设置效果才可以
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        int width = Math.max(bmp.getWidth(), bmp.getHeight());
        Bitmap dest = Bitmap.createBitmap(width, width, Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);
        canvas.drawColor(color);
        int left = (width - bmp.getWidth()) / 2;
        int top = (width - bmp.getHeight()) / 2;
        canvas.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), new Rect(left, top, left + bmp.getWidth(), top + bmp.getHeight()), paint);
        try { canvas.setBitmap(null); } catch (Exception e) {}
        return dest;
    }
}
