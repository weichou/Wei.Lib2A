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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;

import hobby.wei.c.L;
import hobby.wei.c.data.abs.AbsJsonTyped;
import hobby.wei.c.data.abs.TypeToken;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class PhotoUtils {
	/**
	 * 将图片保存到相册，同时保证能够立即显示在相册里。
	 * @param context
	 * @param bitmap		要保存的图像（优先）
	 * @param srcPath		要保存的原始图片路径（若bitmap不存在，则使用此路径）
	 * @param name			数据库里面的一个字段，作用不大
	 * @param description	数据库里面的一个字段，描述
	 * @return
	 */
	public static String savePictureToPhotoAlbum(Context context, Bitmap bitmap, String srcPath, String name, String description) {
		String desPath = null;
		Cursor cursor = null;
		try {
			ContentResolver cr = context.getContentResolver();
			String uri;
			if (bitmap != null) {
				uri = MediaStore.Images.Media.insertImage(cr, bitmap, name, description);
			} else {
				uri = MediaStore.Images.Media.insertImage(cr, srcPath, name != null ? name : new File(srcPath).getName(), description);
			}
			L.i(PhotoUtils.class, "MediaStore.Images.Media.insertImage:%s", L.s(uri));

			cursor = MediaStore.Images.Media.query(cr, Uri.parse(uri), new String[]{MediaStore.Images.Media.DATA});
			if (cursor.moveToNext()) {
				desPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			}
		} catch (Exception e) {
			L.e(PhotoUtils.class, e);
		} finally {
			if (cursor != null) cursor.close();
		}
		if (desPath != null) notifyPhotoChange(context, desPath);
		return desPath;
	}

	public static void notifyPhotoChange(Context context, String photoPath) {
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(photoPath))));
	}

	public static Session openSysCamaraToTakeImageCapture(Activity activity, int requestCode, String outputFilePath, CropArgs cropArgs) {
	    try {
	        final Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(outputFilePath)));
	        activity.startActivityForResult(i, requestCode);
	        return new Session(requestCode, cropArgs);
	    } catch (Exception e) {
	        L.e(PhotoUtils.class, "[openSysCamaraToTakeImageCapture]INTENT_ACTION_STILL_IMAGE_CAMERA", e);
	        return null;
	    }
	}

    public static boolean openSysCamara(Context context) {
        try {
            final Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            L.e(PhotoUtils.class, "[openSysCamara]INTENT_ACTION_STILL_IMAGE_CAMERA", e);
            return false;
        }
    }

    /**
     * 尝试调起锁屏之上的相机。
     * @param context
     */
    public static boolean openSysCamaraSecure(Context context) {
        try {
            final Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            L.e(PhotoUtils.class, "[openSysCamaraSecure]INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE", e);
            return false;
        }
    }

	public static int openSysCamaraPure(Context context) {
        try {   //尝试调起锁屏之上的相机
            final Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return 0;
        } catch (Exception e) {
            L.e(PhotoUtils.class, "[openSysCamaraPure]INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE", e);
            try {
                final Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return 1;
            } catch (Exception e1) {
                L.e(PhotoUtils.class, "[openSysCamaraPure]INTENT_ACTION_STILL_IMAGE_CAMERA", e1);
                return 2;
            }
        }
	}

	public static Session openSysGallery2ChoosePhoto(Activity activity, int requestCode, CropArgs cropArgs) {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("image/*");
		i.putExtra("return-data", false); //true会把图像bitmap对象的数据传回来，太大，忌用
		activity.startActivityForResult(i, requestCode);
		return new Session(requestCode, cropArgs);
	}

	public static void openSysPhotoZoomTool(Activity activity, Uri uri, CropArgs cropArgs) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");

		intent.putExtra("crop", true);	//crop=true 有这句才能出来最后的裁剪页面
		intent.putExtra("return-data", false); //true会把图像bitmap对象的数据传回来，太大，忌用
		intent.putExtra("output", Uri.fromFile(new File(cropArgs.outputFilePath)));
		intent.putExtra("outputFormat", cropArgs.format.name());

		//加上下面的这两句之后，图片就会被拉伸了，就没有黑边了
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", true);

		if (cropArgs.circleCrop) intent.putExtra("circleCrop", true);	//剪裁成圆形
		if (!cropArgs.faceDetection) intent.putExtra("noFaceDetection", true); //关闭人脸检测

		boolean outputLimit = false;
		if (cropArgs.aspectX > 0 && cropArgs.aspectY > 0) {
			intent.putExtra("aspectX", cropArgs.aspectX);	//这两项为裁剪框的比例s
			intent.putExtra("aspectY", cropArgs.aspectY);	//x:y=1.25
		} else if (cropArgs.boundsOutOfLimit(activity, uri)) {
			intent.putExtra("aspectX", cropArgs.limitWidth);
			intent.putExtra("aspectY", cropArgs.limitHeight);
			outputLimit = true;
		}
		if (outputLimit) {
			intent.putExtra("outputX", cropArgs.limitWidth);
			intent.putExtra("outputY", cropArgs.limitHeight);
		} else if (cropArgs.outputX > 0 && cropArgs.outputY > 0) {
			intent.putExtra("outputX", cropArgs.outputX);	//裁剪区的宽，可不设置，则会按照实际尺寸输出
			intent.putExtra("outputY", cropArgs.outputY);	//裁剪区的高
		}
		activity.startActivityForResult(intent, cropArgs.requestCode);
	}

	public static Uri onActivityResult(Activity activity, Session session, int requestCode, int resultCode, Intent data) {
		Uri uri = data != null ? data.getData() : null;
		L.i(PhotoUtils.class, "uri:%s", uri);
		if (resultCode == Activity.RESULT_OK) {
			if (uri != null) {
				if (requestCode == session.requestCode) {
					L.i(PhotoUtils.class, "pick photo result:%s", uri);
					if (session.cropArgs != null && session.cropArgs.mustCrop(activity, uri)) {
						File outputFile = new File(session.cropArgs.outputFilePath);
						if (outputFile.exists()) outputFile.delete();
						openSysPhotoZoomTool(activity, uri, session.cropArgs);
						uri = null;
					}
				} else if (session.cropArgs != null && requestCode == session.cropArgs.requestCode) {
					L.i(PhotoUtils.class, "crop photo result:%s", uri);
				} else {
				    uri = null;
				}
			} else {
				if (session.cropArgs != null && requestCode == session.cropArgs.requestCode) {
					L.i(PhotoUtils.class, "crop photo result:%s", uri);
					File outputFile = new File(session.cropArgs.outputFilePath);
					if (outputFile.exists()) {
						uri = Uri.fromFile(outputFile);
						L.i(PhotoUtils.class, "Uri.fromFile:%s", uri);
					}
				}
			}
		}
		return uri;
	}

	public static class Session extends AbsJsonTyped<Session> {
		private static final String type = "session";

		public final int requestCode;
		public final CropArgs cropArgs;
		private Session(int requestCode, CropArgs cropArgs) {
			//if (crop && cropArgs == null) throw new IllegalArgumentException("crop为true时，cropArgs不能为空");
			if (cropArgs != null && cropArgs.requestCode == requestCode) throw new IllegalArgumentException("cropArgs.requestCode不能与requestCode相等");
			this.requestCode = requestCode;
			this.cropArgs = cropArgs;
		}

		@Override
		protected String typeKey() {
			return "type";
		}

		@Override
		protected String[] typeValues() {
			return new String[] {type};
		}

		@Override
		protected TypeToken<Session> getTypeToken() {
			return new TypeToken<Session>(){};
		}
	}

	public static class CropArgs {
		public final int requestCode;
		public final String outputFilePath;
		public final Bitmap.CompressFormat format;
		public final boolean circleCrop;
		/**如果两个参数都不为0，则视为保持裁切的纵横比，那么就必须裁切。但是裁切的输出尺寸可能根据实际裁切的尺寸，也可能根据设置的输出尺寸进行拉伸。**/
		public final int aspectX;
		public final int aspectY;
		/**如果两个参数都不为0，则必须裁切，视为将裁切之后的内容拉伸为该尺寸进行保存。**/
		public final int outputX;
		public final int outputY;
		/**如果其他条件没有强制裁切，则如果图片尺寸任意一边大于该尺寸时进行裁切，且设置裁切纵横比为该比例，输出尺寸为该尺寸。**/
		public final int limitWidth;
		public final int limitHeight;
		/**人脸检测**/
		public final boolean faceDetection;
		///////////////////////
		private boolean boundsOutOfLimit = false;
		private boolean boundsTested = false;
		///////////////////////
		public CropArgs(int requestCode, String outputFilePath, Bitmap.CompressFormat format,
				boolean circleCrop, boolean faceDetection,
				int aspectX, int aspectY, int outputX, int outputY, int limitWidth, int limitHeight) {
			this.requestCode = requestCode;
			this.outputFilePath = outputFilePath;
			this.format = format;
			this.circleCrop = circleCrop;
			this.faceDetection = faceDetection;
			this.aspectX = aspectX;
			this.aspectY = aspectY;
			this.outputX = outputX;
			this.outputY = outputY;
			this.limitWidth = limitWidth;
			this.limitHeight = limitHeight;
		}

		public boolean mustCrop(Context ctx, Uri uri) {
			return aspectX > 0 && aspectY > 0 || outputX > 0 && outputY > 0 || boundsOutOfLimit(ctx, uri);
		}

		public boolean boundsOutOfLimit(Context ctx, Uri uri) {
			if (boundsTested) return boundsOutOfLimit;
			boundsOutOfLimit = false;
			if (limitWidth > 0 && limitHeight > 0) {
				try {
					int[] bounds = BitmapUtils.decodeImageBounds(ctx.getContentResolver().openInputStream(uri));
					L.w(PhotoUtils.class, "width:%s, height:%s", bounds[0], bounds[1]);
					boundsOutOfLimit = bounds[0] > limitWidth || bounds[1] > limitHeight;
				} catch (FileNotFoundException e) {
					L.e(PhotoUtils.class, e);
				}
			}
			boundsTested = true;
			return boundsOutOfLimit;
		}
	}
}
