package measurement.client.nats;

import java.util.ArrayList;
import java.util.List;

import measurement.client.Driver;
import measurement.client.AbstractPublisher;

public class NatsDriver implements Driver{

    private List<AbstractPublisher> publisher = new ArrayList<>();
    private List<AbstractPublisher> subscriber = new ArrayList<>();
    

    public NatsDriver(String fileName){

    }

    @Override
    public void setupClients(){
        publisher.add(new NatsPublisher("publisher-1", 0.0, "STREAM.pull"));
    }

    @Override
    public void startMeasurement(){
        for(AbstractPublisher pub : publisher){
            pub.start();
        }
    }

    @Override
    public void stopMeasurement(){
        for(AbstractPublisher pub : publisher){
            pub.terminate();
        }
    }

    @Override
    public void treadownClients(){
        for(AbstractPublisher pub : publisher){
            pub.close();
        }
    }
}
