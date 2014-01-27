package io.smartlogic.smartchat.hypermedia;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FriendSearch {
    @JsonProperty("_embedded")
    protected Embedded embedded;

    public List<Friend> getFriends() {
        return embedded.friends;
    }

    public static class Embedded {
        @JsonProperty("friends")
        List<Friend> friends;
    }

    public static class Friend {
        /**
         * Local contact ID
         */
        @JsonIgnore
        public Integer contactId;

        @JsonProperty("_links")
        protected FriendLinks links;

        @JsonProperty("username")
        public String username;

        @JsonProperty("email")
        public String email;

        @JsonProperty("phone_number")
        public String phoneNumber;

        public String getAddLink() {
            return links.addFriend.href;
        }
    }

    public static class FriendLinks {
        @JsonProperty("smartchat:add-friend")
        protected Link addFriend;
    }
}
