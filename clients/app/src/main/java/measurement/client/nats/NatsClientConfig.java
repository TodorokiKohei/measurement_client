package measurement.client.nats;

public abstract class NatsClientConfig {
    private String server;
    private String stream;
    private String subject;
    private int number;

    public String getServer() {
        return server;
    }

    public String getStream() {
        return stream;
    }

    public String getSubject() {
        return subject;
    }

    public int getNumber() {
        return number;
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

    public void setNumber(int number) {
        this.number = number;
    }

}
