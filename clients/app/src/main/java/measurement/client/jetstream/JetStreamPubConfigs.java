package measurement.client.jetstream;

import measurement.client.base.CommonPubConfigs;

public class JetStreamPubConfigs extends CommonPubConfigs {
    private String server;
    private String stream;
    private String subject;

    public String getServer() {
        return server;
    }

    public String getStream() {
        return stream;
    }

    public String getSubject() {
        return subject;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
