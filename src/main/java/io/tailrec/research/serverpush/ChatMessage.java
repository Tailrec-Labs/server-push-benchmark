package io.tailrec.research.serverpush;

/**
 * @author Hussachai Puripunpinyo
 */
public class ChatMessage {

    private String label;

    private String channel;

    private String username;

    private String body;

    private int n;

    private long timestamp;

    private String viaServer;

    private long processingTime;

    public String toResult() {
        // label, channel, RTT, PT
        return String.format("%s\t%s\t%s\t%s\n", label, channel, getRTT(), getPT());
    }

    @Override
    public String toString() {
        return "io.tailrec.research.serverpush.ChatMessage{" +
                "label='" + label + '\'' +
                ", channel='" + channel + '\'' +
                ", username='" + username + '\'' +
                ", body='" + body + '\'' +
                ", n=" + n +
                ", timestamp=" + timestamp +
                ", viaServer='" + viaServer + '\'' +
                ", processingTime=" + processingTime +
                '}';
    }

    public String getLabel() {
        return label;
    }

    public String getChannel() {
        return channel;
    }

    public String getUsername() {
        return username;
    }

    public String getBody() {
        return body;
    }

    public int getN() {
        return n;
    }

//    @JsonIgnore
    public long getRTT() {
        return System.currentTimeMillis() - timestamp;
    }

//    @JsonIgnore
    public long getPT() {
        return processingTime;
    }
}
