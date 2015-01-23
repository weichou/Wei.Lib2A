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

package com.wei.c.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import com.wei.c.L;
import com.wei.c.exception.FileCreateFailureException;

/**
 * 对读写文件进行了简单版本控制的工具类，用来解决进程内频繁读写同一个文件时，易产生脏数据等冲突问题。
 * 同时由于操作系统已经对进程间读写同一个文件的冲突作了处理，因此本类适用于进程内和进程间的所有情况。
 * 
 * 由于在进程间共享{@link android.content.SharedPreferences SharedPreferences}时值极易丢失，而本类可以很好的解决该问题。
 * 
 * 实现策略：对同一个文件的写操作实现同步。
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class FileVersioned {
	private static final String CHARSET = "UTF-8";
	private static final List<WeakReference<FileVersioned>> sWorkingSessions = new LinkedList<WeakReference<FileVersioned>>();

	private final String mFileDir;
	private final String mFileName;
	private FileVersioned(String fileDir, String fileName) {
		mFileDir = fileDir;
		mFileName = fileName;
	}

	private static synchronized FileVersioned getFileVersioned(String fileDir, String fileName) {
		FileVersioned fver = null;
		List<WeakReference<FileVersioned>> emptyRefs = new LinkedList<WeakReference<FileVersioned>>();
		for (WeakReference<FileVersioned> ref : sWorkingSessions) {
			fver = ref.get();
			if (fver == null) {
				emptyRefs.add(ref);
			} else if (fver.mFileDir.equalsIgnoreCase(fileDir) && fver.mFileName.equalsIgnoreCase(fileName)) {
				break;
			} else {
				fver = null;
			}
		}
		sWorkingSessions.removeAll(emptyRefs);
		if (fver == null) {
			fver = new FileVersioned(fileDir, fileName);
			sWorkingSessions.add(new WeakReference<FileVersioned>(fver));
		}
		return fver;
	}

	public static void saveAsFileInDefaultDir(Context context, String fileName, String content) {
		saveAsFile(context, getFileSharedPrefDir(context).getPath(), fileName, content);
	}

	public static void saveAsFileInDefaultDir(Context context, String fileName, InputStream in) {
		saveAsFile(context, getFileSharedPrefDir(context).getPath(), fileName, in);
	}

	public static void saveAsFile(Context context, String fileDir, String fileName, String content) {
		FileUtils.checkDirExists(fileDir);
		FileUtils.checkFileNameValid(fileName);
		getFileVersioned(fileDir, fileName).saveAsFile(context, content);
	}

	public static void saveAsFile(Context context, String fileDir, String fileName, InputStream in) {
		FileUtils.checkDirExists(fileDir);
		FileUtils.checkFileNameValid(fileName);
		getFileVersioned(fileDir, fileName).saveAsFile(context, in);
	}

	public static String getStringFromFileInDefaultDir(Context context, String fileName) {
		return getStringFromFile(context, getFileSharedPrefDir(context).getPath(), fileName);
	}

	public static String getStringFromFile(Context context, String fileDir, String fileName) {
		FileUtils.checkDirExists(fileDir);
		FileUtils.checkFileNameValid(fileName);
		return getFileVersioned(fileDir, fileName).getStringFromFile(context);
	}

	public static InputStream getStreamFromFileInDefaultDir(Context context, String fileName) {
		return getStreamFromFile(context, getFileSharedPrefDir(context).getPath(), fileName);
	}

	public static InputStream getStreamFromFile(Context context, String fileDir, String fileName) {
		FileUtils.checkDirExists(fileDir);
		FileUtils.checkFileNameValid(fileName);
		return getFileVersioned(fileDir, fileName).getStreamFromFile(context);
	}

	private void saveAsFile(Context context, String content) {
		if (content == null) {
			deleteAll(getVersionFiles(context, mFileDir, mFileName, false));
		} else {
			try {
				saveAsFile(context, new ByteArrayInputStream(content.getBytes(CHARSET)));
			} catch (IOException e) {
				L.e(FileVersioned.class, e);
			}
		}
	}

	private void saveAsFile(Context context, InputStream in) {
		if (in == null) {
			deleteAll(getVersionFiles(context, mFileDir, mFileName, false));
		} else {
			FileVersions vfiles = getVersionFiles(context, mFileDir, mFileName, true);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(vfiles.writing);
				byte[] buffer = new byte[1024];
				int count;
				while ((count = in.read(buffer)) != -1) {
					out.write(buffer, 0, count);
				}
				synchronized (this) {
					vfiles.writing.renameTo(vfiles.renameTo);
				}
			} catch (FileNotFoundException e) {
				L.e(FileVersioned.class, e);
				throw new RuntimeException(e);
			} catch (IOException e) {
				L.e(FileVersioned.class, e);
			} finally {
				if (in != null) try { in.close(); } catch (IOException e) {}
				if (out != null) try { out.close(); } catch (IOException e) {}
			}
			deleteOthers(vfiles.others);
		}
	}

	public String getStringFromFile(Context context) {
		FileInputStream in = getStreamFromFile(context);
		if (in != null) {
			ByteArrayOutputStream arrayBuffer = new ByteArrayOutputStream(512);
			try {
				byte[] buffer = new byte[512];
				int count;
				while ((count = in.read(buffer)) != -1) {
					arrayBuffer.write(buffer, 0, count);
				}
				return new String(arrayBuffer.toByteArray(), CHARSET);
			} catch (IOException e) {
				L.e(FileVersioned.class, e);
			} finally {
				if (in != null) try { in.close(); } catch (IOException e) {}
				if (arrayBuffer != null) try { arrayBuffer.close(); } catch (IOException e) {}
			}
		}
		return null;
	}

	public FileInputStream getStreamFromFile(Context context) {
		FileVersions vfiles = getVersionFiles(context, mFileDir, mFileName, false);
		if (vfiles.read != null) {
			try {
				return new FileInputStream(vfiles.read);
			} catch (FileNotFoundException e) {
				L.e(FileVersioned.class, e);
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private synchronized void deleteAll(FileVersions vfiles) {
		if (vfiles.writing != null) vfiles.writing.delete();
		if (vfiles.renameTo != null) vfiles.renameTo.delete();
		if (vfiles.read != null) vfiles.read.delete();
		deleteOthers(vfiles.others);
	}

	private synchronized void deleteOthers(File[] others) {
		if (others != null && others.length > 0) {
			for (File file : others) {
				file.delete();
			}
		}
	}

	private FileVersions getVersionFiles(Context context, String dirName, String fileName, boolean isWrite) {
		String fileName_ = fileName + "-";
		File fileDir = FileUtils.getDir(dirName);
		String[] names;
		synchronized (this) {
			names = fileDir.list();	//这一句决定了后面的一切
		}
		String maxName = null;
		FileVersions vfiles = new FileVersions();
		if (names != null && names.length > 0) {
			List<File> list = new ArrayList<File>();
			String fileName_1 = fileName_ + "1";
			int len = fileName.length();
			for (String name : names) {
				if (name.startsWith(fileName_1) && name.lastIndexOf("-1") == len) {
					if (maxName == null) {
						maxName = name;
					} else if (name.compareToIgnoreCase(maxName) > 0) {
						list.add(new File(fileDir, maxName));
						maxName = name;
					} else {
						list.add(new File(fileDir, name));
					}
				}
			}
			//无论读写模式都需要
			if (list.size() > 0) vfiles.others = list.toArray(new File[list.size()]);
		}
		int fileNameIndex = 1000000000;
		if (isWrite) {
			if (maxName != null) {
				try {
					int fileNameIndex0 = Integer.valueOf(maxName.substring(fileName_.length()));
					if (fileNameIndex0 >= fileNameIndex) {
						fileNameIndex = fileNameIndex0;
						fileNameIndex++;
					}
				} catch (Exception e) {}
			}
			vfiles.writing = new File(fileDir, fileName + "." + fileNameIndex);
			vfiles.renameTo = new File(fileDir, fileName_ + fileNameIndex);
			try {
				FileUtils.makeFile(vfiles.writing.getPath(), true);
			} catch (FileCreateFailureException e) {
				L.e(FileVersioned.class, e);
				throw new RuntimeException(e);
			}
		} else {
			vfiles.read = maxName != null ? new File(fileDir, maxName) : null;
		}
		return vfiles;
	}

	public static File getFileSharedPrefDir(Context context) {
		try {
			return FileUtils.makeDir(new File(context.getFilesDir(), "fileSharedPrefs").getPath(), true);
		} catch (FileCreateFailureException e) {
			L.e(FileVersioned.class, e);
			throw new RuntimeException(e);
		}
	}

	private static class FileVersions {
		public File writing;
		public File renameTo;
		public File read;
		public File[] others;
	}
}
