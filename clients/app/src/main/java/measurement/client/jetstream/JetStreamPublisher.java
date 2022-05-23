package measurement.client.jetstream;

import measurement.client.Measurement;
import measurement.client.base.AbstractPublisher;
import measurement.client.base.Payload;
import measurement.client.base.Record;

import java.time.Duration;
import java.time.Instant;

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
    private int lastMessageNum;

    public JetStreamPublisher(String clientId, long interval, int messageSize, String server, String stream,
            String subject) {
        super(clientId, interval, messageSize);

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
        this.subject = subject;
        this.lastMessageNum = -1;
    }

    @Override
    public Record publish() {
        // ペイロード作成
        Payload payload = new Payload(clientId, ++lastMessageNum, Instant.now().toEpochMilli());
        setMessageData(payload);
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
        // 処理したメッセージ数を返す
        return record;
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
