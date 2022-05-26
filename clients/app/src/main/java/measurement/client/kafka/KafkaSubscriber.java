package measurement.client.kafka;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import measurement.client.Measurement;
import measurement.client.base.AbstractSubscriber;
import measurement.client.base.Payload;
import measurement.client.base.Record;

public class KafkaSubscriber extends AbstractSubscriber {
    private Consumer<String, byte[]> consumer;
    private long maxWait;

    public KafkaSubscriber(String clientId, String topicName, long maxWait, Properties properties) {
        super(clientId);
        this.maxWait = maxWait;

        properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(topicName));
    }

    @Override
    public List<Record> subscribe() {
        List<Record> records = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ConsumerRecords<String, byte[]> messages = consumer.poll(Duration.ofMillis(maxWait));
        for(ConsumerRecord<String, byte[]> msg: messages){
            String json = new String(msg.value());
            try {
                Payload payload = mapper.readValue(json, Payload.class);
                long receivedTime = Instant.now().toEpochMilli();
                records.add(new Record(payload, receivedTime, json.length(), clientId));
            } catch (Exception e) {
                Measurement.logger.warning("Error receiving message.\n" + e.getMessage());
                e.printStackTrace();
            }
        }
        return records;
    }

    @Override
    public Boolean isConnected() {
        if (consumer == null)
            return false;
        return true;
    }

    @Override
    public void close() {
        if (consumer != null){
            consumer.close();
        }
    }
}
