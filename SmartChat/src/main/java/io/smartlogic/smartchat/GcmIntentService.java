package io.smartlogic.smartchat;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

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

import io.smartlogic.smartchat.api.RSAEncryption;

public class GcmIntentService extends IntentService {
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String encodedPrivateKey = prefs.getString(Constants.EXTRA_PRIVATE_KEY, "");

        PrivateKey privateKey = RSAEncryption.loadPrivateKeyFromString(encodedPrivateKey);

        for (String key : extras.keySet()) {
            Log.d("smartchat", key);
        }

        Log.d("smartchat", extras.getString(Constants.EXTRA_S3_FILE_URL));

        byte[] encryptedAesKey = Base64.decode(extras.getString(Constants.EXTRA_ENCRYPTED_AES_KEY), Base64.DEFAULT);
        byte[] encryptedAesIv = Base64.decode(extras.getString(Constants.EXTRA_ENCRYPTED_AES_IV), Base64.DEFAULT);

        try {
            // ECB is there for java's sake, it doesn't actually get used.
            Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsa.init(Cipher.DECRYPT_MODE, privateKey);
            SecretKey aesKey = new SecretKeySpec(rsa.doFinal(encryptedAesKey), "AES");
            IvParameterSpec ips = new IvParameterSpec(rsa.doFinal(encryptedAesIv));

            Cipher aes = Cipher.getInstance("AES/CBC/NoPadding");
            aes.init(Cipher.DECRYPT_MODE, aesKey, ips);

            HttpClient client = new DefaultHttpClient();
            HttpGet s3File = new HttpGet(extras.getString(Constants.EXTRA_S3_FILE_URL));

            HttpResponse response = client.execute(s3File);

            byte[] data = EntityUtils.toByteArray(response.getEntity());

            byte[] decrypted_data = aes.doFinal(data);

            File pictureFile = File.createTempFile("smartchat", ".jpg", getExternalCacheDir());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pictureFile));
            bos.write(decrypted_data);
            bos.flush();
            bos.close();
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
    }
}
