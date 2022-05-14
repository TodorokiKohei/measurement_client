package measurement.client;

public abstract class AbstractClient {
    protected String clientId;

    AbstractClient(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
    
    public abstract void close();
}
