# Wei.Lib2A
------------------------------------------------------------------------------------------------
---
2016年新增内容：
### 全新基于`@Annotation`的混淆配置库 [Annoguard](http://github.com/WeiChou/Annoguard)
* 用法详见 [README](http://github.com/WeiChou/Annoguard/blob/master/README.md)

------------------------------------------------------------------------------------------------
---


Android快速开发库，Android常用工具集。任务管理、数据加载、下载库请查阅其他项目。<br>
对于初学者欢迎加 [QQ群:215621863](http://shang.qq.com/wpa/qunwpa?idkey=c39a32d6e9036209430732ff38f23729675bf1fac1d3e9faac09ed2165ae6e17 "Android编程&移动互联") 相互学习探讨！


### 这个库都有什么？能帮我们做什么？
#### 我写代码遵循两个基本原则：
* 减少代码量。希望达到的效果是，让使用者尽可能减少代码，能一句话搞定的，绝不两句，能静态方法搞定的，绝不new对象；<br>
* 增强适应性和稳定性。由于Android平台厂商定制的碎片化和众多版本兼容性问题，本库的开发会着重考虑这些因素，而且基本都是经上线的项目考验过的。

本库的所有代码都经过本人严格测试，字斟句酌，了如指掌，注释也很清晰。覆盖Android基础开发的方方面面，推荐作为项目的基础框架来使用，以便于各功能的正常便利集成。有任何疑问或建议请联系作者：
[weichou2010@gmail.com](mailto:weichou2010@gmail.com)、[微信](http://github.com/WeiChou/Wei.Lib2A/blob/master/README.md#联系作者_29) 或 [加群@群主](http://shang.qq.com/wpa/qunwpa?idkey=c39a32d6e9036209430732ff38f23729675bf1fac1d3e9faac09ed2165ae6e17 "Android编程&移动互联")。


### 基础常用组件介绍如下：

### 1、存储卡工具类 [Storage](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/phone/Storage.java)
仅通过几个常量即可便捷的取得内置或外置存储卡 [Storage.SdCard](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/phone/Storage.java#L149) 对象。例如：

```Java

    //输出外置存储卡的路径到logcat
    if (Storage.CARD_EXT != null) L.i(this, Storage.CARD_EXT.path);
    
    //是否可以在默认的卡上创建任意目录
    if (Storage.CARD_DEF.isCustomDirCreatable(context)) {
        //...
        try {
            File dir = FileUtils.makeDir(dirPath, true);
            L.i(this, dir.getPath());
            //...
        } catch (FileCreateFailureException e) {
            L.e(this, e);
        }
    }
```

### 2、存储卡自动选择与存储 [FStoreLoc](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FStoreLoc.java)
仅有`存储卡工具`就够了吗？它需要去判断存储卡是否存在、能否创建目录和文件，再决定是否写文件。而`FStoreLoc`可以一步完成：

```Java

    public static File getImagesCacheDir() throws SdCardNotMountedException,
                                SdCardNotValidException, FileCreateFailureException {
		return FStoreLoc.BIGFILE.getImagesCacheDir(get(), DirLevel.CUSTOM);
	}
```

* 上面代码中的 [`DirLevel.CUSTOM`](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FStoreLoc.java#L65)
表示选择自定义根目录，只有在存储卡支持创建自定义目录时才有效，否则抛异常。当然也可选择私有目录
[`DirLevel.PRIVATE`](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FStoreLoc.java#L67)
或自适应目录 [`DirLevel.DEFAULT`](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FStoreLoc.java#L69)，详见代码中的文档。

`FStoreLoc`有三种预置存储模式：

* 小文件模式：[`FStoreLoc.DEFAULT`](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FStoreLoc.java#L50);
* 大文件模式：[`FStoreLoc.BIGFILE`](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FStoreLoc.java#L52);
*   生存模式：[`FStoreLoc.SURVIVE`](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FStoreLoc.java#L60).

而`大文件模式`和`生存模式`根据业务需要可能需要设置存储卡根目录，那就放在App初始化里：

```Java

    public class App extends AbsApp {
        @Override
        public void onCreate() {
            Debug.DEBUG = isDebugMode();
            if (Debug.DEBUG) {
                //...
            }
            super.onCreate();
            //...
            /* 设置大文件模式的文件根目录。若存储卡根目录允许写文件，则创建该目录，否则会使用存储卡上的
             * 系统为App分配的私有目录：Android/appname/files/
             * 大文件模式，即只存放在内置或外置存储卡上，外置优先。
             */
            FStoreLoc.BIGFILE.setBaseDirName(this, Const.APP_DIR_NAME);
            //切换到外置卡（默认会自动选择剩余空间最大的那张卡）。但是读写文件过程中如果不存在会自动切换到内置卡。
            FStoreLoc.BIGFILE.switchTo(this, Storage.CARD_EXT);
        }
        //...
    }
```

### 3、文件工具 [FileUtils](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FileUtils.java) 和基于版本控制的多进程文件并发读写工具 [FileVersioned](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/file/FileVersioned.java)

### 4、增强的SharedPreferences工具：[Keeper](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/persist/Keeper.java)
具有多进程读写安全、基于Locale的文件隔离能力等。

基本用法：

```Java

    public final class XxxKeeper extends Keeper.Wrapper {
        private static final String SPREF_NAME = "spref_name";
    
        // 以下为固定写法
        public static WrapperImpl get() {
            return get(AbsApp.get().getApplicationContext(), SPREF_NAME);
        }
        
        public static final String KEY_VIEW_PEGER_INDEX = "view_peger_index";
        private static final String KEY_XXX_JSON = "xxx_json";
        // ...
        
        public static void saveXxxObj(XxxObj entity) {
            get()
                .withLocale()           // 根据需求可选
                .multiProcess()         // 根据需求可选
                .edit()                 // 根据需求可选
                .keepString(KEY_XXX_JSON, AbsJson.toJsonWithAllFields(entity));
                // 如果上面的Api无法满足需求，可用原生的
                .getSharedPreferences()
                .putString(KEY_XXX_JSON, AbsJson.toJsonWithAllFields(entity))
                .xxx();
        }
    
        public static XxxObj getXxxObj() {
            try {
                return AbsJson.fromJsonWithAllFields(
                    get()
                    .withLocale()           // 根据需求可选
                    .multiProcess()         // 根据需求可选
                    .edit()                 // 根据需求可选
                    .readString(KEY_XXX_JSON)
                    // 如果上面的Api无法满足需求，可用原生的
                    .getSharedPreferences()
                    .getString(KEY_XXX_JSON, null)
                    , XxxObj.clazz);
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    // 或者这样使用
    XxxKeeper.get().
                .withLocale()           // 根据需求可选
                .multiProcess()         // 根据需求可选
                .edit()                 // 根据需求可选
                .keepInt(XxxKeeper.KEY_VIEW_PEGER_INDEX, 1);
                // 如果上面的Api无法满足需求，可用原生的
                .getSharedPreferences()
                .putInt(XxxKeeper.KEY_VIEW_PEGER_INDEX, 1)
                .xxx();
```

### 5、网络连接状况判断 [Network](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/phone/Network.java)

### 6、网络连接状况监听 [NetConnectionReceiver](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/receiver/net)

### 7、存储卡挂载状况监听 [StorageReceiver](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/receiver/storage)
示例：

```Java

    @Override
	protected void onResume() {
		super.onResume();
		NetConnectionReceiver.registerObserver(mNetObserver);
    	StorageReceiver.registerObserver(mToastStorageObserver);
    	//StorageReceiver.registerObserver(mStorageObserver);
	}

	@Override
	protected void onPause() {
    	StorageReceiver.unregisterObserver(mToastStorageObserver);
    	//StorageReceiver.unregisterObserver(mStorageObserver);
		NetConnectionReceiver.unregisterObserver(mNetObserver);
		super.onPause();
	}

    private final NetObserver mNetObserver = new NetObserver() {
		@Override
		public void onChanged(Type type, State state) {
			ensureViewState(true, false);
		}
	};

    private final ToastStorageObserver mToastStorageObserver = new ToastStorageObserver(this);
    private final StorageObserver mStorageObserver = new StorageObserver() {
	    /**按下"MediaButton"按键时发出的广播,假如有"MediaButton"按键的话(硬件按键)，
	     * Intent.EXTRA_KEY_EVENT携带了这个KeyEvent对象**/
    	protected void onMediaButton(KeyEvent ev) {}
    	/**已经插入，但是不能挂载**/
    	protected void onMediaUnMountable(SdCard sdcard) {}
    	/**对象为空白或正在使用不受支持的文件系统，未格式化**/
    	protected void onMediaNoFS(SdCard sdcard) {}
    	/**扩展介质被插入，而且已经被挂载。intent包含一个名为"read-only"的boolean extra表明挂载点是否只读**/
    	protected void onMediaMounted(SdCard sdcard, boolean readOnly) {}
    	/**正在磁盘检查**/
    	protected void onMediaChecking(SdCard sdcard) {}
    	/**请求媒体扫描器扫描存储介质，以将媒体文件信息放入数据库**/
    	protected void onMediaScannerScanFile(String filePath) {}
    	/**媒体扫描器开始扫描文件目录**/
    	protected void onMediaScannerStarted(String dirPath) {}
    	/**媒体扫描器完成扫描文件目录**/
    	protected void onMediaScannerFinished(String dirPath) {}
    	/**由于通过USB存储共享导致无法挂载，挂载点路径可参见参数Intent.mData**/
    	protected void onMediaShared(SdCard sdcard) {}
    	/**用户希望弹出外部存储介质，收到此广播之后应该立即关闭正在读写的文件；
    	 * 要弹出的卡的路径在intent.getData()里面包含了，可以先判断是不是正在读写的文件的那个卡**/
    	protected void onMediaEject(SdCard sdcard) {}
    	/**已经卸载但未拔出**/
    	protected void onMediaUnmounted(SdCard sdcard) {}
    	/**已经卸载并拔出**/
    	protected void onMediaRemoved(SdCard sdcard) {}
    	/**未卸载直接拔出**/
    	protected void onMediaBadRemoval(SdCard sdcard) {}
    }
```

### 8、托管广播事件监听器 [EventDelegater](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/framework/EventDelegater.java)
基于但简化了`Broadcast`和`LocalBroadcast`的发送/接收操作，在页面初始化的时候可以调用[AbsActivity/AbsFragment.hostingLocalEventReceiver(...)](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/framework/AbsActivity.java#L233)将事件监听器进行托管，不再需要在`onPause()`、`onResume()`以及`onDestroy()`等事件的时候进行`unregisterXxx()`和`registerXxx()`编码，托管组件将自动完成。受惠于`Broadcast`的松散耦合机制和简化了的发送/接收操作，可将传统的`Callack`模式用本事件模式重构，轻松解决由于某些原因导致的`资源无法释放`和`内存泄露`等问题。示例：

```Java

    //在Activity或Fragment中发送事件
    @ViewLayoutId(R.layout.m_f_tabs_left_btn_page)
    public class MTabsLeftFrgmt extends AbsFragment {
        public void scroll2TopAndRefresh() {
            //发送事件
            sendLocalEvent(mRbtnRecommend.isChecked() ? Const.EventName.SCROLL_2_TOP_AND_REFRESH_frgmt_recommend
                    : Const.EventName.SCROLL_2_TOP_AND_REFRESH_frgmt_favorite, null);
        }
        //...
    }
    
    //在子Fragment中接收事件
    @ViewLayoutId(R.layout.f_m_f_tabs_left_btn_page_recommend)
    public class RecommendFrgmt extends AbsFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //只需要提前托管就好了，不用考虑onPause()、onResume()以及onDestroy()的时候还要取消注册或重新注册事件监听
            hostingLocalEventReceiver(Const.EventName.SCROLL_2_TOP_AND_REFRESH_frgmt_recommend, PeriodMode.PAUSE_RESUME, new EventReceiver() {
                @Override
                public void onEvent(Bundle data) {
                    scroll2TopAndRefresh();
                }
            });
        }
    
        public void scroll2TopAndRefresh() {
            L.i(this, "scroll2TopAndRefresh");
            //TODO 这里需要处理多次连续调用的情况
        }
    }
```

### 9、ResourcesId注解 [anno.inject](http://github.com/WeiChou/Wei.Lib2A/tree/master/Wei.Lib2A/src/hobby/wei/c/anno/inject)
* 注意`@ViewOnClick`的用法非常灵活。示例：

```Java

    @ViewLayoutId(R.layout.m_edit)
    public class EditActy extends AbsActivity implements OnClickListener {
        @ViewOnClick    // 当本类 implements OnClickListener, 那么直接加上本注解即可，不用参数
        @ViewId(R.id.m_edit_title_left_btn_back)
        private ImageButton mBtnBack;
        @ViewId(value = R.id.m_edit_magic_board, visibility = View.GONE)    // 多参数
        private MagicBoardView mMagicBoard;
        @ViewId(name = "m_edit_text", visibility = View.INVISIBLE)    // 对于库项目，R.id.xxx非final的情况下，可写name字符串
        private TextView mText;
        //...
    
        @Override
        @ViewOnClick(@Ids(R.id.m_edit_magic_board)) // 若前面没有加@ViewOnClick, 也可以写在onClick()上面，参数也可以像下面这样
        @ViewOnClick(@Ids({R.id.m_edit_title_left_btn_back, R.id.m_edit_magic_board}))
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.m_acty_btn_download:
                //...
            break;
        }
    
        @ViewOnClick(@Ids({R.id.m_edit_title_left_btn_back, R.id.m_edit_magic_board}))  // 或者可以写在任意自定义方法上面
        private void myOnClick(View v) {  // 这里也可以不用参数，像这样: private void myOnClick() {}
            //...
        }
    
        @ViewOnClick(@Ids({R.id.m_edit_title_left_btn_back, R.id.m_edit_magic_board}))  //甚至还可以这样，有没有觉得很cool
        private OnClickListener mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                //...
            }
        };
    }
```

### 10、Json的抽象 [IJson](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/data/abs)
示例：

```Java

    /**由于typeKey可能全局都一样，做一个抽象**/
    public abstract class AbsData<T extends AbsData<T>> extends AbsJsonTyped<T> {
        public static final String KEY_RESULT		= "result";
    
        @Expose
        public String result;
    
        protected String typeKey() {
            return KEY_RESULT;
        }
    }
    
    /**构建与Json字符串对应的类结构**/
    public class EditBean extends AbsData<EditBean> {
        @Expose
        public long id;
        @Expose
        public long topicId;
        @Expose
        public boolean favorite;
        @Expose
        public Info info;
    
        public boolean selected = false;
    
        public static class Info {
            public Temp[] temps;
            public int total_number;
        }
    
        public static class Temp {
            public long mid;
            public MagicBoardBody body;
            public int forwarding_count;
            public int classify;
            public long start_time;
            public long end_time;
            public int collect;
        }
    
        public EditBean() {}
    
        @Override
        public int hashCode() {
            return (int)id;
        }
    
        @Override
        public boolean equals(Object o) {
            EditBean fb = ((EditBean)o);
            return fb.id == id && fb.topicId == topicId;
        }
    
        @Override
        public EditBean fromJson(String json) {
            return fromJsonWithExposeAnnoFields(json, getTypeToken());
        }
    
        @Override
        public String toJson() {
            return toJsonWithExposeAnnoFields(this);
        }
    
        @Override
        protected String[] typeValues() {
            return null;
        }
    
        @Override
        protected TypeToken<EditBean> getTypeToken() {
            return new TypeToken<EditBean>(){};
        }
    }
    
    /*以下为操作数据*/
    
    EditBean edit = new EditBean();
    //edit.xxx = xxx;
    //...
    //序列化
    edit.toJson();
    
    //判断是否属于本类型，一般情况用不到。
    if (new EditBean().isBelongToMe(new JSONObject(jsonString))) {
        //...
    }
    
    //反序列化
    new EditBean().fromJson(jsonString);
```

### 11、用途广泛的 [ViewHolder](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/framework/ViewHolder.java)
既可用于`ListView`的`Adapter`：

```Java

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return EditGridViewHolder.getAndBindView(position, convertView, parent, getInflater(), EditGridViewHolder.class, getItem(position), mOnFavoriteClick);
	}
    //...

    @ViewLayoutId(R.layout.i_m_edit)
    public static class EditGridViewHolder extends ViewHolder<EditBean, OnClickListener> {
		@ViewId(R.id.i_m_edit_magic_board)
		private MagicBoardView mMagicBoard;
		@ViewId(R.id.i_m_edit_cb_favorite)
		private CheckBox mCBFav;

		private Animation mAnimYes, mAnimNo;
		private boolean mAnimYesStarted = false, mAnimNoStarted = false;

		public EditGridViewHolder(View view) {
			super(view);
		}

		@Override
		protected void init(OnClickListener... args) {
			mCBFav.setOnClickListener(args[0]);
		}

		@Override
		public void bind(int position, EditListBean data) {
			mCBFav.setTag(this);
			MagicBoardUtils.display(getView().getContext(), mMagicBoard, data.magicBoard);
			
			mPanelTranslucence.setVisibility(data.selected ? View.VISIBLE : View.GONE);
			updateFavorite(false);
		}
        //...
    }
```

也可用于`ViewPager`的`PagerAdapter`:

```Java

    mViewPager.setAdapter(new PagerAdapter() {
    		@Override
			public int getCount() {
				return 5;
			}

			@Override
			public boolean isViewFromObject(View view, Object obj) {
				return view == ((ViewHolder<?, ?>)obj).getView();
			}

			@Override
			public int getItemPosition(Object obj) {
				int position = super.getItemPosition(obj);
				if (obj instanceof ViewHolder4) {
					position = 4;
				} else if (obj instanceof ViewHolder3) {
					position = 3;
				} else if (obj instanceof ViewHolder2) {
					position = 2;
				} else if (obj instanceof ViewHolder1) {
					position = 1;
				} else if (obj instanceof ViewHolder0) {
					position = 0;
				}
				return position;
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				ViewHolder<Void, OnClickListener> vHolder = null;
				switch (position) {
				case 0:
					vHolder = ViewHolder.bindView(0, ViewHolder.makeView(ViewHolder0.class, getLayoutInflater(), container), ViewHolder0.class, null, mOnNextClick);
					break;
				case 1:
					vHolder = ViewHolder.bindView(0, ViewHolder.makeView(ViewHolder1.class, getLayoutInflater(), container), ViewHolder1.class, null, mOnNextClick);
					break;
				case 2:
					vHolder = ViewHolder.bindView(0, ViewHolder.makeView(ViewHolder2.class, getLayoutInflater(), container), ViewHolder2.class, null, mOnNextClick);
					break;
				case 3:
					vHolder = ViewHolder.bindView(0, ViewHolder.makeView(ViewHolder3.class, getLayoutInflater(), container), ViewHolder3.class, null, mOnNextClick);
					break;
				case 4:
					vHolder = ViewHolder.bindView(0, ViewHolder.makeView(ViewHolder4.class, getLayoutInflater(), container), ViewHolder4.class, null, mOnCompleteClick);
					break;
				}
				container.addView(vHolder.getView());
				return vHolder;
			}

			@Override
			public void destroyItem(ViewGroup container, int position, Object obj) {
				container.removeView(((ViewHolder<?, ?>)obj).getView());
			}
		});
	}
    //...
    
    @ViewLayoutId(R.layout.i_m_guide_next)
    private static class ViewHolder0 extends ViewHolder<Void, OnClickListener> {
		@ViewId(R.id.i_m_guide_bg)
		protected View mBg;
		@ViewId(R.id.i_m_guide_btn_next)
		protected ImageButton mBtnNext;

		protected static final int WIDTH		= 720;
		protected static final int HEIGHT		= 1280;
		protected static final int WIDTH_BTN	= 224;
		protected static final int HEIGHT_BTN	= 88;
		protected static final int RIGHT_BTN	= 16;
		protected static final int BOTTOM_BTN	= 29;

		public ViewHolder0(View view) {
			super(view);
		}

		@Override
		protected void init(OnClickListener... args) {
			mBg.setBackgroundResource(R.drawable.img_i_m_guide_0);
			mBtnNext.setTag(0);
			mBtnNext.setOnClickListener(args[0]);
			initBtnNextPosition();
		}

		@Override
		public void bind(int position, Void data) {}

		protected void initBtnNextPosition() {
			int screenWidth = Device.getInstance(getView().getContext()).width;
			int screenHeight = Device.getInstance(getView().getContext()).height;
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBtnNext.getLayoutParams();
			float widthScale = screenWidth * 1.0f / WIDTH;
			float heightScale = screenHeight * 1.0f / HEIGHT;
			lp.width = (int) (widthScale * WIDTH_BTN);
			lp.height = (int) (heightScale * HEIGHT_BTN);
			lp.rightMargin = (int) (widthScale * RIGHT_BTN);
			lp.bottomMargin = (int) (heightScale * BOTTOM_BTN);
			mBtnNext.setLayoutParams(lp);
		}
	}

	private static class ViewHolder1 extends ViewHolder0 {
		public ViewHolder1(View view) {
			super(view);
		}

		@Override
		protected void init(OnClickListener... args) {
			mBg.setBackgroundResource(R.drawable.img_i_m_guide_1);
			mBtnNext.setTag(1);
			mBtnNext.setOnClickListener(args[0]);
			initBtnNextPosition();
		}
	}
    //...
```

还可用于其他任何场景：

```Java

    public class Xxx {
        @Override
        public void onClick(View v) {
            //弹出删除对话框
            FavoriteDeleteViewHolder.showDeleteDialog(FavoriteActy.this, (ViewGroup)getWindow().getDecorView(), mOnDeleteClick);
        }
        //...
    }
    
    @ViewLayoutId(R.layout.m_favorite_delete_panel)
    public class FavoriteDeleteViewHolder extends ViewHolder<Void, OnClickListener> {
        @ViewId(R.id.m_favorite_delete_panel_content)
        private ViewGroup mContentView;
        @ViewId(R.id.m_favorite_delete_panel_btn_delete)
        private Button mBtnDelete;
        @ViewId(R.id.m_favorite_delete_panel_btn_cancel)
        private Button mBtnCancel;
    
        private Context mContext;
        private OnClickListener mOnDeleteClickCallback;
        private boolean mDelete;
    
        public FavoriteDeleteViewHolder(View view) {
            super(view);
            mContext = view.getContext();
        }
    
        public static FavoriteDeleteViewHolder showDeleteDialog(Activity context, ViewGroup parent, OnClickListener onDeleteClickCallback) {
            View view = FavoriteDeleteViewHolder.makeView(FavoriteDeleteViewHolder.class, context.getLayoutInflater(), parent);
            FavoriteDeleteViewHolder vHolder = FavoriteDeleteViewHolder.bindView(0, view, FavoriteDeleteViewHolder.class, null, onDeleteClickCallback);
            parent.addView(view);
            vHolder.startAnimIn();
            return vHolder;
        }
    
        public void destroy() {
            startAnimOut();
        }
        //...
    }
```
* 简直神乎其技呀，有木有？！！！

### 12、相册选择图片、剪裁，保存图片到相册并广播刷新 [PhotoUtils](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/utils/PhotoUtils.java)
示例：

```Java

    private static final String EXTRA_SESSION           = ContributeActy.class.getName() + ".SESSION";
	private static final String EXTRA_LIST_DATA         = ContributeActy.class.getName() + ".LIST_DATA";

    private static final int REQUEST_CODE_PICK_PHOTO    = 100;
    private static final int REQUEST_CODE_CROP          = 101;

    private PhotoUtils.Session session;
    //...

    @Override
    public void onClick(View v) {
        try {
        	session = PhotoUtils.openSysGallery2ChoosePhoto(ContributeActy.this, REQUEST_CODE_PICK_PHOTO,
        		new CropArgs(REQUEST_CODE_CROP,
            		new File(App.getImagesCacheDirPrivate(), "croptemp-" + System.currentTimeMillis() + ".png").getPath(),
            		Bitmap.CompressFormat.PNG, false, true, 0, 0, 0, 0, 640, 640));
        	L.i(ContributeActy.class, session.toString());
        } catch (Exception e) {
        	L.e(ContributeActy.class, e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	Uri uri = PhotoUtils.onActivityResult(this, session, requestCode, resultCode, data);
    	if (uri != null) {
    		getAdapter().getData().add(uri);
    		getAdapter().notifyDataSetChanged();
    		session = null;
    	}
    }

    @Override
	protected void onSaveInstanceState(Bundle outState) {
		List<Uri> data = getAdapter().getData();
		if (data.size() > 0) {
			ArrayList<String> value = new ArrayList<String>();
			for (Uri uri : data) {
				value.add(uri.toString());
			}
			outState.putStringArrayList(EXTRA_LIST_DATA, value);
		}
		if (session != null) outState.putString(EXTRA_SESSION, session.toJson());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		ArrayList<String> value = savedInstanceState.getStringArrayList(EXTRA_LIST_DATA);
		if (value != null && value.size() > 0) {
			List<Uri> data = new ArrayList<Uri>();
			for (String s : value) {
				data.add(Uri.parse(s));
			}
			getAdapter().setDataSource(data);
		}
		String json = savedInstanceState.getString(EXTRA_SESSION);
		if (json != null) session = Session.fromJsonWithAllFields(json, Session.class);
	}
```

### 13、简化ListView及同类组件数据更新的套件 [AbsListViewActivity](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/framework/AbsListViewActivity.java)、[AbsListViewFragment](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/framework/AbsListViewFragment.java) 和 [AbsAdapter<T>](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/adapter/AbsAdapter.java)
示例：

```Java

    @ViewLayoutId(R.layout.m_edit)
    @ViewListId(R.id.m_edit_grid_view)
    public class EditActy extends AbsListViewActivity<GridView, EditBean, EditGridAdapter> {
        private static final String EXTRA_CATEGORY_ID		= EditActy.class.getName() + ".EXTRA_CATEGORY_ID";
    
        public static void startMe(Context context, long categoryId) {
            Intent intent = new Intent(context, EditActy.class);
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
            startMe(context, intent);
        }
    
        @ViewId(R.id.m_edit_title_left_btn_back)
        private ImageButton mBtnBack;
        @ViewId(R.id.m_edit_magic_board)
        private MagicBoardView mMagicBoard;
    
        @Override
        protected EditGridAdapter newAdapter() {
            return new EditGridAdapter(this, mOnFavoriteClick);
        }
        //...
    }
```

### 14、图像工具 [BitmapUtils](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/utils/BitmapUtils.java)

### 15、AndroidManifest.xml属性读取工具 [Manifest](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/utils/Manifest.java)

### 16、对话框、弹窗和进度条 [DialogHelper、Prompt 和 PromptProgress](http://github.com/WeiChou/Wei.Lib2A/tree/master/Wei.Lib2A/src/hobby/wei/c/widget/)
* UI和动画都可全局定制；
* `DialogHelper`可弹出在桌面上。

### 17、文本超链接点击效果 [LinkMovementMethod、LinkSpan](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/widget/text)

### 18、具有退出监听能力的 [AbsApp](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/framework/AbsApp.java)

```Java

    public class App extends AbsApp {
        @Override
        protected boolean onExit() {
            L.i(this, "程序正常退出------App.onExit()");
            //...
            super.onExit();
            if (mExitForRestart) {
                WelcomeActy.startMe(this);
                mExitForRestart = false;
            }
            return false;
        }
        //...
    }
```

### 19、非对称密钥对生成工具 [RsaUtils](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/utils/RsaUtils.java)
* 可生成iOS项目能识别的公钥密钥文件(iOS要求比较苛刻，而java的库对密钥文件的识别能力较强)。

###20、时间工具 [TimeUtils](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/utils/TimeUtils.java)
* 可解析C#生成的Java标准API`SimpleDateFormat`无法识别的格式化字符串如`2013-05-22T09:16:44.871589GMT+0800`和`2013-05-22T09:16:44.871589+08:00`；
* 可根据生日计算年龄；
* 可生成无时区的时间长度表示，并定制单位，如：3天前，1周前，2年6个月前等；
* 可根据系统时间返回凌晨、上午、中午、下午、晚上、晚休等信息。

### 21、Log的简化版[ L ](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/L.java)
* 可在release打包混淆时，将低级别日志自动删除，同时优化掉作为参数的字符串常量(对参数有规范化要求，见代码文档：[@Burden](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/anno/proguard/Burden.java) 或 [proguard.README](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/anno/proguard/README.md))。

### 22、全局异常拦截器 [CrashHandler](http://github.com/WeiChou/Wei.Lib2A/blob/master/Wei.Lib2A/src/hobby/wei/c/utils/CrashHandler.java)
* 可dump运行时内存到存储卡(.hprof文件)用于内存分析；
* 可拦截某线程的某异常或所有异常；
* 可拦截所有线程的某异常或所有异常；
* 拦截后的处理方式应该根据业务需要自定义处理，默认是作闪退处理。

### 23、全新基于`@Annotation`的混淆配置库 [Annoguard](http://github.com/WeiChou/Annoguard)
* 用法详见 [README](http://github.com/WeiChou/Annoguard/blob/master/README.md)

#### 联系作者

![微信](weichat_qr_code.jpg)
![支付宝](zhifubao_qr_code.jpg)
