package measurement.client.base;

import java.util.StringJoiner;

public class Record {
    private String sentClientId;
    private Integer seqNum;
    private Long sentTime;
    private Long receivedTime;
    private Integer size;
    private Boolean isLast;

    protected Record(String sentClientId, Integer seqNum, Long sentTime, Long receivedTime, Integer size, boolean isLast) {
        this.sentClientId = sentClientId;
        this.seqNum = seqNum;
        this.sentTime = sentTime;
        this.receivedTime = receivedTime;
        this.size = size;
        this.isLast = isLast;
    }

    public Record(Boolean isLast) {
        this.isLast = isLast;
    }

    // public Record(String sentClientId, Integer seqNum, Long sentTime, Integer size) {
    //     this(sentClientId, seqNum, sentTime, 0L, size, false);
    // }

    // public Record(String sentClientId, Integer seqNum, Long sentTime, Long receivedTime, Integer size) {
    //     this(sentClientId, seqNum, sentTime, receivedTime, size, false);
    // }

    public Record(Payload payload, Integer size) {
        this(payload.sentClientId, payload.seqNum, payload.sentTime, 0L, size, false);
    }

    public Record(Payload payload, Long receivedTime, Integer size) {
        this(payload.sentClientId, payload.seqNum, payload.sentTime, receivedTime, size, false);
    }

    public String getSentClientId() {
        return this.sentClientId;
    }

    public Integer getSeqNum() {
        return this.seqNum;
    }

    public Long getSentTime() {
        return this.sentTime;
    }

    public Long getReceivedTime() {
        return receivedTime;
    }

    public Integer getSize() {
        return size;
    }

    public Boolean getIsLast() {
        return isLast;
    }

    // レコードのフォーマット形式に変換
    public String toRecordFormat() {
        String values[] = { sentClientId, seqNum.toString(), sentTime.toString(), receivedTime.toString() };
        StringJoiner sj = new StringJoiner(",");
        for (String val : values) {
            if (val == null) {
                sj.add("");
            } else {
                sj.add(val);
            }
        }
        return sj.toString();
    }

    // レコードのフォーマット形式のヘッダーを作成
    public String toRecordFormatHeader() {
        String headers[] = { "sentClientId", "seqNum", "sentTime", "receivedTime" };
        StringJoiner sj = new StringJoiner(",");
        for (String val : headers) {
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
