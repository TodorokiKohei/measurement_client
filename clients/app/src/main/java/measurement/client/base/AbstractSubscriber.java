package measurement.client.base;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import measurement.client.Measurement;

public abstract class AbstractSubscriber extends AbstractClient implements Runnable {
    protected volatile boolean isTerminated;
    protected Map<Long, Long[]> throuputMap;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractSubscriber(String clientId) {
        super(clientId);
        this.isTerminated = false;
        this.throuputMap = new TreeMap<Long, Long[]>();
    }

    public void start() {
        // 別スレッドでSubscribe処理を開始
        Measurement.logger.info("Start " + clientId + ".");
        service = Executors.newSingleThreadScheduledExecutor();
        future = service.schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    public void setRecorder(Recorder recorder) {
        this.recorder = recorder;
    }

    private void recordThrouput(List<Record> records) {
        for (Record record : records) {
            Long recievedTimeSec = record.getReceivedTime() / 1000;
            if (throuputMap.containsKey(recievedTimeSec)) {
                Long[] array = throuputMap.get(recievedTimeSec);
                array[0]++;
                array[1] += record.getSize();
            } else {
                throuputMap.put(recievedTimeSec, new Long[] { 1L, record.getSize().longValue() });
            }
        }
    }

    @Override
    public void run() {
        // terminateが呼ばれるまでSubscribe処理
        Measurement.logger.info("Start subscribe.");
        while (!isTerminated) {
            List<Record> records = subscribe();
            recordThrouput(records);
            if (recorder != null)
                recorder.add(records);
        }
    }

    public void terminate() {
        // 別スレッドのタスクを終了させる
        if (!future.isDone()) {
            isTerminated = true;
            future.cancel(false);
        }
        service.shutdown();

        // スレッドが終了するまで一定時間待機する
        try {
            if (service.awaitTermination(5, TimeUnit.SECONDS)) {
                Measurement.logger.info(clientId + " closed successfully.");
            }
        } catch (InterruptedException e) {
            Measurement.logger.warning(clientId + " could not be closed.\n" + e.getMessage());
        }
    }

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
        Iterator<Map.Entry<Long, Long[]>> itr = throuputMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Long, Long[]> entry = itr.next();
            try {
                bw.append(entry.getKey() + "," + entry.getValue()[0] + "," + entry.getValue()[1]);
                bw.newLine();
                count++;
                if (count > 5) continue;
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

    public abstract List<Record> subscribe();
}
