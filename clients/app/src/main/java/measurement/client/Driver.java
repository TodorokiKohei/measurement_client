package measurement.client;

public abstract class Driver {
    public abstract void setupClients();
    public abstract void startMeasurement();
    public abstract void waitForMeasurement();
    public abstract void stopMeasurement();
    public abstract void treadownClients();
}
