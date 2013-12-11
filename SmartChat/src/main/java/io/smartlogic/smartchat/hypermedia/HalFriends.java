package io.smartlogic.smartchat.hypermedia;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HalFriends {

    @JsonProperty("_links")
    protected FriendLinks links;

    public String getSearchLink() {
        return links.search.href;
    }

    public class FriendLinks {
        @JsonProperty("self")
        Link self;

        @JsonProperty("search")
        Link search;
    }
}
