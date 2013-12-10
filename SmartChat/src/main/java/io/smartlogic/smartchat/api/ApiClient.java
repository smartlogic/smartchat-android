package io.smartlogic.smartchat.api;

import android.util.Base64;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import io.smartlogic.smartchat.hypermedia.HalRoot;
import io.smartlogic.smartchat.models.User;

public class ApiClient {
    public static final String rootUrl = "http://192.168.2.236:3000/";

    /**
     * Register a new user
     *
     * @param user User to register.
     * @return Private key in base 64 format
     */
    public String registerUser(User user) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet rootRequest = new HttpGet(rootUrl);
            HttpResponse response = client.execute(rootRequest);

            String responseJson = EntityUtils.toString(response.getEntity());

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

            HalRoot root = mapper.readValue(responseJson, HalRoot.class);

            String usersUrl = root.getUsersLink();
            String requestJson = mapper.writeValueAsString(user);

            HttpPost userPost = new HttpPost(usersUrl);
            userPost.addHeader("Content-Type", "application/json");
            userPost.setEntity(new StringEntity(requestJson));

            response = client.execute(userPost);

            responseJson = EntityUtils.toString(response.getEntity());
            User loadedUser = mapper.readValue(responseJson, User.class);

            ByteArrayInputStream tube = new ByteArrayInputStream(loadedUser.getPrivateKey().getBytes());
            Reader stringReader = new BufferedReader(new InputStreamReader(tube));
            PEMParser pemParser = new PEMParser(stringReader);
            Object object = pemParser.readObject();

            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().
                    build(User.hashPasswordForPrivateKey(user).toCharArray());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            KeyPair keyPair = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));

            String base64PrivateKey = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.NO_WRAP);

            return base64PrivateKey;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private PrivateKey loadPrivateKeyFromString(String base64EncodedPrivateKey) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(base64EncodedPrivateKey, Base64.NO_WRAP));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String signUrl(PrivateKey privateKey, String url) {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(url.getBytes());
            return Base64.encodeToString(signer.sign(), Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return "";
    }
}
