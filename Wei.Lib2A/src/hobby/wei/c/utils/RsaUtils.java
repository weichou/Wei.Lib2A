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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.util.Base64;

/**
 * @author 周伟 Wei Chou(weichou2010@gmail.com)
 */
public class RsaUtils {
	private static final String RSA_ARITHEMTIC_NAME		= "RSA";
	private static final String RSA_TRANSFORMATION		= "RSA/ECB/PKCS1Padding";

	/**
	 * 加密。可以用公钥方式加密，也可以用私钥方式加密。
	 * 
	 * @param isByPublic 是公钥还是私钥方式加密
	 * @param base64Key 经过Base64编码的密钥字符串
	 * @param plaintext 明文
	 * @param charset 明文的编码方式
	 * @return 加密之后的字符串(密文)
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static String encrypt(boolean isByPublic, String base64Key, String plaintext, String charset)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException,
			IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, isByPublic ? getPublicKey(RSA_ARITHEMTIC_NAME, base64Key) : getPrivateKey(RSA_ARITHEMTIC_NAME, base64Key));
		byte[] output = cipher.doFinal(plaintext.getBytes(charset));
		return Base64.encodeToString(output, Base64.NO_WRAP);	//asc编码，不需要charset; NO_WRAP不要换行
	}

	/**
	 * 解密。可以用公钥方式解密，也可以用私钥方式解密。
	 * 
	 * @param isByPublic 是公钥还是私钥方式解密
	 * @param base64Key 经过Base64编码的密钥字符串
	 * @param ciphertext 密文
	 * @param charset 明文的编码方式
	 * @return 解密之后的字符串(明文)
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static String decrypt(boolean isByPublic, String base64Key, String ciphertext, String charset)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException,
			IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, isByPublic ? getPublicKey(RSA_ARITHEMTIC_NAME, base64Key) : getPrivateKey(RSA_ARITHEMTIC_NAME, base64Key));
		byte[] output = cipher.doFinal(Base64.decode(ciphertext, Base64.DEFAULT));
		return new String(output, charset);
	}

	/**
	 * 获取公钥的PublicKey对象
	 * 
	 * @param arithmetic 算法
	 * @param base64EncodedKey 经过Base64编码的公钥字符串
	 * @return 公钥的PublicKey对象
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private static PublicKey getPublicKey(String arithmetic, String base64EncodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = Base64.decode(base64EncodedKey, Base64.DEFAULT);
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(arithmetic);
		return keyFactory.generatePublic(x509);
	}

	/**
	 * 获取私钥的PrivateKey对象
	 * 
	 * @param arithmetic 算法
	 * @param base64EncodedKey 经过Base64编码的私钥字符串
	 * @return 私钥的PrivateKey对象
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private static PrivateKey getPrivateKey(String arithmetic, String base64EncodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = Base64.decode(base64EncodedKey, Base64.DEFAULT);
		PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(arithmetic);
		return keyFactory.generatePrivate(pkcs8);
	}

	/**
	 * 生成一对经过Base64编码的密钥。使用Base64.DEFAULT而不是Base64.NO_WRAP，前者会有换行，后者没有换行，
	 * 不过php端只支持有换行的，经过测试，换行的，在java端，把换行符去掉就跟后者一样了，可以正常使用。
	 * 
	 * @param size 生成的密钥对的字节长度，该参数应>=512。但实际返回的是对字节做了Base64编码的字符串，不能反应字节的长度。
	 * @return 返回含有两个元素的数组，每个元素都是对密钥字节做了Base64编码生成的字符串。第0个元素是公钥，第1个元素是私钥。
	 **/
	public static String[] makeKeyPair(int size) throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA_ARITHEMTIC_NAME);
		keyPairGen.initialize(size < 512 ? 512 : size);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		//需要privateKey.getFormat()返回值为"PKCS#8"才能于上面的getPrivateKey()匹配
		return new String[] {Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT),
				Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT)};
	}

	public static void makeKeyPairToFile(int size) {
		try {
			String[] keys = makeKeyPair(size);
			String publicKey = keys[0];
			String privateKey = keys[1];
			keyToPemFile("rsa_public_key.pem", publicKey, "utf-8");
			keyToPemFile("rsa_private_key.pem", privateKey, "utf-8");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static void keyToPemFile(String filePathName, String key, String charset) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePathName);
			String seperator = key.substring(key.length() - 1);
			out.write(("-----BEGIN PUBLIC KEY-----" + seperator).getBytes(charset));
			out.write(key.getBytes(charset));
			out.write(("-----END PUBLIC KEY-----" + seperator).getBytes(charset));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) try { out.close(); } catch (IOException e) {}
		}
	}
}
