package measurement.client.jetstream;

import io.nats.client.PushSubscribeOptions;
import io.nats.client.PushSubscribeOptions.Builder;

import java.util.List;
import java.time.Instant;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Message;

import measurement.client.Measurement;
import measurement.client.base.Payload;
import measurement.client.base.Record;

public class JetStreamPushSubscriber extends JetStreamSubscriber {
    private long maxWait = 500;

    public JetStreamPushSubscriber(String clientId, String server, String stream, String subject, String durable,
            long maxWait, String queueGroup) {
        super(clientId, server);

        Builder builder = PushSubscribeOptions.builder();
        if (stream != null)
            builder.stream(stream);
        if (durable != null)
            builder.durable(durable);
        PushSubscribeOptions pso = builder.build();

        try {
            if (queueGroup != null) {
                sub = js.subscribe(subject, queueGroup, pso);
            } else {
                sub = js.subscribe(subject, pso);
            }
        } catch (Exception e) {
            Measurement.logger.warning("Failed to create subscription in the mode of push.\n" + e.getMessage());
        }
        this.maxWait = maxWait;
    }

    @Override
    public List<Record> subscribe() {
        ObjectMapper mapper = new ObjectMapper();
        List<Record> records = new ArrayList<>();
        try {
            Message msg = sub.nextMessage(maxWait);
            if (msg == null) {
                return records;
            }
            msg.ack();
            String json = new String(msg.getData());
            Payload payload = mapper.readValue(json, Payload.class);
            long receivedTime = Instant.now().toEpochMilli();
            Record record = new Record(payload, receivedTime, json.length(), clientId);
            records.add(record);

        } catch (Exception e) {
            Measurement.logger.warning("Error recieving message.\n" + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }

}
