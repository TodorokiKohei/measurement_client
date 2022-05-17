package measurement.client;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Recorder implements Runnable{
    
    private 
    private Thread thread;
    private ArrayBlockingQueue<Payload> queue = new ArrayBlockingQueue<>(1000000);



    public void start(Boolean shouldRecordPayload){
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run(){
        Payload payload = null;
        while(true){
            payload = queue.
        }
    }

    public void add(Payload payload){
        queue.add(payload);
    }

    public void add(List<Payload> payloads){
        for(Payload payload: payloads){
            queue.add(payload);
        }
    }

    public void terminate(){
        Payload payload = new Payload();
        try {
            thread.join();   
        } catch (Exception e) {
            //TODO: handle exception
        }
    }
}
