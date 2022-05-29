package measurement.client.base;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import measurement.client.Measurement;

public class Utils {

    public static long calcMicroSecInterval(String messageRate, String messageSize) {
        if (messageRate == null)
            return 0;
        long messageNum = byteStringToDouble(messageRate) / byteStringToDouble(messageSize);
        if (messageNum == 0){
            return 0;
        } else{
            return 1000000 / messageNum;
        }
    }

    public static long byteStringToDouble(String byteString) {
        byteString = byteString.toUpperCase();
        String[] splitByteString = byteString.split("MB|KB|B");
        long byteLong = Long.parseLong(splitByteString[0]);
        if (byteString.contains("KB")) {
            byteLong *= 1000;
        } else if (byteString.contains("MB")) {
            byteLong *= 1000 * 1000;
        }
        return byteLong;
    }

    public static <E extends CommonConfigs> E loadConfigsFromYaml(String resourceName, String fileName, Class<E> cls) throws FileNotFoundException{
        InputStream is = null;
        if (fileName == null) {
            // 指定がなければクラスパス内のリソースファイルを読み込み
            is = AbstractDriver.class.getResourceAsStream(resourceName);
            Measurement.logger.info("Load resource file.(" + resourceName + ")");
            if (is == null)
                throw new FileNotFoundException(resourceName + " not found.");
        } else {
            // 指定されたファイルを読み込み
            is = new FileInputStream(fileName);
            Measurement.logger.info("Load argument file.(" + fileName + ")");
        }
        Yaml yaml = new Yaml();
        E configs = yaml.loadAs(is, cls);
        return configs;
    }
}