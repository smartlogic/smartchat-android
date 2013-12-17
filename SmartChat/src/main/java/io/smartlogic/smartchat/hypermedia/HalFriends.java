package io.smartlogic.smartchat.hypermedia;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import io.smartlogic.smartchat.models.Friend;

public class HalFriends {
    @JsonProperty("_links")
    protected FriendLinks links;
    @JsonProperty("_embedded")
    protected Embedded embedded;

    public String getSearchLink() {
        return links.search.href;
    }

    public List<Friend> getFriends() {
        return embedded.friends;
    }

    public class Embedded {
        @JsonProperty("friends")
        List<Friend> friends;
    }

    public class FriendLinks {
        @JsonProperty("self")
        Link self;
        @JsonProperty("search")
        Link search;
    }
}
