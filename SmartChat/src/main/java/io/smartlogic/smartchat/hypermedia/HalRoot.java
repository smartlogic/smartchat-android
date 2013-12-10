package io.smartlogic.smartchat.hypermedia;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HalRoot {
    @JsonProperty("_links")
    protected RootLinks links;

    public String getUsersLink() {
        return links.users.href;
    }

    public String getSelfLink() {
        return links.self.href;
    }

    public class RootLinks {
        @JsonProperty("self")
        Link self;

        @JsonProperty("smartchat:users")
        Link users;
    }
}