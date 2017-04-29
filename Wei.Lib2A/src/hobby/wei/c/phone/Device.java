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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import hobby.wei.c.used.UsedKeeper;
import hobby.wei.c.utils.MD5Utils;

/**
 * <pre>
 * 需要权限：
 * android.permission.READ_PHONE_STATE
 * </pre>
 *
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
@SuppressLint("NewApi")
public class Device {
    public final int width;
    public final int height;
    /**
     * 改为需要的时候获取，避免在登录时提示获取权限
     **/
    private String phoneNumber;
    /**
     * 唯一标识串码IMEI。注意：没有通话模块的手机或者平板没有该信息（测试发现有些手机的DevId会改变，机型Mac地址以及其他信息都不变，而DevId会变）
     **/
    public final String deviceId;
    public final String macAddress;
    public final String androidId;
    public final String sysVersion;
    public final String brand;
    public final String cpuAbi;

    private static Device mDeviceInfo;

    public static Device getInstance(Context context) {
        if (mDeviceInfo == null) mDeviceInfo = new Device(context);
        return mDeviceInfo;
    }

    private Device(Context context) {
        //屏幕尺寸
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenW = dm.widthPixels;// 获取分辨率宽度
        int screenH = dm.heightPixels;
        if (screenW > screenH) {
            int i = screenW;
            screenW = screenH;
            screenH = i;
        }
        width = screenW;
        height = screenH;

        //唯一标识串码
        deviceId = getTelephonyManager(context).getDeviceId();
        //系统版本
        sysVersion = "Android " + Build.VERSION.RELEASE;
        //设备型号
        brand = Build.BRAND + " " + Build.MODEL;

        if (Build.VERSION.SDK_INT < 8) {
            cpuAbi = Build.CPU_ABI;
        } else {
            cpuAbi = Build.CPU_ABI + (Build.CPU_ABI2.equals(Build.UNKNOWN) ? "" : ", " + Build.CPU_ABI2);
        }

        WifiManager wManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // TODO: 16/3/19 整个类待重构
        WifiInfo info = wManager.getConnectionInfo();
        macAddress = info == null ? null : info.getMacAddress();

        androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getPhoneNumber(Context context) {
        Device device = getInstance(context);
        if (device.phoneNumber == null) {
            device.phoneNumber = getTelephonyManager(context).getLine1Number();
            if (device.phoneNumber == null || device.phoneNumber.length() == 0 || device.phoneNumber.matches("0*")) device.phoneNumber = "";
        }
        return device.phoneNumber;
    }

    public static String getUniqueId(Context context) {
        String id = UsedKeeper.DeviceS.getUniqueId();
        if (id == null) {
            Device device = getInstance(context);
            if (device.deviceId != null) {
                id = device.deviceId;
            } else if (device.androidId != null) {
                id = device.androidId;
            } else {
                id = device.macAddress;
            }
            id = MD5Utils.toMD5(id);
            UsedKeeper.DeviceS.saveUniqueId(id);
        }
        return id;
    }

    private static TelephonyManager getTelephonyManager(Context context) {
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }
}
