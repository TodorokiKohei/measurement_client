package measurement.client.base;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.RandomStringUtils;

import measurement.client.Measurement;

public abstract class AbstractPublisher extends AbstractClient implements Runnable {
    protected long interval;
    protected int messageSize;
    protected int lastMessageNum;

    protected volatile String messageData;
    protected volatile boolean isTerminated;
    protected Map<Long, Long[]> throuputMap;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractPublisher(String clientId, long interval, int messageSize) {
        super(clientId);
        this.interval = interval;
        this.messageSize = messageSize;
        this.lastMessageNum = -1;

        this.messageData = null;
        this.isTerminated = false;
        this.throuputMap = new TreeMap<Long, Long[]>();
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
        if (interval == 0) {
            Measurement.logger.info("Start continuing publish.");
            future = service.schedule(this, 0, TimeUnit.MICROSECONDS);
        } else {
            Measurement.logger.info("Start interval publish.(interval:" + interval + " [μsec])");
            future = service.scheduleAtFixedRate(this, 0, interval, TimeUnit.MICROSECONDS);
        }
    }

    private void recordThrouput(Record record) {
        Long sentTimeSec = record.getSentTime() / 1000;
        if (throuputMap.containsKey(sentTimeSec)) {
            Long[] array = throuputMap.get(sentTimeSec);
            array[0]++;
            array[1] += record.getSize();
        } else {
            throuputMap.put(sentTimeSec, new Long[] { 1L, record.getSize().longValue() });
        }
    }

    @Override
    public void run() {
        if (interval == 0) {
            continuingPublish();
        } else {
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

    // スレッドは1回Publishして終了する
    public void intervalPublish() {
        if (!isTerminated) {
            Record record = publish();
            if (record != null)
                recordThrouput(record);
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
        Path path = Path.of(outputDir, clientId + "-throuput.csv");
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

        Measurement.logger.info(clientId + " results.");
        Iterator<Map.Entry<Long, Long[]>> itr = throuputMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Long, Long[]> entry = itr.next();
            try {
                bw.append(entry.getKey() + "," + entry.getValue()[0] + "," + entry.getValue()[1]);
                bw.newLine();
                Measurement.logger.info("time: " + entry.getKey() + ", throuput(msg/sec): " + entry.getValue()[0]
                        + ",  throuput(byte/sec): " + entry.getValue()[1]);
            } catch (Exception e) {
                Measurement.logger.warning("Failed to write results of throuput.(" + clientId + ")");
                return;
            }
        }

        try {
            bw.flush();
            bw.close();
        } catch (Exception e) {
        }
    }

    public abstract Record publish();
}
