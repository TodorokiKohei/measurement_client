package measurement.client.base;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.RandomStringUtils;

import measurement.client.Measurement;

public abstract class AbstractPublisher extends AbstractClient implements Runnable {
    protected long interval;
    protected int messageSize;
    protected int lastMessageNum;
    protected Boolean pubAsync;

    protected volatile String messageData;
    protected volatile boolean isTerminated;
    protected ConcurrentMap<Long, LongAdder> totalMsgMap;
    protected ConcurrentMap<Long, LongAdder> totalByteMap;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractPublisher(String clientId, long interval, int messageSize, Boolean pubAsync) {
        super(clientId);
        this.interval = interval;
        this.messageSize = messageSize;
        this.lastMessageNum = -1;
        this.pubAsync = pubAsync;

        this.messageData = null;
        this.isTerminated = false;
        this.totalMsgMap = new ConcurrentSkipListMap<Long, LongAdder>();
        this.totalByteMap = new ConcurrentSkipListMap<Long, LongAdder>();
    }

    protected Payload createPayload() {
        Payload payload = new Payload(clientId, ++lastMessageNum, Instant.now().toEpochMilli());
        if (messageData == null)
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(payload);
                if (messageSize - json.length() < 0) {
                    Measurement.logger.warning("Message size is too small.");
                    this.messageData = "";
                } else {
                    this.messageData = RandomStringUtils.randomAscii(messageSize - json.length());
                }
            } catch (Exception e) {
                Measurement.logger.warning("Failed to serialize payload class to json string.\n" + e.getMessage());
                this.messageData = "";
            }
        payload.data = this.messageData;
        return payload;
    }

    protected void setMessageData(Payload payload) {
        if (messageData == null)
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(payload);
                if (messageSize - json.length() < 0) {
                    Measurement.logger.warning("Message size is too small.");
                    this.messageData = "";
                } else {
                    this.messageData = RandomStringUtils.randomAscii(messageSize - json.length());
                }
            } catch (Exception e) {
                Measurement.logger.warning("Failed to serialize payload class to json string.\n" + e.getMessage());
                this.messageData = "";
            }
        payload.data = this.messageData;
    }

    // スレッド起動処理
    public void start() {
        Measurement.logger.info("Start " + clientId + ".");
        service = Executors.newSingleThreadScheduledExecutor();
        String asyncOrSync = pubAsync ? "async" : "sync";
        if (interval == 0) {
            Measurement.logger.info("Start continuing " + asyncOrSync + " publish.");
            future = service.schedule(this, 0, TimeUnit.MICROSECONDS);
        } else {
            Measurement.logger.info("Start interval "+ asyncOrSync + " publish.(interval:" + interval + " [μsec])");
            future = service.scheduleAtFixedRate(this, 0, interval, TimeUnit.MICROSECONDS);
        }
    }

    private void recordThrouput(Record record) {
        Long sentTimeSec;
        if (pubAsync){
            sentTimeSec = Instant.now().getEpochSecond();
        }else{
            sentTimeSec = record.getSentTime() / 1000;
        }
        totalMsgMap.computeIfAbsent(sentTimeSec, k -> new LongAdder()).increment();;
        totalByteMap.computeIfAbsent(sentTimeSec, k -> new LongAdder()).add(record.getSize());
    }

    @Override
    public void run() {
        if (interval == 0) {
            if (pubAsync)
                continuingPublishAsync();
            else
                continuingPublish();
        } else {
            if (pubAsync)
                intervalPublishAsync();
            else
                intervalPublish();
        }
    }

    // スレッドはループしてPublishを続ける
    public void continuingPublish() {
        while (!isTerminated) {
            Record record = publish();
            recordThrouput(record);
        }
    }

    public void continuingPublishAsync(){
        while (!isTerminated) {
            CompletableFuture<Record> future = publishAsync();
            future.thenAccept((record) -> {
                recordThrouput(record);
            }).exceptionally(ex -> {
                Measurement.logger.warning("Write error on massage:" + ex.getMessage());
                return null;
            });
        }
    }

    // スレッドは1回Publishして終了する
    public void intervalPublish() {
        if (!isTerminated) {
            Record record = publish();
            if (record != null)
                recordThrouput(record);
        }
    }

    public void intervalPublishAsync() {
        if (!isTerminated) {
            CompletableFuture<Record> future = publishAsync();
            future.thenAccept((record) -> {
                recordThrouput(record);
            }).exceptionally(ex -> {
                Measurement.logger.warning("Write error on massage:" + ex.getMessage());
                return null;
            });
        }
    }

    // 各スレッドを終了し、クライアントの接続を終了させる
    public void terminate() {
        if (!future.isDone()) {
            isTerminated = true;
            future.cancel(false);
        }
        service.shutdown();
        try {
            if (service.awaitTermination(5, TimeUnit.SECONDS)) {
                Measurement.logger.info(clientId + " closed successfully.");
            }
        } catch (InterruptedException e) {
            Measurement.logger.warning(clientId + " could not be closed.\n" + e.getMessage());
        }
    }

    // スループットをファイルに書き込み
    public void recordThrouput(String outputDir) {
        Path path = Path.of(outputDir, clientId + "-throughput.csv");
        BufferedWriter bw = null;
        try {
            bw = Files.newBufferedWriter(path, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            bw.append("time,message_count_totla,message_bytes_total");
            bw.newLine();
        } catch (Exception e) {
            Measurement.logger.warning("Failed to write results of throuput.(" + clientId + ")");
            return;
        }

        Measurement.logger.info("Outputs the first 10 results for " + clientId);
        int count = 0;
        Iterator<Map.Entry<Long, LongAdder>> itr = totalMsgMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Long, LongAdder> entry = itr.next();
            try {
                Long sumTotalMsg = entry.getValue().sum();
                Long sumTotalByte = totalByteMap.get(entry.getKey()).sum();
                bw.append(entry.getKey() + "," + sumTotalMsg + "," + sumTotalByte);
                bw.newLine();
                count++;
                if (count > 5)
                    continue;
                Measurement.logger.info("time: " + entry.getKey() + ", throuput(msg/sec): " + sumTotalMsg
                        + ",  throuput(byte/sec): " + sumTotalByte);
            } catch (Exception e) {
                Measurement.logger.warning("Failed to write results of throuput.(" + clientId + ")");
                return;
            }
        }
        Measurement.logger.info("Total mssage num: " + lastMessageNum);
        try {
            bw.flush();
            bw.close();
        } catch (Exception e) {
        }
    }

    public abstract Record publish();
    public abstract CompletableFuture<Record> publishAsync();
}
