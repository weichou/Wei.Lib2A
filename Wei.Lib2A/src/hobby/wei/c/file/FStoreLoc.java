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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

import hobby.wei.c.L;
import hobby.wei.c.exception.FileCreateFailureException;
import hobby.wei.c.exception.SdCardNotMountedException;
import hobby.wei.c.exception.SdCardNotValidException;
import hobby.wei.c.phone.Storage;
import hobby.wei.c.phone.Storage.SdCard;

/**
 * 本地存储位置管理和文件读写管理助手（默认的三个配置会保存上一次的设置）。
 * 注意需要权限android.permission.WRITE_EXTERNAL_STORAGE，该权限包含了
 * android.permission.READ_EXTERNAL_STORAGE
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class FStoreLoc {
	/**用于存储lib、db和小文件的地方。优先顺序为：内置存储卡、系统/data/目录，不会使用外置存储卡**/
	public static final FStoreLoc DEFAULT;
	/**用于存储大文件的地方，比如下载的媒体文件。外置存储卡优先，不会使用系统/data/目录。注意{@link #sdcardDef}在每次启动App之后可能不一样，总是寻找剩余空间最大的那张卡**/
	public static final FStoreLoc BIGFILE;
	/**生存模式，由外向内尽量找到栖息之所。优先顺序为：外置存储卡、内置存储卡、系统/data/目录**/
	public static final FStoreLoc SURVIVE;

	static {
		NAME_SET = new HashSet<String>(3);
		DEFAULT = new FStoreLoc("DEFAULT", Storage.CARD_COUNT > 1 ? Storage.CARD_DEF : Storage.DATA, false, true);
		BIGFILE = new FStoreLoc("BIGFILE", Storage.getMaxUsableCard(true), true, false);
		SURVIVE = new FStoreLoc("SURVIVE", Storage.CARD_EXT != null ? Storage.CARD_EXT : Storage.CARD_DEF, true, true);
	}

	public enum DirLevel {
		/**仅仅使用在存储卡上创建的目录，而不使用App私有目录，用于分享文件给其他App的情况，例如第三方平台分享图片**/
		CUSTOM,
		/**仅仅使用App私有目录**/
		PRIVATE,
		/**优先使用在存储卡上创建的目录，若没有或无法创建则使用私有目录**/
		DEFAULT
	}

	/**
	 * @param name					该对象的名称，不可与现有的重名
	 * @param sdcardDef				默认的存储卡位置，但是实际不一定使用该位置，并且该位置在每次启动App之后可能不一样，如{@link #BIGFILE}在每次启动之后会寻找剩余空间最大的那张卡
	 * @param anotherCardEnabled	是否可切换到另一张卡，非data目录。注意：如果默认为非data目录，即使这里为false， 默认位置(两个卡中的一个)也是有效的(另一个卡无效)；但如果默认是data目录，则这里为false时，非data位置无效。
	 * @param dataDirEnabled		是否可切换到data/data/目录上
	 */
	public FStoreLoc(String name, SdCard sdcardDef, boolean anotherCardEnabled, boolean dataDirEnabled) {
		synchronized (FStoreLoc.class) {
			if (NAME_SET.contains(name)) throw new IllegalArgumentException("已经配置过该name的Local:" + name);
			NAME_SET.add(name);
		}
		this.name = name;
		this.sdcardDef = sdcardDef;
		this.anotherCardEnabled = anotherCardEnabled;
		this.dataDirEnabled = dataDirEnabled;
		checkConfig();
	}

	/**设置自定义App根目录，如果为空，则使用Android/data/或者data/data/目录**/
	public void setBaseDirName(Context context, String baseDir) {
		ensureCurrent(context);
		if (baseDir != null) baseDir = baseDir.trim();
		FileUtils.checkFileNameValid(baseDir);
		if (baseDirName != null) {
			if (!baseDirName.equals(baseDir)) L.i(this, "不可重复设置不同的目录");
			return;
		}
		baseDirName = baseDir;
		saveCurrent(context);
	}

	public String getBaseDirName(Context context) {
		ensureCurrent(context);
		return baseDirName;
	}

	public SdCard getCurrentSdCard(Context context) {
		ensureCurrent(context);
		return sdcardCur;
	}

	/**换一个存储位置。切换顺序：
	 * sdcardDef != data/data/ ? sdcardDef->anotherSdCard->data/data/
	 * sdcardDef == data/data/ ? data/data/->card_inner->card_ext
	 * @return 是否切换成功，或者是否支持切换**/
	public boolean switchSdCard(Context context) {
		ensureCurrent(context);
		SdCard sdcardTo = getAnother(sdcardCur);
		if (sdcardTo != null) {
			doSwitch(context, sdcardTo);
			return true;
		}
		return false;
	}

	public boolean switchTo(Context context, SdCard sdcard) {
		ensureCurrent(context);
		if (isMeetConfig(sdcard, true)) {
			doSwitch(context, sdcard);
			return true;
		}
		return false;
	}

	/**切换到另一个有效的存储位置，注意跟{@link #getValidSdCard(Context)}的区别。
	 * 查找顺序：
	 * sdcardDef != data/data/ ? sdcardDef->anotherSdCard->data/data/
	 * sdcardDef == data/data/ ? data/data/->card_inner->card_ext
	 * @return 是否切换成功，或者是否支持切换**/ 
	public boolean switchToValid(Context context) {
		ensureCurrent(context);
		SdCard start = sdcardCur;
		SdCard valid = start;
		do {
			valid = getAnother(valid);
			if (valid == null) return false;
			if (valid.isValid(context)) {
				doSwitch(context, valid);
				return true;
			}
		} while (valid != start);
		return false;	//转了一圈回到开头
	}

	public void switchToDefault(Context context) {
		ensureCurrent(context);
		doSwitch(context, sdcardDef);
	}

	/**取得另一个存储位置，如果配置为只有一个存储位置，而参数current又刚好占据了该位置，则本方法会返回null**/
	private SdCard getAnother(SdCard current) {
		if (current == null) return sdcardCur;
		SdCard sdcardTo = null;
		if (sdcardDef == Storage.DATA) {
			if (current == Storage.DATA) {
				if (anotherCardEnabled) {
					sdcardTo = Storage.CARD_COUNT > 1 ? Storage.CARD_INNER : Storage.CARD_DEF;
				}
			} else {
				if (Storage.CARD_COUNT > 1) {
					//sdcardDef == data/data/ ? data/data/->card_inner->card_ext
					sdcardTo = current == Storage.CARD_INNER ? Storage.CARD_EXT : Storage.DATA;
				} else {
					sdcardTo = Storage.DATA;
				}
			}
		} else {
			if (current == Storage.DATA) {
				sdcardTo = sdcardDef;
			} else {
				//sdcardDef != data/data/ ? sdcardDef->anotherSdCard->data/data/
				if (anotherCardEnabled && Storage.CARD_COUNT > 1) {
					if (current == sdcardDef) {
						sdcardTo = sdcardDef == Storage.CARD_INNER ? Storage.CARD_EXT : Storage.CARD_INNER;
					} else if (dataDirEnabled) {
						sdcardTo = Storage.DATA;
					} else {
						sdcardTo = sdcardDef;
					}
				} else if (dataDirEnabled) {
					sdcardTo = Storage.DATA;
				}
			}
		}
		L.i(FStoreLoc.class, "getAnother----sdcardTo:%s", sdcardTo);
		return sdcardTo;
	}

	private void doSwitch(Context context, SdCard sdcardTo) {
		if (sdcardTo != null && sdcardTo != sdcardCur) {
			sdcardCur = sdcardTo;
			saveCurrent(context);
		}
	}

	private void checkConfig() {
		if (!isMeetConfig(sdcardDef, false)) throw new NullPointerException("没有正确配置存储卡位置及相关参数，请检查");
	}

	private void checkLevel(DirLevel level) {
		if (level == DirLevel.CUSTOM && baseDirName == null) throw new IllegalArgumentException("使用DirLevel.CUSTOM模式，但baseDirName没有设置");
	}

	private void checkStartAndStop(SdCard start, SdCard stop) {
		if (start == null) throw new IllegalArgumentException("start不能为null");
		if (stop == null) throw new IllegalArgumentException("stop不能为null");
	}

	private boolean isMeetConfig(SdCard sdcard, boolean switchTo) {
		if (switchTo) {
			return sdcard != null && (dataDirEnabled && sdcard == Storage.DATA
					|| anotherCardEnabled && (sdcard == Storage.CARD_INNER || sdcard == Storage.CARD_EXT));
		} else {
			return sdcard != null && (dataDirEnabled && sdcard == Storage.DATA
					|| sdcard == Storage.CARD_INNER || sdcard == Storage.CARD_EXT);
		}
	}

	/**清空设置以便使用默认位置和设置新的根目录**/
	public void clear(Context context) {
		sdcardCur = null;
		baseDirName = null;
		Editor editor = getEditor(context);
		editor.clear();
		editor.commit();
	}

	/**取得有效的存储位置，注意并不切换存储位置**/
	public SdCard getValidSdCard(Context context) {
		ensureCurrent(context);
		return getValidSdCard(context, sdcardCur, sdcardCur);
	}

	private SdCard getValidSdCard(Context context, SdCard start, SdCard stop) {
		checkStartAndStop(start, stop);
		SdCard valid = start;
		do {
			if (valid.isValid(context)) return valid;
			valid = getAnother(valid);
		} while (valid != null && valid != stop);
		return null;
	}

	public SdCard getCustomDirCreatableSdCard(Context context) {
		ensureCurrent(context);
		return getCustomDirCreatableSdCard(context, sdcardCur, sdcardCur);
	}

	private SdCard getCustomDirCreatableSdCard(Context context, SdCard start, SdCard stop) {
		checkStartAndStop(start, stop);
		SdCard valid = start;
		do {
			if (valid.isCustomDirCreatable(context)) return valid;
			valid = getAnother(valid);
		} while (valid != null && valid != stop);
		return null;
	}

	/**取得App根目录（已经创建好的）**/
	public File getBaseDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		//ensureCurrent(context);
		checkLevel(level);
		File file = null;
		SdCard sdcardTo = null;
		if (level != DirLevel.PRIVATE && baseDirName != null) sdcardTo = getCustomDirCreatableSdCard(context);
		if (level != DirLevel.CUSTOM && sdcardTo == null) sdcardTo = getValidSdCard(context);
		if (sdcardTo != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				//确保目录已经被系统生成，防止自己创建不成功
				context.getFilesDir();
				context.getExternalCacheDirs();
			}
			file = FileUtils.makeDir(getAppDirPath(context, sdcardTo, level), true);
		} else {
			if (level == DirLevel.CUSTOM) {
				throw new SdCardNotValidException("没有可创建目录的存储卡");
			}
			/*这种情况下，必然dataDirEnabled=false，anotherCardEnabled为false且默认为外置卡，或可能为true但只有一张卡。
			 即默认为外置卡且不可切换*/
			if (sdcardDef == Storage.SDCARD) {	//只有这个可以区分开是否挂载还是挂载了但没有格式化
				if (!sdcardDef.isMounted(context)) {
					throw new SdCardNotMountedException("存储卡没有挂载");
				} else if (!sdcardDef.isValid(context)) {
					throw new SdCardNotValidException("存储卡无效，可能没有格式化");
				}
			}
			throw new SdCardNotValidException("没有可用的存储位置，存储卡可能没有挂载或格式化");
		}
		return file;
	}

	public File getExistsFileOrDir(Context context, String dirOrFileRelativePath) {
		File[] files = searchFilesWithRelativePath(context, dirOrFileRelativePath);
		return getLastModified(files);
	}

	public File makeFile(Context context, String fileRelativePath, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		deleteFileOrDir(context, fileRelativePath);
		File file = FileUtils.makeFile(getBaseDir(context, level).getPath() + File.separator + fileRelativePath, true);
		file.setLastModified(System.currentTimeMillis());
		return file;
	}

	public File getDir(Context context, String dirRelativePath, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return FileUtils.makeDir(getBaseDir(context, level).getPath() + File.separator + dirRelativePath, true);
	}

	/**在所有可能存在的文件位置（参数配置的Location范围内）搜索某目录或文件
	 * @param dirOrFileRelativePath	目录或文件相对于 {@link #getBaseDir(Context, DirLevel) baseDir} 的位置
	 **/
	public File[] searchFilesWithRelativePath(Context context, String dirOrFileRelativePath) {
		ensureCurrent(context);
		List<File> files = new ArrayList<File>(3);
		File file;
		SdCard start = sdcardCur, sdcardTo = start;
		do {
			sdcardTo = getValidSdCard(context, sdcardTo, start);
			if (sdcardTo == null) break;
			file = new File(getPathFormated(context, sdcardTo, dirOrFileRelativePath, DirLevel.DEFAULT));
			if (file.exists()) files.add(file);
		} while (true);
		return files.toArray(new File[files.size()]);
	}

	public void deleteFileOrDir(Context context, String dirOrFileRelativePath) {
		File[] files = searchFilesWithRelativePath(context, dirOrFileRelativePath);
		for (File file : files) {
			FileUtils.deleteFileOrDir(file, null, true);
		}
	}

	private File getLastModified(File[] files) {
		if (files != null) {
			if (files.length == 1) {
				return files[0];
			} else if (files.length > 1) {
				File file = null;
				long lastModified = 0, lm;
				for (File f : files) {
					lm = f.lastModified();
					if (file == null || lm > lastModified) {
						file = f;
						lastModified = lm;
					} else {
						//FileUtils.deleteFile(f, null, true);	//不删除文件夹
						if (f.isFile()) f.delete();
					}
				}
				return file;
			}
		}
		return null;
	}

	private String getAppDirPath(Context context, SdCard sdcard, DirLevel level) {
		checkLevel(level);
		return level != DirLevel.PRIVATE && baseDirName != null && sdcard.isCustomDirCreatable(context) ?
				sdcard.path + File.separator + baseDirName : sdcard.getAppDataDir(context);
	}

	private String getPathFormated(Context context, SdCard sdcard, String relativePath, DirLevel level) {
		return FileUtils.formatPath(getAppDirPath(context, sdcard, level) + File.separator + relativePath);
	}

	public File getFilesDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, FILES, level);
	}

	public File getCacheDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, CACHE, level);
	}

	public File getDbsDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, DBS, level);
	}

	public File getLibDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, LIBS, level);
	}

	public File getDocsDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, DOCS, level);
	}

	public File getMediaDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, MEDIA, level);
	}

	public File getImagesDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, IMAGES, level);
	}

	public File getTempDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, TEMP, level);
	}

	public File getImagesCacheDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, IMAGES_CACHE, level);
	}

	public File getTempCacheDir(Context context, DirLevel level) throws SdCardNotMountedException, SdCardNotValidException, FileCreateFailureException {
		return getDir(context, TEMP_CACHE, level);
	}

	private void ensureCurrent(Context context) {
		if (sdcardCur == null) {
			SharedPreferences prefs = getSharedPrefs(context);
			sdcardCur = index2SdCard(prefs.getInt("currentSdCard", -1));
			baseDirName = prefs.getString("baseDirName", null);
			if (sdcardCur == null) sdcardCur = sdcardDef;
		}
	}

	private void saveCurrent(Context context) {
		Editor editor = getEditor(context);
		editor.putInt("currentSdCard", sdcard2Index(sdcardCur));
		if (baseDirName != null) editor.putString("baseDirName", baseDirName);
		editor.commit();
	}

	private SharedPreferences getSharedPrefs(Context context) {
		return context.getSharedPreferences(getClass().getName() + "." + name, Context.MODE_PRIVATE);
	}

	private Editor getEditor(Context context) {
		return getSharedPrefs(context).edit();
	}

	private SdCard index2SdCard(int cardIndex) {
		SdCard sdcard = null;
		switch (cardIndex) {
		case 0:
			sdcard = Storage.DATA;
			break;
		case 1:
			sdcard = Storage.CARD_INNER;
			break;
		case 2:
			sdcard = Storage.CARD_EXT;
			break;
		}
		return sdcard;
	}

	private int sdcard2Index(SdCard sdcard) {
		int index = -1;
		if (sdcard == Storage.DATA) {
			index = 0;
		} else if (sdcard == Storage.CARD_INNER) {
			index = 1;
		} else if (sdcard == Storage.CARD_EXT) {
			index = 2;
		}
		return index;
	}

	/*以下是常用目录常量*/
	public static final String FILES				= "files";
	public static final String CACHE				= "cache";
	public static final String DBS				= "databases";
	public static final String LIBS				= "libs";
	public static final String SHAPREFS			= "shared_prefs";
	public static final String DOCS				= "docs";
	public static final String MEDIA				= "media";
	public static final String IMAGES				= "images";
	public static final String TEMP				= "temp";
	public static final String IMAGES_CACHE		= CACHE + File.separator + IMAGES;
	public static final String TEMP_CACHE			= CACHE + File.separator + TEMP;

	public final String name;
	/**是否可切换到另一张卡，非data目录。注意：如果默认为非data目录，即使这里为false，
	 默认位置(两个卡中的一个)也是有效的(另一个卡无效)；但如果默认是data目录，则这里为false时，非data位置无效。
	 **/
	public final boolean anotherCardEnabled;
	/**是否可切换到data/data/目录上**/
	public final boolean dataDirEnabled;
	/**默认的存储卡位置，但是实际不一定使用该位置，并且该位置在每次启动App之后可能不一样，如{@link #BIGFILE}在每次启动之后会寻找剩余空间最大的那张卡**/
	public final SdCard sdcardDef;

	/**当前使用的存储卡**/
	private SdCard sdcardCur;
	/**自定义App根目录，如果为空，则使用Android/data/或者data/data/目录**/
	private String baseDirName;

	private static final Set<String> NAME_SET;
}
