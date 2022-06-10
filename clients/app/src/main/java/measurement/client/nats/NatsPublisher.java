package measurement.client.nats;

import java.util.concurrent.CompletableFuture;

import io.nats.client.Connection;
import io.nats.client.Nats;
import measurement.client.Measurement;
import measurement.client.base.AbstractPublisher;
import measurement.client.base.Payload;
import measurement.client.base.Record;

public class NatsPublisher extends AbstractPublisher {
    private String subject;
    private Connection nc;

    public NatsPublisher(String clientId, long interval, int messageSize, String server, String subject) {
        super(clientId, interval, messageSize, false);
        this.subject = subject;
        try {
            nc = Nats.connect(server);
        } catch (Exception e) {
            Measurement.logger.warning("Failed to establish publisher connection.\n" + e.getMessage());
        }
    }

    @Override
    public Record publish() {
        Payload payload = createPayload();
        Record record = null;
        if (isConnected()) {
            try {
                String json = mapper.writeValueAsString(payload);
                nc.publish(subject, json.getBytes());
                record = new Record(payload, json.length(), clientId);
            } catch (Exception e) {
                Measurement.logger.warning("Error sending message.\n" + e.getMessage());
                e.printStackTrace();
            }
        }
        return record;
    }

    @Override
    public CompletableFuture<Record> publishAsync() {
        // No need to implement.
        return null;
    }

    @Override
    public Boolean isConnected() {
        if (nc == null)
            return false;
        return true;
    }

    @Override
    public void close() {
        if (nc != null) {
            try {
                nc.close();
            } catch (Exception e) {
            }
        }
    }
}
