package io.smartlogic.smartchat.hypermedia;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Link {
    @JsonProperty("href")
    public String href;

    @JsonProperty("templated")
    public boolean templated;

    @JsonProperty("name")
    public String name;
}