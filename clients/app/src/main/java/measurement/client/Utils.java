package measurement.client;

public class Utils {

    public static long calcMsInterval(String messageRate, String messageSize) {
        if (messageRate == null)
            return 0;
        return (long)(byteStringToDouble(messageRate) / byteStringToDouble(messageSize));
    }

    public static double byteStringToDouble(String byteString) {
        byteString = byteString.toUpperCase();
        String[] splitByteString = byteString.split("MB|KB|B");
        double byteDouble = Double.parseDouble(splitByteString[0]);
        if (byteString.contains("KB")) {
            byteDouble *= 1000;
        } else if (byteString.contains("MB")) {
            byteDouble *= 1000 * 1000;
        }
        return byteDouble;
    }
}
