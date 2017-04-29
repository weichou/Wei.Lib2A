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

package hobby.wei.c.framework;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hobby.wei.c.anno.inject.Injector;
import hobby.wei.c.framework.EventDelegater.EventReceiver;
import hobby.wei.c.framework.EventDelegater.PeriodMode;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class AbsFragment extends Fragment {
    private Activity mActy;
    private View mCreatedView;

    protected Activity getActy() {
        if (mActy == null) {
            synchronized (this) {
                if (mActy == null) {
                    mActy = getActivity();
                }
            }
        }
        return mActy;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCreatedView = inflater.inflate(Injector.layoutID(inflater.getContext(), getClass()), container, false);
        Injector.inject(this, mCreatedView, AbsFragment.class);
        return mCreatedView;
    }

	/*@Override
    public View getView() {
	}*/

    /** 注意父类有个{@link #getView()} **/
    public View getCreatedView() {
        return mCreatedView;
    }

    public void sendLocalEvent(String eventName, Bundle data) {
        EventDelegater.sendLocalEvent(getActy(), eventName, data);
    }

    public void sendGlobalEvent(String eventName, Bundle data) {
        EventDelegater.sendGlobalEvent(getActy(), eventName, data);
    }

    public void hostingLocalEventReceiver(String eventName, PeriodMode periodMode, EventReceiver receiver) {
        ensureEventDelegaterMade();
        mEventDelegater.hostingLocalEventReceiver(eventName, periodMode, receiver);
    }

    public void hostingGlobalEventReceiver(String eventName, PeriodMode periodMode, EventReceiver receiver) {
        ensureEventDelegaterMade();
        mEventDelegater.hostingGlobalEventReceiver(eventName, periodMode, receiver);
    }

    public void unhostingLocalEventReceiver(String eventName) {
        ensureEventDelegaterMade();
        mEventDelegater.unhostingLocalEventReceiver(eventName);
    }

    public void unhostingGlobalEventReceiver(String eventName) {
        ensureEventDelegaterMade();
        mEventDelegater.unhostingGlobalEventReceiver(eventName);
    }

    private void ensureEventDelegaterMade() {
        if (mEventDelegater == null) mEventDelegater = new EventDelegater(getActy());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mEventDelegater != null) mEventDelegater.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mEventDelegater != null) mEventDelegater.onActivityCreated(getActy());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEventDelegater != null) mEventDelegater.onResume();
    }

    @Override
    public void onPause() {
        if (mEventDelegater != null) mEventDelegater.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mEventDelegater != null) mEventDelegater.onStop();
        super.onStop();
    }

    private EventDelegater mEventDelegater;
}
