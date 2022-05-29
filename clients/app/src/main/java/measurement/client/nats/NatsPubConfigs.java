package measurement.client.nats;

import measurement.client.base.CommonPubConfigs;

public class NatsPubConfigs extends CommonPubConfigs {
    private String server;
    private String subject;

    public String getServer() {
        return server;
    }

    public String getSubject() {
        return subject;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
