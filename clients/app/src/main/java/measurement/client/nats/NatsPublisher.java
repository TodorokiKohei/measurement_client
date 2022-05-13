package measurement.client.nats;

import measurement.client.AbstractPublisher;
import measurement.client.Measurement;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.api.PublishAck;
import io.nats.client.JetStream;

public class NatsPublisher extends AbstractPublisher{

    private Connection nc;
    private JetStream js;
    private int messageCount;
    private String subject;

    public NatsPublisher(String clientId, double interval, String subject){
        super(clientId, interval);
        try {
            nc = Nats.connect("nats://nats-1");
            js = nc.jetStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.messageCount = 0;
        this.subject = subject;
    }

    @Override
    public void publish(){
        try {
            PublishAck pa = js.publish(subject, ("data"+messageCount).getBytes());   
            messageCount++;
        } catch (Exception e) {
            Measurement.logger.warning("Error sending message");
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            nc.close();   
        } catch (Exception e) {
            //TODO: handle exception
        }
    }
}
