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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.wei.c.L;
import com.wei.c.exception.FileCreateFailureException;
import com.wei.c.file.FileUtils;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class BitmapUtils {
	public static boolean saveImage(String path, Bitmap bitmap) {
		File file = FileUtils.getFile(path);
		if(FileUtils.isExistsFile(file)) {
			FileUtils.deleteFile(file, null, false);
		}
		FileOutputStream out = null;
		try {
			FileUtils.makeFile(file.getPath(), true);
			out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			return true;
		} catch (FileCreateFailureException e) {
			L.e(BitmapUtils.class, e);
		} catch (FileNotFoundException e) {
			L.e(BitmapUtils.class, e);	//吞掉
		} catch (IOException e) {
			L.e(BitmapUtils.class, e);
		} finally {
			if(out != null) try { out.close(); }catch(Exception e) {}
		}
		return false;
	}

	public static Bitmap readImage(String path, BitmapFactory.Options opts) {
		return readImage(FileUtils.getFile(path), opts);
	}

	public static Bitmap readImage(File file, BitmapFactory.Options opts) {
		Bitmap bmp = null;
		if(FileUtils.isExistsFile(file)) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				bmp = readImage(in, opts);
			} catch (FileNotFoundException e) {
				L.e(BitmapUtils.class, e);	//吞掉，是已经存在的文件了
			} finally {
				if(in != null) try { in.close(); } catch(IOException e) {}
			}
		}
		return bmp;
	}

	public static Bitmap readImage(Context context, Uri uri, BitmapFactory.Options opts) {
		String scheme = uri.getScheme();
		Bitmap bmp = null;
		if("http".equals(scheme)) {
			bmp = readImageWithUrl(uri.toString(), opts);
		}else if(ContentResolver.SCHEME_FILE.equals(scheme)) {
			bmp = readImage(uri.getPath(), opts);
		}else if(ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			InputStream in = null;
			try {
				in = context.getContentResolver().openInputStream(uri);
				bmp = readImage(in, opts);
			} catch (FileNotFoundException e) {
				L.e(BitmapUtils.class, e);
			} finally {
				if(in != null) try { in.close(); } catch(IOException e) {}
			}
		}else {
			throw new IllegalArgumentException("不支持的Uri: " + uri.toString());
		}
		return bmp;
	}

	public static Bitmap readImageWithUrl(String url, BitmapFactory.Options opts) {
		Bitmap bmp = null;
		InputStream in = null;
		try {
			in = new URL(url).openStream();
			bmp = readImage(in, opts);
		} catch (MalformedURLException e) {
			L.e(BitmapUtils.class, e);
		} catch (IOException e) {
			L.e(BitmapUtils.class, e);
		} finally {
			if(in != null) try { in.close(); } catch(IOException e) {}
		}
		return bmp;
	}

	public static Bitmap readImage(InputStream in, BitmapFactory.Options opts) {
		return BitmapFactory.decodeStream(in, null, opts);
	}

	public static Bitmap readImage(Resources res, int drawableId, BitmapFactory.Options opts) {
		return BitmapFactory.decodeResource(res, drawableId, opts);
	}

	public static Bitmap readImage(String path, int width, int height) {
		return readImage(FileUtils.getFile(path), width, height);
	}

	public static Bitmap readImage(File file, int width, int height) {
		Options opts = new Options();
		decodeImageBounds(file, opts);
		return ensureOptsBounds(opts, width, height) ? readImage(file, opts) : readImage(file, null);
	}

	public static Bitmap readImage(Context context, Uri uri, int width, int height) {
		String scheme = uri.getScheme();
		Bitmap bmp = null;
		if("http".equals(scheme)) {
			bmp = readImageWithUrl(uri.toString(), width, height);
		}else if(ContentResolver.SCHEME_FILE.equals(scheme)) {
			bmp = readImage(uri.getPath(), width, height);
		}else if(ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			InputStream in = null;
			try {
				in = context.getContentResolver().openInputStream(uri);
				bmp = readImage(in, width, height);
			} catch (FileNotFoundException e) {
				L.e(BitmapUtils.class, e);
			} finally {
				if(in != null) try { in.close(); } catch(IOException e) {}
			}
		}else {
			throw new IllegalArgumentException("不支持的Uri: " + uri.toString());
		}
		return bmp;
	}

	public static Bitmap readImageWithUrl(String url, int width, int height) {
		Bitmap bmp = null;
		InputStream in = null;
		try {
			in = new URL(url).openStream();
			bmp = readImage(in, width, height);
		} catch (MalformedURLException e) {
			L.e(BitmapUtils.class, e);
		} catch (IOException e) {
			L.e(BitmapUtils.class, e);
		} finally {
			if(in != null) try { in.close(); } catch(IOException e) {}
		}
		return bmp;
	}

	public static Bitmap readImage(InputStream in, int width, int height) {
		Options opts = new Options();
		if(!in.markSupported()) in = new BufferedInputStream(in);
		in.mark(Integer.MAX_VALUE);	//无论多大都得缓存，否则无法解析出完整的图片，但是大小肯定不会超限，最多只是相当于把一张图片无压缩的全部读到内存
		//由于BitmapFactory.decodeStream()也会有in.mark()覆盖掉前面的mark()，所以重新new一个
		InputStream in2 = new BufferedInputStream(in);	//in2不能关闭，否则in也会随之关闭
		decodeImageBounds(in2, opts);
		try { in.reset(); } catch (IOException e) { L.e(BitmapUtils.class, e); }
		return ensureOptsBounds(opts, width, height) ? readImage(in, opts) : readImage(in, null);
	}

	public static Bitmap readImage(Resources res, int drawableId, int width, int height) {
		Options opts = new Options();
		decodeImageBounds(res, drawableId, opts);
		if(!ensureOptsBounds(opts, width, height)) {
			opts = null;
		}
		return BitmapFactory.decodeResource(res, drawableId, opts);
	}

	public static int[] decodeImageBounds(String path) {
		return decodeImageBounds(path, null);
	}

	private static int[] decodeImageBounds(String path, Options opt) {
		return decodeImageBounds(FileUtils.getFile(path), opt);
	}

	public static int[] decodeImageBounds(File file) {
		return decodeImageBounds(file, null);
	}

	private static int[] decodeImageBounds(File file, Options opt) {
		if(opt == null) opt = new Options();
		opt.inJustDecodeBounds = true;
		readImage(file, opt);
		L.i(BitmapUtils.class, "opt.mCancel:"+opt.mCancel+", opt.outWidth:"+opt.outWidth+", opt.outHeight:"+opt.outHeight);

		return new int[] {opt.outWidth, opt.outHeight};
	}

	public static int[] decodeImageBounds(Resources res, int drawableId) {
		return decodeImageBounds(res, drawableId, null);
	}

	private static int[] decodeImageBounds(Resources res, int drawableId, Options opt) {
		if(opt == null) opt = new Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, drawableId, opt);
		L.i(BitmapUtils.class, "opt.mCancel:"+opt.mCancel+", opt.outWidth:"+opt.outWidth+", opt.outHeight:"+opt.outHeight);

		return new int[] {opt.outWidth, opt.outHeight};
	}

	public static int[] decodeImageBounds(InputStream in) {
		return decodeImageBounds(in, null);
	}

	private static int[] decodeImageBounds(InputStream in, Options opts) {
		if(opts == null) opts = new Options();
		opts.inJustDecodeBounds = true;
		readImage(in, opts);
		L.i(BitmapUtils.class, "opt.mCancel:"+opts.mCancel+", opt.outWidth:"+opts.outWidth+", opt.outHeight:"+opts.outHeight);

		return new int[] {opts.outWidth, opts.outHeight};
	}

	private static boolean ensureOptsBounds(BitmapFactory.Options opts, int width, int height) {
		if(!opts.mCancel && opts.outWidth > 0 && opts.outHeight > 0) {
			opts.inJustDecodeBounds = false;
			opts.inSampleSize = (opts.outWidth*10/width + opts.outHeight*10/height)/20;
			return true;
		}
		return false;
	}

	public static byte[] bmpToBytes(Bitmap bmp, boolean recycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (recycle) bmp.recycle();
		byte[] bytes = output.toByteArray();
		try { output.close(); } catch (Exception e) {}
		return bytes;
	}

	public static Bitmap bytesToBmp(byte[] bytes) {
		return readImage(new ByteArrayInputStream(bytes), null);
	}

	/** 将图像压缩到指定大小以下。注意：只能生成jpg，因为压缩只对jpg有效。**/
	public static byte[] compressToSize(Bitmap bmp, int bytesLength, boolean recycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		//boolean first = true;
		int i = 0;
		do {
			L.d(BitmapUtils.class, "compressToSize-----i:" + i);
			bmp.compress(CompressFormat.JPEG, 100 - i, output);
			if (output.size() > bytesLength) {
				/*if (first) {
					first = false;
					i = (int) (bytesLength * 100.0f / output.size() * 2 / 3);	//实际结果跟这个参数不成比例，因此不这样计算
					if (i >= 100) i = 90;
					if (i <= 0) i = 1;
				}*/
				output.reset();
			} else {
				break;
			}
		} while (++i < 100);
		if (recycle) bmp.recycle();
		byte[] bytes = output.toByteArray();
		try { output.close(); } catch (Exception e) {}
		L.d(BitmapUtils.class, "compressToSize-----bytes.length:" + bytes.length);
		return bytes;
	}

	public static Bitmap blur(Context context, Bitmap srcBmp, float radius) {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 ?
				createBlurBmpWithCommonCompute(srcBmp, (int)(radius)) :
					createBlurBmpWithRenderScript(context, srcBmp, radius);
	}

	/**创建一个虚化效果的Bitmap对象，使用了RenderScript，但是要求最低SDK版本号为17.**/
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static Bitmap createBlurBmpWithRenderScript(Context context, Bitmap srcBmp, float radius) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) throw new IllegalStateException("要求最低SDK版本号为17");
		if (radius < 1) return null;
		Bitmap bitmap = Bitmap.createBitmap(srcBmp.getWidth(), srcBmp.getHeight(), srcBmp.getConfig());	//srcBmp.copy(srcBmp.getConfig(), true);

		RenderScript rs = RenderScript.create(context);
		Allocation alloc = Allocation.createFromBitmap(rs, srcBmp);
		ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, alloc.getElement());
		blur.setInput(alloc);
		blur.setRadius(radius);
		blur.forEach(alloc);
		alloc.copyTo(bitmap);
		rs.destroy();
		return bitmap;
	}

	/*
	 * This method was copied from http://stackoverflow.com/a/10028267/694378.
	 * The only modifications I've made are to remove a couple of Log
	 * statements which could slow things down slightly.
	 */
	/**创建一个虚化效果的Bitmap对象，使用了普通的数学运算，效率不是最优。**/
	public static Bitmap createBlurBmpWithCommonCompute(Bitmap srcBmp, int radius) {
		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		if (radius < 1) return null;

		Bitmap bitmap = srcBmp.copy(srcBmp.getConfig(), true);

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
			sir[1] = (p & 0x00ff00) >> 8;
		sir[2] = (p & 0x0000ff);
		rbs = r1 - Math.abs(i);
		rsum += sir[0] * rbs;
		gsum += sir[1] * rbs;
		bsum += sir[2] * rbs;
		if (i > 0) {
			rinsum += sir[0];
			ginsum += sir[1];
			binsum += sir[2];
		} else {
			routsum += sir[0];
			goutsum += sir[1];
			boutsum += sir[2];
		}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
			sir[1] = (p & 0x00ff00) >> 8;
			sir[2] = (p & 0x0000ff);

			rinsum += sir[0];
			ginsum += sir[1];
			binsum += sir[2];

			rsum += rinsum;
			gsum += ginsum;
			bsum += binsum;

			stackpointer = (stackpointer + 1) % div;
			sir = stack[(stackpointer) % div];

			routsum += sir[0];
			goutsum += sir[1];
			boutsum += sir[2];

			rinsum -= sir[0];
			ginsum -= sir[1];
			binsum -= sir[2];

			yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return bitmap;
	}
}
