package io.smartlogic.smartchat.hypermedia;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class HalNotifications {
    @JsonProperty("_embedded")
    protected Embedded embedded;

    public List<Notification> getNotifications() {
        return embedded.notifications;
    }

    public static class Notification {
        @JsonProperty("_embedded")
        CreatorEmbedded embedded;

        @JsonProperty("_links")
        NotificationLinks links;

        @JsonProperty("uuid")
        String uuid;

        public int getCreatorId() {
            return embedded.creator.id;
        }

        public String getCreatorUsername() {
            return embedded.creator.username;
        }

        public String getFileUrl() {
            for (Link link : links.files) {
                if (link.name.equals("file")) {
                    return link.href;
                }
            }

            return "";
        }

        public String getDrawingUrl() {
            for (Link link : links.files) {
                if (link.name.equals("drawing")) {
                    return link.href;
                }
            }

            return "";
        }

        public String getUuid() {
            return uuid;
        }
    }

    public class Embedded {
        @JsonProperty("media")
        List<Notification> notifications;
    }

    public static class CreatorEmbedded {
        @JsonProperty("creator")
        Creator creator;
    }

    public static class Creator {
        @JsonProperty("id")
        int id;
        @JsonProperty("username")
        String username;
    }

    public static class NotificationLinks {
        @JsonProperty("smartchat:files")
        List<Link> files;
    }
}
