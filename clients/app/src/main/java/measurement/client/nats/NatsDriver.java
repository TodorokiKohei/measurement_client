package measurement.client.nats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import measurement.client.Driver;
import measurement.client.Measurement;
import measurement.client.AbstractPublisher;

public class NatsDriver implements Driver{

    private NatsConfigs natsConfigs;
    private List<AbstractPublisher> publisher = new ArrayList<>();
    private List<AbstractPublisher> subscriber = new ArrayList<>();
    

    public NatsDriver(String fileName){
        InputStream is = null;
        try {
            if (fileName == null){
                is = Driver.class.getResourceAsStream("/natsconf.yaml");
                Measurement.logger.info("Use resource");
            }
            else{
                is = new FileInputStream(fileName);
                Measurement.logger.info("Use argument");
            }
        } catch (FileNotFoundException e) {
            Measurement.logger.warning(fileName + "not found.");
            System.exit(1);
        }
        Yaml yaml = new Yaml();
        natsConfigs = yaml.loadAs(is, NatsConfigs.class);
    }

    @Override
    public void setupClients(){
        if (natsConfigs.getPubConf() != null){
            for(int i = 0; i < natsConfigs.getPubConf().getNumber(); i++){
                publisher.add(new NatsPublisher("publisher-1", 0.0, "STREAM.pull"));
            }
        }
        if (natsConfigs.getSubConf() != null){
            for(int i = 0; i < natsConfigs.getSubConf().getNumber(); i++){
                
            }
        }
    }

    @Override
    public void startMeasurement(){
        for(AbstractPublisher pub : publisher){
            pub.start();
        }
    }

    @Override
    public void stopMeasurement(){
        for(AbstractPublisher pub : publisher){
            pub.terminate();
        }
    }

    @Override
    public void treadownClients(){
        for(AbstractPublisher pub : publisher){
            pub.close();
        }
    }
}
