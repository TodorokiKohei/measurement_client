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
    protected Map<Long, Long> throuputMap;
    protected volatile boolean isTerminated;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractSubscriber(String clientId) {
        super(clientId);
        this.throuputMap = new TreeMap<Long, Long>();
        this.isTerminated = false;
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
            throuputMap.merge(record.getReceivedTime() / 1000, 1L, Long::sum);
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
        Path path = Path.of(outputDir, clientId + "-throuput.csv");
        BufferedWriter bw = null;
        try {
            bw = Files.newBufferedWriter(path, Charset.forName("UTF-8"), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            bw.append("time,total_msg_count");
            bw.newLine();
        } catch (Exception e) {
            Measurement.logger.warning("Failed to write results of throuput.(" + clientId + ")");
            return;
        }

        Iterator<Map.Entry<Long, Long>> itr = throuputMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Long, Long> entry = itr.next();
            try {
                bw.append(entry.getKey() + "," + entry.getValue());
                bw.newLine();
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
