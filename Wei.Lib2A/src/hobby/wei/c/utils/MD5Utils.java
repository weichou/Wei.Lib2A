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

package hobby.wei.c.utils;

import android.annotation.SuppressLint;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;

/**
 * MD5生成及验证工具
 * 
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class MD5Utils {
	private final static String HEX_NUMS_STR = "0123456789ABCDEF";
	private final static int SALT_LENGTH = 12;

	/**
	 * MD5验证
	 * @param passwd 未MD5加密的字符串
	 * @param md5Hexs 已保存的MD5加密后的字符串
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static boolean verify(String passwd, String md5Hexs) {
		try {
			byte[] pwdmd5ed =  hexsToBytes(md5Hexs);
			byte[] digestMd5edPwd = pwdmd5ed;
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (pwdmd5ed.length == 28) {	//有加盐
				byte[] salt = new byte[SALT_LENGTH];
				System.arraycopy(pwdmd5ed, 0, salt, 0, SALT_LENGTH);
				md.update(salt);
				digestMd5edPwd = new byte[pwdmd5ed.length - SALT_LENGTH];
				System.arraycopy(pwdmd5ed, SALT_LENGTH, digestMd5edPwd, 0, digestMd5edPwd.length);
			}
			md.update(passwd.getBytes("UTF-8"));
			byte[] digest = md.digest();
			return Arrays.equals(digest, digestMd5edPwd);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		throw new RuntimeException();
	}

	/**
	 * 获得md5生成的16进制字符串，长度是56位，其中的24位是salt生成的
	 * @param passwd 需要生成md5摘要的字符串
	 * @return md5加密后的字符串，长度是56位
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressLint("TrulyRandom")
    public static String toMD5WithSalt(String passwd) {
		try {
			//拿到一个随机数组，作为盐
			byte[] salt = new byte[SALT_LENGTH];
			new SecureRandom().nextBytes(salt);
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt);
			md.update(passwd.getBytes("UTF-8"));
			byte[] digest = md.digest();
			byte[] pwd = new byte[salt.length + digest.length];
			System.arraycopy(salt, 0, pwd, 0, SALT_LENGTH);
			System.arraycopy(digest, 0, pwd, SALT_LENGTH, digest.length);
			return bytesToHexs(pwd);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		throw new RuntimeException();
	}

	/**
	 * 获得md5生成的16进制字符串，长度是32位
	 * @param passwd 需要生成md5摘要的字符串
	 * @return md5加密后的字符串，长度是32位
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String toMD5(String passwd) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(passwd.getBytes("UTF-8"));
			return bytesToHexs(md.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		throw new RuntimeException();
	}

	/**
	 * 将16进制字符串转换成数组
	 *
	 * @return byte[]
	 * @author jacob
	 */
	public static byte[] hexsToBytes(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] hexChars = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte)(HEX_NUMS_STR.indexOf(hexChars[pos]) << 4 | HEX_NUMS_STR.indexOf(hexChars[pos + 1]));
		}
		return result;
	}

	/**
	 * 将数组转换成16进制字符串
	 *
	 *@param salt 字节串
	 * @return String
	 * @author jacob
	 */
	public static String bytesToHexs(byte[] salt) {
		StringBuffer hexs = new StringBuffer();
		for (int i = 0; i < salt.length; i++) {
			String hex = Integer.toHexString(salt[i] & 0xFF);
			if(hex.length() == 1) {
				hex = '0' + hex;
			}
			hexs.append(hex.toUpperCase(Locale.US));
		}
		return hexs.toString();
	}
}