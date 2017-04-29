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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
<pre>
需要权限：
android.permission.ACCESS_NETWORK_STATE
android.permission.ACCESS_WIFI_STATE
Ping或获取本机IP需要权限：
android.permission.INTERNET
设置漫游状态需要权限：
android.permission.WRITE_SETTINGS
</pre>
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class Network {
    public static Type getNetworkType(Context context) {
		NetworkInfo networkInfo = getConnectManager(context).getActiveNetworkInfo();
		if(networkInfo == null) {
			return Type.NO_NET;
		}
		return getType(networkInfo);
	}

	public static Type getConnectedNetworkType(Context context) {
		NetworkInfo networkInfo = getConnectManager(context).getActiveNetworkInfo();
		if(networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED) {
			return Type.NO_NET;
		}
		return getType(networkInfo);
	}

	public static State getNetworkState(Context context) {
		NetworkInfo networkInfo = getConnectManager(context).getActiveNetworkInfo();
		if(networkInfo == null) return State.DISCONNECTED;

		State state = State.UNKNOWN;

		switch(networkInfo.getState()) {
		case CONNECTING:
			state = State.CONNECTING;
			break;
		case CONNECTED:
			state = State.CONNECTED;
			break;
		case SUSPENDED:
			state = State.SUSPENDED;
			break;
		case DISCONNECTING:
			state = State.DISCONNECTING;
			break;
		case DISCONNECTED:
			state = State.DISCONNECTED;
			break;
		case UNKNOWN:
			state = State.UNKNOWN;
			break;
		}
		return state;
	}

	public static boolean isNetConnected(Context context) {
		NetworkInfo networkInfo = getConnectManager(context).getActiveNetworkInfo();
		if(networkInfo == null) {
			return false;
		}
		return networkInfo.getState() == NetworkInfo.State.CONNECTED;
	}

	public static boolean isWifiConnected(Context context) {
		return getConnectedNetworkType(context) == Type.WIFI;
	}

	public static boolean is4GConnected(Context context) {
		return getConnectedNetworkType(context) == Type.G4;
	}

	public static boolean is3GConnected(Context context) {
		return getConnectedNetworkType(context) == Type.G3;
	}

	public static boolean is2GConnected(Context context) {
		return getConnectedNetworkType(context) == Type.G2;
	}

	public static boolean isRoaming(Context context) {
		NetworkInfo networkInfo = getConnectManager(context).getActiveNetworkInfo();
		boolean isMobile = (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
		boolean isRoaming = isMobile && getTelephonyManager(context).isNetworkRoaming();
		return isRoaming;
	}

	@SuppressWarnings("deprecation")
	public static void setAirplaneMode(Context context, boolean on) {
		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, on ? 1 : 0);
	}

	@SuppressWarnings("deprecation")
	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	public static void openNetGraceful(Context context) {
		try {
			context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
		} catch (Exception e) {
			context.startActivity(new Intent(Settings.ACTION_SETTINGS));
		}
	}

	private static ConnectivityManager getConnectManager(Context context) {
		return (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	private static TelephonyManager getTelephonyManager(Context context) {
		return (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	private static Type getType(NetworkInfo netInfo) {
		Type type;
		//状态有：TYPE_WIFI、TYPE_MOBILE、TYPE_MOBILE_MMS等
		if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {	//wifi为当前活动网络连接
			type = Type.WIFI;
		} else {	//除TYPE_WIFI以外
			switch(netInfo.getSubtype()) {
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				type = Type.NO_NET;
				break;
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
				type = Type.G2;
				break;
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_CDMA:	//都属于3G或更快网络
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_1xRTT:	//属于2.5G，但属于CDMA范畴
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_IDEN:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				type = Type.G3;
				break;
			case TelephonyManager.NETWORK_TYPE_LTE:
				type = Type.G4;
				break;
			default:
				type = Type.G4;
				break;
			}
		}
		return type;
	}

	public static String IP_Host(String host, boolean format) {
		try {
			String ip = InetAddress.getByName(host).getHostAddress();
			return format ? ip.replace('.', '_') : ip;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String IP_Me(boolean format) {
		HttpGet get = new HttpGet("http://whois.pconline.com.cn/ipJson.jsp");
		Header[] headers = new Header[4];
		headers[0] = new BasicHeader("Accept", "Application/json;q=0.9,*/*;q=0.8");
		headers[1] = new BasicHeader("Accept-Charset", "utf-8");
		headers[2] = new BasicHeader("Content-Type", "application/x-www-form-urlencoded");
		headers[3] = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11" +
				" (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
		get.setHeaders(headers);
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
		HttpConnectionParams.setSoTimeout(httpParams, 30000);
		ConnManagerParams.setTimeout(httpParams, 30000);
		try {
			HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(get);
			StatusLine statusLine = httpResponse.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if(statusCode == 200) {
				String json = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
				if(!TextUtils.isEmpty(json)) {
					json = json.substring(json.indexOf("{\""), json.indexOf(");")).trim();
					JSONObject obj = new JSONObject(json);
					if(format) {
						json = obj.getString("ip").replace('.', '_');
						//json += "-" + obj.getString("pro");
						//json += "-" + obj.getString("city");
						json += "-" + obj.getString("addr").replace(" ", "");
						//obj.getString("regionNames");
					}else {
						json = obj.getString("ip");
						//json += " " + obj.getString("pro");
						//json += " " + obj.getString("city");
						json += " " + obj.getString("addr");
						//obj.getString("regionNames");
					}
				}
				return json;
			}else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static String DNS(int n, boolean format) {
		String dns = null;
		Process process = null;
		LineNumberReader reader = null;
		try {
			final String CMD = "getprop net.dns" + (n <= 1 ? 1 : 2);

			process = Runtime.getRuntime().exec(CMD);
			reader = new LineNumberReader(new InputStreamReader(process.getInputStream()));

			String line = null;
			while ((line = reader.readLine()) != null) {
				dns = line.trim();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(reader != null) reader.close();
				if(process != null) process.destroy();	//测试发现，可能会抛异常
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return format ? (dns != null ? dns.replace('.', '_') : dns) : dns;
	}

	public static String PING(String host, boolean format) {
		final int PACKAGES = 4;
		String info = null;
		String print = null;
		Process process = null;
		LineNumberReader reader = null;
		try {
			final String CMD = "ping -c " + PACKAGES + " " + host;
			if(format) {
				info = "ping-c" + PACKAGES + "-" + host.replace('.', '_');
			}else {
				print = CMD + "\n";
			}

			process = Runtime.getRuntime().exec(CMD);
			reader = new LineNumberReader(new InputStreamReader(process.getInputStream()));

			String line = null;
			boolean start = false;
			int index = -1;
			while ((line = reader.readLine()) != null) {
				if(!format) {
					print += line + "\n";
				}else {
					line = line.trim();
					if(line.toLowerCase().startsWith("ping")) {
						line = line.substring(0, line.indexOf(')'));
						line = line.replace("(", "");
						line = line.replace(' ', '-');
						line = line.replace('.', '_');
						start = true;
					}else if(start) {
						index = line.indexOf(':');
						if(index > 0) {
							//取得ttl=53部分
							line = line.substring(index+1).trim();
							index = line.indexOf(' ');
							line = line.substring(index+1, line.indexOf(' ', index+3)).trim();
							line = line.replace('=', '_');
							start = false;
						}else {
							start = false;
							continue;
						}
					}else if(line.startsWith(""+PACKAGES)) {
						index = line.indexOf(',');
						line = line.substring(index+1).trim();
						line = line.substring(0, line.indexOf(' ')).trim();
						line = line + "in" + PACKAGES + "received";
					}else if(line.startsWith("rtt")) {
						line = line.replaceFirst(" ", "-");
						line = line.replace(" ", "");
						line = line.replace('/', '-');
						line = line.replace('.', '_');
						line = line.replace("=", "--");
					}else {
						continue;
					}
					if(info == null) info = line;
					info += "--" + line;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if(reader != null) reader.close();
				if(process != null) process.destroy();	//测试发现，可能会抛异常
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		return format ? info : print;
	}

	public enum State {
		CONNECTING,
		CONNECTED,
		/**网络连接被禁用**/
		SUSPENDED,
		DISCONNECTING,
		DISCONNECTED,
		UNKNOWN
	}

	public enum Type {
		NO_NET,
		/**2G网络**/
		G2,
		/**3G网络**/
		G3,
		/**4G或更快的网络**/
		G4,
		WIFI;
	}
}
