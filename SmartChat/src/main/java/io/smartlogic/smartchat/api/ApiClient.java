package io.smartlogic.smartchat.api;

import android.util.Base64;
import android.util.Log;

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.VariableExpansionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartlogic.smartchat.hypermedia.FriendSearch;
import io.smartlogic.smartchat.hypermedia.HalFriends;
import io.smartlogic.smartchat.hypermedia.HalRoot;
import io.smartlogic.smartchat.models.User;

public class ApiClient {
    public static final String rootUrl = "http://192.168.1.254:3000/";

    private String email;
    private String encodedPrivateKey;
    private PrivateKey privateKey;

    public ApiClient() {

    }

    public ApiClient(String email, String privateKey) {
        this.email = email;
        this.encodedPrivateKey = privateKey;
    }

    private void loadPrivateKey() {
        if (privateKey != null) {
            return;
        }

        this.privateKey = loadPrivateKeyFromString(encodedPrivateKey);
    }

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

    public List<FriendSearch.Friend> searchForFriends(Map<String, Integer> phoneNumbers) {
        Map<String, Integer> scrubbedPhoneNumbers = scrubPhoneNumbers(phoneNumbers);

        loadPrivateKey();

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            Map<String, Object[]> map = new HashMap<String, Object[]>();
            map.put("phone_numbers", scrubbedPhoneNumbers.keySet().toArray());

            String requestJson = mapper.writeValueAsString(map);

            Log.d("smartchat", requestJson);

            HttpClient client = new DefaultHttpClient();
            HttpGet rootRequest = new HttpGet(rootUrl);

            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(email, signUrl(privateKey, rootUrl));
            Header basicAuthHeader = BasicScheme.authenticate(creds, "US-ASCII", false);

            rootRequest.addHeader(basicAuthHeader);

            HttpResponse response = client.execute(rootRequest);

            String responseJson = EntityUtils.toString(response.getEntity());
            HalRoot root = mapper.readValue(responseJson, HalRoot.class);

            String friendsUrl = root.getFriendsLink();

            HttpGet friendsRequest = new HttpGet(friendsUrl);
            creds = new UsernamePasswordCredentials(email, signUrl(privateKey, friendsUrl));
            basicAuthHeader = BasicScheme.authenticate(creds, "US-ASCII", false);
            friendsRequest.addHeader(basicAuthHeader);

            response = client.execute(friendsRequest);
            responseJson = EntityUtils.toString(response.getEntity());

            HalFriends friends = mapper.readValue(responseJson, HalFriends.class);

            String searchUrl = UriTemplate.fromTemplate(friends.getSearchLink()).expand();

            HttpPost searchRequest = new HttpPost(searchUrl);
            creds = new UsernamePasswordCredentials(email, signUrl(privateKey, searchUrl));
            basicAuthHeader = BasicScheme.authenticate(creds, "US-ASCII", false);
            searchRequest.addHeader(basicAuthHeader);

            searchRequest.addHeader("Content-Type", "application/json");
            searchRequest.setEntity(new StringEntity(requestJson));

            response = client.execute(searchRequest);

            responseJson = EntityUtils.toString(response.getEntity());
            Log.d("smartchat", responseJson);

            FriendSearch friendSearch = mapper.readValue(responseJson, FriendSearch.class);

            for (FriendSearch.Friend friend : friendSearch.getFriends()) {
                friend.contactId = scrubbedPhoneNumbers.get(friend.phoneNumber);
            }

            return friendSearch.getFriends();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedUriTemplateException e) {
            e.printStackTrace();
        } catch (VariableExpansionException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Removes all non-digits and MD5 hashes phone numbers
     * @param phoneNumbers List of phone numbers
     * @return Scrubbed numbers
     */
    private Map<String, Integer> scrubPhoneNumbers(Map<String, Integer> phoneNumbers) {
        Map<String, Integer> scrubbedPhoneNumbers = new HashMap<String, Integer>();

        for (String phoneNumber : phoneNumbers.keySet()) {
            String scrubbedPhoneNumber = phoneNumber.replaceAll("[^\\d]", "");
            scrubbedPhoneNumber = new String(Hex.encodeHex(DigestUtils.md5(scrubbedPhoneNumber)));
            scrubbedPhoneNumbers.put(scrubbedPhoneNumber, phoneNumbers.get(phoneNumber));
        }

        return scrubbedPhoneNumbers;
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
