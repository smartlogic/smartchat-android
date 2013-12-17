package io.smartlogic.smartchat.api;

import android.util.Base64;
import android.util.Log;

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.VariableExpansionException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartlogic.smartchat.hypermedia.FriendSearch;
import io.smartlogic.smartchat.hypermedia.HalFriends;
import io.smartlogic.smartchat.hypermedia.HalRoot;
import io.smartlogic.smartchat.models.Device;
import io.smartlogic.smartchat.models.Friend;
import io.smartlogic.smartchat.models.Media;
import io.smartlogic.smartchat.models.User;

public class ApiClient {
    public static final String rootUrl = "http://192.168.1.254:3000/";
    private String email;
    private String encodedPrivateKey;
    private PrivateKey privateKey;
    private HttpClient client;

    public ApiClient() {

    }

    public ApiClient(String email, String privateKey) {
        this.email = email;
        this.encodedPrivateKey = privateKey;
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

        client = new DefaultHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        try {
            HttpGet rootRequest = new HttpGet(rootUrl);
            HalRoot root = (HalRoot) executeAndParseJson(rootRequest, mapper, HalRoot.class);

            HttpGet friendsRequest = new HttpGet(root.getFriendsLink());
            HalFriends friends = (HalFriends) executeAndParseJson(friendsRequest, mapper, HalFriends.class);

            Map<String, Object[]> map = new HashMap<String, Object[]>();
            map.put("phone_numbers", scrubbedPhoneNumbers.keySet().toArray());
            String requestJson = mapper.writeValueAsString(map);

            String searchUrl = UriTemplate.fromTemplate(friends.getSearchLink()).expand();
            HttpPost searchRequest = new HttpPost(searchUrl);
            searchRequest.addHeader("Content-Type", "application/json");
            searchRequest.setEntity(new StringEntity(requestJson));
            FriendSearch friendSearch = (FriendSearch) executeAndParseJson(searchRequest, mapper, FriendSearch.class);

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

    public void addFriend(String addFriendUrl) {
        loadPrivateKey();

        client = new DefaultHttpClient();

        HttpPost addFriend = new HttpPost(addFriendUrl);
        signRequest(addFriend);

        try {
            client.execute(addFriend);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerDevice(String deviceToken) {
        loadPrivateKey();

        client = new DefaultHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        try {
            HttpGet rootRequest = new HttpGet(rootUrl);
            HalRoot root = (HalRoot) executeAndParseJson(rootRequest, mapper, HalRoot.class);

            Device device = new Device();
            device.setDeviceId(deviceToken);
            String requestJson = mapper.writeValueAsString(device);

            HttpPost searchRequest = new HttpPost(root.getDevicesLink());
            searchRequest.addHeader("Content-Type", "application/json");
            searchRequest.setEntity(new StringEntity(requestJson));

            signRequest(searchRequest);
            HttpResponse response = client.execute(searchRequest);

            if (response.getStatusLine().getStatusCode() != 201) {
                Log.d("smartchat", "error registering device");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Friend> getFriends() {
        loadPrivateKey();

        client = new DefaultHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        HttpGet rootRequest = new HttpGet(rootUrl);
        HalRoot root = (HalRoot) executeAndParseJson(rootRequest, mapper, HalRoot.class);

        HttpGet friendRequest = new HttpGet(root.getFriendsLink());
        HalFriends friends = (HalFriends) executeAndParseJson(friendRequest, mapper, HalFriends.class);

        Log.d("smarthcat", String.valueOf(friends.getFriends().size()));

        return friends.getFriends();
    }

    public void uploadMedia(List<Integer> friendIds, String photoPath) {
        loadPrivateKey();

        client = new DefaultHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        Media media = new Media();

        try {
            File photo = new File(photoPath);
            byte[] encoded = FileUtils.readFileToByteArray(photo);
            String photoBase64 = Base64.encodeToString(encoded, Base64.NO_WRAP);
            media.setFile(photoBase64);
            media.setFileName(photo.getName());
            media.setFriendIds(friendIds);

            String requestJson = mapper.writeValueAsString(media);

            HttpGet rootRequest = new HttpGet(rootUrl);
            HalRoot root = (HalRoot) executeAndParseJson(rootRequest, mapper, HalRoot.class);

            HttpPost mediaRequest = new HttpPost(root.getMediaLink());
            mediaRequest.addHeader("Content-Type", "application/json");
            mediaRequest.setEntity(new StringEntity(requestJson));

            signRequest(mediaRequest);
            HttpResponse response = client.execute(mediaRequest);

            if (response.getStatusLine().getStatusCode() != 201) {
                Log.d("smartchat", "error uploading media");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPrivateKey() {
        if (privateKey != null) {
            return;
        }

        this.privateKey = loadPrivateKeyFromString(encodedPrivateKey);
    }

    private Object executeAndParseJson(HttpUriRequest request, ObjectMapper mapper, Class klass) {
        try {
            signRequest(request);
            HttpResponse response = client.execute(request);
            String responseJson = EntityUtils.toString(response.getEntity());
            Log.d("smartchat", responseJson);
            return mapper.readValue(responseJson, klass);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void signRequest(HttpUriRequest request) {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(email, signUrl(privateKey, request.getURI().toString()));
        Header basicAuthHeader = BasicScheme.authenticate(credentials, "US-ASCII", false);
        request.addHeader(basicAuthHeader);
    }

    /**
     * Removes all non-digits and MD5 hashes phone numbers
     *
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
