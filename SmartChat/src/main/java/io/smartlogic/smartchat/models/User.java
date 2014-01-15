package io.smartlogic.smartchat.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

@JsonRootName("user")
public class User {
    @JsonProperty("username")
    private String username;
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;
    @JsonProperty("phone")
    private String phoneNumber;
    @JsonProperty("private_key")
    private String privateKey;

    public static String hashPasswordForPrivateKey(User user) {
        String hashedPassword = user.getPassword();
        for (int i = 0; i < 1000; i++) {
            hashedPassword = new String(Hex.encodeHex(DigestUtils.sha256(hashedPassword)));
        }

        return hashedPassword;
    }

    @Override
    public String toString() {
        return "{User email: " + getEmail() +
                " password: " + getPassword() +
                " phoneNumber: " + getPhoneNumber() + "}";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
