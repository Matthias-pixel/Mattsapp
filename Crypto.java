import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import java.util.Arrays;

public abstract class Crypto {
    public static String hashPassword(String username, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(String.format("ULgf&E$!q2W$%sftuLfztl%strFTGZHI8", password, username).getBytes(StandardCharsets.UTF_8));
            return Util.encode16(hash);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static KeyPair generateRsaKeys() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch(Exception e) {
            e.printStackTrace();
        }
        generator.initialize(2048);
        return generator.generateKeyPair();
    }
    public static String encryptRsa(String input, PublicKey publicKey) {
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = encryptCipher.doFinal(inputBytes);
            String encodedMessage = Util.encode64(encryptedBytes);
            return encodedMessage;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String decryptRsa(String input, PrivateKey privateKey) {
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] inputBytes = Util.decode64(input);
            byte[] decryptedBytes = decryptCipher.doFinal(inputBytes);
            return new String(decryptedBytes, "UTF8");
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String encodeKey(Key key) {
        return Util.encode64(key.getEncoded());
    }
    public static PublicKey decodePublicKey(String input) {
        try{
            PublicKey publicKey = null;
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Util.decode64(input));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static PrivateKey decodePrivateKey(String input) {
        try{
            PrivateKey privateKey = null;
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Util.decode64(input));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
            return privateKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void printKeyPair(KeyPair keyPair) {
        System.out.println("Public Key: " + encodeKey(keyPair.getPublic()));
        System.out.println("Private Key: " + encodeKey(keyPair.getPrivate()));
    }
    
    
    public static SecretKey generateAesKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch(Exception e) {
            e.printStackTrace();
        }
        keyGenerator.init(192);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }
    public static IvParameterSpec generateIVector() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
    public static String encryptAes(String input, SecretKey key, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return Util.encode64(cipherText);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String decryptAes(String cipherText, SecretKey key, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plainText = cipher.doFinal(Util.decode64(cipherText));
            return new String(plainText);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String encodeAesBundle(String key, String iv) {
        byte[] keyBytes = Util.decode64(key);
        byte[] ivBytes = Util.decode64(iv);
        byte[] res = new byte[keyBytes.length + ivBytes.length];
        for(int i = 0; i < keyBytes.length; i++) {
            res[i] = keyBytes[i];
        }
        for(int i = 0; i < ivBytes.length; i++) {
            res[keyBytes.length+i] = ivBytes[i];
        }
        return Util.encode64(res);
    }
    public static String decodeAesBundleKey(String aesBundle) {
        byte[] decodedAesBundle = Util.decode64(aesBundle);
        byte[] encodedKey = Arrays.copyOfRange(decodedAesBundle, 0, 24);
        return Util.encode64(encodedKey);
    }
    public static String decodeAesBundleIv(String aesBundle) {
        byte[] decodedAesBundle = Util.decode64(aesBundle);
        byte[] encodedIv = Arrays.copyOfRange(decodedAesBundle, 24, decodedAesBundle.length);
        return Util.encode64(encodedIv);
    }
}
