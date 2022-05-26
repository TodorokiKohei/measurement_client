package measurement.client.sample;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaSub {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "kafka-1:9092");
        properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        properties.setProperty("fetch.min.bytes", "1");
        properties.setProperty("group.id", "consumer-1");
        properties.setProperty("acks", "1");
        properties.setProperty("auto.offset.reset", "earliest");

        Consumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
        String topicName = "test-topic-2";
        List<String> topicList = new ArrayList<>();
        topicList.add(topicName);
        consumer.subscribe(topicList);

        int count = 0;
        while (true) {
            if (count >= 100)
                break;
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, byte[]> record : records) {
                String value = new String(record.value());
                String meta = String.format("partition = %d, offset = %d, keySize = %d, valueSize = %d, value=%s",
                record.partition(), record.offset(), record.serializedKeySize(),
                record.serializedValueSize(), value);
                System.out.println(meta);
                count++;
            }
        }
        consumer.close();
    }
}
