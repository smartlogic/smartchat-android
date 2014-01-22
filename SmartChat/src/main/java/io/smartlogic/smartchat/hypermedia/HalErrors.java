package io.smartlogic.smartchat.hypermedia;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class HalErrors {
    @JsonProperty("_embedded")
    protected Embedded embedded;

    public Map<String, List<String>> getErrors() {
        return embedded.errors;
    }

    public class Embedded {
        @JsonProperty("errors")
        Map<String, List<String>> errors;
    }
}
