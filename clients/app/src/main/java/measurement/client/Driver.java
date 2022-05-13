package measurement.client;

public interface Driver {
    public abstract void setupClients();
    public abstract void startMeasurement();
    public abstract void stopMeasurement();
    public abstract void treadownClients();
}
