package io.smartlogic.smartchat.hypermedia;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HalSmsVerification {
    @JsonProperty("verification_code")
    private String verificationCode;

    @JsonProperty("verification_phone_number")
    private String verificationPhoneNumber;

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getVerificationPhoneNumber() {
        return verificationPhoneNumber;
    }
}
