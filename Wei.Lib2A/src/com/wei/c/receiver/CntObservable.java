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

package com.wei.c.receiver;

import android.content.Context;
import android.content.Intent;
import android.database.Observable;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public abstract class CntObservable<O extends Observer, DATA> extends Observable<O> {

	public int countObservers() {
		return mObservers.size();
	}
	
	void notifyChanged(Context context, Intent intent) {
		/*Intent service = new Intent(context, DownloadService.class);
		INetObserver binder = (INetObserver)peekService(context, service);
		if(binder != null) binder.notifyNetworkStateChange();*/
		
		 DATA data = onParserData(context, intent);
		 
		 synchronized(mObservers) {
	             /*since onChanged() is implemented by the app, it could do anything, including
	             removing itself from {@link mObservers} - and that could cause problems if
	             an iterator is used on the ArrayList {@link mObservers}.
	             to avoid such problems, just march thru the list in the reverse order.*/
			 
	            for (int i = mObservers.size() - 1; i >= 0; i--) {
	            	onChange(mObservers.get(i), data);
	            }
	        }
	}
	
	protected abstract DATA onParserData(Context context, Intent intent);
	
	protected abstract void onChange(O observer, DATA data);
}
