package io.smartlogic.smartchat.api.responses;

public class SMSVerificationResponse {
    private String smsVerificationCode;

    private String phoneNumber;

    public SMSVerificationResponse(String smsVerificationCode, String phoneNumber) {
        this.smsVerificationCode = smsVerificationCode;
        this.phoneNumber = phoneNumber;
    }

    public String getSmsVerificationCode() {
        return smsVerificationCode;
    }

    public void setSmsVerificationCode(String smsVerificationCode) {
        this.smsVerificationCode = smsVerificationCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
