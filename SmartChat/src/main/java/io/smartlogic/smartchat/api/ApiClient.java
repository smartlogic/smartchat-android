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
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartlogic.smartchat.hypermedia.FriendSearch;
import io.smartlogic.smartchat.hypermedia.HalErrors;
import io.smartlogic.smartchat.hypermedia.HalFriends;
import io.smartlogic.smartchat.hypermedia.HalNotifications;
import io.smartlogic.smartchat.hypermedia.HalRoot;
import io.smartlogic.smartchat.models.Device;
import io.smartlogic.smartchat.models.Friend;
import io.smartlogic.smartchat.models.Media;
import io.smartlogic.smartchat.models.User;

public class ApiClient {
    public static final String TAG = "ApiClient";
    public static final String rootUrl = "http://192.168.1.254:5000/";
    private String username;
    private String encodedPrivateKey;
    private PrivateKey privateKey;
    private HttpClient client;

    public ApiClient() {

    }

    public ApiClient(String username, String privateKey) {
        this.username = username;
        this.encodedPrivateKey = privateKey;
    }

    /**
     * Register a new user
     *
     * @param user User to register.
     * @return Private key in base 64 format
     */
    public String registerUser(User user) throws RegistrationException {
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

            if (response.getStatusLine().getStatusCode() == 422) {
                HalErrors errors = mapper.readValue(responseJson, HalErrors.class);
                throw new RegistrationException(errors.getErrors());
            }

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

    public User login(String username, String password) throws AuthenticationException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet rootRequest = new HttpGet(rootUrl);
            HttpResponse response = client.execute(rootRequest);

            Log.d("smartchat", String.valueOf(response.getStatusLine().getStatusCode()));

            String responseJson = EntityUtils.toString(response.getEntity());
            HalRoot root = mapper.readValue(responseJson, HalRoot.class);

            HttpPost userPost = new HttpPost(root.getUserSignIn());

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            Header basicAuthHeader = BasicScheme.authenticate(credentials, "US-ASCII", false);
            userPost.addHeader(basicAuthHeader);

            response = client.execute(userPost);
            checkStatusCode(response);

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
            user.setPrivateKey(base64PrivateKey);

            return user;
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

    public List<FriendSearch.Friend> searchForFriends(Map<String, Integer> phoneNumbers, Map<String, Integer> emails) throws AuthenticationException {
        Map<String, Integer> scrubbedPhoneNumbers = scrubPhoneNumbers(phoneNumbers);
        Map<String, Integer> scrubbedEmails = scrubEmails(emails);

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
            map.put("emails", scrubbedEmails.keySet().toArray());
            String requestJson = mapper.writeValueAsString(map);

            String searchUrl = UriTemplate.fromTemplate(friends.getSearchLink()).expand();
            HttpPost searchRequest = new HttpPost(searchUrl);
            searchRequest.addHeader("Content-Type", "application/json");
            searchRequest.setEntity(new StringEntity(requestJson));
            FriendSearch friendSearch = (FriendSearch) executeAndParseJson(searchRequest, mapper, FriendSearch.class);

            for (FriendSearch.Friend friend : friendSearch.getFriends()) {
                if (friend.phoneNumber != null) {
                    friend.contactId = scrubbedPhoneNumbers.get(friend.phoneNumber);
                }
                if (friend.email != null) {
                    friend.contactId = scrubbedEmails.get(friend.email);
                }
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

    public void addFriend(String addFriendUrl) throws AuthenticationException {
        loadPrivateKey();

        client = new DefaultHttpClient();

        HttpPost addFriend = new HttpPost(addFriendUrl);
        signRequest(addFriend);

        try {
            HttpResponse response = client.execute(addFriend);
            checkStatusCode(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerDevice(String deviceToken) throws AuthenticationException {
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
            checkStatusCode(response);

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

    public List<Friend> getFriends() throws AuthenticationException {
        loadPrivateKey();

        client = new DefaultHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        HttpGet rootRequest = new HttpGet(rootUrl);
        HalRoot root = (HalRoot) executeAndParseJson(rootRequest, mapper, HalRoot.class);

        HttpGet friendRequest = new HttpGet(root.getFriendsLink());
        HalFriends friends = (HalFriends) executeAndParseJson(friendRequest, mapper, HalFriends.class);

        return friends.getFriends();
    }

    public List<HalNotifications.Notification> getNotifications() throws AuthenticationException {
        loadPrivateKey();

        client = new DefaultHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        HttpGet rootRequest = new HttpGet(rootUrl);
        HalRoot root = (HalRoot) executeAndParseJson(rootRequest, mapper, HalRoot.class);

        HttpGet mediaRequest = new HttpGet(root.getMediaLink());
        HalNotifications notifications = (HalNotifications) executeAndParseJson(mediaRequest, mapper, HalNotifications.class);

        return notifications.getNotifications();
    }

    public void uploadMedia(List<Integer> friendIds, String photoPath, String drawingPath, int expireIn) throws AuthenticationException {
        loadPrivateKey();

        client = new DefaultHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        Media media = new Media();

        try {
            File photo = new File(photoPath);
            byte[] photoEncoded = FileUtils.readFileToByteArray(photo);
            String photoBase64 = Base64.encodeToString(photoEncoded, Base64.NO_WRAP);

            media.setFile(photoBase64);
            media.setFileName(photo.getName());

            if (drawingPath != null && !drawingPath.equals("")) {
                File drawing = new File(drawingPath);
                byte[] drawingEncoded = FileUtils.readFileToByteArray(drawing);
                String drawingBase64 = Base64.encodeToString(drawingEncoded, Base64.NO_WRAP);

                media.setDrawing(drawingBase64);
            }

            media.setFriendIds(friendIds);
            media.setExpireIn(expireIn);

            String requestJson = mapper.writeValueAsString(media);

            HttpGet rootRequest = new HttpGet(rootUrl);
            HalRoot root = (HalRoot) executeAndParseJson(rootRequest, mapper, HalRoot.class);

            HttpPost mediaRequest = new HttpPost(root.getMediaLink());
            mediaRequest.addHeader("Content-Type", "application/json");
            mediaRequest.setEntity(new StringEntity(requestJson));

            signRequest(mediaRequest);
            HttpResponse response = client.execute(mediaRequest);
            checkStatusCode(response);

            if (response.getStatusLine().getStatusCode() != 202) {
                Log.d("smartchat", "error uploading media");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void inviteUser(String email, String message) throws AuthenticationException {
        loadPrivateKey();

        client = new DefaultHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            HttpGet rootRequest = new HttpGet(rootUrl);
            HalRoot root = (HalRoot) executeAndParseJson(rootRequest, mapper, HalRoot.class);

            HttpPost inviteRequest = new HttpPost(root.getInvitationsLink());
            signRequest(inviteRequest);

            Map<String, String> invitation = new HashMap<String, String>();
            invitation.put("email", email);
            invitation.put("message", message);

            String requestJson = mapper.writeValueAsString(invitation);

            inviteRequest.addHeader("Content-Type", "application/json");
            inviteRequest.setEntity(new StringEntity(requestJson));

            HttpResponse response = client.execute(inviteRequest);
            checkStatusCode(response);

            if (response.getStatusLine().getStatusCode() != 204) {
                Log.d("smartchat api client", "error inviting user");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPrivateKey() {
        if (privateKey != null) {
            return;
        }

        this.privateKey = RSAEncryption.loadPrivateKeyFromString(encodedPrivateKey);
    }

    private Object executeAndParseJson(HttpUriRequest request, ObjectMapper mapper, Class klass) throws AuthenticationException {
        try {
            signRequest(request);
            HttpResponse response = client.execute(request);
            checkStatusCode(response);

            String responseJson = EntityUtils.toString(response.getEntity());
            Log.d("response json", responseJson);
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
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, signUrl(privateKey, request.getURI().toString()));
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

    /**
     * MD5 hashes emails
     *
     * @param emails List of emails
     * @return Scrubbed emails
     */
    private Map<String, Integer> scrubEmails(Map<String, Integer> emails) {
        Map<String, Integer> scrubbedEmails = new HashMap<String, Integer>();

        for (String email : emails.keySet()) {
            String scrubbedEmail = new String(Hex.encodeHex(DigestUtils.md5(email)));
            scrubbedEmails.put(scrubbedEmail, emails.get(email));
        }

        return scrubbedEmails;
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

    private void checkStatusCode(HttpResponse response) throws AuthenticationException {
        switch (response.getStatusLine().getStatusCode()) {
            case 401:
                Log.e(TAG, "Authentication error");
                throw new AuthenticationException();
        }
    }
}
