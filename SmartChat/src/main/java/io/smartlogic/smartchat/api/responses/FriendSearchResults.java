package io.smartlogic.smartchat.api.responses;

import java.util.ArrayList;
import java.util.List;

import io.smartlogic.smartchat.hypermedia.FriendSearch;

public class FriendSearchResults {
    List<FriendSearch.Friend> foundFriends = new ArrayList<FriendSearch.Friend>();
    List<FriendSearch.Friend> groupies = new ArrayList<FriendSearch.Friend>();

    public List<FriendSearch.Friend> getFoundFriends() {
        return foundFriends;
    }

    public void setFoundFriends(List<FriendSearch.Friend> foundFriends) {
        this.foundFriends = foundFriends;
    }

    public List<FriendSearch.Friend> getGroupies() {
        return groupies;
    }

    public void setGroupies(List<FriendSearch.Friend> groupies) {
        this.groupies = groupies;
    }
}
