package measurement.client;

import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Payload {
    public String sentClientId;
    public Integer seqNum;
    public Long sentTime;
    public String data;

    @JsonIgnore
    public Long receivedTime;
    @JsonIgnore
    public Integer size;
    @JsonIgnore
    public Boolean isLast = false;
    @JsonIgnore
    public Boolean isLast = false;
    
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

    // レコードのフォーマット形式に変換
    public String toRecordFormat() {
        String values[] = { sentClientId, seqNum.toString(), sentTime.toString(), receivedTime.toString() };
        StringJoiner sj = new StringJoiner(",");
        for(String val: values){
            if (val == null) {
                sj.add("");
            }else{
                sj.add(val);
            }
            
        }
        return sj.toString();
    }

    // レコードのフォーマット形式のヘッダーを作成
    public String toRecordFormatHeader() {
        String headers[] = { "sentClientId", "seqNum", "sentTime", "receivedTime" };
        StringJoiner sj = new StringJoiner(",");
        for(String val: headers){
            sj.add(val);
        }
        return sj.toString();
    }

    // 受信 - 発信 でレイテンシーを計算
    public long calcLatency() {
        if (receivedTime == null)
            return 0;
        return receivedTime - sentTime;
    }
}
