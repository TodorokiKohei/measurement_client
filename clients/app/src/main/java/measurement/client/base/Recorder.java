package measurement.client.base;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import measurement.client.Measurement;

public class Recorder implements Runnable {

    private Boolean shouldRecordPayload;
    private Thread thread;
    private ArrayBlockingQueue<Record> queue = new ArrayBlockingQueue<>(1000000);

    public Recorder(Boolean shouldRecordPayload) {
        this.shouldRecordPayload = shouldRecordPayload;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run(){
        Record record = null;
        while(true){
            try {
                record = queue.take();   
            } catch (Exception e) {
                Measurement.logger.warning("Failed to take from queue.\n" + e.getMessage());
            }
            if (record.getIsLast()){
                break;
            }
            
        }
    }

    public void add(Record record) {
        queue.add(record);
    }

    public void add(List<Record> records) {
        for (Record record : records) {
            queue.add(record);
        }
    }

    public void terminate() {
        Record record = new Record(true);
        this.add(record);
        try {
            thread.join();
        } catch (Exception e) {
            Measurement.logger.warning(e.getMessage());
        }
    }
}
