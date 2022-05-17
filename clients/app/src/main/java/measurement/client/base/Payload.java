package measurement.client.base;

public class Payload {
    public String sentClientId;
    public Integer seqNum;
    public Long sentTime;
    public String data;

    public Payload() {
    }

    public Payload(String sentClientId, Integer seqNum, Long sentTime, String data) {
        this.sentClientId = sentClientId;
        this.seqNum = seqNum;
        this.sentTime = sentTime;
        this.data = data;
    }

    public Payload(String sentClientId, Integer seqNum, Long sentTime) {
        this(sentClientId, seqNum, sentTime, "");
    }
}
