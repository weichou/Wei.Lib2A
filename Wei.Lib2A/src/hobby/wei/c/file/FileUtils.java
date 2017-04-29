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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import hobby.wei.c.L;
import hobby.wei.c.exception.FileCreateFailureException;
import hobby.wei.c.exception.SdCardNotMountedException;
import hobby.wei.c.exception.SdCardNotValidException;
import hobby.wei.c.phone.Storage.SdCard;
import hobby.wei.c.utils.ArrayUtils;

/**
 * 注意需要权限android.permission.WRITE_EXTERNAL_STORAGE，该权限包含了
 * android.permission.READ_EXTERNAL_STORAGE
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class FileUtils {
	/**
	 * String str = "/data/data/12///34\\\\///\\\\data/78";
	 * 注意\\\\表示正则表达式字符串"\\"，用于匹配转义之前的字符串"\\"，而"\\"表示一个“\”字符，
	 * 经测试必须成四个反斜杠的倍数出现才能正确匹配多个“\”字符、
	 * String[] arr0 = dirName.split("[/\\\\]+");
	 */
	public static final String separatorRegExp = "[\\s]*[/\\\\]+[\\s/\\\\]*";
	public static final String fileNameRegExp = "^[\\w%+,.=-][\\w %+,.=-]*[\\w%+,.=-]$|[\\w%+,.=-]";	//一个非点字符，或者点开头非点结尾的字符串（注意正则里面-+不能在一起，可测试）

	public static final Pattern separatorPattern = Pattern.compile(separatorRegExp);
	public static final Pattern fileNamePattern = Pattern.compile(fileNameRegExp);

	public static boolean isFileNameValid(String name) {
		return fileNamePattern.matcher(name).matches();
	}

	/**如果文件名不合法将抛异常**/
	public static void checkFileNameValid(String name) {
		if (!isFileNameValid(name)) throw new IllegalArgumentException("文件名不合法：" + name);
	}

	/**如果文件路径不合法将抛异常**/
	public static void checkFilePathValid(String path) {
		for (String name : getPathElements(path)) {
			checkFileNameValid(name);
		}
	}

	/**如果文件不存在将抛异常**/
	public static void checkFileExists(String path) {
		if (!isExistsFile(path)) throw new IllegalArgumentException("文件不存在：" + path);
	}

	/**如果文件不存在将抛异常**/
	public static void checkFileExists(File path) {
		if (!isExistsFile(path)) throw new IllegalArgumentException("文件不存在：" + path);
	}

	/**如果目录不存在将抛异常**/
	public static void checkDirExists(String dirPath) {
		if (!isExistsDir(dirPath)) throw new IllegalArgumentException("目录不存在：" + dirPath);
	}

	/**如果目录不存在将抛异常**/
	public static void checkDirExists(File dir) {
		if (!isExistsDir(dir)) throw new IllegalArgumentException("目录不存在：" + dir);
	}

	public static String adjustPathSeparator(String path) {
		return separatorPattern.matcher(path).replaceAll(File.separator);
	}

	public static String[] getPathElements(String path) {
		return getPathElementsSepAdjusted(adjustPathSeparator(path));
	}

	private static String[] getPathElementsSepAdjusted(String path) {
		String[] elements = path.split(File.separator);
		int offset = 0;
		for (String s : elements) {
			if (s != null && s.length() > 0) {	//一般在路径的第一个字符为斜杠的情况下会在第[0]个元素产生一个""
				checkFileNameValid(s);
				elements[offset++] = s;
			}
		}
		String[] dirs = new String[offset];
		System.arraycopy(elements, 0, dirs, 0, offset);
		return dirs;
	}

	public static String formatPath(String path) {
		String pathSepAdjusted = adjustPathSeparator(path);
		return formatPathWithPathElementsAndSepAdjusted(getPathElementsSepAdjusted(pathSepAdjusted), pathSepAdjusted);
	}

	//不能开放这个，仅限Api内部使用
	private static String formatPathWithPathElementsAndSepAdjusted(String[] dirs, String pathSepAdjusted) {
		String path = pathSepAdjusted;
		boolean startp = path.startsWith(File.separator);
		boolean endp = path.endsWith(File.separator);
		path = startp ? File.separator : "";
		path += formatPathWithPathElements(dirs);
		path += endp ? File.separator : "";
		return path;
	}

	//不能开放这个，仅限Api内部使用
	private static String formatPathWithPathElements(String[] dirs) {
		String path = "";
		boolean first = true;
		for (String dir : dirs) {
			if (first) {
				first = false;
			} else {
				path += File.separator;
			}
			path += dir;
		}
		return path;
	}

	/**
	 * 根据指定的路径创建文件
	 * @param filePath			文件路径
	 * @param deleteMissType	如果路径中已存在的节点（中间目录或文件）类型与需求不符是否删除
	 * @return					返回创建成功的文件或抛出异常
	 * @throws FileCreateFailureException
	 */
	public static File makeFile(String filePath, boolean deleteMissType) throws FileCreateFailureException {
		File file = getFile(filePath);
		if (isExistsFile(file)) return file;
		String pathSepAdjusted = adjustPathSeparator(filePath);
		String[] elements = getPathElementsSepAdjusted(pathSepAdjusted);
		if (elements == null || elements.length == 0) throw new FileCreateFailureException("路径为空。filePath: " + filePath + ", formatPath：" + formatPathWithPathElementsAndSepAdjusted(elements, pathSepAdjusted));
		if (elements.length == 1) {
			file = new File(elements[0]);
		} else {
			String[] dirs = new String[elements.length - 1];
			System.arraycopy(elements, 0, dirs, 0, dirs.length);
			File dir = makeDir(dirs, pathSepAdjusted.startsWith(File.separator), deleteMissType);
			file = new File(dir, elements[elements.length - 1]);
		}
		if (file.exists()) {
			if (file.isFile()) {
				return file;
			} else {
				if (deleteMissType && deleteFileOrDir(file, null, true)) {
					//下面进行了创建
				} else 
					throw new FileCreateFailureException("要创建的文件是个已存在的目录：" + file.getPath());
			}
		}
		try {
			file.createNewFile();
		} catch(IOException e) {
			throw new FileCreateFailureException(e);
		}
		if (!file.exists()) throw new FileCreateFailureException();
		return file;
	}

	private static File makeDir(String[] dirs, boolean startSep, boolean deleteMissType) throws FileCreateFailureException {
		File dir = null;
		String path = startSep ? File.separator : "";
		path += formatPathWithPathElements(dirs);
		dir = new File(path);
		if (!dir.exists()) dir.mkdirs();
		if (dir.isDirectory()) return dir;

		//dir.mkdirs();创建失败或者路径中有存在的文件而不是目录
		dir = null;
		for (int i=0; i < dirs.length; i++) {
			if (dir == null) {
				dir = new File((startSep ? File.separator : "") + dirs[i]);
			} else {
				dir = new File(dir, dirs[i]);
			}
			if (dir.exists()) {
				if (dir.isDirectory()) {
					continue;
				} else {
					if (deleteMissType && deleteFileOrDir(dir, null, true)) {
						//下面进行了创建
					} else 
						throw new FileCreateFailureException("要创建的目录是个已存在的文件：" + dir.getPath());
				}
			}
			dir.mkdir();
			if (!dir.exists()) throw new FileCreateFailureException("目录创建失败：" + dir.getPath());
		}
		return dir;
	}

	public static File makeDir(String dirPath, boolean deleteMissType) throws FileCreateFailureException {
		File dir = getDir(dirPath);
		if (isExistsDir(dir)) return dir;
		String pathSepAdjusted = adjustPathSeparator(dirPath);
		String[] elements = getPathElementsSepAdjusted(pathSepAdjusted);
		if (elements == null || elements.length == 0) throw new FileCreateFailureException("路径为空。dirPath: " + dirPath + ", formatPath：" + formatPathWithPathElementsAndSepAdjusted(elements, pathSepAdjusted));
		return makeDir(elements, pathSepAdjusted.startsWith(File.separator), deleteMissType);
	}

	public static File createAppDir(Context context, SdCard sdcard) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		File file = null;
		if (sdcard.isMounted(context)) {
			if (sdcard.isValid(context)) {
				file = makeDir(sdcard.getAppDataDir(context), true);
			} else {
				throw new SdCardNotValidException("存储卡无效，可能没有格式化");
			}
		} else {
			throw new SdCardNotMountedException("存储卡没有挂载");
		}
		return file;
	}

	public static File getFile(String path) {
		File file = new File(path);
		if (isExistsFile(file)) return file;
		return new File(formatPath(path));
	}

	public static File getDir(String dirPath) {
		File dir = new File(dirPath);
		if (isExistsDir(dir)) return dir;
		return new File(formatPath(dirPath));
	}

	public static boolean isExistsFile(String path) {
		return isExistsFile(new File(path)) || isExistsFile(new File(formatPath(path)));
	}

	public static boolean isExistsFile(File file) {
		return file.exists() && file.isFile();
	}

	public static boolean isExistsDir(String dirPath) {
		return isExistsDir(new File(dirPath)) || isExistsDir(new File(formatPath(dirPath)));
	}

	public static boolean isExistsDir(File file) {
		return file.exists() && file.isDirectory();
	}

	public static boolean isPathInDir(String path, String dirPath) {
		return isPathInDir(path, getDir(dirPath));
	}

	public static boolean isPathInDir(String path, File dir) {
		checkDirExists(dir);
		checkFilePathValid(path);
		return getFile(path).getParentFile().equals(dir);
	}

	public static boolean deleteFileOrDir(File fileOrDir, String suffix, boolean deleteRootDir) {
		return deleteFileInDirWithExcepts(fileOrDir, suffix, deleteRootDir, null, null);
	}

	/**
	 * 删除文件或者目录
	 * @param fileOrDir         要删除的文件或目录
	 * @param suffix            要删除的文件后缀，如果为空则删除全部
	 * @param deleteRootDir     是否删除当前要删除的目录树的根目录（有时只想清空目录，而不删除该目录）
	 * @param exceptFileNames   不删除的文件名列表
	 * @param exceptDirNames    不删除的目录名列表
	 */
	public static boolean deleteFileInDirWithExcepts(File fileOrDir, String suffix, boolean deleteRootDir, String[] exceptFileNames, String[] exceptDirNames) {
		suffix = suffix == null ? null : suffix.trim().toLowerCase(Locale.US);
		if (exceptFileNames != null) exceptFileNames = ArrayUtils.toLowerCase(exceptFileNames, Locale.US, true);
		if (exceptDirNames != null) exceptDirNames = ArrayUtils.toLowerCase(exceptDirNames, Locale.US, true);
		return deleteFileInDirWithExceptsInner(fileOrDir, suffix, deleteRootDir, exceptFileNames, exceptDirNames, null);
	}

    private static boolean deleteFileInDirWithExceptsInner(File root, String suffix, boolean deleteRootDir, String[] exceptFileNames, String[] exceptDirNames, AtomicBoolean excepted) {
        if (root == null || !root.exists()) return true;
        if (root.isFile()) {
            return deleteFileWithExceptsInner(root, suffix, exceptFileNames, excepted);
        } else {
            boolean result = true;
            AtomicBoolean exceptedInner = new AtomicBoolean(false);
            File[] files = root.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (equalsFileNames(file, exceptDirNames)) {
                        exceptedInner.set(true);
                    } else {
                        result &= deleteFileInDirWithExceptsInner(file, suffix, true, exceptFileNames, exceptDirNames, exceptedInner);
                    }
                } else {
                    result &= deleteFileWithExceptsInner(file, suffix, exceptFileNames, exceptedInner);
                }
            }
            if (deleteRootDir && result && !exceptedInner.get()) {
                result &= root.delete();
            } else {
                if (excepted != null) excepted.set(true);
            }
            return result;
        }
    }

    private static boolean deleteFileWithExceptsInner(File file, String suffix, String[] exceptFileNames, AtomicBoolean excepted) {
        String fileName = file.getName().toLowerCase(Locale.US);
        if (canDeleteWithSuffixInner(fileName, suffix) && !equalsFileNamesInner(fileName, exceptFileNames)) {
            return file.delete();
        } else {
            if (excepted != null) excepted.set(true);
        }
        return true;
    }

	private static boolean canDeleteWithSuffixInner(String fileName, String suffix) {
		return suffix == null || suffix.length() <= 0 || fileName.endsWith(suffix);
	}

	private static boolean equalsFileNamesInner(String fileName, String[] exceptFileNames) {
		if (exceptFileNames != null) {
			for (String exceptName : exceptFileNames) {
				if (fileName.equals(exceptName)) return true;
			}
		}
		return false;
	}

	public static boolean equalsFileName(File file, String fileName) {
		return fileName != null && file.getName().toLowerCase(Locale.US).equals(fileName.trim().toLowerCase(Locale.US));
	}

	public static boolean equalsFileNames(File file, String[] fileNames) {
		return fileNames != null && equalsFileNamesInner(file.getName().toLowerCase(Locale.US), ArrayUtils.toLowerCase(fileNames, Locale.US, true));
	}

    /**获取目录占用空间大小**/
    public static FsSize getSizeFS(String path) {
        return new FsSize(getSize(path));
    }

    public static FsSize getSizeFS(File file) {
        return new FsSize(getSize(file));
    }

    /**获取目录占用空间大小**/
    public static long getSize(String path) {
        File file = getFile(path);
        return file.exists() ? getSize(file) : 0;
    }

    public static long getSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                size += getSize(f);
            }
        } else {
            size += file.length();
        }
        return size;
    }

	public static void printFileList(File dirFile) {
		File current = dirFile;	//返回的必定是目录
		if (current == null || !current.exists()) return;
		if (current.isFile()) {
			L.i(FileUtils.class, "[printFileList][file]path:%s", L.s(current.getPath()));
		} else {
			File[] files = current.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					L.i(FileUtils.class, "[printFileList][dir]path:%s", L.s(file.getPath()));
					printFileList(file);
				} else {
					L.i(FileUtils.class, "[printFileList][file]path:%s", L.s(file.getPath()));
				}
			}
		}
	}

	public static File copyFileToDir(String pathSrc, String dirDes, String prefix, String suffix) {
		return copyFileToDir(pathSrc, dirDes, prefix, suffix, 0, 0, 0, null);
	}

	public static File copyFileToDir(String pathSrc, String dirDes, String prefix, String suffix, long contentLen, int increaseUnit, int minInterval, Callback callback) {
		File fileDes = makeNewFileInDir(dirDes, prefix, suffix);
		if (fileDes != null && copyFileToFile(pathSrc, fileDes.getPath(), contentLen, increaseUnit, minInterval, callback)) {
			return fileDes;
		}
		return null;
	}

	public static boolean copyFileToFile(String pathSrc, String pathDes) {
		return copyFileToFile(pathSrc, pathDes, 0, 0, 0, null);
	}

	public static boolean copyFileToFile(String pathSrc, String pathDes, long contentLen, int increaseUnit, int minInterval, Callback callback) {
		checkFileExists(pathSrc);
		File fileDes;
		try {
			fileDes = makeFile(pathDes, false);
		} catch (FileCreateFailureException e) {
			L.e(FileUtils.class, e);
			return false;
		}
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(getFile(pathSrc));
			out = new FileOutputStream(fileDes);
			if (!joinStream(in, out, true, contentLen, increaseUnit, minInterval, callback)) {
				fileDes.delete();
				return false;
			}
			return true;
		} catch (Exception e) {
			L.e(FileUtils.class, e);
			return false;
		} finally {
			closeIO(in);
			closeIO(out);
		}
	}

	public static File copyStreamToDir(InputStream in, String dirDes, String suffix) {
		return copyStreamToDir(in, dirDes, suffix, true, 0, 0, 0, null);
	}

	public static File copyStreamToDir(InputStream in, String dirDes, String suffix, boolean closeOnEnd, long contentLen, int increaseUnit, int minInterval, Callback callback) {
		return copyStreamToDir(in, dirDes, String.valueOf(System.currentTimeMillis()), suffix, closeOnEnd, contentLen, increaseUnit, minInterval, callback);
	}

	/**
	 * 将输入流写入到文件
	 * @param in      输入流
	 * @param dirDes  目标目录
	 * @param prefix  文件名前缀
	 * @param suffix  文件名后缀
	 * @return
	 */
	public static File copyStreamToDir(InputStream in, String dirDes, String prefix, String suffix) {
		return copyStreamToDir(in, dirDes, prefix, suffix, true, 0, 0, 0, null);
	}

	public static File copyStreamToDir(InputStream in, String dirDes, String prefix, String suffix, boolean closeOnEnd, long contentLen, int increaseUnit, int minInterval, Callback callback) {
		File fileDes = makeNewFileInDir(dirDes, prefix, suffix);
		if (fileDes != null && copyStreamToFile(in, fileDes.getPath(), closeOnEnd, contentLen, increaseUnit, minInterval, callback)) {
			return fileDes;
		}
		return null;
	}

	public static boolean copyStreamToFile(InputStream in, String pathDes) {
		return copyStreamToFile(in, pathDes, true, 0, 0, 0, null);
	}

	public static boolean copyStreamToFile(InputStream in, String pathDes, boolean closeOnEnd, long contentLen, int increaseUnit, int minInterval, Callback callback) {
		File fileDes;
		try {
			fileDes = makeFile(pathDes, false);
		} catch (FileCreateFailureException e) {
			L.e(FileUtils.class, e);
			return false;
		}
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(fileDes);
			if (!joinStream(in, out, false, contentLen, increaseUnit, minInterval, callback)) {
				fileDes.delete();
				return false;
			}
			return true;
		} catch (Exception e) {
			L.e(FileUtils.class, e);
			return false;
		} finally {
			if (closeOnEnd) closeIO(in);
			closeIO(out);
		}
	}

	public static boolean joinStream(InputStream in, OutputStream out) {
		return joinStream(in, out, true, 0, 0, 0, null);
	}

	/**
	 * 把输入流写入到输出流。
	 * @param in
	 * @param out
	 * @param closeOnEnd	写入完成后是否关闭流
	 * @param contentLen	输入流的总长度，用于反馈进度
	 * @param increaseUnit	反馈进度的精度，每增加1/increaseUnit的字节才会反馈一次进度，10000表示反馈进度的精度为0.01%
	 * @param minInterval   进度反馈的最小时间间隔
	 * @param callback		是否中断和反馈进度的回调接口
	 * @return				返回成功还是失败
	 */
	public static boolean joinStream(InputStream in, OutputStream out, boolean closeOnEnd, long contentLen, int increaseUnit, int minInterval, Callback callback) {
		try {
			StreamTracker tracker = streamTracker(contentLen, increaseUnit, minInterval, callback);
			byte[] buffer = new byte[2048];
			int count = 0;
			while ((tracker == null || tracker.track(count)) && (count = in.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
			return count == -1;
		} catch (Exception e) {
			L.e(FileUtils.class, e);
			return false;
		} finally {
			if (closeOnEnd) {
				closeIO(in);
				closeIO(out);
			}
		}
	}

	public static StreamTracker streamTracker(long contentLen, int increaseUnit, int minInterval, Callback callback) {
		return callback == null ? null : new StreamTracker(contentLen, increaseUnit, minInterval, callback);
	}

	public static class StreamTracker {
		private final long mTotalLength;
		private final int mMinInterval;
		private final Callback mCallback;
		private final int mUnitSize;
		private long mOffset, mCurrMaxSize;
		private long mTime, mLastTime;
		private boolean mB;

		private StreamTracker(long contentLen, int increaseUnit, int minInterval, Callback callback) {
			mTotalLength = contentLen;
			mMinInterval = minInterval;
			mCallback = callback;
			mUnitSize = contentLen > 0 && increaseUnit > 0 ? (int)(contentLen / increaseUnit) : 0;   //increaseUnit==10000表示进度精确到0.01%

			reset();
		}

		public void reset() {
			mCurrMaxSize = mOffset = 0;
			mB = true;
		}

		public boolean track(int count) {
			if (mCallback != null) {
				if (mCallback.interrupt()) {
					return false;
				}
				mOffset += count;
				mTime = System.currentTimeMillis();
                if (mB && mOffset >= mTotalLength) {
                    mCallback.onProcess(mTotalLength);
                    mB = false;
                    if (mUnitSize > 0 && mOffset >= mCurrMaxSize) mCurrMaxSize += mUnitSize;
                    mLastTime = mTime;
                } else if (mTime - mLastTime >= mMinInterval) {
                    if (mUnitSize == 0) {
                        mCallback.onProcess(mOffset);
                        mLastTime = mTime;
                    } else if (mOffset >= mCurrMaxSize) {
                        mCallback.onProcess(mOffset);
                        mCurrMaxSize += mUnitSize;
                        mLastTime = mTime;
                    }
                }
			}
			return true;
		}
	}

	/**
	 * e.g:
	 * <pre>
	 * public boolean interrupt() {
	 *     return Thread.currentThread().interrupted(); //or Thread.interrupted();
	 * }
	 * </pre>
	 */
	public interface Callback {
		boolean interrupt();
		void onProcess(long offset);
	}

	public static File makeNewFileInDir(String dirPath, String suffix) {
		return makeNewFileInDir(dirPath, String.valueOf(System.currentTimeMillis()), suffix);
	}

	public static File makeNewFileInDir(String dirPath, String prefix, String suffix) {
		try {
			dirPath = makeDir(dirPath, false).getPath();
		} catch (FileCreateFailureException e) {
			L.e(FileUtils.class, e);
			return null;
		}
		File file;
		int count = 1;
		String nameNew = prefix;
		do {
			file = new File(dirPath + File.separator + nameNew + (suffix != null ? "." + suffix : ""));
			if (file.exists()) {
				nameNew = prefix + "(" + count++ + ")";
			} else {
				break;
			}
		} while (true);
		try {
			return makeFile(file.getPath(), false);
		} catch (FileCreateFailureException e) {
			L.e(FileUtils.class, e);
			return null;
		}
	}

	public static File bringUriFileToDir(Context ctx, Uri uri, String dir, String prefix, String suffix) {
		String scheme = uri.getScheme();
		if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			File file = new File(uri.getPath());
			if (isPathInDir(file.getPath(), dir)) {
				return file;
			} else {
				try {
					return copyStreamToDir(ctx.getContentResolver().openInputStream(uri), dir, prefix + file.getName(), suffix);
				} catch (FileNotFoundException e) {
					L.e(FileUtils.class, e);
				}
			}
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			try {
				return copyStreamToDir(ctx.getContentResolver().openInputStream(uri), dir, prefix, suffix);
			} catch (FileNotFoundException e) {
				L.e(FileUtils.class, e);
			}
		} else {
			throw new IllegalArgumentException("不支持的scheme:" + uri);
		}
		return null;
	}

	public static byte[] readBytesFromFile(String filePath, int offset, int len) {
		checkFileExists(filePath);
		if (offset < 0) throw new IllegalArgumentException("offset不能小于0:" + offset);
		File file = getFile(filePath);
		if (len < 0) len = (int) file.length() - offset;
		if (len == 0) return null;
		if (offset + len > file.length()) throw new IllegalArgumentException("offset:" + offset + " + len:" + len + " > file.length:" + file.length());
		byte[] buffer = null;
		RandomAccessFile in = null;
		try {
			in = new RandomAccessFile(filePath, "r");
			buffer = new byte[len]; //创建合适文件大小的数组
			in.seek(offset);
			in.readFully(buffer);
		} catch (Exception e) {
			L.e(FileUtils.class, e);
		} finally {
			closeIO(in);
		}
		return buffer;
	}

	public static void closeIO(Closeable io) {
		if (io != null) try { io.close(); } catch (Exception e) {}
	}
}
