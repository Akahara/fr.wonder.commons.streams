package fr.wonder.commons.streams.encryption;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class RSAEncryption {
	
	private final PrivateKey privateKey;
	private final PublicKey publicKey;
	
	private final Cipher encryptionCipher;
	private final Cipher decryptionCipher;
	
	public static RSAEncryption getKeyPair(int keyLength) {
		return new RSAEncryption(keyLength);
	}
	
	public static RSAEncryption getKeyPair(byte[] privateKey) {
		return new RSAEncryption(privateKey, false);
	}
	
	public static RSAEncryption getEncriptionKey(byte[] publicKey) {
		return new RSAEncryption(publicKey, true);
	}
	
	private RSAEncryption(int keyLength) {
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(keyLength);
			KeyPair pair = gen.generateKeyPair();
			this.privateKey = pair.getPrivate();
			this.publicKey = pair.getPublic();
			this.encryptionCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			this.encryptionCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			this.decryptionCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			this.decryptionCipher.init(Cipher.DECRYPT_MODE, privateKey);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to generate a key pair", e);
		}
	}
	
	private RSAEncryption(byte[] key, boolean keyIsPublic) {
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			if(keyIsPublic) {
				this.publicKey = fact.generatePublic(keySpec);
				this.privateKey = null;
				this.decryptionCipher = null;
				this.encryptionCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				encryptionCipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
			} else {
				this.publicKey = null;
				this.privateKey = fact.generatePrivate(keySpec);
				this.decryptionCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				decryptionCipher.init(Cipher.DECRYPT_MODE, this.privateKey);
				this.encryptionCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				encryptionCipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to generate a public key", e);
		}
	}
	
	public byte[] getPublicKey() {
		return publicKey.getEncoded();
	}
	
	public byte[] getPrivateKey() {
		return privateKey.getEncoded();
	}
	
	public byte[] encrypt(byte[] bytes) throws IOException {
		try {
			return encryptionCipher.doFinal(bytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException("Could not encrypt data", e);
		}
	}
	
	public byte[] decrypt(byte[] bytes) throws IOException {
		if(decryptionCipher == null)
			throw new IllegalAccessError("This factory only does encryption");
		try {
			return decryptionCipher.doFinal(bytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException("Could not decrypt data", e);
		}
	}
	
}
