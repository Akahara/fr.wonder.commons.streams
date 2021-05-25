package fr.wonder.commons.streams.encryption;

import java.io.IOException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {
	
	private final SecretKey secretKey;
	
	private final Cipher encryptionCipher;
	private final Cipher decryptionCipher;
	
	public AESEncryption(int keyLength) {
		try {
			KeyGenerator gen = KeyGenerator.getInstance("AES");
			gen.init(keyLength);
			this.secretKey = gen.generateKey();
			this.encryptionCipher = Cipher.getInstance("AES");
			this.encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			this.decryptionCipher = Cipher.getInstance("AES");
			this.decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to generate a key", e);
		}
	}
	
	public AESEncryption(byte[] key) {
		try {
			this.secretKey = new SecretKeySpec(key, "AES");
			this.encryptionCipher = Cipher.getInstance("AES");
			this.encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			this.decryptionCipher = Cipher.getInstance("AES");
			this.decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to generate a key", e);
		}
	}
	
	public byte[] getKey() {
		return secretKey.getEncoded();
	}
	
	public byte[] encrypt(byte[] bytes) throws IOException {
		try {
			return encryptionCipher.doFinal(bytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException("Could not encrypt data", e);
		}
	}
	
	public byte[] decrypt(byte[] bytes) throws IOException {
		try {
			return decryptionCipher.doFinal(bytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException("Could not decrypt data", e);
		}
	}
	
}
