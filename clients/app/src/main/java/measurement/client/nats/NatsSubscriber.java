package measurement.client.nats;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.PullSubscribeOptions.Builder;
import measurement.client.AbstractSubscriber;
import measurement.client.Measurement;
import measurement.client.Payload;

public class NatsSubscriber extends AbstractSubscriber {
    private Connection nc;
    private JetStream js;
    private JetStreamSubscription sub;

    private int batchSize;
    private long maxWait;

    public NatsSubscriber(String clientId, String server, String stream, String subject,
            String durable, int batchSize, long maxWait, NatsSubMode mode, String queueGroup) {
        super(clientId);

        Builder builder = PullSubscribeOptions.builder();
        if (durable != null)
            builder.durable(durable);
        if (stream != null)
            builder.stream(stream);
        PullSubscribeOptions pullOptions = builder.build();

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
    public List<Payload> subscribe() {
        ObjectMapper mapper = new ObjectMapper();
        List<Payload> payloads = new ArrayList<>();
        List<Message> messages = sub.fetch(batchSize, maxWait);
        for (Message msg : messages) {
            try {
                String json = new String(msg.getData());
                Payload payload = mapper.readValue(json, Payload.class);
                payload.receivedTime = Instant.now().toEpochMilli();
                payload.size = json.length();
                payloads.add(payload);
            } catch (Exception e) {
                Measurement.logger.warning("Error receiving message.\n" + e.getMessage());
            }
            msg.ack();
        }
        return payloads;
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
