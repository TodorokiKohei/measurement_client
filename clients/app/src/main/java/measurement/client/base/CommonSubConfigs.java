package measurement.client.base;

public class CommonSubConfigs extends CommonClientConfigs{
    private Boolean recordMessage = false;

    public Boolean getRecordMessage() {
        return recordMessage;
    }

    public void setRecordMessage(Boolean recordMessage) {
        this.recordMessage = recordMessage;
    }
}
