package measurement.client;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class AbstractPublisher extends AbstractClient implements Runnable {
    protected long interval;
    protected int messageSize;

    protected Map<Long, Long> throuputMap;
    protected volatile boolean isTerminated;

    protected ScheduledExecutorService service;
    protected ScheduledFuture<?> future;

    public AbstractPublisher(String clientId, long interval, int messageSize) {
        super(clientId);
        this.interval = interval;
        this.messageSize = messageSize;

        this.throuputMap = new TreeMap<Long, Long>();
        this.isTerminated = false;
    }

    protected String createMessage() {
        return RandomStringUtils.randomAscii(messageSize);
    }

    // スレッド起動処理
    public void start() {
        Measurement.logger.info("Start " + clientId + ".");
        service = Executors.newSingleThreadScheduledExecutor();
        if (interval == 0){
            Measurement.logger.info("Start continuing publish.");
            future = service.schedule(this, 0, TimeUnit.MICROSECONDS);
        }else{
            Measurement.logger.info("Start interval publish.(interval:" + interval + " [μsec])");
            future = service.scheduleAtFixedRate(this, 0, interval, TimeUnit.MICROSECONDS);
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

    private void recordThrouput(Payload payload){
        throuputMap.merge(payload.sentTime, 1L, Long::sum);
    }

    // スレッドはループしてPublishを続ける
    public void continuingPublish() {
        while (!isTerminated) {
            Payload payload =  publish();
            recordThrouput(payload);
        }
    }

    // スレッドは1回Publishして終了する
    public void intervalPublish() {
        if (!isTerminated) {
            Payload payload = publish();
            recordThrouput(payload);
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
            Measurement.logger.warning(clientId + " could not be closed.");
            e.printStackTrace();
        }
    }

    public void printThrouput(){
        Measurement.logger.info(clientId + "'s throuput results.");
        Iterator<Map.Entry<Long, Long>> itr = throuputMap.entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry<Long, Long> entry = itr.next();
            Measurement.logger.info("(" + clientId + ") " + entry.getKey() + ":" + entry.getValue());
        }
    }

    public abstract Payload publish();
}
