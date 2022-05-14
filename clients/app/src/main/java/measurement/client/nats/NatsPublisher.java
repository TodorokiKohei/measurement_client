package measurement.client.nats;

import measurement.client.AbstractPublisher;
import measurement.client.Measurement;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import io.nats.client.JetStream;

public class NatsPublisher extends AbstractPublisher{

    private Connection nc;
    private JetStream js;
    private PublishOptions pubOptions;

    private String stream;
    private String subject;
    private double messageSize;

    private int totalMessageCount;

    public NatsPublisher(String clientId, long interval, String server, String stream, String subject,
        double messageSize){
        super(clientId, interval);
        try {
            nc = Nats.connect(server);
            js = nc.jetStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        pubOptions = PublishOptions.builder()
            .expectedStream(stream)
            .build();

        this.stream = stream;
        this.subject = subject;
        this.messageSize = messageSize;
        this.totalMessageCount = 0;
    }

    @Override
    public void publish(){
        try {
            PublishAck pa = js.publish(subject, ("data"+totalMessageCount).getBytes(), pubOptions);
            totalMessageCount++;
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
            e.printStackTrace();
        }
    }
}
