package measurement.client.jetstream;

import java.util.List;
import java.time.Instant;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.PullSubscribeOptions;
import io.nats.client.PullSubscribeOptions.Builder;
import io.nats.client.Message;

import measurement.client.Measurement;
import measurement.client.base.Payload;
import measurement.client.base.Record;

public class JetStreamPullSubscriber extends JetStreamSubscriber {
    private int batchSize = 1;
    private long maxWait = 500;

    public JetStreamPullSubscriber(String clientId, String server, String stream, String subject,
            String durable, int batchSize, long maxWait) {
        super(clientId, server);

        Builder builder = PullSubscribeOptions.builder();
        if (stream != null)
            builder.stream(stream);
        if (durable != null)
            builder.durable(durable);
        PullSubscribeOptions pullOptions = builder.build();

        try {
            sub = js.subscribe(subject, pullOptions);
        } catch (Exception e) {
            Measurement.logger.warning("Failed to create subscription in the mode of pull.\n" + e.getMessage());
        }
        this.batchSize = batchSize;
        this.maxWait = maxWait;
    }

    @Override
    public List<Record> subscribe() {
        ObjectMapper mapper = new ObjectMapper();
        List<Record> records = new ArrayList<>();
        List<Message> messages = sub.fetch(batchSize, maxWait);
        for (Message msg : messages) {
            try {
                String json = new String(msg.getData());
                Payload payload = mapper.readValue(json, Payload.class);
                long receivedTime = Instant.now().toEpochMilli();
                records.add(new Record(payload, receivedTime, json.length(), clientId));
            } catch (Exception e) {
                Measurement.logger.warning("Error receiving message.\n" + e.getMessage());
                this.isTerminated = true;
            }
            msg.ack();
        }
        return records;
    }
}
