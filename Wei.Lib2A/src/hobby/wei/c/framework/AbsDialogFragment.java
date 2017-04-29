package hobby.wei.c.framework;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hobby.wei.c.anno.inject.Injector;
import hobby.wei.c.lib.R;

/**
 * @author Wei.Chou
 * @version 1.0, 22/03/2016
 */
public abstract class AbsDialogFragment extends DialogFragment implements DialogInterface.OnShowListener {
    private OnDialogListener mOnDialogListener;
    private Activity mActy;

    public void show(FragmentManager manager, String tag, boolean allowStateLoss) {
        if (allowStateLoss) {
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss();
        } else {
            super.show(manager, tag);
        }
    }

    /**
     * @deprecated 请替换成 {@link #show(FragmentManager, String, boolean)}
     */
    @Deprecated
    public void show(FragmentManager manager, String tag) {
        throw new RuntimeException();
    }

    public Activity getActy() {
        if (mActy == null) {
            synchronized (this) {
                if (mActy == null) {
                    mActy = super.getActivity();
                }
            }
        }
        return mActy;
    }

    public Resources getRes() {
        return getActy().getResources();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActy = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(cancelable());
        setStyle(STYLE_NO_TITLE, R.style.Theme_Wei_C_Dialog_Alert);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(Injector.layoutID(getActivity(), getClass()), container, false);
        Injector.inject(this, view, AbsDialogFragment.class);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside());
        dialog.setOnShowListener(this);
        // 不可以设置这两个监听
//        dialog.setOnCancelListener(this);
//        dialog.setOnDismissListener(this);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(getLayoutWidth(), getLayoutHeight());
    }

    protected abstract boolean cancelable();

    protected abstract int getLayoutWidth();

    protected abstract int getLayoutHeight();

    /**
     * 仅当{@link #cancelable()} 返回true的时候起作用。
     */
    protected boolean canceledOnTouchOutside() {
        return false;
    }

    public void delayDismiss(final boolean allowingStateLoss, final int timeDelayed) {
        AbsApp.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (allowingStateLoss) {
                    dismissAllowingStateLoss();
                } else {
                    dismiss();
                }
            }
        }, timeDelayed);
    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (mOnDialogListener != null) mOnDialogListener.onShow(this);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnDialogListener != null) mOnDialogListener.onCancel(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDialogListener != null) mOnDialogListener.onDismiss(this);
    }

    public void setOnDialogListener(OnDialogListener listener) {
        mOnDialogListener = listener;
    }

    public interface OnDialogListener {
        void onShow(AbsDialogFragment fragment);

        void onCancel(AbsDialogFragment fragment);

        void onDismiss(AbsDialogFragment fragment);
    }
}
