package measurement.client;

import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Payload {
    public String sentClientId;
    public Integer seqNum;
    public Long sentTime;
    public String data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Long receivedTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer size;

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

    public String toRecordFormat() {
        String values[] = { sentClientId, seqNum.toString(), sentTime.toString(), receivedTime.toString() };
        StringJoiner sj = new StringJoiner(",");
        for(String val: values){
            sj.add(val);
        }
        return sj.toString();
    }

    public String toRecordFormatHeader() {
        String headers[] = { "sentClientId", "seqNum", "sentTime", "receivedTime" };
        StringJoiner sj = new StringJoiner(",");
        for(String val: headers){
            sj.add(val);
        }
        return sj.toString();
    }

    public long calcLatency() {
        if (receivedTime == null)
            return 0;
        return receivedTime - sentTime;
    }

    public long getSize(){
        if (size == null)
            return 0;
        return size;
    }
}
