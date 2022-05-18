package measurement.client.base;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import measurement.client.Measurement;

public class Recorder implements Runnable {

    private Path outputDir;
    private Thread thread;
    private ArrayBlockingQueue<Record> queue = new ArrayBlockingQueue<>(1000000);
    private Map<String, BufferedWriter> clientMap;

    public Recorder(String outputDir){
        try {
            this.outputDir = Path.of(outputDir);
            Files.createDirectories(this.outputDir);   
        } catch (IOException e) {
            Measurement.logger.warning("Failed to create directory for outputs.\n" + e.getMessage());
            System.exit(1);
        }
        this.clientMap = new TreeMap<String, BufferedWriter>();
    }

    public void createOutputFile(String clientId){
        try {
            Path path = this.outputDir.resolve(clientId + ".csv");
            BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            clientMap.put(clientId, bw);
            // ヘッダー追記
            bw.append(new Record().toRecordFormatHeader());
            bw.newLine();
        } catch (IOException e) {
            Measurement.logger.warning("Failed to create output file.\n" + e.getMessage());
        }
    }


    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Record record = null;
        while (true) {
            try {
                record = queue.take();
            } catch (Exception e) {
                Measurement.logger.warning("Failed to take from queue.\n" + e.getMessage());
            }
            if (record.getIsLast()) {
                break;
            }
            // 追記
            BufferedWriter bw = clientMap.get(record.getClientId());
            try {
                bw.append(record.toRecordFormat());
                bw.newLine();
            } catch (IOException e) {
                Measurement.logger.warning("Failed to write record to file.\n" + e.getMessage());
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

    public void close(){
        Iterator<Map.Entry<String, BufferedWriter>> itr = clientMap.entrySet().iterator();
        while(itr.hasNext()){
            Map.Entry<String, BufferedWriter> entry = itr.next();
            try {
                entry.getValue().flush();
                entry.getValue().close();   
            } catch (IOException e) {
                Measurement.logger.warning(" Failed to close("+ entry.getKey() + ")\n" + e.getMessage());
            }
        }
    }
}
