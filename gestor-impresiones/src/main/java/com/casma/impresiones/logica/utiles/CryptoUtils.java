package com.casma.impresiones.logica.utiles;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtils {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    private static String storedKey = "***DO_NOT_MODIFY_THIS***_·$%&!!3342yukh7384b6,3kd!!";
    private SecretKey secretKey;
    private Cipher ecipher;
    private Cipher dcipher;

    private static CryptoUtils instance;

    private CryptoUtils() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        byte[] encodedKey = Base64.decodeBase64(storedKey);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(storedKey.toCharArray(), encodedKey, 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        this.secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        this.ecipher = Cipher.getInstance("AES");
        this.dcipher = Cipher.getInstance("AES");
        this.ecipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
        this.dcipher.init(Cipher.DECRYPT_MODE, this.secretKey);
    }

    public synchronized static CryptoUtils getInstance() {
        if (instance == null) {
            try {
                instance = new CryptoUtils();
            } catch (Exception e) {
                logger.error("Could not initialize crypto utils", e);
            }
        }
        return instance;
    }

    public String encrypt(String plaintext) {
        try {
            return new BASE64Encoder().encode(ecipher.doFinal(plaintext.getBytes("UTF8")));
        } catch (Exception e) {
            return plaintext;
        }
    }

    public String decrypt(String ciphertext) {
        try {
            return new String(dcipher.doFinal(new BASE64Decoder().decodeBuffer(ciphertext)), "UTF8");
        } catch (Exception e) {
            return ciphertext;
        }
    }

    public static void main(String[] args) {
        System.out.println("TEST: " + getInstance().encrypt("sebasm17"));
    }
}