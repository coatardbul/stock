package com.coatardbul.stock.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RSAUtil {

	/**
	 * 算法
	 * 
	 */
	private static final String ALGORITHM = "RSA";

	/**
	 * Map公钥key值
	 * 
	 */
	private static final String PUBLICK_EY = "PUBLICK_KEY";

	/**
	 * Map私钥key值
	 * 
	 */
	private static final String PRIVATE_KEY = "PRIVATE_KEY";

	/**
	 * 加密算法
	 * 
	 */
	private static final String CIPHER_DE = "RSA";

	/**
	 * 解密算法
	 * 
	 */
	private static final String CIPHER_EN = "RSA";

	/**
	 * 密钥长度
	 * 
	 */
	private static final Integer KEY_LENGTH = 1024;

	/**
	 * RSA最大加密明文大小
	 * 
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * RSA最大解密密文大小
	 * 
	 */
	private static final int MAX_DECRYPT_BLOCK = KEY_LENGTH / 8;

	/**
	 * 生成秘钥对，公钥和私钥
	 *
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static Map<String, Object> genKeyPair() throws NoSuchAlgorithmException {
		Map<String, Object> keyMap = new HashMap<String, Object>();
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGenerator.initialize(KEY_LENGTH);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		keyMap.put(PUBLICK_EY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}
	
	/**
	 * 公钥加密（字符串）
	 * 
	 * @param data
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPublicKey(String data, String publicKey) throws Exception {
		// 得到公钥
		byte[] b = encryptByPublicKey(data.getBytes(), publicKey);
		return new String(Base64.encodeBase64(b));
	}

	/**
	 * 公钥加密（数组）
	 *
	 * @param data
	 * @param publicKey
	 * @return
	 * @throws InvalidKeySpecException
	 */
	public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {



		// 得到公钥
		byte[] keyBytes = Base64.decodeBase64(publicKey.getBytes());
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		Key key = keyFactory.generatePublic(x509EncodedKeySpec);
		// 加密数据，分段加密
		Cipher cipher = Cipher.getInstance(CIPHER_EN);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		int inputLength = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offset = 0;
		byte[] cache;
		int i = 0;
		while (inputLength - offset > 0) {
			if (inputLength - offset > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offset, inputLength - offset);
			}
			out.write(cache, 0, cache.length);
			i++;
			offset = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}
	
	/**
	 * 私钥解密（字符串）
	 *
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static String decryptByPrivateKey(String data, String privateKey) throws Exception {
		byte[] decryptStrByte = decryptByPrivateKey(Base64.decodeBase64(data.getBytes()), privateKey);
		return new String(decryptStrByte);
	}

	/**
	 * 私钥解密（数组）
	 *
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, String privateKey) throws Exception {
		// 得到私钥
		byte[] keyBytes = Base64.decodeBase64(privateKey.getBytes());
		PKCS8EncodedKeySpec pKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		Key key = keyFactory.generatePrivate(pKCS8EncodedKeySpec);
		// 解密数据，分段解密
		Cipher cipher = Cipher.getInstance(CIPHER_DE);
		cipher.init(Cipher.DECRYPT_MODE, key);
		int inputLength = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offset = 0;
		byte[] cache;
		int i = 0;
		while (inputLength - offset > 0) {
			if (inputLength - offset > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(data, offset, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offset, inputLength - offset);
			}
			out.write(cache);
			i++;
			offset = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}
	
	/**
	 * 获取公钥
	 *
	 * @param keyMap
	 * @return
	 */
	public static String getPublicKey(Map<String, Object> keyMap) {
		Key key = (Key) keyMap.get(PUBLICK_EY);
		String str = new String(Base64.encodeBase64(key.getEncoded()));
		return str;
	}

	/**
	 * 获取私钥
	 *
	 * @param keyMap
	 * @return
	 */
	public static String getPrivateKey(Map<String, Object> keyMap) {
		Key key = (Key) keyMap.get(PRIVATE_KEY);
		String str = new String(Base64.encodeBase64(key.getEncoded()));
		return str;
	}

	public static void main(String[] args) throws Exception {


		Map<String, Object> keyMap = genKeyPair();
		String publicKey = getPublicKey(keyMap);
		String privateKey = getPrivateKey(keyMap);
		String sourceStr="1234567890";
		String en = encryptByPublicKey(sourceStr, publicKey);
		String de = decryptByPrivateKey(en, privateKey);
		log.info("公钥：{}", publicKey);
		log.info("私钥：{}", privateKey);
		log.info("加密后：{}", en);
		log.info("解密后：{}", de);
//		String en = "cCPj4i1ug2Q5sAsy920rsg+Q4Y8OThSgLOUkUF5/uWaOgLdzcDNtA5CdbfJrOG/8CkqYYsKjrK3H59MRoJJisYPw2F0TFwaz/oAHArQc9n2kjOjC7atW5LDGTBrs6NNFV+qAfdQTADqAtb4FqdeuOSNv7V2gxJJN4zeMsB2TiRE=";
//		String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKexam8i0Ur/bc256ZP4VL7X022y6hW7uyVK8YFQAgvoc1LaYlbMgkgnUxf0nXd+QcXDlSmSsaqBxdVGMt1g3MHsFgyFHIgNrt29PUrd3IZ3s1K5XKnDpflVixuhwrVGbCRGbvqif9Fw2vkLgo+3ywlZK6gS2C10WCCVT7jyxGhpAgMBAAECgYB1H9elE/52kX1R6X4ZWaNnQDtuLT6OO3b1DykrFoS3aH3T6rAwy9pbJwxEeRfatBagWIDAbrMk0kspMUSpltPYgontHNnVjxhSucnD9038rCJrt8wGDR9lwQjn101xxgNR99CYaL/ra8OB6YL8jo0uYMA4hCwqN/4TBmK1s4UoYQJBAOuMdpPhlTTQnUTjAHEySZ227eA/GuI+u1SyobwhDLVSt2ziYfNin9Yvv6WQfjPA/aqCtpwlbrlY/ZZ6eJpiglUCQQC2QLndjMR3ol3DmBSG/AaAQwTxgEUg+RKI0pwws1vh/0M+t8guTR9IxoHXHUQZQ9XY7RmSgFSO0A08vuFNPKnFAkEAjPcp+8R0HACBLjnACP50aguQ/rQlbaLnT3/8JO+OgHF5TpOI1TR/vZoj0nzP2j80UDgb+wS35/qnAMhYpuQvZQJAMj4nvyb2ZpCxTqGXTxsdY7CWJUmaRRvE+0ksW9SmSOhJDno1ymXicPqggsADrP9hbbr3aMbOCMzcSVnJ3LQTjQJAXO4Vsmjf3kdcvMvvp0/aWv1sGye+XDOlCWpwFnCV3cGH37MoIrBMVv7G/bc9MFCqr18/pEatWRU/gY92IFJ1rw==";
//		String de = decryptByPrivateKey(en, privateKey);
		log.info("解密后：{}", de);

		//String de = "QQqq1111";
		//String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnsWpvItFK/23NuemT+FS+19NtsuoVu7slSvGBUAIL6HNS2mJWzIJIJ1MX9J13fkHFw5UpkrGqgcXVRjLdYNzB7BYMhRyIDa7dvT1K3dyGd7NSuVypw6X5VYsbocK1RmwkRm76on/RcNr5C4KPt8sJWSuoEtgtdFgglU+48sRoaQIDAQAB";
		//String en = encryptByPublicKey(de, publicKey);
		//log.info("加密后：{}",en);
	}
	
}