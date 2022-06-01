package measurement.client.jetstream;

import measurement.client.Measurement;
import measurement.client.base.AbstractPublisher;
import measurement.client.base.Payload;
import measurement.client.base.Record;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.PublishOptions;
import io.nats.client.PublishOptions.Builder;
import io.nats.client.api.PublishAck;
import io.nats.client.JetStream;

public class JetStreamPublisher extends AbstractPublisher {

    private Connection nc;
    private JetStream js;
    private PublishOptions pubOptions;

    private String subject;

    public JetStreamPublisher(String clientId, long interval, int messageSize, Boolean pubAsync, String server, String stream,
            String subject) {
        super(clientId, interval, messageSize, pubAsync);
        this.subject = subject;

        Builder builder = PublishOptions.builder();
        if (stream != null) {
            builder.expectedStream(stream);
        }
        builder.streamTimeout(Duration.ofSeconds(5));
        pubOptions = builder.build();

        try {
            nc = Nats.connect(server);
            js = nc.jetStream();
        } catch (Exception e) {
            Measurement.logger.warning("Failed to establish publisher connection.\n" + e.getMessage());
        }
    }

    @Override
    public Record publish() {
        // ペイロード作成
        Payload payload = createPayload();
        Record record = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);
            PublishAck pa = js.publish(subject, json.getBytes(), pubOptions);
            record = new Record(payload, json.length(), clientId);
        } catch (Exception e) {
            Measurement.logger.warning("Error sending message.\n" + e.getMessage());
            e.printStackTrace();
            // this.isTerminated = true;
        }
        
        return record;
    }

    @Override
    public CompletableFuture<Record> publishAsync() {
        CompletableFuture<Record> future = new CompletableFuture<>();
        Payload payload = createPayload();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);
            final Record record = new Record(payload, json.length(), clientId);
            js.publishAsync(subject, json.getBytes(), pubOptions).thenAccept(pa -> {
                future.complete(record);
            }).exceptionally(exception -> {
                future.completeExceptionally(exception);
                return null;
            });
        } catch (Exception e) {
            Measurement.logger.warning("Error sending message.\n" + e.getMessage());
            e.printStackTrace();
            // this.isTerminated = true;
        }
        
        return future;
    }

    @Override
    public Boolean isConnected() {
        if (nc == null)
            return false;
        return true;
    }

    @Override
    public void close() {
        try {
            if (nc != null) {
                nc.close();
            }
        } catch (Exception e) {
            Measurement.logger.warning(e.getMessage());
        }
    }
}
