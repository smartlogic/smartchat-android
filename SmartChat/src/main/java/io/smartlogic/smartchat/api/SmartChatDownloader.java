package io.smartlogic.smartchat.api;

import android.content.Context;
import android.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SmartChatDownloader {
    private Context context;
    private String encodedPrivateKey;
    private String encryptedAesKey;
    private String encryptedAesIv;
    private String s3Url;

    public SmartChatDownloader(Context context, String encodedPrivateKey, String encryptedAesKey, String encryptedAesIv, String s3Url) {
        this.context = context;
        this.encodedPrivateKey = encodedPrivateKey;
        this.encryptedAesKey = encryptedAesKey;
        this.encryptedAesIv = encryptedAesIv;
        this.s3Url = s3Url;
    }

    public File download() {
        PrivateKey privateKey = RSAEncryption.loadPrivateKeyFromString(encodedPrivateKey);
        byte[] encryptedAesKey = Base64.decode(this.encryptedAesKey, Base64.DEFAULT);
        byte[] encryptedAesIv = Base64.decode(this.encryptedAesIv, Base64.DEFAULT);

        try {
            // ECB is there for java's sake, it doesn't actually get used.
            Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsa.init(Cipher.DECRYPT_MODE, privateKey);
            SecretKey aesKey = new SecretKeySpec(rsa.doFinal(encryptedAesKey), "AES");
            IvParameterSpec ips = new IvParameterSpec(rsa.doFinal(encryptedAesIv));

            Cipher aes = Cipher.getInstance("AES/CBC/NoPadding");
            aes.init(Cipher.DECRYPT_MODE, aesKey, ips);

            HttpClient client = new DefaultHttpClient();
            HttpGet s3File = new HttpGet(s3Url);

            HttpResponse response = client.execute(s3File);

            byte[] data = EntityUtils.toByteArray(response.getEntity());

            byte[] decrypted_data = aes.doFinal(data);

            File pictureFile = File.createTempFile("smartchat", ".jpg", context.getExternalCacheDir());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pictureFile));
            bos.write(decrypted_data);
            bos.flush();
            bos.close();

            return pictureFile;
        } catch (IOException e) {
            e.printStackTrace();
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

        return null;
    }
}
