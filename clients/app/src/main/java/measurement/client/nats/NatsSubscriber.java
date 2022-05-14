package measurement.client.nats;

import java.nio.charset.StandardCharsets;
import java.util.List;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.PullSubscribeOptions;
import measurement.client.AbstractSubscriber;
import measurement.client.Measurement;

public class NatsSubscriber extends AbstractSubscriber {
    private Connection nc;
    private JetStream js;
    private JetStreamSubscription sub;

    private String stream;
    private String subject;
    private int batchSize;
    private long maxWait;

    public NatsSubscriber(String clientId, String server, String stream, String subject,
        String durable, int batchSize, long maxWait, NatsSubMode mode, String queueGroup) {
        super(clientId);

        PullSubscribeOptions pullOptions = PullSubscribeOptions.builder()
            .durable(durable)
            .stream(stream)
            .build();
        try {
            nc = Nats.connect(server);
            js = nc.jetStream();
            sub = js.subscribe(subject, pullOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.batchSize = batchSize;
        this.maxWait = maxWait;
    }

    @Override
    public void subscribe() {
        List<Message> messages = sub.fetch(batchSize, maxWait);
        for(Message msg: messages){
            Measurement.logger.info(new String(msg.getData()));
            msg.ack();
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
