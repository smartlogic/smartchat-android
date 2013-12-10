package io.smartlogic.smartchat.api;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.security.KeyPair;

import io.smartlogic.smartchat.hypermedia.HalRoot;
import io.smartlogic.smartchat.models.User;

public class ApiClient {
    public static final String rootUrl = "http://192.168.2.236:3000/";

    public void registerUser(User user) {
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

            Log.d("smartchat", responseJson);

            User loadedUser = mapper.readValue(responseJson, User.class);

            Log.d("smartchat", loadedUser.getPrivateKey());

            ByteArrayInputStream tube = new ByteArrayInputStream(loadedUser.getPrivateKey().getBytes());
            Reader stringReader = new BufferedReader(new InputStreamReader(tube));
            PEMParser pemParser = new PEMParser(stringReader);
            Object object = pemParser.readObject();

            String hashedPassword = user.getPassword();
            for (int i = 0; i < 1000; i++) {
                hashedPassword = new String(Hex.encodeHex(DigestUtils.sha256(hashedPassword)));
            }

            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(hashedPassword.toCharArray());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            KeyPair keyPair = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));

            Log.d("smartchat", keyPair.getPrivate().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
