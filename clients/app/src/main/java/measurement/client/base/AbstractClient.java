package measurement.client.base;

public abstract class AbstractClient {
    protected String clientId;
    protected Recorder recorder;

    AbstractClient(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setRecorder(Recorder recorder){
        this.recorder = recorder;
    }
    
    public abstract void close();
}
