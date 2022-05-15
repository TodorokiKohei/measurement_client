package measurement.client;

public class Utils {

    public static long calcMicroSecInterval(String messageRate, String messageSize) {
        if (messageRate == null)
            return 0;
        long messageNum = byteStringToDouble(messageRate) / byteStringToDouble(messageSize);
        return 1000000 / messageNum;
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
}
