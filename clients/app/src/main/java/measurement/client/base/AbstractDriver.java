package measurement.client.base;

public abstract class AbstractDriver {
    public abstract void setupClients();
    public abstract void setupRecoder(String outputDir);
    public abstract void startMeasurement();
    public abstract void waitForMeasurement();
    public abstract void stopMeasurement();
    public abstract void treadownClients();
    public abstract void recordResults(String outputDir);
}
