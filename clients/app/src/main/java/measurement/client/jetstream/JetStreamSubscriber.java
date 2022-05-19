package measurement.client.jetstream;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Nats;
import measurement.client.base.AbstractSubscriber;

public abstract class JetStreamSubscriber extends AbstractSubscriber {
    protected Connection nc;
    protected JetStream js;
    protected JetStreamSubscription sub;

    public JetStreamSubscriber(String clientId, String server) {
        super(clientId);
        try {
            nc = Nats.connect(server);
            js = nc.jetStream();
        } catch (Exception e) {
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
