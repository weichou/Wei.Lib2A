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

package hobby.wei.c.phone;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import hobby.wei.c.file.FsSize;
import hobby.wei.c.utils.ArrayUtils;

/**
 * 存储卡助手。通过几个常量即可便捷的取得内置或外置SdCard对象。完美兼容各种定制机型。
 * 注意需要权限android.permission.WRITE_EXTERNAL_STORAGE，该权限包含了
 * android.permission.READ_EXTERNAL_STORAGE
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@SuppressLint({"DefaultLocale"})
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.KITKAT)
public class Storage {
	/**内置SD卡，如果只有一张卡则为空。可能为sdcard2目录，也可能为sdcard目录。**/
	public static final SdCard CARD_INNER;
	/**可插拔SD卡。可能为sdcard目录，也可能为extSdCard目录**/
	public static final SdCard CARD_EXT;
	/**本API默认SD卡。不会为/data目录，不会为空，内部优先**/
	public static final SdCard CARD_DEF;
	/**系统默认的sdcard**/
	public static final SdCard SDCARD;
	/**系统/data目录**/
	public static final SdCard DATA;

	public static final int CARD_COUNT;
	public static final String[] CARD_PATHS;

	private static File sdcard_file;
	private static File[] card_files;
	private static boolean searchChild = false;
	private static final String extFlag = "ext";

	static {
		sdcard_file = Environment.getExternalStorageDirectory();
		card_files = externalStorageFiles();

		if (card_files.length == 1) {
			//if(CARD_FILES[0].equals(SDCARD_FILE)) {	//必然的，详见externalStorageFiles()
			CARD_COUNT = 1;
			//对于只有一张卡的，即使目录名不带"ext"，低版本的手机也往往是外置卡，而高版本的可以精确判断，没错的
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD || Environment.isExternalStorageRemovable()) {
				SDCARD = CARD_DEF = CARD_EXT = new SdCard(sdcard_file, true);
				CARD_INNER = null;
			} else {
				SDCARD = CARD_DEF = CARD_INNER = new SdCard(sdcard_file, false);
				CARD_EXT = null;
			}
		} else {
			File ext = null, inner = null;
			if (searchChild) {
				ext = card_files[1];
			} else {
				boolean searchExt = true;

				/* 暂且认为内置只有一个，其他神马的全是外置。
				 * 如果系统默认的卡是外置，则只需要找一个内置的即可，其他也都是外置（几乎不会有超过两张卡，/data/data除外），不管；
				 * 如果系统默认的卡是内置，则只需要找一个外置的即可，其他也都是外置，不管；
				 * 对于低版本的系统，无法判断系统默认的卡是内置还是外置的，而且默认卡目录名也不带"ext"的，则查找非默认卡带"ext"的；
				 * 如果非默认卡也不带"ext"的，则比较目录名大小（拼人品的时候到了），小的为内置，大的为外置，如：
				 * 华为P6：/mnt/sdcard与/mnt/sdcard2，/storage/sdcard0与/storage/sdcard1都符合这种判断方式。
				 */
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					searchExt = !Environment.isExternalStorageRemovable();
					if (card_files.length == 2) {
						if (searchExt) {
							ext = card_files[1];
						} else {
							inner = card_files[1];
						}
					} else {
						if (searchExt) {
							ext = searchCardFileWithFlagInName(card_files, 1, extFlag, true);
							if (ext == null) ext = card_files[1];
						} else {
							inner = searchCardFileWithFlagInName(card_files, 1, extFlag, false);
							if (inner == null) inner = card_files[1];
						}
					}
				} else {
					searchExt = !sdcard_file.getName().toLowerCase().contains(extFlag);
					if (searchExt) {	//默认的不是外置的
						ext = searchCardFileWithFlagInName(card_files, 1, extFlag, true);
						if (ext == null) {	//全部都不是外置的，拼人品比较大小
							if (card_files[1].getName().compareToIgnoreCase(sdcard_file.getName()) > 0) {
								ext = card_files[1];
							} else {	//名字不可能有相同的，externalStorageFiles()里面已经去重复了
								inner = card_files[1];
							}
						}
					} else {
						inner = searchCardFileWithFlagInName(card_files, 1, extFlag, false);
						if (inner == null) {	//这种情况几乎不存在
							inner = card_files[1];
						}
					}
				}
			}
			if (ext != null) {
				CARD_COUNT = card_files.length;
				CARD_EXT = new SdCard(ext, true);
				SDCARD = CARD_DEF = CARD_INNER = new SdCard(sdcard_file, false);
			} else {	//inner != null
				CARD_COUNT = card_files.length;
				SDCARD = CARD_EXT = new SdCard(sdcard_file, true);
				CARD_DEF = CARD_INNER = new SdCard(inner, false);
			}
		}
		DATA = new SdCard(Environment.getDataDirectory(), false);

		CARD_PATHS = ArrayUtils.toPathArray(card_files);

		sdcard_file = null;
		card_files = null;
	}

	private Storage() {}

	public static class SdCard {
		public final String path;
		/**SD卡所在目录的名称**/
		public final String name;
		/**是否可插拔**/
		public final boolean hotswap;
		/**是否系统默认的sdcard**/
		public final boolean sysdef;
		/**是否/data目录**/
		public final boolean data;

		private SdCard(File file, boolean removable) {
			path = file.getPath();
			name = file.getName();
			hotswap = removable;
			sysdef = name.equals(sdcard_file.getName());
			data = sysdef ? false : name.equals("data");
		}

		/**外部存储器总容量 **/
		public FsSize totalSpaceFS() {
			return new FsSize(totalSpace());
		}

		public long totalSpace() {
			return Storage.totalSpace(path);
		}

		/**外部存储器空闲容量。注意,这可能是一个乐观的高估,不应视为保证您的应用程序可以写这麽多字节。(root用户可用的剩余空间)**/
		public FsSize freeSpaceFS() {
			return new FsSize(freeSpace());
		}

		public long freeSpace() {
			return Storage.freeSpace(path);
		}

		/**外部存储器对程序所在用户可以使用的有效的容量。注意不是已经使用了的容量，值与freeSpace可能相同**/
		public FsSize usableSpaceFS() {
			return new FsSize(usableSpace());
		}

		public long usableSpace() {
			return Storage.usableSpace(path);
		}

		/**是否挂载。注意需要权限android.permission.WRITE_EXTERNAL_STORAGE，否则
		 * 对于非默认的ExternalStorage挂载状态总是返回false.<br>
		 * 对于外部卡在未插卡状态下，与内置卡共用同一存储位置的情况，这里返回false.
		 **/
		public boolean isMounted(Context context) {
			if (!hotswap) return true;
			//执行到这里说明就是CARD_EXT
			//只有一张卡的情况下，CARD_DEF==CARD_EXT，就不用比较大小了
			/*若有两张卡，且外部卡跟内部卡容量完全相同，说明是三星i9100等机型:CARD_EXT.path = /sdcard/external_sd，
			 在未插卡情况下，两者共用同一个存储位置，即容量完全相同。仅仅容量相同可能是凑巧，但是freeSpace()也相同就不是凑巧了*/
			if (!equals(CARD_DEF) && isSameSpace(path, CARD_DEF.path)) {
				return false;
			}
			//new File(path).exists();	//此方法不可行，即使存储卡已经拔出，这个目录也是存在的
			if (equals(SDCARD)) {
				return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
			} else {
				return isValid(context);
			}
		}

		public boolean isValid(Context context) {
			if (data) return true;
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				//确保目录已经被系统生成，防止自己创建不成功
				context.getFilesDir();
				context.getExternalCacheDirs();
			}
			File file = new File(path + File.separator + "Android");
			boolean b = file.exists();
			if (!b) b = file.mkdir();
			return b;
		}

		/**是否可以在这张卡上创建任意目录**/
		public boolean isCustomDirCreatable(Context context) {	//不通过系统版本号来判断，直接通过创建文件夹的方式更准确
			if (data) return false;
			File file = new File(path + File.separator + ".wei.c");
			boolean b = file.exists();
			if (!b) b = file.mkdir();
			return b;
		}

		/**空间是否已满**/
		public boolean isExceed(long lengthNeed) {
			return usableSpace() < lengthNeed;
		}

		/**获取Context所在的App的标准存储目录**/
		public String getAppDataDir(Context context) {
			return path + File.separator + (data ? "data" : "Android" + File.separator + "data") + File.separator + context.getApplicationInfo().packageName;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof SdCard)) return false;
			return path.equals(((SdCard)o).path);
		}

		@Override
		public String toString() {
			return path;
		}
	}

	public static SdCard getSdCardByFilePath(String path) {
		if (isPathOfCard(CARD_EXT, path)) return CARD_EXT;
		if (isPathOfCard(CARD_DEF, path)) return CARD_DEF;
		if (isPathOfCard(DATA, path)) return DATA;
		return null;
	}

	public static boolean isPathOfCard(SdCard card, String path) {
		if (card == null || path == null || path.length() <= 0) return false;
		return path.substring(0, path.indexOf(File.separatorChar, card.path.length())).equals(card.path);
	}

	public static SdCard getMaxSizeCard(boolean exceptDataDir) {
		return getMaxCard(true, exceptDataDir);
	}

	public static SdCard getMaxUsableCard(boolean exceptDataDir) {
		return getMaxCard(false, exceptDataDir);
	}

	/**查找存储卡目录。所有/storage或/mnt或根目录下符合规则的目录。注意：即使外部存储卡已拔出，目录仍然存在**/
	private static File[] externalStorageFiles() {
		File[] cards = null;
		File root = File.listRoots()[0], sameDir = null, file;
		String parentName = null;
		String storage = "storage", mnt = "mnt";

		/* 有些特例，比如Nexus 7 二代，SDCARD_FILE的路径为/storage/emulated/0，其中没有sdcard相关字符；
		 * 同时其他几个路径都可以到达存储卡：/sdcard, /storage/sdcard0, /mnt/sdcard.
		 * 还有华为P6为/mnt/shell/emulated/0，同时有5个路径到达内置存储卡。。。
		 * 鉴于这种复杂的情况，使用以下策略：
		 * 找出所有疑似存储卡位置，然后去重复。
		 */
		if (nameMatches(sdcard_file.getName(), CARD_NAME_RULE, RULE_INCLUDE, true)) {
			sameDir = sdcard_file.getParentFile();
			//不在根目录("/")下搜索，只有/sdcard目录，没有其他外置存储卡目录，只在最后才到根目录下搜索
			if (sameDir.equals(root)) {
				sameDir = null;
			} else {
				parentName = sameDir.getName();
			}
		} else {
			//如果路径名不匹配常规名称，则不在同级目录中查找
		}

		/*下面的查找都不会过滤系统默认卡的目录名*/
		if (sameDir != null && sameDir.isDirectory()) {	//先找默认存储卡同目录下的
			cards = filterSubFilesWithName(sameDir, CARD_NAME_RULE, RULE_INCLUDE, true, true, null);
		}
		if (ArrayUtils.isEmpty(cards) && (parentName == null || !parentName.equalsIgnoreCase(mnt))) {
			file = new File(root, mnt);
			if (file.exists() && file.isDirectory()) {
				cards = filterSubFilesWithName(file, CARD_NAME_RULE, RULE_INCLUDE, true, true, null);
			}
		}
		if (ArrayUtils.isEmpty(cards) && (parentName == null || !parentName.equalsIgnoreCase(storage))) {
			file = new File(root, storage);
			if (file.exists() && file.isDirectory()) {
				cards = filterSubFilesWithName(file, CARD_NAME_RULE, RULE_INCLUDE, true, true, null);
			}
		}
		if (ArrayUtils.isEmpty(cards)) {	//最后去root目录下面去找
			cards = filterSubFilesWithName(root, CARD_NAME_RULE, RULE_INCLUDE, true, true, null);
		}
		//找默认存储卡子目录下的。如：三星i9100的外置卡external_sd就是在默认存储卡子目录下。注意系统默认卡的子目录不可能是内置卡
		if (cards != null && cards.length == 1 && isSameSpace(sdcard_file, cards[0])) {
			cards = filterSubFilesWithName(sdcard_file, EXT_RULE_CHILD, RULE_INCLUDE, true, true, null);
			searchChild = true;
		}
		if (!ArrayUtils.isEmpty(cards)) {
			List<File> fs = new ArrayList<File>(3);
			/*注意对于i9100的外置卡external_sd的处理，如果是sdcard_file的子目录，则先查找空间大小非0且不与sdcard_file相同的，其他删除*/
			if (searchChild) {	//保留非默认卡
				for (File f : cards) {
					if (f != null) {
						//子目录上，不可能有空间大小等于0的，要么拔出了或者不是真的另一个卡，则相等；要么就不相等，则绝对是个不同的卡
						if (isAvailableNotSameSpace(sdcard_file, f)) fs.add(f);
					}
				}
				if (fs.size() > 0) {	//它们都是外置卡，可能有重复
					cards = fs.toArray(new File[fs.size()]);
				} else {	//都与sdcard_file相等，可能是拔出了，先留一个看看，由于搜索命名比较严格，相差无几，人为恶作剧就没办法了，不过肯定不会往上写东西，将会判定为未挂载
					File[] cards2 = null;
					for (File f : cards) {
						if (f != null) {
							cards2 = new File[] {f};
							break;
						}
					}
					cards = cards2;
				}
			} else {
				//去重复，保留非默认卡
				for (int i=0; i < cards.length; i++) {
					if (cards[i] != null) {
						if (cards[i].equals(sdcard_file) || isSameSpace(sdcard_file, cards[i])) cards[i] = null;
					}
				}
			}
			if (!ArrayUtils.isEmpty(cards)) {	//非默认卡去重复
				/*将名称带有ext的排在最前面，以防止去重复的时候干掉，
				 *旨在让外置卡的路径尽量名如其人，当然干掉也是无所谓的，仍然能正确找出外置卡*/
				fs.clear();
				for (int i=0; i < cards.length; i++) {
					if (cards[i] != null && cards[i].getName().toLowerCase().contains(extFlag)) {
						fs.add(cards[i]);
						cards[i] = null;
					}
				}
				for (File f : cards) {
					if (f != null) fs.add(f);
				}
				cards = fs.size() > 0 ? fs.toArray(new File[fs.size()]) : null;
				//去重复
				if (!ArrayUtils.isEmpty(cards)) {
					for (int i=0; i < cards.length; i++) {
						if (cards[i] != null) {
							for (int j=i + 1; j < cards.length; j++) {
								if (cards[j] != null) {
									if (isSameSpace(cards[i], cards[j])) cards[j] = null;
								}
							}
						}
					}
					fs.clear();
					for (File f : cards) {
						if (f != null) fs.add(f);
					}
					if (fs.size() > 0) {
						cards = fs.toArray(new File[fs.size()]);
					} else {
						cards = null;
					}
				}
			}
		}
		//合入sdcard_file并作为第一个
		if (ArrayUtils.isEmpty(cards)) {
			cards = new File[] {sdcard_file};
		} else {
			File[] cards2 = new File[cards.length + 1];
			cards2[0] = sdcard_file;
			for (int i=0; i < cards.length; i++) {
				cards2[i + 1] = cards[i];
			}
			cards = cards2;
		}
		return cards;
	}

	private static File searchCardFileWithFlagInName(File[] files, int startIndex, String flag, boolean containOrExcept) {
		File cardfile = null;
		boolean contain;
		for (int i = startIndex; i < files.length; i++) {
			contain = files[i].getName().toLowerCase().contains(flag);
			if (containOrExcept && contain || !containOrExcept && !contain) {
				cardfile = files[i];
				break;
			}
		}
		return cardfile;
	}

	public static String[] filterSubFilesWithNameSa(File parentDir, String namePart,
			int rule, boolean ignoreCase, boolean filterDir, String exceptName) {
		return ArrayUtils.toPathArray(filterSubFilesWithName(parentDir, namePart, rule, ignoreCase, filterDir, exceptName));
	}

	public static File[] filterSubFilesWithName(File parent, final String namePart,
			final int rule, final boolean ignoreCase, final boolean filterDir, final String exceptName) {
		if (rule < 0 || rule > 4) throw new IllegalArgumentException("filterType参数不正确");
		if (namePart == null || namePart.length() <= 0 || !parent.isDirectory()) return null;
		final FilenameFilter FILTER = new FilenameFilter() {
			boolean matchExceptName = exceptName != null && exceptName.length() > 0;
			@Override
			public boolean accept(File dir, String filename) {
				if (LOG) Log.i(LOG_TAG, "dir: " + dir + ", filename: " + filename);
				boolean accept = (!matchExceptName || !filename.equalsIgnoreCase(exceptName)) && nameMatches(filename, namePart, rule, ignoreCase);
				if (filterDir) {
					accept = accept && new File(dir, filename).isDirectory();
				}
				return accept;
			}
		};
		if (rule == RULE_WHOLE) {
			String[] filenames = parent.list();
			if (filenames == null) return null;
			for (String filename : filenames) {
				if (FILTER.accept(parent, filename)) {
					return new File[] {new File(parent, filename)};
				}
			}
		}
		return parent.listFiles(FILTER);
	}

	public static boolean nameMatches(String name, String contain, int rule, boolean ignoreCase) {
		boolean match = false;
		if (ignoreCase) {
			name = name.toLowerCase();
			contain = contain.toLowerCase();
		}
		switch (rule) {
		case RULE_START:
			match = name.startsWith(contain);
			break;
		case RULE_MIDDLE:
			int index = name.indexOf(contain);
			match = index > 0 && index < (name.length() - contain.length());
			break;
		case RULE_END:
			match = name.endsWith(contain);
			break;
		case RULE_WHOLE:
			match = name.equals(contain);
			break;
		case RULE_INCLUDE:
			String[] arr = contain.split("\\|");	//正则表达式需要转义
			if (!ArrayUtils.isEmpty(arr)) {
				for (String s : arr) {
					if (s != null && s.length() > 0) {
						String[] arr$ = s.split("&");	//可能没有&，没关系
						if (!ArrayUtils.isEmpty(arr$)) {
							for (String s$ : arr$) {
								if (s$ != null && s$.length() > 0) {
									if (s$.startsWith("^")) {
										s$ = s$.substring(1);
										if (s$.endsWith("$")) {
											s$ = s$.substring(0, s$.length() - 1);
											match = name.equals(s$);
										} else {
											match = name.startsWith(s$);
										}
									} else if (s$.endsWith("$")) {
										s$ = s$.substring(0, s$.length() - 1);
										match = name.endsWith(s$);
									} else {
										match = name.indexOf(s$) >= 0;
									}
									if (!match) break;
								}
							}
						}
						if (match) break;
					}
				}
			}
			break;
		}
		return match;
	}

	private static boolean isAvailableNotSameSpace(File fileExists, File fileForCheck) {
		long total = totalSpace(fileForCheck);
		long free = freeSpace(fileForCheck);
		long total2 = totalSpace(fileExists);
		long free2 = freeSpace(fileExists);
		return total > 0 && free > 0 && !(total == total2 && free == free2);
	}

	private static boolean isSameSpace(String path, String path2) {
		return isSameSpace(new File(path), new File(path2));
	}

	private static boolean isSameSpace(File f, File f2) {
		return totalSpace(f) == totalSpace(f2) && freeSpace(f) == freeSpace(f2);
	}

	/**外部存储器总容量 **/
	private static long totalSpace(String path) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return new File(path).getTotalSpace();
		}
		StatFs statFs = new StatFs(path);
		int blockSize = statFs.getBlockSize();
		long blockCount = statFs.getBlockCount();
		return blockSize * blockCount;
	}

	private static long totalSpace(File file) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return file.getTotalSpace();
		}
		StatFs statFs = new StatFs(file.getPath());
		int blockSize = statFs.getBlockSize();
		long blockCount = statFs.getBlockCount();
		return blockSize * blockCount;
	}

	/**注意,这可能是一个乐观的高估,不应视为保证您的应用程序可以写这麽多字节。(root用户可用的剩余空间)**/
	private static long freeSpace(String path) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return new File(path).getFreeSpace();
		}
		StatFs statFs = new StatFs(path);
		int blockSize = statFs.getBlockSize();
		long blockCount = statFs.getFreeBlocks();
		return blockSize * blockCount;
	}

	private static long freeSpace(File file) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return file.getFreeSpace();
		}
		StatFs statFs = new StatFs(file.getPath());
		int blockSize = statFs.getBlockSize();
		long blockCount = statFs.getFreeBlocks();
		return blockSize * blockCount;
	}

	/**外部存储器对程序所在用户可以使用的有效的容量。注意不是已经使用了的容量，值与freeSpace可能相同**/
	private static long usableSpace(String path) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return new File(path).getUsableSpace();
		}
		StatFs statFs = new StatFs(path);
		int blockSize = statFs.getBlockSize();
		long blockCount = statFs.getAvailableBlocks();
		return blockSize * blockCount;
	}

	@SuppressWarnings("unused")
	private static long usableSpace(File file) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return file.getUsableSpace();
		}
		StatFs statFs = new StatFs(file.getPath());
		int blockSize = statFs.getBlockSize();
		long blockCount = statFs.getAvailableBlocks();
		return blockSize * blockCount;
	}

	private static SdCard getMaxCard(boolean total, boolean exceptDataDir) {
		SdCard card = null;
		if (Storage.CARD_COUNT > 1) {
			if (total) {
				if (Storage.CARD_INNER.totalSpace() > Storage.CARD_EXT.totalSpace()) {
					if(exceptDataDir) {
						card = Storage.CARD_INNER;
					}else {
						if (Storage.DATA.totalSpace() > Storage.CARD_INNER.totalSpace()) {
							card = Storage.DATA;
						} else {
							card = Storage.CARD_INNER;
						}
					}
				} else {
					if(exceptDataDir) {
						card = Storage.CARD_EXT;
					}else {
						if (Storage.DATA.totalSpace() > Storage.CARD_EXT.totalSpace()) {
							card = Storage.DATA;
						} else {
							card = Storage.CARD_EXT;
						}
					}
				}
			} else {
				if (Storage.CARD_INNER.usableSpace() > Storage.CARD_EXT.usableSpace()) {
					if(exceptDataDir) {
						card = Storage.CARD_INNER;
					}else {
						if (Storage.DATA.usableSpace() > Storage.CARD_INNER.usableSpace()) {
							card = Storage.DATA;
						} else {
							card = Storage.CARD_INNER;
						}
					}
				} else {
					if(exceptDataDir) {
						card = Storage.CARD_EXT;
					}else {
						if (Storage.DATA.usableSpace() > Storage.CARD_EXT.usableSpace()) {
							card = Storage.DATA;
						} else {
							card = Storage.CARD_EXT;
						}
					}
				}
			}
		} else {
			if(exceptDataDir) {
				card = Storage.CARD_DEF;
			}else {
				if (total) {
					if (Storage.DATA.totalSpace() > Storage.CARD_DEF.totalSpace()) {
						card = Storage.DATA;
					} else {
						card = Storage.CARD_DEF;
					}
				} else {
					if (Storage.DATA.usableSpace() > Storage.CARD_DEF.usableSpace()) {
						card = Storage.DATA;
					} else {
						card = Storage.CARD_DEF;
					}
				}
			}
		}
		return card;
	}

	private static final String LOG_TAG = "Storage";
	private static final boolean LOG = false;

	private static final String EXT_RULE_CHILD = "^extsdcard$|^external_sd$|^ext&card$|^ext&sd$|^sd&card$";
	private static final String EXT_CARD_RULE = EXT_RULE_CHILD + "|^sdcard&ext|^card&ext|^extrasd_bind$";
	private static final String CARD_NAME_RULE = "^sdcard$|" + EXT_CARD_RULE + "|^sdcard|sdcard$";

	public static final int RULE_START			= 0;
	public static final int RULE_MIDDLE			= 1;
	public static final int RULE_END				= 2;
	public static final int RULE_WHOLE			= 3;
	/**可匹配伪正则表达式，"^"表示开头，"$"表示结尾。运算优先级："&"，"^"，"$"，"|"**/
	public static final int RULE_INCLUDE			= 4;
}

/* 以下方法可以一试。通过adb shell命令测试，/sdcard/external_sd目录还是不会找出来
 try {
			Runtime runtime = Runtime.getRuntime();
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			String line;
			String mount = new String();
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				if (line.contains("secure")) continue;
				if (line.contains("asec")) continue;

				if (line.contains("fat")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						mount = mount.concat("*" + columns[1] + "\n");
					}
				} else if (line.contains("fuse")) {
					String columns[] = line.split(" ");
					if (columns != null && columns.length > 1) {
						mount = mount.concat(columns[1] + "\n");
					}
				}
			}
			txtView.setText(mount);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
 */
