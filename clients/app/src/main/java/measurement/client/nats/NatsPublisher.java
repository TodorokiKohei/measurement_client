package measurement.client.nats;

import measurement.client.AbstractPublisher;
import measurement.client.Measurement;
import measurement.client.Payload;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.PublishOptions;
import io.nats.client.PublishOptions.Builder;
import io.nats.client.api.PublishAck;
import io.nats.client.JetStream;

public class NatsPublisher extends AbstractPublisher {

    private Connection nc;
    private JetStream js;
    private PublishOptions pubOptions;

    private String subject;
    private int lastMessageNum;

    public NatsPublisher(String clientId, long interval, int messageSize, String server, String stream,
            String subject) {
        super(clientId, interval, messageSize);

        Builder builder = PublishOptions.builder();
        if (stream != null) {
            builder.expectedStream(stream);
        }
        pubOptions = builder.build();

        try {
            nc = Nats.connect(server);
            js = nc.jetStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.subject = subject;
        this.lastMessageNum = -1;
    }

    @Override
    public Payload publish() {
        // ペイロード作成
        Payload payload = new Payload(clientId, ++lastMessageNum, Instant.now().toEpochMilli(), createMessage());
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);
            PublishAck pa = js.publish(subject, json.getBytes(), pubOptions);
        } catch (Exception e) {
            Measurement.logger.warning("Error sending message.\n" + e.getMessage());
        }
        // 処理したメッセージ数を返す
        return payload;
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
