package measurement.client.kafka;

import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import measurement.client.Measurement;
import measurement.client.base.AbstractPublisher;
import measurement.client.base.Payload;
import measurement.client.base.Record;

public class KafkaPublisher extends AbstractPublisher{
    private String topicName;
    private Producer<String, byte[]> producer;
    public KafkaPublisher(String clientId, long interval, int messageSize, String topicName, Properties properties){
        super(clientId, interval, messageSize);
        this.topicName = topicName;

        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        producer = new KafkaProducer<>(properties);
    }

    @Override
    public Record publish() {
        Payload payload = createPayload();
        Record record = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);
            ProducerRecord<String, byte[]> message = new ProducerRecord<String,byte[]>(topicName, json.getBytes());
            producer.send(message).get();
            // RecordMetadata rMetadata = producer.send(message).get();
            // producer.send(message);
            record = new Record(payload, json.length(), clientId);
        } catch (Exception e) {
            Measurement.logger.warning("Error sending message.\n" + e.getMessage());
            e.printStackTrace();
        }
        return record;
    }

    @Override
    public void close() {
        if (producer != null){
            producer.close();
        }
    }

    @Override
    public Boolean isConnected() {
        if (producer == null){
            return false;
        }
        return true;
    }
}
