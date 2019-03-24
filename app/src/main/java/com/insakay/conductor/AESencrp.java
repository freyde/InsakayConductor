package com.insakay.conductor;

import android.util.Base64;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;


public class AESencrp {


    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{ 'J', 'e', 'f', 'f', 'r', 'e', 'y', 'E', 'd', 'c', 'e', 'l', '2', '0', '1', '8' };

//    public static String encrypt(String Data) throws Exception {
//        Key key = generateKey();
//        Cipher c = Cipher.getInstance(ALGO);
//        c.init(Cipher.ENCRYPT_MODE, key);
//        byte[] encVal = c.doFinal(Data.getBytes());
//        String encryptedValue = new String(encVal);
//        return encryptedValue;
//    }

    public static String decrypt(String raw) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] encryptedData = Base64.decode(raw, Base64.DEFAULT);
        byte[] decValue = c.doFinal(encryptedData);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

}
