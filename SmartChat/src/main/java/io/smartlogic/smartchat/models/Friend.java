package io.smartlogic.smartchat.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Friend {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("email")
    private String email;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
