package sir.barchable.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;


public class SecurePreferences {

    public static class SecurePreferencesException extends RuntimeException {

        public SecurePreferencesException(Throwable e) {
            super(e);
        }

    }

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY_HASH_TRANSFORMATION = "SHA-256";
    private static final String CHARSET = "UTF-8";

    private MessageDigest sha256MessageDigest;

    private String deviceId = "d55a32b78cab1616";

    public SecurePreferences() {
        try {
            sha256MessageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public SecretKeySpec makeKeyCipher(String key) {

        byte[] hashedKey = sha256MessageDigest.digest(key.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKeySpec = new SecretKeySpec(hashedKey, "AES");

        return secretKeySpec;
    }

    public String decryptKey(String key) {

        SecretKeySpec keySpec = makeKeyCipher(deviceId);
        byte[] bytes = Base64.getDecoder().decode(key);
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(bytes);
            return new String(decrypted);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public String decryptValue(String value) {
        SecretKeySpec keySpec = makeKeyCipher(deviceId);

        byte[] bytes = Base64.getDecoder().decode(value);
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec("fldsjfodasjifuds".getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(bytes);
            return new String(decrypted);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public HashMap<String, String> readMap(String path) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        Document document = builder.parse(fileInputStream);
        NodeList nodeList = document.getElementsByTagName("string");

        int length = nodeList.getLength();

        HashMap<String, String> map = new LinkedHashMap<>();
        for(int i=0; i<length; i++) {
            Node node = nodeList.item(i);
            String key = node.getAttributes().getNamedItem("name").getTextContent();

            map.put(decryptKey(key), decryptValue(node.getTextContent()));
        }
        return map;
    }



    public void writeMap(HashMap<String, String> map, String outFile) throws Exception {

        Set<String> keys  = map.keySet();
        for(String key : keys) {
            System.out.println(encryptKey(key) + " : " + encryptValue(map.get(key)));
        }

    }

    public String encryptValue(String value) {
        SecretKeySpec keySpec = makeKeyCipher(deviceId);

        byte[] bytes = value.getBytes();
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec("fldsjfodasjifuds".getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(bytes);
            return Base64.getEncoder().encodeToString(encrypted) ;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public String encryptKey(String key) {
        SecretKeySpec keySpec = makeKeyCipher(deviceId);
        byte[] bytes = key.getBytes();
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(bytes);
            return Base64.getEncoder().encodeToString(encrypted) ;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public static void main(String[] args) throws Exception {

        HashMap<String, String> map = new SecurePreferences().readMap("/Users/sankala/Projects/CoC/Preferences/storage.xml");

        System.out.println( map );
        int highInt = Integer.parseInt(map.get("High_PROD2"));
        int lowInt = Integer.parseInt(map.get("Low_PROD2"));

        System.out.println( "High : " + highInt + "; Low : " + lowInt);

        long userId = highInt;
        userId = userId << 32;
        userId = userId + lowInt;
        System.out.println( "User id = " + userId);

        userId = 141734835518L;

        highInt = (int) (userId >> 32);
        lowInt = (int)( userId - ( (long)highInt << 32));

        System.out.println( "High : " + highInt + "; Low : " + lowInt);
        map.put("High_PROD2", ""+highInt);
        map.put("Low_PROD2", ""+lowInt);
        new SecurePreferences().writeMap(map, "/Users/sankala/Projects/CoC/Preferences/storage.xml.new");
    }
}