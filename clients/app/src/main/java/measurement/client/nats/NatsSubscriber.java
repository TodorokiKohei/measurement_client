package measurement.client.nats;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import io.nats.client.Consumer;
import io.nats.client.ErrorListener;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.Subscription;
import measurement.client.Measurement;
import measurement.client.base.AbstractSubscriber;
import measurement.client.base.Payload;
import measurement.client.base.Record;

class SlowConsumerReporter implements ErrorListener {
    private Boolean isDisplayed = false;
    public void errorOccurred(Connection conn, String error){
    }
    public void exceptionOccurred(Connection conn, Exception exp) {
    }
    public void slowConsumerDetected(Connection conn, Consumer consumer) {
        if (isDisplayed)
            return;
        Measurement.logger.warning("\n*********************************************\nA slow consumer dropped messages.\n*********************************************");
        isDisplayed = true;
    }
}

public class NatsSubscriber extends AbstractSubscriber {
    private long maxWait;
    private Connection nc;
    private Subscription sub;

    public NatsSubscriber(String clientId, String server, String subject, long maxWait, String queueGroup){
        super(clientId);
        this.maxWait = maxWait;
        try {
            Options options = new Options.Builder().
                server(server).
                errorListener(new SlowConsumerReporter()).
                build();
            nc = Nats.connect(options);
            // nc = Nats.connect(server);
            if (queueGroup != null){
                sub = nc.subscribe(subject, queueGroup);
            }else{
                sub = nc.subscribe(subject);
            }
        } catch (Exception e) {
            Measurement.logger.warning("Failed to create subscription.\n" + e.getMessage());
        }
    }

    @Override
    public List<Record> subscribe() {
        List<Record> records = new ArrayList<>();
        try {
            Message msg = sub.nextMessage(maxWait);
            if (msg == null){
                return records;
            }
            ObjectMapper mapper = new ObjectMapper();
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
