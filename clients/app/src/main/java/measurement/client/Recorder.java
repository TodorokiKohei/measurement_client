package measurement.client;

import java.util.concurrent.ArrayBlockingQueue;

public class Recorder implements Runnable{
    private Thread thread;
    private ArrayBlockingQueue<Payload> queue = new ArrayBlockingQueue<>(1000000);

    public void start(){
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run(){
        Payload payload = null;
        // while(){
        //     payload = queue.
        // }
    }

    public void terminate(){
        
    }
}
