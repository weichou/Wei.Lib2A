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

package hobby.wei.c.receiver.net;

import android.content.Context;

import hobby.wei.c.phone.Network;
import hobby.wei.c.phone.Network.State;
import hobby.wei.c.phone.Network.Type;
import hobby.wei.c.receiver.AbsRcvrObservable;
import hobby.wei.c.receiver.net.NetObservable.Data;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class NetObservable extends AbsRcvrObservable<NetObserver, Data> {
    static class Data {
        Type type;
        State state;

        Data(Context context) {
            type = Network.getNetworkType(context);
            state = Network.getNetworkState(context);
        }
    }

    @Override
    protected Data onParseData(Tuple tuple) {
        return new Data(tuple.context);
    }

    @Override
    protected void onNotifyChange(NetObserver observer, Data data) {
        observer.onChanged(data.type, data.state);
    }
}
